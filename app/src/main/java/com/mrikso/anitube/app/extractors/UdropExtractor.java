package com.mrikso.anitube.app.extractors;

import android.util.Log;

import androidx.core.util.Pair;

import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.VideoLinksModel;
import com.mrikso.anitube.app.utils.ParserUtils;

import io.reactivex.rxjava3.core.Single;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class UdropExtractor extends BaseVideoLinkExtracror {

    private final String TAG = "UdropExtractor";
    private final String MP4_PATTERN = "mp4HD:\\s\"([^\"]+)\"";

    public UdropExtractor(String url, OkHttpClient client) {
        super(url, client);
    }

    @Override
    public Single<Pair<LoadState, VideoLinksModel>> parse() {
        return Single.fromCallable(() -> {
            String videoUrl;
            if (getUrl().endsWith(".mp4")) {
                videoUrl = getUrl();
            } else {
                String page = client.newCall(
                                new Request.Builder().url(getUrl()).get().build())
                        .execute()
                        .body()
                        .string();
                videoUrl = ParserUtils.getMatcherResult(MP4_PATTERN, page, 1);
                Log.i(TAG, "url: " + videoUrl);
            }
            VideoLinksModel model = new VideoLinksModel(url);
            model.setSingleDirectUrl(videoUrl);
            return new Pair<>(LoadState.DONE, model);
        });
    }
}
