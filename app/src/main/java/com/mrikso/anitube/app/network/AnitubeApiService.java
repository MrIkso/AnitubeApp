package com.mrikso.anitube.app.network;

import com.mrikso.anitube.app.model.ChangeStatusResponse;
import com.mrikso.anitube.app.model.CommentsResponse;

import io.reactivex.rxjava3.core.Single;

import org.jsoup.nodes.Document;

import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface AnitubeApiService {

    @GET("/")
    Single<Document> getHomePage();

    @GET("/anime/page/{page}")
    Single<Document> getAnimeByPage(@Path("page") String page);

    @GET
    Single<Document> getPage(@Url String url);

    @POST("?do=search&subaction=search")
    Single<Document> search(@Query("story") String query, @Query("from_page") int page);

    @POST("/engine/lazydev/dle_search/ajax.php")
    @Headers("X-Requested-With: XMLHttpRequest")
    @FormUrlEncoded
    Single<String> quickSearch(@Field("story") String story, @Field("dle_hash") String dleHash);
    
    // https://anitube.in.ua/engine/ajax/playlists.php?news_id=4597&xfield=playlist&user_hash=f6dbfddfd72df5d81c9731a18abdf53742d37bba
    @GET("/engine/ajax/playlists.php")
    @Headers("X-Requested-With: XMLHttpRequest")
    Single<String> getPlaylist(@Query("news_id") int newsId, @Query("xfield") String xField, @Query("user_hash") String userHash);

    @POST("/")
    @FormUrlEncoded
    Single<Response<Document>> login(
            @Field("login") String submit,
            @Field("login_name") String loginName,
            @Field("login_password") String loginPassword);

    @POST("/mylists")
    @Headers("X-Requested-With: XMLHttpRequest")
    @FormUrlEncoded
    Single<ChangeStatusResponse> changeAnimeStatus(@Field("news_id") int newsId, @Field("status_id") int statusId);

    @GET("/mylists/page/{page}")
    Single<Document> getAllMyLists(@Path("page") int page);

    @GET("/mylists/seen/page/{page}")
    Single<Document> getSeenList(@Path("page") int page);

    @GET("/mylists/will/page/{page}")
    Single<Document> getWllList(@Path("page") int page);

    @GET("/mylists/watch/page/{page}")
    Single<Document> getWatchList(@Path("page") int page);

    @GET("/mylists/poned/page/{page}")
    Single<Document> getPonedList(@Path("page") int page);

    @GET("/mylists/aband/page/{page}")
    Single<Document> getAbandList(@Path("page") int page);

    @GET("/favorites/page/{page}")
    Single<Document> getFavorites(@Path("page") int page);

    @GET("/engine/ajax/controller.php?mod=favorites")
    Single<Document> addOrRemoveFromFavorites(
            @Query("fav_id") int favId, @Query("action") String action, @Query("user_hash") String userHash);

    @GET("/collections/page/{page}")
    Single<Document> getCollections(@Path("page") int page);

    @GET("/?do=random_anime")
    Single<Document> getRandomAnime();

    @GET("/engine/ajax/controller.php?mod=comments&skin=smartphone&massact=disable")
    Single<CommentsResponse> getCommentsForAnime(@Query("cstart") int cstart, @Query("news_id") int animeId);
}
