package com.mrikso.anitube.app.extractors;

import android.util.Log;

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

import io.lindstrom.m3u8.model.MultivariantPlaylist;
import io.lindstrom.m3u8.model.Variant;
import io.lindstrom.m3u8.parser.MultivariantPlaylistParser;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AhsdiVideosExtractor extends BaseVideoLinkExtracror {
    private final String TAG = "AhsdiVideosExtractor";
    private final String PLAYER_JS_PATTERN = "Playerjs\\(([^)]+)\\)";
    private final MultivariantPlaylistParser masterPlaylistParser = new MultivariantPlaylistParser();
	
    public AhsdiVideosExtractor(String url, OkHttpClient client) {
        super(url, client);
    }

    private VideoLinksModel getModel(String masterU3u8, PlayerJsResponse playerJs) throws IOException {
        Map<String, String> qualitiesMap = new HashMap<>();
        VideoLinksModel model = new VideoLinksModel(playerJs.getFile());
        MultivariantPlaylist masterPlayList = masterPlaylistParser.readPlaylist(masterU3u8);
        //Log.i(TAG, "start parse playlist");
        Log.i(TAG, masterPlayList.toString());
        for (Variant variant : masterPlayList.variants()) {
            String uri = variant.uri();
            String newUri = uri;
            Pattern pattern = Pattern.compile("/hls/(\\d+)/");
            Matcher matcher = pattern.matcher(newUri);

            if (matcher.find()) {
                String resolution = matcher.group(1);
                Log.i(TAG, " " + resolution + "=>" + newUri);
                qualitiesMap.put(ParserUtils.standardizeQuality(resolution), newUri);
            } else {
                qualitiesMap.put("AUTO", playerJs.getFile());
            }

        }
        model.setHeaders(Collections.singletonMap("User-Agent", ApiClient.DESKTOP_USER_AGENT));
        if (!qualitiesMap.isEmpty()) {
            model.setLinksQuality(qualitiesMap);
            model.setDefaultQuality(ParserUtils.standardizeQuality(playerJs.getDefaultQuality()));
        } else {
            model.setSingleDirectUrl(playerJs.getFile());
        }
        model.setSubtileUrl(playerJs.getSubtitle());
        return model;
    }

    private Single<Pair<String, PlayerJsResponse>> downloadManifest() {
        return Single.create(emitter -> {
            Request request1 = new Request.Builder().url(getUrl()).get().build();
            Call call1 = client.newCall(request1);
            emitter.setCancellable(call1::cancel);

            try {
                try (Response resp1 = call1.execute()) {
                    if (!resp1.isSuccessful() || resp1.body() == null) {
                        if (!emitter.isDisposed()) {
                            emitter.tryOnError(new IOException("Failed to download manifest page"));
                        }
                        return;
                    }
                    String page = resp1.body().string();
                    Gson gson = new Gson();
                    String json = ParserUtils.getMatcherResult(PLAYER_JS_PATTERN, page, 1);
                    PlayerJsResponse playerJs = gson.fromJson(json, PlayerJsResponse.class);

                    Request request2 = new Request.Builder().url(playerJs.getFile()).get().build();
                    Call call2 = client.newCall(request2);
                    emitter.setCancellable(call2::cancel);

                    try (Response resp2 = call2.execute()) {
                        if (!resp2.isSuccessful() || resp2.body() == null) {
                            if (!emitter.isDisposed()) {
                                emitter.tryOnError(new IOException("Failed to download master playlist"));
                            }
                            return;
                        }
                        String masterPlaylist = resp2.body().string();
                        if (!emitter.isDisposed()) {
                            emitter.onSuccess(new Pair<>(masterPlaylist, playerJs));
                        }
                    }
                }
            } catch (Exception ex) {
                if (!emitter.isDisposed()) {
                    emitter.tryOnError(ex);
                }
            }
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
