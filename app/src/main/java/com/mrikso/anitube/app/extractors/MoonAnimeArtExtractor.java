package com.mrikso.anitube.app.extractors;

import android.util.Log;

import androidx.core.util.Pair;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.mrikso.anitube.app.extractors.model.PlayerJsResponse;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.VideoLinksModel;
import com.mrikso.anitube.app.network.ApiClient;
import com.mrikso.anitube.app.utils.ParserUtils;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.lindstrom.m3u8.model.MasterPlaylist;
import io.lindstrom.m3u8.model.Resolution;
import io.lindstrom.m3u8.model.Variant;
import io.lindstrom.m3u8.parser.MasterPlaylistParser;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MoonAnimeArtExtractor extends BaseVideoLinkExtracror {

    private final String TAG = "MoonAnimeArtExtractor";
    private final String PLAYER_JS_PATTERN = "Playerjs\\(([^)]+)\\)";
    private final MasterPlaylistParser masterPlaylistParser = new MasterPlaylistParser();

    public MoonAnimeArtExtractor(String url, OkHttpClient client) {
        super(url, client);
    }

    @Override
    public Single<Pair<LoadState, VideoLinksModel>> parse() {
        return downloadManifest()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(v -> new Pair<>(LoadState.DONE, getModel(v.first, v.second)))
                .doOnError(v -> new Pair<>(LoadState.ERROR, getUrl()));
    }

    private VideoLinksModel getModel(String masterU3u8, PlayerJsResponse playerJs) throws IOException {
        Map<String, String> qualitiesMap = new HashMap<>();
        VideoLinksModel model = new VideoLinksModel(playerJs.getFile());
        MasterPlaylist masterPlayList = masterPlaylistParser.readPlaylist(masterU3u8);
        // Log.i(TAG, "start parse playlist");
        // Log.i(TAG, masterPlayList.toString());
        for (Variant variant : masterPlayList.variants()) {
            String newUri = playerJs.getFile().replace("playlist.m3u8", variant.uri().replaceFirst("./", ""));

            Optional<Resolution> resolutionOptional = variant.resolution();
            if (resolutionOptional.isPresent()) {
                qualitiesMap.put(ParserUtils.standardizeQuality(String.valueOf(resolutionOptional.get().height())), newUri);
            } else {
                qualitiesMap.put("AUTO", playerJs.getFile());
            }
        }
        model.setHeaders(Collections.singletonMap("User-Agent", ApiClient.DESKTOP_USER_AGENT));
        model.setLinksQuality(qualitiesMap);
        model.setDefaultQuality(ParserUtils.standardizeQuality(playerJs.getDefaultQuality()));
        model.setSubtileUrl(playerJs.getSubtitle());
        return model;
    }

    private Single<Pair<String, PlayerJsResponse>> downloadManifest() {
        return Single.create(emitter -> {
            extract();
            Gson gson = new Gson();
            String json = ParserUtils.getMatcherResult(
                    PLAYER_JS_PATTERN, getDocument().data(), 1);
            int lastIndex = json.lastIndexOf(",");
            if (lastIndex >= 0) {
                json = json.substring(0, lastIndex) + "}";
            }
            PlayerJsResponse playerJs = gson.fromJson(json, PlayerJsResponse.class);
            Log.d(TAG, playerJs.getFile());
            Request build = new Request.Builder().url(playerJs.getFile()).get().build();
            try (Response response = client.newCall(build).execute()) {
                String masterPlaylist = response.body().string();
                if (Strings.isNullOrEmpty(masterPlaylist)) {
                    emitter.onError(new Throwable("masterPlaylist is null of empty"));
                }
                emitter.onSuccess(new Pair<>(masterPlaylist, playerJs));

            } catch (UnknownHostException exception) {
                emitter.onError(exception.fillInStackTrace());
            }
        });
    }
}
