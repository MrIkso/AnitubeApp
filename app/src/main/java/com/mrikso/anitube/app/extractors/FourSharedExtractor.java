package com.mrikso.anitube.app.extractors;

import android.util.Log;

import androidx.core.util.Pair;

import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.VideoLinksModel;

import io.reactivex.rxjava3.core.Single;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class FourSharedExtractor extends BaseVideoLinkExtracror {

    private final String TAG = "FourSharedExtractor";

    public FourSharedExtractor(String url, OkHttpClient client) {
        super(url, client);
    }

    @Override
    public Single<Pair<LoadState, VideoLinksModel>> parse() {
        return Single.fromCallable(() -> {
            String page = client.newCall(
                            new Request.Builder().url(getUrl()).get().build())
                    .execute()
                    .body()
                    .string();
            Document doc = Jsoup.parse(page);
            Element videoElement = doc.selectFirst("div.video-container video");
            VideoLinksModel model = new VideoLinksModel(url);
            if (videoElement != null) {
                String videoUrl = videoElement.getElementsByTag("source").attr("src");

                Log.i(TAG, "url: " + videoUrl);
                model.setSingleDirectUrl(videoUrl);
                return new Pair<>(LoadState.DONE, model);
            }
            return new Pair<>(LoadState.ERROR, model);
        });
    }
}
