package com.mrikso.anitube.app.extractors;

import android.util.Log;

import androidx.core.util.Pair;

import com.google.gson.Gson;
import com.mrikso.anitube.app.extractors.model.StreamSbResponse;
import com.mrikso.anitube.app.extractors.utils.Utils;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.VideoLinksModel;
import com.mrikso.anitube.app.network.ApiClient;
import com.mrikso.anitube.app.utils.OkHttpUtils;
import com.mrikso.anitube.app.utils.ParserUtils;

import io.lindstrom.m3u8.model.MasterPlaylist;
import io.lindstrom.m3u8.model.Variant;
import io.lindstrom.m3u8.parser.MasterPlaylistParser;
import io.reactivex.rxjava3.core.Single;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StreamSBExtractor extends BaseVideoLinkExtracror {
    private final String TAG = "StreamSBExtractor";
    private final MasterPlaylistParser masterPlaylistParser = new MasterPlaylistParser();

    // https://github.com/jmir1/aniyomi-extensions/blob/0c031d7bc2f38949097c3f1db7f8248d71d190dd/lib/streamsb-extractor/src/main/java/eu/kanade/tachiyomi/lib/streamsbextractor/StreamSBExtractor.kt
    public StreamSBExtractor(String ifRame, OkHttpClient client) {
        super(ifRame, client);
    }

    @Override
    public Single<Pair<LoadState, VideoLinksModel>> parse() {
        return Single.fromCallable(
                () -> {
                    VideoLinksModel model =
                            videosFromUrl(getUrl(), Headers.of(new String[0]), false, false);
                    if (model != null) return new Pair<>(LoadState.DONE, model);
                    else return new Pair<>(LoadState.ERROR, new VideoLinksModel(getUrl()));
                });
    }

    // animension, asianload and dramacool uses "common = false"
    private String fixUrl(String url, boolean common) {
        String host = Utils.getDomainFromURL(url);
        String sbUrl = host + "/sources16";
        String id = Utils.getID(url);
        if (common) {
            String hexBytes = bytesToHex(id.getBytes());
            return sbUrl
                    + "/625a364258615242766475327c7c"
                    + hexBytes
                    + "7c7c4761574550654f7461566d347c7c73747265616d7362";
        } else {
            return sbUrl + "/" + bytesToHex(("||" + id + "||||streamsb").getBytes()) + "/";
        }
    }

    protected String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;

            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private VideoLinksModel videosFromUrl(
            String url, Headers headers, boolean common, boolean manualData) {
        String trimmedUrl = url.trim(); // Prevents some crashes
        Headers newHeaders =
                manualData
                        ? headers
                        : headers.newBuilder()
                                .set("referer", trimmedUrl)
                                .set("watchsb", "sbstream")
                                .set("authority", "embedsb.com")
                                .build();
        try {
            String master = manualData ? trimmedUrl : fixUrl(trimmedUrl, common);
            ResponseBody response =
                    client.newCall(
                                    new Request.Builder()
                                            .url(master)
                                            .headers(newHeaders)
                                            .get()
                                            .build())
                            .execute()
                            .body();

            if (response == null) {
                return null;
            }
            String responseBody = response.string();
            Log.i(TAG, responseBody);
            Gson gson = new Gson();
            StreamSbResponse responseModel = gson.fromJson(responseBody, StreamSbResponse.class);
            if (responseModel.getStatusCode() == 200) {
                // download masterPlaylist and parse it
                String masterUrl = responseModel.getStreamData().getFile().trim();
                String masterPlaylist =
                        client.newCall(
                                        new Request.Builder()
                                                .url(masterUrl)
                                                .headers(newHeaders)
                                                .get()
                                                .build())
                                .execute()
                                .body()
                                .string();
                return buildModel(masterPlaylist, newHeaders);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private VideoLinksModel buildModel(String masterU3u8, Headers headers) throws IOException {
        Map<String, String> qualitiesMap = new HashMap<>();
        VideoLinksModel model = new VideoLinksModel(getUrl());
        MasterPlaylist masterPlayList = masterPlaylistParser.readPlaylist(masterU3u8);
        Log.i(TAG, "start parse playlist");
        Log.i(TAG, masterPlayList.toString());
        for (Variant variant : masterPlayList.variants()) {
            String newUri = variant.uri();
            // String[] parts = newUri.split("/");
            String resolution = String.valueOf(variant.resolution().get().height());
            Log.i(TAG, " " + resolution + "=>" + newUri);
            qualitiesMap.put(ParserUtils.standardizeQuality(resolution), newUri);
        }

        model.setLinksQuality(qualitiesMap);
        Map<String, String> headersMap = OkHttpUtils.headersToMap(headers);
        headersMap.put("User-Agent", ApiClient.DESKTOP_USER_AGENT);
        model.setHeaders(headersMap);
        //  model.setDefaultQuality(ParserUtils.standardizeQuality(playerJs.getDefaultQuality()));
        //  model.setSubtileUrl(playerJs.getSubtitle());
        return model;
    }
}
