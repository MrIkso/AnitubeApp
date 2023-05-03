package com.mrikso.anitube.app.extractors;

import android.util.Log;

import androidx.core.util.Pair;

import com.google.common.base.Strings;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.VideoLinksModel;
import com.mrikso.anitube.app.utils.JSUnpacker;

import io.reactivex.rxjava3.core.Single;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MP4UploadExtractor extends BaseVideoLinkExtracror {
    private final String TAG = "MP4UploadExtractor";

    public MP4UploadExtractor(String ifRame) {
        super(fixURL(ifRame));
    }

    @Override
    public Single<Pair<LoadState, VideoLinksModel>> parse() {
        return Single.fromCallable(
                () -> {
                    extract();
                    return parseVideo(getDocument().html());
                });
    }

    private Pair<LoadState, VideoLinksModel> parseVideo(String response) {
        JSUnpacker jsUnpacker = new JSUnpacker(getEvalCode(response));
        if (jsUnpacker.detect()) {
            String src = getSrc(jsUnpacker.unpack());
            Log.i(TAG, "src" + src);
            if (!Strings.isNullOrEmpty(src)) {
                VideoLinksModel videoLinks = new VideoLinksModel(getUrl());
                videoLinks.setSingleDirectUrl(src);
                videoLinks.setIgnoreSSL(true);
                videoLinks.setHeaders(
                        Collections.singletonMap("Referer", "https://www.mp4upload.com/"));
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
