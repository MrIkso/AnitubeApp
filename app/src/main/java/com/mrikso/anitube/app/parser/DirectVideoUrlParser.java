package com.mrikso.anitube.app.parser;

import android.util.Log;

import androidx.core.util.Pair;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.mrikso.anitube.app.App;
import com.mrikso.anitube.app.extractors.AhsdiVideosExtractor;
import com.mrikso.anitube.app.extractors.BaseVideoLinkExtracror;
import com.mrikso.anitube.app.extractors.FourSharedExtractor;
import com.mrikso.anitube.app.extractors.GoogleDriveExtractor;
import com.mrikso.anitube.app.extractors.MP4UploadExtractor;
import com.mrikso.anitube.app.extractors.MoonAnimeArtExtractor;
import com.mrikso.anitube.app.extractors.PeertubeExtractor;
import com.mrikso.anitube.app.extractors.StreamSBExtractor;
import com.mrikso.anitube.app.extractors.TortugaVideosExtractor;
import com.mrikso.anitube.app.extractors.UdropExtractor;
import com.mrikso.anitube.app.extractors.VeohVideodExtractor;
import com.mrikso.anitube.app.extractors.csstExtractor;
import com.mrikso.anitube.app.extractors.model.DomainsModel;
import com.mrikso.anitube.app.extractors.utils.Utils;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.VideoLinksModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;
import okhttp3.OkHttpClient;

public class DirectVideoUrlParser {
    private static final String TAG = "DirectVideoUrlParser";
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

            String iframeDomain = Objects.requireNonNull(Utils.getDomainFromURL(iframeUrl)).replace("https://", "");
            Log.i(TAG, "iframeurl: " + iframeUrl + " iframeDomain: " + iframeDomain);
            BaseVideoLinkExtracror extracror;

            if (iframeDomain.contains("tortuga")) {
                extracror = new TortugaVideosExtractor(iframeUrl, client);
            } else if (iframeDomain.contains("ashdi.vip")) {
                extracror = new AhsdiVideosExtractor(iframeUrl, client);
            } else if (iframeDomain.contains("udrop.com")) {
                extracror = new UdropExtractor(iframeUrl, client);
            } else if (iframeDomain.contains("csst.online")) {
                extracror = new csstExtractor(iframeUrl);
            } else if (iframeDomain.contains("www.mp4upload.com")) {
                extracror = new MP4UploadExtractor(iframeUrl, client);
            } else if (iframeDomain.contains("moonanime.art")) {
                extracror = new MoonAnimeArtExtractor(iframeUrl, client);
            } else if (iframeDomain.contains("monstro.site") || iframeDomain.contains("alllvideo.monster")) {
                //плеєр монстр
                extracror = new csstExtractor(iframeUrl);
            } else if (iframeDomain.contains("veoh.com")) {
                extracror = new VeohVideodExtractor(iframeUrl);
            } else if (iframeDomain.contains("4shared.com")) {
                extracror = new FourSharedExtractor(iframeUrl, client);
            } else if (domainsModel.getPeertube().contains(iframeDomain)) {
                extracror = new PeertubeExtractor(iframeUrl, client);
            } else if (domainsModel.getStreamsb().contains(iframeDomain)) {
                extracror = new StreamSBExtractor(iframeUrl, client);
            } else if (iframeDomain.contains("drive.google.com")) {
                extracror = new GoogleDriveExtractor(iframeUrl);
            }else {
                Log.i(TAG, "unsupported, iframe: " + iframeUrl);
                return Single.just(new Pair<>(LoadState.ERROR, new VideoLinksModel(iframeUrl)));
            }

            return extracror.parse();
        } catch (Exception err) {
           // err.printStackTrace();
            return Single.just(new Pair<>(LoadState.ERROR, new VideoLinksModel(iframeUrl)));
        }
    }
}
