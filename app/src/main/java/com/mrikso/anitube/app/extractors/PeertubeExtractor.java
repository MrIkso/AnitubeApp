package com.mrikso.anitube.app.extractors;

import android.util.Log;

import androidx.core.util.Pair;

import com.google.common.base.Strings;
import com.mrikso.anitube.app.extractors.utils.DownloaderImpl;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.VideoLinksModel;
import com.mrikso.anitube.app.network.ApiClient;
import com.mrikso.anitube.app.utils.ParserUtils;

import io.reactivex.rxjava3.core.Single;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeStreamExtractor;
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.VideoStream;

import okhttp3.OkHttpClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PeertubeExtractor extends BaseVideoLinkExtracror {
    private static LinkHandler linkHandler;
    private static StreamExtractor extractor;

    public PeertubeExtractor(String ifRame, OkHttpClient client) {
        super(ifRame);
        NewPipe.init(DownloaderImpl.init(client.newBuilder()));
    }

    @Override
    public Single<Pair<LoadState, VideoLinksModel>> parse() {
        return Single.fromCallable(() -> {
            linkHandler = PeertubeStreamLinkHandlerFactory.getInstance().fromUrl(getUrl());

            extractor = new PeertubeStreamExtractor(ServiceList.PeerTube, linkHandler);
            extractor.fetchPage();

            VideoLinksModel model = new VideoLinksModel(getUrl());
            Map<String, String> qualityMap = new HashMap<>();
            for (VideoStream videoStream : extractor.getVideoStreams()) {

                if (!Strings.isNullOrEmpty(videoStream.getManifestUrl())) {
                    Log.i("PeertubeExtractor", "" + videoStream.getResolution() + " " + videoStream.getManifestUrl());

                    qualityMap.put(
                            ParserUtils.standardizeQuality(videoStream.getResolution()), videoStream.getManifestUrl());
                }
            }
            model.setLinksQuality(qualityMap);

            model.setHeaders(Collections.singletonMap("User-Agent", ApiClient.DESKTOP_USER_AGENT));

            return new Pair<>(LoadState.DONE, model);
        });
    }
}
