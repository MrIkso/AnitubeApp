package com.mrikso.anitube.app.extractors;

import androidx.core.util.Pair;

import com.google.gson.Gson;
import com.mrikso.anitube.app.extractors.model.PlayerJsResponse;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.VideoLinksModel;
import com.mrikso.anitube.app.network.ApiClient;
import com.mrikso.anitube.app.utils.ParserUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.lindstrom.m3u8.model.MasterPlaylist;
import io.lindstrom.m3u8.model.Variant;
import io.lindstrom.m3u8.parser.MasterPlaylistParser;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class TortugaVideosExtractor extends BaseVideoLinkExtracror {
    private final String TAG = "TortugaVideosExtractor";
    private final String PLAYER_JS_PATTERN = "Playerjs\\(([^)]+)\\)";
    private final MasterPlaylistParser masterPlaylistParser = new MasterPlaylistParser();

    public TortugaVideosExtractor(String url, OkHttpClient client) {
        super(url, client);
    }

    private VideoLinksModel getModel(String masterU3u8, PlayerJsResponse playerJs) throws IOException {
        Map<String, String> qualitiesMap = new HashMap<>();
        VideoLinksModel model = new VideoLinksModel(playerJs.getFile());
        MasterPlaylist masterPlayList = masterPlaylistParser.readPlaylist(masterU3u8);
        //Log.i(TAG, "start parse playlist");
        //  Log.i(TAG, masterPlayList.toString());
        for (Variant variant : masterPlayList.variants()) {
            String uri = variant.uri();

            Pattern pattern = Pattern.compile("./(\\d+)/");
            Matcher matcher = pattern.matcher(uri);

            if (matcher.find()) {
                String resolution = matcher.group(1);
                //Log.i(TAG, " " + resolution + "=>" + uri);
                uri = playerJs.getFile().replaceAll("playlist.m3u8|index.m3u8", variant.uri().replaceFirst("./", ""));
                qualitiesMap.put(ParserUtils.standardizeQuality(resolution), uri);
            } else {
                qualitiesMap.put("AUTO", playerJs.getFile());
            }

            /*if(uri.startsWith("./")) {
                 newUri = playerJs.getFile().replace("playlist.m3u8", variant.uri().replaceFirst("./", ""));
            }
            else {
                newUri = uri;
            }
            Optional<Resolution> resolutionOptional =  variant.resolution();
            if(resolutionOptional.isPresent()){
                qualitiesMap.put(ParserUtils.standardizeQuality(String.valueOf(resolutionOptional.get().height())), newUri);
            }
            else {
                qualitiesMap.put("AUTO", newUri);
            }*/

        }
        model.setHeaders(Collections.singletonMap("User-Agent", ApiClient.DESKTOP_USER_AGENT));
        model.setLinksQuality(qualitiesMap);
        model.setDefaultQuality(ParserUtils.standardizeQuality(playerJs.getDefaultQuality()));
        model.setSubtileUrl(playerJs.getSubtitle());
        return model;
    }

    private Single<Pair<String, PlayerJsResponse>> downloadManifest() {
        return Single.create(emitter -> {
            String page = client.newCall(
                            new Request.Builder().url(getUrl()).get().build())
                    .execute()
                    .body()
                    .string();
            Gson gson = new Gson();
            //  String jsCode = getDocument().selectFirst("script").data();
            //  Log.i(TAG, " " + getDocument().bo());
            String json = ParserUtils.getMatcherResult(PLAYER_JS_PATTERN, page, 1);
            // Log.i(TAG, " " + json);
            PlayerJsResponse playerJs = gson.fromJson(json, PlayerJsResponse.class);

            String masterPlaylist = client.newCall(
                            new Request.Builder().url(playerJs.getFile()).get().build())
                    .execute()
                    .body()
                    .string();

            emitter.onSuccess(new Pair<>(masterPlaylist, playerJs));
        });
    }

    @Override
    public Single<Pair<LoadState, VideoLinksModel>> parse() {
        return downloadManifest()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(v -> new Pair<>(LoadState.DONE, getModel(v.first, v.second)));
    }
}
