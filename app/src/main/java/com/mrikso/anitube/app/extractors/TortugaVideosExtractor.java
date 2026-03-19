package com.mrikso.anitube.app.extractors;

import android.util.Base64;

import androidx.core.util.Pair;

import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.VideoLinksModel;
import com.mrikso.anitube.app.network.ApiClient;
import com.mrikso.anitube.app.utils.ParserUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class TortugaVideosExtractor extends BaseVideoLinkExtracror {
    private final String TAG = "TortugaVideosExtractor";
    private final String PLAYER_JS_PATTERN = "file:\\s*\"([^\"]*|\\d+|\\{[^}]*\\})\"";
    private final MultivariantPlaylistParser masterPlaylistParser = new MultivariantPlaylistParser();

    public TortugaVideosExtractor(String url, OkHttpClient client) {
        super(url, client);
    }

    private VideoLinksModel getModel(String masterU3u8, String masterPlayListUrl) throws IOException {
        Map<String, String> qualitiesMap = new HashMap<>();
        VideoLinksModel model = new VideoLinksModel(masterPlayListUrl);
        MultivariantPlaylist masterPlayList = masterPlaylistParser.readPlaylist(masterU3u8);
        //Log.i(TAG, "start parse playlist");
        //Log.i(TAG, masterPlayList.toString());
        for (Variant variant : masterPlayList.variants()) {
            String uri = variant.uri();

            Pattern pattern = Pattern.compile("/(\\d+)/");
            Matcher matcher = pattern.matcher(uri);

            if (matcher.find()) {
                String resolution = matcher.group(1);
                if (!uri.startsWith("https://")) {
                    uri = model.getIfRameUrl().replaceAll("playlist.m3u8|index.m3u8", variant.uri().replaceFirst("./", ""));
                }
                //Log.i(TAG, " " + resolution + "=>" + uri);
                qualitiesMap.put(ParserUtils.standardizeQuality(resolution), uri);
            } else {
                qualitiesMap.put("AUTO", model.getIfRameUrl());
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
        if (!qualitiesMap.isEmpty()) {
            model.setDefaultQuality(qualitiesMap.keySet().stream().findFirst().get());
        }
        //model.setSubtileUrl(playerJs.getSubtitle());
        return model;
    }

    private Single<Pair<String, String>> downloadManifest() {
        return Single.create(emitter -> {
            String newPlayerUrl = getUrl().replaceAll("https://tortuga\\.[a-z]{2,3}/vod/(\\d+\\w*)", "https://tortuga.tw/vod/$1");
            String page = client.newCall(
                            new Request.Builder().url(newPlayerUrl).get().build())
                    .execute()
                    .body()
                    .string();
            // Gson gson = new Gson();
            //  String jsCode = getDocument().selectFirst("script").data();
            //  Log.i(TAG, " " + getDocument().bo());
            String masterPlaylistUrlEnc = ParserUtils.getMatcherResult(PLAYER_JS_PATTERN, page, 1);
            //Log.i(TAG, "masterPlaylistUrl: " + masterPlaylistUrlEnc);
            //  PlayerJsResponse playerJs = gson.fromJson(json, PlayerJsResponse.class);
            String masterPlaylistUrl = decryptUrl(masterPlaylistUrlEnc);

            String masterPlaylist = client.newCall(
                            new Request.Builder().url(masterPlaylistUrl).get().build())
                    .execute()
                    .body()
                    .string();

            emitter.onSuccess(new Pair<>(masterPlaylist, masterPlaylistUrl));
        });
    }

    @Override
    public Single<Pair<LoadState, VideoLinksModel>> parse() {
        return downloadManifest()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(v -> new Pair<>(LoadState.DONE, getModel(v.first, v.second)));
    }

    private static String decryptUrl(String base64Input) {
        if (base64Input == null || base64Input.isEmpty()) return "";
        if (base64Input.endsWith("==")) {
            base64Input = base64Input.replaceAll("==", "");
        }

        byte[] bytes = Base64.decode(base64Input,
                Base64.NO_WRAP | Base64.NO_PADDING);
        String s = new String(bytes, StandardCharsets.UTF_8);
        return new StringBuilder(s).reverse().toString();
    }
}
