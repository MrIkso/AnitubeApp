package com.mrikso.anitube.app.network;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

import org.jsoup.nodes.Document;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface AnitubeApiService {

    @GET("/")
    Single<Document> getHome();

    @GET("/anime/page/{page}")
    Single<Document> getAnimeByPage(@Path("page") String page);

    @GET
    Single<Document> getAnimePage(@Url String url);

    @GET("/engine/ajax/playlists.php")
    Single<String> getPlaylist(
            @Query("news_id") int newsId, @Query("xfield") String xfield, @Query("time") long time);
}
