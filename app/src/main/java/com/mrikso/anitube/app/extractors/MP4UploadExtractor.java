package com.mrikso.anitube.app.extractors;

import android.util.Log;

import androidx.core.util.Pair;

import com.google.common.base.Strings;
import com.mrikso.anitube.app.extractors.utils.JSUnpacker;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.VideoLinksModel;
import com.mrikso.anitube.app.utils.ParserUtils;

import io.reactivex.rxjava3.core.Single;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MP4UploadExtractor extends BaseVideoLinkExtracror {
    private final String TAG = "MP4UploadExtractor";
    private final String PLAYER_PATTERN = "player\\.src\\(\\{[^}]*src:\\s*\"([^\"]*)\"[^}]*\\}\\);";

    public MP4UploadExtractor(String ifRame, OkHttpClient client) {
        super(ifRame, client);
    }

    @Override
    public Single<Pair<LoadState, VideoLinksModel>> parse() {
        return Single.fromCallable(() -> {
            Response response = client.newCall(
                            new Request.Builder().url(getUrl()).get().build())
                    .execute();
            if (response.code() != 404) {
                String page = response.body().string();
                return parseVideo(page);
            } else {
                return new Pair<>(LoadState.ERROR, new VideoLinksModel(getUrl()));
            }
        });
    }

    private Pair<LoadState, VideoLinksModel> parseVideo(String response) {
        if (!Strings.isNullOrEmpty(response)) {
            Log.i(TAG, response);
            String src = ParserUtils.getMatcherResult(PLAYER_PATTERN, response, 1);
            if (Strings.isNullOrEmpty(src)) {
                JSUnpacker jsUnpacker = new JSUnpacker(getEvalCode(response));
                if (jsUnpacker.detect()) {
                    src = getSrc(jsUnpacker.unpack());
                }
            }

            if (!Strings.isNullOrEmpty(src)) {
                Log.i(TAG, "src: " + src);
                VideoLinksModel videoLinks = new VideoLinksModel(getUrl());
                videoLinks.setSingleDirectUrl(src);
                videoLinks.setIgnoreSSL(true);
                videoLinks.setHeaders(Collections.singletonMap("Referer", "https://www.mp4upload.com/"));
                return new Pair<>(LoadState.DONE, videoLinks);
            }
        }
        return new Pair<>(LoadState.ERROR, new VideoLinksModel(getUrl()));
    }

    private static String fixURL(String url) {
        if (!url.contains("embed-")) {
            final String regex = "com\\/([^']*)";
            final Pattern pattern = Pattern.compile(regex);
            final Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                String id = matcher.group(1);
                if (id.contains("/")) {
                    id = id.substring(0, id.lastIndexOf("/"));
                }
                url = "https://www.mp4upload.com/embed-" + id + ".html";
            }
        }

        return url;
    }

    private static String getSrc(String code) {
        final String regex = "src\\(\"(.*?)\"\\);";
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static String getEvalCode(String html) {
        final String regex = "eval\\(function\\(p,a,c,k,e,(?:r|d)(.*)";
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }
}
