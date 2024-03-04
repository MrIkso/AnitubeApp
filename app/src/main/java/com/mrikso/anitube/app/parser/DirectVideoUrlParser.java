package com.mrikso.anitube.app.parser;

import android.util.Log;

import androidx.core.util.Pair;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.mrikso.anitube.app.App;
import com.mrikso.anitube.app.extractors.AhsdiVideosExtractor;
import com.mrikso.anitube.app.extractors.FourSharedExtractor;
import com.mrikso.anitube.app.extractors.GoogleDriveExtractor;
import com.mrikso.anitube.app.extractors.MP4UploadExtractor;
import com.mrikso.anitube.app.extractors.MoonAnimeArtExtractor;
import com.mrikso.anitube.app.extractors.PeertubeExtractor;
import com.mrikso.anitube.app.extractors.StreamSBExtractor;
import com.mrikso.anitube.app.extractors.UdropExtractor;
import com.mrikso.anitube.app.extractors.VeohVideodExtractor;
import com.mrikso.anitube.app.extractors.csstExtractor;
import com.mrikso.anitube.app.extractors.model.DomainsModel;
import com.mrikso.anitube.app.extractors.utils.Utils;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.VideoLinksModel;

import io.reactivex.rxjava3.core.Single;

import okhttp3.OkHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

public class DirectVideoUrlParser {
    private static String TAG = "DirectVideoUrlParser";
    private final OkHttpClient client;
    private final DomainsModel domainsModel;

    @Inject
    public DirectVideoUrlParser(OkHttpClient client) {
        this.client = client;
        domainsModel = createDomainsModel();
    }

    private DomainsModel createDomainsModel() {
        try {
            InputStream json = App.getApplication().getAssets().open("data.json");
            Gson gson = new Gson();
            JsonReader jsonReader = gson.newJsonReader(new InputStreamReader(json, StandardCharsets.UTF_8));

            DomainsModel model = gson.fromJson(jsonReader, DomainsModel.class);
            json.close();
            return model;
        } catch (IOException err) {
            err.printStackTrace();
        }
        return null;
    }

    public Single<Pair<LoadState, VideoLinksModel>> getDirectUrl(String iframeUrl) {
        try {

            String iframeDomain = Utils.getDomainFromURL(iframeUrl).replace("https://", "");
            Log.i(TAG, "iframeurl: " + iframeUrl + " iframeDomain: " + iframeDomain);
            if (iframeDomain.contains("tortuga.wtf")) {
                Single<Pair<LoadState, VideoLinksModel>> m3u8Url = new AhsdiVideosExtractor(iframeUrl, client).parse();
                return m3u8Url;
            } else if (iframeDomain.contains("ashdi.vip")) {
                Single<Pair<LoadState, VideoLinksModel>> m3u8Url = new AhsdiVideosExtractor(iframeUrl, client).parse();
                return m3u8Url;
            } else if (iframeDomain.contains("udrop.com")) {
                Single<Pair<LoadState, VideoLinksModel>> model = new UdropExtractor(iframeUrl, client).parse();
                return model;
            } else if (iframeDomain.contains("csst.online")) {
                Single<Pair<LoadState, VideoLinksModel>> model = new csstExtractor(iframeUrl).parse();
                return model;
            } else if (iframeDomain.contains("www.mp4upload.com")) {
                Single<Pair<LoadState, VideoLinksModel>> model = new MP4UploadExtractor(iframeUrl, client).parse();
                return model;
            } else if (iframeDomain.contains("moonanime.art")) {
                Single<Pair<LoadState, VideoLinksModel>> model = new MoonAnimeArtExtractor(iframeUrl).parse();
                return model;
            } else if (iframeDomain.contains("monstro.site")) {
				//плеєр монстр
                Single<Pair<LoadState, VideoLinksModel>> model = new csstExtractor(iframeUrl).parse();
                return model;
            } else if (iframeDomain.contains("veoh.com")) {
                Single<Pair<LoadState, VideoLinksModel>> model = new VeohVideodExtractor(iframeUrl).parse();
                return model;
            } else if (iframeDomain.contains("4shared.com")) {
                Single<Pair<LoadState, VideoLinksModel>> model = new FourSharedExtractor(iframeUrl, client).parse();
                return model;
            } else if (domainsModel.getPeertube().contains(iframeDomain)) {
                Single<Pair<LoadState, VideoLinksModel>> model = new PeertubeExtractor(iframeUrl, client).parse();
                return model;
            } else if (domainsModel.getStreamsb().contains(iframeDomain)) {
                Single<Pair<LoadState, VideoLinksModel>> model = new StreamSBExtractor(iframeUrl, client).parse();
                return model;
            } else if (iframeDomain.contains("drive.google.com")) {
                Single<Pair<LoadState, VideoLinksModel>> model = new GoogleDriveExtractor(iframeUrl).parse();
                return model;
            } else {
                Log.i(TAG, "unsupport, iframeurl: " + iframeUrl);
                return Single.just(new Pair<>(LoadState.ERROR, new VideoLinksModel(iframeUrl)));
            }
        } catch (Exception err) {
            err.printStackTrace();
            return Single.just(new Pair<>(LoadState.ERROR, new VideoLinksModel(iframeUrl)));
        }
    }
}
