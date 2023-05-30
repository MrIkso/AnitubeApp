package com.mrikso.anitube.app.repository;

import com.mrikso.anitube.app.model.ChangeStatusResponse;
import com.mrikso.anitube.app.model.CommentsResponse;
import com.mrikso.anitube.app.network.AnitubeApiService;

import io.reactivex.rxjava3.core.Single;

import org.jsoup.nodes.Document;

import retrofit2.Response;

import java.util.Date;

import javax.inject.Inject;

public class AnitubeRepository {
    private AnitubeApiService anitubeApi;

    @Inject
    public AnitubeRepository(AnitubeApiService anitubeApi) {
        this.anitubeApi = anitubeApi;
    }

    public Single<Document> getHome() {
        return anitubeApi.getHomePage();
    }

    public Single<Document> getPage(String url) {
        return anitubeApi.getPage(url);
    }

    public Single<Document> getAnimeByPage(int page) {
        return anitubeApi.getAnimeByPage(String.valueOf(page));
    }

    public Single<Response<Document>> login(String username, String password) {
        return anitubeApi.login("submit", username, password);
    }

    public Single<Document> getRamdomAnime() {
        return anitubeApi.getRandomAnime();
    }

    public Single<Document> addOrRemoveFromFavorites(int animeId, boolean isAdd, String dleHash) {
        return anitubeApi.addOrRemoveFromFavorites(animeId, isAdd ? "plus" : "minus", dleHash);
    }

    public Single<ChangeStatusResponse> changeAnimeStatus(int animeId, int viewStatus) {
        return anitubeApi.changeAnimeStatus(animeId, viewStatus);
    }

    public Single<String> getPlaylist(int animeId) {
        long currentTimeMillis = new Date().getTime();
        return anitubeApi.getPlaylist(animeId, "playlist", currentTimeMillis);
    }

    public Single<CommentsResponse> getCommentsForAnime(int page, int animeId) {
        return anitubeApi.getCommentsForAnime(page, animeId);
    }
}
