package com.mrikso.anitube.app.network;

import com.mrikso.anitube.app.network.request.AddWatchRequest;
import com.mrikso.anitube.app.network.request.TokenRequest;
import com.mrikso.anitube.app.network.response.AnitubeAnimeResponse;
import com.mrikso.anitube.app.network.response.ProfileResponse;
import com.mrikso.anitube.app.network.response.SuccesResponse;
import com.mrikso.anitube.app.network.response.TokenInfoResponse;
import com.mrikso.anitube.app.network.response.TokenResponse;
import com.mrikso.anitube.app.network.response.WatchAnimeResponse;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface HikkaApi {
    @POST("/auth/token")
    Single<TokenResponse> getAuthToken(@Body TokenRequest tokenRequest);

    @GET("/auth/token/info")
    Single<TokenInfoResponse> getTokenInfo();

    @GET("/user/me")
    Single<ProfileResponse> getMeProfile();

    @GET("/integrations/anitube/anime/{anitube_id}")
    Single<AnitubeAnimeResponse> getAnimeFromHikka(@Path("anitube_id") int anitubeId);

    @GET("/watch/{slug}")
    Single<WatchAnimeResponse> getWatchStatus(@Path("slug") String slug);

    @PUT("/watch/{slug}")
    Single<WatchAnimeResponse> addOrUpdateWatchStatus(@Path("slug") String slug, @Body AddWatchRequest addWatchRequest);

    @DELETE("/watch/{slug}")
    Single<SuccesResponse> deleteWatchStatus(@Path("slug") String slug);

}
