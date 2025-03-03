package com.mrikso.anitube.app.repository;

import com.mrikso.anitube.app.network.HikkaApi;
import com.mrikso.anitube.app.network.request.AddWatchRequest;
import com.mrikso.anitube.app.network.request.TokenRequest;
import com.mrikso.anitube.app.network.response.AnitubeAnimeResponse;
import com.mrikso.anitube.app.network.response.SuccesResponse;
import com.mrikso.anitube.app.network.response.TokenResponse;
import com.mrikso.anitube.app.network.response.WatchAnimeResponse;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;

public class HikkaRepository {

    private final HikkaApi hikkaApi;

    @Inject
    public HikkaRepository(HikkaApi hikkaApi) {
        this.hikkaApi = hikkaApi;
    }

    public Single<TokenResponse> getAuthToken(String clientSecret, String requestReference) {
        return hikkaApi.getAuthToken(new TokenRequest(clientSecret, requestReference));
    }

    public Single<AnitubeAnimeResponse> getAnimeFromHikka(int anitubeAnimeId) {
        return hikkaApi.getAnimeFromHikka(anitubeAnimeId);
    }

    public Single<WatchAnimeResponse> getWatchStatus(String slug) {
        return hikkaApi.getWatchStatus(slug);
    }

    public Single<WatchAnimeResponse> changeWatchStatus(String slug, AddWatchRequest addWatchRequest) {
        return hikkaApi.addOrUpdateWatchStatus(slug, addWatchRequest);
    }

    public Single<SuccesResponse> deleteWatchStatus(String slug) {
        return hikkaApi.deleteWatchStatus(slug);
    }
}
