package com.mrikso.anitube.app.parser;

import android.util.Log;

import androidx.core.util.Pair;

import com.mrikso.anitube.app.extractors.AhsdiVideosExtractor;
import com.mrikso.anitube.app.extractors.MP4UploadExtractor;
import com.mrikso.anitube.app.extractors.MoonAnimeArtExtractor;
import com.mrikso.anitube.app.extractors.StreamSBExtractor;
import com.mrikso.anitube.app.extractors.UdropExtractor;
import com.mrikso.anitube.app.extractors.csstExtractor;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.VideoLinksModel;

import io.reactivex.rxjava3.core.Single;

import okhttp3.OkHttpClient;

import javax.inject.Inject;

public class DirectVideoUrlParser {
    private static String TAG = "DirectVideoUrlParser";
    private OkHttpClient client;

    @Inject
    public DirectVideoUrlParser(OkHttpClient client) {
        this.client = client;
    }

    public Single<Pair<LoadState, VideoLinksModel>> getDirectUrl(String iframeUrl) {
        try {
            Log.i(TAG, "iframeurl: " + iframeUrl);
            if (iframeUrl.contains("https://tortuga.wtf/")) {
                Single<Pair<LoadState, VideoLinksModel>> m3u8Url =
                        new AhsdiVideosExtractor(iframeUrl, client).parse();
                return m3u8Url;
            } else if (iframeUrl.contains("https://ashdi.vip/")) {
                Single<Pair<LoadState, VideoLinksModel>> m3u8Url =
                        new AhsdiVideosExtractor(iframeUrl, client).parse();
                return m3u8Url;
            } else if (iframeUrl.contains("https://www.udrop.com/")) {
                Single<Pair<LoadState, VideoLinksModel>> model =
                        new UdropExtractor(iframeUrl, client).parse();
                return model;
            } else if (iframeUrl.contains("csst.online")) {
                Single<Pair<LoadState, VideoLinksModel>> model =
                        new csstExtractor(iframeUrl).parse();
                return model;
            } else if (iframeUrl.contains("https://www.mp4upload.com/")) {
                Single<Pair<LoadState, VideoLinksModel>> model =
                        new MP4UploadExtractor(iframeUrl).parse();
                return model;
            } else if (iframeUrl.contains("https://moonanime.art/")) {
                Single<Pair<LoadState, VideoLinksModel>> model =
                        new MoonAnimeArtExtractor(iframeUrl).parse();
                return model;
            } else if (iframeUrl.contains("sbembed.com")
                    || iframeUrl.contains("sbembed1.com")
                    || iframeUrl.contains("sbplay.org")
                    || iframeUrl.contains("sbvideo.net")
                    || iframeUrl.contains("streamsb.net")
                    || iframeUrl.contains("sbplay.one")
                    || iframeUrl.contains("cloudemb.com")
                    || iframeUrl.contains("playersb.com")
                    || iframeUrl.contains("tubesb.com")
                    || iframeUrl.contains("sbplay1.com")
                    || iframeUrl.contains("embedsb.com")
                    || iframeUrl.contains("watchsb.com")
                    || iframeUrl.contains("sbplay2.com")
                    || iframeUrl.contains("japopav.tv")
                    || iframeUrl.contains("viewsb.com")
                    || iframeUrl.contains("sbfast")
                    || iframeUrl.contains("sbfull.com")
                    || iframeUrl.contains("javplaya.com")
                    || iframeUrl.contains("ssbstream.net")
                    || iframeUrl.contains("p1ayerjavseen.com")
                    || iframeUrl.contains("sbthe.com")
                    || iframeUrl.contains("sbchill.com")
                    || iframeUrl.contains("sblongvu.com")
                    || iframeUrl.contains("sbanh.com")
                    || iframeUrl.contains("lvturbo.com")
                    || iframeUrl.contains("sblanh.com")) {
                Single<Pair<LoadState, VideoLinksModel>> model =
                        new StreamSBExtractor(iframeUrl, client).parse();
                return model;
            }
            /*
            else if (iframeUrl.contains("https://drive.google.com/")) {
                         Single<Pair<LoadState, VideoLinksModel>> model =
                                 new GoogleDriveExtractor(iframeUrl).parse();
                         return model;
                     }
            */
            else {
                Log.i(TAG, "unsupport, iframeurl: " + iframeUrl);
                return Single.just(new Pair<>(LoadState.ERROR, new VideoLinksModel(iframeUrl)));
            }
        } catch (Exception err) {
            err.printStackTrace();
            return Single.just(new Pair<>(LoadState.ERROR, new VideoLinksModel(iframeUrl)));
        }
    }
}
