package com.mrikso.anitube.app.extractors;

import android.util.Log;

import androidx.core.util.Pair;

import com.google.gson.Gson;
import com.mrikso.anitube.app.extractors.model.FenixPlaybackResponse;
import com.mrikso.anitube.app.extractors.model.FenixPlaylistResponse;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.VideoLinksModel;
import com.mrikso.anitube.app.network.ApiClient;
import com.mrikso.anitube.app.utils.ParserUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.lindstrom.m3u8.model.AlternativeRendition;
import io.lindstrom.m3u8.model.MediaType;
import io.lindstrom.m3u8.model.MultivariantPlaylist;
import io.lindstrom.m3u8.model.Variant;
import io.lindstrom.m3u8.parser.MultivariantPlaylistParser;
import io.lindstrom.m3u8.parser.ParsingMode;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FenixPlayExtractor extends BaseVideoLinkExtracror {
    private final String TAG = "FenixPlayExtractor";
    private final String FENIX_PLAYLIST_PATTERN = "window\\.FENIX_PLAYLIST\\s*=\\s*(\\{.+?\\});";
    private final String FENIX_MEDIA_ACCESS_PATTERN = "window\\.FENIX_MEDIA_ACCESS\\s*=\\s*(\\{.+?\\});";

    private final MultivariantPlaylistParser masterPlaylistParser = new MultivariantPlaylistParser(ParsingMode.LENIENT);

    public FenixPlayExtractor(String url, OkHttpClient client) {
        super(url, client);
    }

    @Override
    public Single<Pair<LoadState, VideoLinksModel>> parse() {
        return downloadManifest()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(v -> new Pair<>(LoadState.DONE, getModel(v.first, v.second, v.third)));
    }

    private VideoLinksModel getModel(String masterU3u8, FenixPlaybackResponse playbackResponse, FenixPlaylistResponse.FenixMediaAccess mediaAccess) throws IOException {
        Map<String, String> qualitiesMap = new HashMap<>();

        FenixPlaybackResponse.Episode ep = playbackResponse.getEpisode();
        String rawMasterUrl = ep.getHls_master_url();
        if ((rawMasterUrl == null || rawMasterUrl.isEmpty()) && ep.getTracks() != null && ep.getTracks().length > 0) {
            rawMasterUrl = ep.getTracks()[0].getHls_url();
        }

        String authenticatedMasterUrl = appendTokenToUrl(rawMasterUrl, null, mediaAccess);
        if (authenticatedMasterUrl.startsWith("/")) {
            String origin = mediaAccess != null ? mediaAccess.getOrigin() : "https://media.fenixplay.xyz";
            authenticatedMasterUrl = origin + authenticatedMasterUrl;
        }

        VideoLinksModel model = new VideoLinksModel(authenticatedMasterUrl);

        MultivariantPlaylist masterPlayList = masterPlaylistParser.readPlaylist(masterU3u8);
        Log.i(TAG, masterPlayList.toString());

        // parse video
        for (Variant variant : masterPlayList.variants()) {
            String uri = variant.uri();
            String authenticatedVariantUri = appendTokenToUrl(uri, null, mediaAccess);
            if (authenticatedVariantUri.startsWith("/")) {
                String origin = mediaAccess != null ? mediaAccess.getOrigin() : "https://media.fenixplay.xyz";
                authenticatedVariantUri = origin + authenticatedVariantUri;
            }

            if (variant.resolution().isPresent()) {
                int resolutionHeight = variant.resolution().get().height();
                qualitiesMap.put(ParserUtils.standardizeQuality(String.valueOf(resolutionHeight)), authenticatedVariantUri);
            } else {
                qualitiesMap.put("AUTO", authenticatedMasterUrl);
            }
        }
        // parse audio attribute
        for (AlternativeRendition rendition : masterPlayList.alternativeRenditions()) {
            if (rendition.type() == MediaType.AUDIO && rendition.uri().isPresent()) {
                String audioUri = rendition.uri().get();
                String authenticatedAudioUri = appendTokenToUrl(audioUri, null, mediaAccess);
                if (authenticatedAudioUri.startsWith("/")) {
                    String origin = mediaAccess != null ? mediaAccess.getOrigin() : "https://media.fenixplay.xyz";
                    authenticatedAudioUri = origin + authenticatedAudioUri;
                }
                String audioName = rendition.name();

                Log.i(TAG, "Found audio track: " + audioName + " -> " + authenticatedAudioUri);
                model.addAudioTrack(audioName, authenticatedAudioUri);
            }
        }

        // parse tracks from playbackResponse
        if (ep.getTracks() != null) {
            for (FenixPlaybackResponse.Track track : ep.getTracks()) {
                if (track.getLabel() != null && track.getHls_url() != null) {
                    String trackUrl = appendTokenToUrl(track.getHls_url(), null, mediaAccess);
                    if (trackUrl.startsWith("/")) {
                        String origin = mediaAccess != null ? mediaAccess.getOrigin() : "https://media.fenixplay.xyz";
                        trackUrl = origin + trackUrl;
                    }
                    model.addAudioTrack(track.getLabel(), trackUrl);
                }
            }
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", ApiClient.DESKTOP_USER_AGENT);
        if (mediaAccess != null && mediaAccess.getOrigin() != null && !mediaAccess.getOrigin().isEmpty()) {
            headers.put("Origin", mediaAccess.getOrigin());
            headers.put("Referer", mediaAccess.getOrigin() + "/");
        }

        model.setHeaders(headers);

        if (!qualitiesMap.isEmpty()) {
            model.setLinksQuality(qualitiesMap);
            model.setDefaultQuality("AUTO");
        } else {
            model.setSingleDirectUrl(authenticatedMasterUrl);
        }

        if (ep.getSubtitles() != null) {
            for (FenixPlaybackResponse.Subtitle sub : ep.getSubtitles()) {
                String subUrl = sub.getUrl();
                if (subUrl != null && !subUrl.isEmpty()) {
                    String authSubUrl = appendTokenToUrl(subUrl, null, mediaAccess);
                    if (authSubUrl.startsWith("/")) {
                        String origin = mediaAccess != null ? mediaAccess.getOrigin() : "https://media.fenixplay.xyz";
                        authSubUrl = origin + authSubUrl;
                    }
                    if ("uk".equals(sub.getLanguage()) && ("full".equals(sub.getKind()) || sub.isIs_default())) {
                        model.setSubtileUrl(authSubUrl);
                    } else if (model.getSubtileUrl() == null) {
                        model.setSubtileUrl(authSubUrl);
                    }
                }
            }
        }

        return model;
    }

    private String getToken(FenixPlaylistResponse playlistResponse, FenixPlaylistResponse.FenixMediaAccess mediaAccess) {
        if (mediaAccess != null && mediaAccess.getToken() != null && !mediaAccess.getToken().isEmpty()) {
            return mediaAccess.getToken();
        }
        if (playlistResponse != null && playlistResponse.getPlayback() != null && playlistResponse.getPlayback().getToken() != null && !playlistResponse.getPlayback().getToken().isEmpty()) {
            return playlistResponse.getPlayback().getToken();
        }
        return null;
    }

    private String appendTokenToUrl(String url, FenixPlaylistResponse playlistResponse, FenixPlaylistResponse.FenixMediaAccess mediaAccess) {
        String token = getToken(playlistResponse, mediaAccess);
        if (url == null || token == null || token.isEmpty()) {
            return url;
        }
        if (url.contains("token=")) {
            return url;
        }
        char separator = url.contains("?") ? '&' : '?';
        return url + separator + "token=" + token;
    }

    private Single<Triple<String, FenixPlaybackResponse, FenixPlaylistResponse.FenixMediaAccess>> downloadManifest() {
        return Single.create(emitter -> {
            Request request1 = new Request.Builder().url(getUrl()).get().build();
            Call call1 = client.newCall(request1);
            emitter.setCancellable(call1::cancel);

            try {
                try (Response resp1 = call1.execute()) {
                    if (!resp1.isSuccessful() || resp1.body() == null) {
                        if (!emitter.isDisposed()) {
                            emitter.tryOnError(new IOException("Failed to download Fenix page"));
                        }
                        return;
                    }
                    String page = resp1.body().string();

                    String playlistJson = ParserUtils.getMatcherResult(FENIX_PLAYLIST_PATTERN, page, 1);
                    if (playlistJson == null || playlistJson.isEmpty()) {
                        if (!emitter.isDisposed()) {
                            emitter.tryOnError(new IOException("Fenix playlist JSON not found in page"));
                        }
                        return;
                    }

                    String mediaAccessJson = ParserUtils.getMatcherResult(FENIX_MEDIA_ACCESS_PATTERN, page, 1);

                    Gson gson = new Gson();
                    FenixPlaylistResponse playlistResponse = gson.fromJson(playlistJson, FenixPlaylistResponse.class);
                    FenixPlaylistResponse.FenixMediaAccess mediaAccess = null;

                    if (mediaAccessJson != null && !mediaAccessJson.isEmpty()) {
                        mediaAccess = gson.fromJson(mediaAccessJson, FenixPlaylistResponse.FenixMediaAccess.class);
                    }

                    String slug = playlistResponse.getSlug();
                    String edition = playlistResponse.getDefault_edition();
                    int number = 1;

                    if (playlistResponse.getEpisodes() != null && playlistResponse.getEpisodes().length > 0) {
                        FenixPlaylistResponse.Episode ep = playlistResponse.getEpisodes()[0];
                        if (ep.getEdition() != null && !ep.getEdition().isEmpty()) {
                            edition = ep.getEdition();
                        }
                        if (ep.getNumber() > 0) {
                            number = ep.getNumber();
                        }
                    }

                    if (slug == null || slug.isEmpty()) {
                        if (!emitter.isDisposed()) {
                            emitter.tryOnError(new IOException("Fenix slug not found"));
                        }
                        return;
                    }
                    if (edition == null || edition.isEmpty()) {
                        edition = "tv";
                    }

                    String playbackApiUrl = "https://fenixplay.xyz/api/playback/" + slug + "/episodes/" + edition + "/" + number;

                    String token = getToken(playlistResponse, mediaAccess);

                    Request.Builder reqBuilderPlayback = new Request.Builder().url(playbackApiUrl).get();
                    reqBuilderPlayback.addHeader("User-Agent", ApiClient.DESKTOP_USER_AGENT);

                    if (token != null && !token.isEmpty()) {
                        reqBuilderPlayback.addHeader("x-fenix-playback", token);
                    }

                    Call callPlayback = client.newCall(reqBuilderPlayback.build());
                    emitter.setCancellable(callPlayback::cancel);

                    try (Response respPlayback = callPlayback.execute()) {
                        if (!respPlayback.isSuccessful() || respPlayback.body() == null) {
                            if (!emitter.isDisposed()) {
                                emitter.tryOnError(new IOException("Failed to fetch Fenix playback API. Code: " + respPlayback.code()));
                            }
                            return;
                        }
                        String playbackJsonStr = respPlayback.body().string();
                        FenixPlaybackResponse playbackResponse = gson.fromJson(playbackJsonStr, FenixPlaybackResponse.class);

                        if (playbackResponse == null || playbackResponse.getEpisode() == null) {
                            if (!emitter.isDisposed()) {
                                emitter.tryOnError(new IOException("Invalid Fenix playback response"));
                            }
                            return;
                        }

                        String rawMasterUrl = playbackResponse.getEpisode().getHls_master_url();
                        if ((rawMasterUrl == null || rawMasterUrl.isEmpty()) && playbackResponse.getEpisode().getTracks() != null && playbackResponse.getEpisode().getTracks().length > 0) {
                            rawMasterUrl = playbackResponse.getEpisode().getTracks()[0].getHls_url();
                        }

                        if (rawMasterUrl == null || rawMasterUrl.isEmpty()) {
                            if (!emitter.isDisposed()) {
                                emitter.tryOnError(new IOException("Master HLS URL not found in Fenix playback response"));
                            }
                            return;
                        }

                        String authenticatedMasterUrl = appendTokenToUrl(rawMasterUrl, playlistResponse, mediaAccess);
                        if (authenticatedMasterUrl.startsWith("/")) {
                            authenticatedMasterUrl = mediaAccess.getOrigin() + authenticatedMasterUrl;
                        }

                        Request.Builder reqBuilder2 = new Request.Builder().url(authenticatedMasterUrl).get();
                        reqBuilder2.addHeader("User-Agent", ApiClient.DESKTOP_USER_AGENT);
                        if (mediaAccess != null && mediaAccess.getOrigin() != null) {
                            reqBuilder2.addHeader("Origin", mediaAccess.getOrigin());
                            reqBuilder2.addHeader("Referer", mediaAccess.getOrigin() + "/");
                        }

                        Request request2 = reqBuilder2.build();
                        Call call2 = client.newCall(request2);
                        emitter.setCancellable(call2::cancel);

                        try (Response resp2 = call2.execute()) {
                            if (!resp2.isSuccessful() || resp2.body() == null) {
                                if (!emitter.isDisposed()) {
                                    emitter.tryOnError(new IOException("Failed to download master playlist. Code: " + resp2.code()));
                                }
                                return;
                            }
                            String masterPlaylist = resp2.body().string();
                            if (!emitter.isDisposed()) {
                                emitter.onSuccess(new Triple<>(masterPlaylist, playbackResponse, mediaAccess));
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                if (!emitter.isDisposed()) {
                    emitter.tryOnError(ex);
                }
            }
        });
    }

    private static class Triple<A, B, C> {
        public final A first;
        public final B second;
        public final C third;

        public Triple(A first, B second, C third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }
    }

}
