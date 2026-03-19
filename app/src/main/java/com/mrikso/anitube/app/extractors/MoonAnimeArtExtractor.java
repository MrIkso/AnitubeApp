package com.mrikso.anitube.app.extractors;

import android.util.Base64;
import android.util.Log;

import androidx.core.util.Pair;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.mrikso.anitube.app.extractors.model.PlayerJsResponse;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.VideoLinksModel;
import com.mrikso.anitube.app.network.ApiClient;
import com.mrikso.anitube.app.utils.ParserUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.lindstrom.m3u8.model.MultivariantPlaylist;
import io.lindstrom.m3u8.model.Resolution;
import io.lindstrom.m3u8.model.Variant;
import io.lindstrom.m3u8.parser.MultivariantPlaylistParser;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MoonAnimeArtExtractor extends BaseVideoLinkExtracror {

    private final String TAG = "MoonAnimeArtExtractor";
    private final String PLAYER_JS_PATTERN = "Playerjs\\(([^)]+)\\)";
    private final MultivariantPlaylistParser masterPlaylistParser = new MultivariantPlaylistParser();

    private final Map<String, String> HEADERS = Map.of("User-Agent", ApiClient.DESKTOP_USER_AGENT,
            "Accept", "*/*",
            "accept-language", "uk,ru;q=0.9,en-US;q=0.8,en;q=0.7",
            "origin", "https://moonanime.art");

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
        MultivariantPlaylist masterPlayList = masterPlaylistParser.readPlaylist(masterU3u8);
        // Log.i(TAG, "start parse playlist");
        // Log.i(TAG, masterPlayList.toString());
        for (Variant variant : masterPlayList.variants()) {
            String newUri = variant.uri();

            Optional<Resolution> resolutionOptional = variant.resolution();
            if (resolutionOptional.isPresent()) {
                qualitiesMap.put(ParserUtils.standardizeQuality(String.valueOf(resolutionOptional.get().height())), newUri);
            } else {
                qualitiesMap.put("AUTO", playerJs.getFile());
            }
        }
        model.setHeaders(HEADERS);
        model.setLinksQuality(qualitiesMap);
        model.setDefaultQuality(ParserUtils.standardizeQuality(playerJs.getDefaultQuality()));
        model.setSubtileUrl(playerJs.getSubtitle());
        return model;
    }

    private Single<Pair<String, PlayerJsResponse>> downloadManifest() {
        return Single.create(emitter -> {
            Response manifestRequest = client.newCall(new Request.Builder()
                            .url(getUrl())
                            .headers(Headers.of(HEADERS))
                            .get()
                            .build())
                    .execute();
            if (!manifestRequest.isSuccessful()) {
                emitter.onError(new Exception("moonanime.art manifest don`t downloaded"));
                return;
            }
            String responseBody = manifestRequest.body().string();
            // Log.d(TAG, responseBody);
            // find player js script
            Element bodyElement = Jsoup.parse(responseBody).body();
            Element scriptElement = bodyElement.getElementsByTag("script").first();
            String jsTextCode = scriptElement.html();
            // Log.d(TAG, jsTextCode);
            // find encrypted text on this script
            String regex = "atob\\([\"']([A-Za-z0-9+/=]+)[\"']\\)";

            String scriptBase64 = ParserUtils.getMatcherResult(
                    regex, jsTextCode, 1);
            if (Strings.isNullOrEmpty(scriptBase64)) {
                emitter.onError(new Exception("Encrypted PlayesJS script not found"));
                return;
            }
            String decryptedScript = decryptPlayerJsScript(scriptBase64);
            Log.d(TAG, decryptedScript);

            Matcher funcMatcher = Pattern.compile("([a-zA-Z0-9_$]+)\\([\"']([A-Za-z0-9+/=]+)[\"']\\)").matcher(decryptedScript);
            if (!funcMatcher.find()) {
                emitter.onError(new Exception("Decryption link function script not found on PlayesJS script"));
                return;
            }
            String funcName = funcMatcher.group(1);
            String bodyRegex = "function " + funcName + "\\(e\\)\\{(.*?)\\}";
            String funcBody = ParserUtils.getMatcherResult(bodyRegex, decryptedScript, 1);
            String dynamicKey = ParserUtils.getMatcherResult("(?:var|let|const)\\s+\\w+\\s*=\\s*[\"'](.*?)[\"']", funcBody, 1);

            String dynamicCallRegex = funcName + "\\([\"']([A-Za-z0-9+/=]+)[\"']\\)";
            Pattern dataPattern = Pattern.compile(dynamicCallRegex);
            Matcher dataMatcher = dataPattern.matcher(decryptedScript);
            StringBuffer sb = new StringBuffer();

            while (dataMatcher.find()) {
                String encryptedValue = dataMatcher.group(1);
                String decryptedValue = decryptLinks(encryptedValue, dynamicKey);
                dataMatcher.appendReplacement(sb, "\"" + decryptedValue + "\"");
            }
            dataMatcher.appendTail(sb);
            String cleanJsCode = sb.toString();

            Gson gson = new Gson();
            String json = ParserUtils.getMatcherResult(
                    PLAYER_JS_PATTERN, cleanJsCode, 1);
            //  Log.d(TAG, json);
            int lastIndex = json.lastIndexOf(",");
            if (lastIndex >= 0) {
                json = json.substring(0, lastIndex) + "}";
            }
            PlayerJsResponse playerJs = gson.fromJson(json, PlayerJsResponse.class);
            //Log.d(TAG, playerJs.getFile());
            manifestRequest.close();
            Request masterPlaylistRequest = new Request.Builder()
                    .url(playerJs.getFile())
                    .headers(Headers.of(HEADERS))
                    .get()
                    .build();
            try (Response response = client.newCall(masterPlaylistRequest).execute()) {
                String masterPlaylist = response.body().string();
                if (Strings.isNullOrEmpty(masterPlaylist)) {
                    emitter.onError(new Throwable("masterPlaylist is null of empty"));
                }
                response.close();
                emitter.onSuccess(new Pair<>(masterPlaylist, playerJs));

            } catch (UnknownHostException exception) {
                emitter.onError(exception);
            }
        });
    }

    public static String decryptPlayerJsScript(String base64Input) {
        byte[] base64Bytes = Base64.decode(base64Input, Base64.DEFAULT);
        byte[] keyBytes = new byte[32];
        System.arraycopy(base64Bytes, 0, keyBytes, 0, 32);

        byte[] resultBytes = new byte[base64Bytes.length - 32];
        for (int i = 0; i < resultBytes.length; i++) {
            resultBytes[i] = (byte) (base64Bytes[i + 32] ^ keyBytes[i % 32]);
        }

        return new String(resultBytes, StandardCharsets.UTF_8);
    }

    public static String decryptLinks(String base64Input, String key) {
        try {
            byte[] base64Bytes = Base64.decode(base64Input, Base64.DEFAULT);
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            byte[] resultBytes = new byte[base64Bytes.length];

            for (int i = 0; i < base64Bytes.length; i++) {
                resultBytes[i] = (byte) (base64Bytes[i] ^ keyBytes[i % keyBytes.length]);
            }
            return new String(resultBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

}
