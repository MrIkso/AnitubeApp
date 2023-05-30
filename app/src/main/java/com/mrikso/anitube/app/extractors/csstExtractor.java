package com.mrikso.anitube.app.extractors;

import android.util.Log;

import androidx.core.util.Pair;

import com.google.gson.Gson;
import com.mrikso.anitube.app.extractors.model.PlayerJsResponse;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.VideoLinksModel;
import com.mrikso.anitube.app.network.ApiClient;
import com.mrikso.anitube.app.utils.ParserUtils;
import com.mrikso.anitube.app.utils.StringUtils;

import io.reactivex.rxjava3.core.Single;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class csstExtractor extends BaseVideoLinkExtracror {
    private final String TAG = "csstExtractor";
    private final String PLAYER_JS_PATTERN = "Playerjs\\(([^)]+)\\)";
    private final String FILE_PATTERN = "\\[([\\d]+p?)\\](.*)";

    public csstExtractor(String ifRame) {
        super(ifRame);
    }

    @Override
    public Single<Pair<LoadState, VideoLinksModel>> parse() {
        return Single.fromCallable(() -> {
            extract();
            Gson gson = new Gson();
            String json = ParserUtils.getMatcherResult(
                    PLAYER_JS_PATTERN, getDocument().data(), 1);
            int lastIndex = json.lastIndexOf(",");
            if (lastIndex >= 0) {
                json = json.substring(0, lastIndex) + "}";
            }
            Log.i(TAG, json);
            PlayerJsResponse playerJs = gson.fromJson(json, PlayerJsResponse.class);
            VideoLinksModel model = new VideoLinksModel(url);
            Map<String, String> qualityMap = new HashMap<>();
            String file = playerJs.getFile();
            if (file.contains(",")) {

                String[] fileArray = file.split(",");
                for (String fileBase : fileArray) {
                    Pattern pattern = Pattern.compile(FILE_PATTERN);
                    Matcher matcher = pattern.matcher(fileBase);
                    if (matcher.find()) {
                        String quality = ParserUtils.standardizeQuality(matcher.group(1)); // "360p"
                        String url = StringUtils.removeLastChar(matcher.group(2));
                        Log.i(TAG, quality + " " + url);
                        qualityMap.put(quality, url);
                    }
                }
                model.setLinksQuality(qualityMap);
            } else {
                model.setSingleDirectUrl(file.endsWith("/") ? StringUtils.removeLastChar(file) : file);
            }
            model.setDefaultQuality(ParserUtils.standardizeQuality(playerJs.getDefaultQuality()));
            model.setHeaders(Collections.singletonMap("User-Agent", ApiClient.DESKTOP_USER_AGENT));
            model.setSubtileUrl(playerJs.getSubtitle());
            return new Pair<>(LoadState.DONE, model);
        });
    }
}
