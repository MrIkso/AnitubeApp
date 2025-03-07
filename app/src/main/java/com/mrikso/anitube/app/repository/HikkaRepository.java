package com.mrikso.anitube.app.repository;

import com.google.gson.Gson;
import com.mrikso.anitube.app.model.UserProfileModel;
import com.mrikso.anitube.app.network.HikkaApi;
import com.mrikso.anitube.app.network.request.AddWatchRequest;
import com.mrikso.anitube.app.network.request.TokenRequest;
import com.mrikso.anitube.app.network.response.AnitubeAnimeResponse;
import com.mrikso.anitube.app.network.response.ErrorResponse;
import com.mrikso.anitube.app.network.response.ProfileResponse;
import com.mrikso.anitube.app.network.response.SuccesResponse;
import com.mrikso.anitube.app.network.response.TokenInfoResponse;
import com.mrikso.anitube.app.network.response.TokenResponse;
import com.mrikso.anitube.app.network.response.WatchAnimeResponse;

import java.io.IOException;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleTransformer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.HttpException;

public class HikkaRepository {

    private final HikkaApi hikkaApi;

    @Inject
    public HikkaRepository(HikkaApi hikkaApi) {
        this.hikkaApi = hikkaApi;
    }

    public Single<TokenResponse> getAuthToken(String clientSecret, String requestReference) {
        return hikkaApi.getAuthToken(new TokenRequest(clientSecret, requestReference))
                .compose(handleErrors())
                .subscribeOn(Schedulers.io());
    }

    public Single<ProfileResponse> getMeProfile() {
        return hikkaApi.getMeProfile()
                .compose(handleErrors())
                .subscribeOn(Schedulers.io());
    }

    public Single<TokenInfoResponse> getTokenInfo() {
        return hikkaApi.getTokenInfo()
                .compose(handleErrors())
                .subscribeOn(Schedulers.io());
    }

    public Single<TokenInfoResponse> updateToken() {
        return getMeProfile()
                .flatMap(profileResponse -> getTokenInfo())
                .subscribeOn(Schedulers.io());
    }

    public Single<AnitubeAnimeResponse> getAnimeFromHikka(int anitubeAnimeId) {
        return hikkaApi.getAnimeFromHikka(anitubeAnimeId).compose(handleErrors())
                .subscribeOn(Schedulers.io());
    }

    public Single<WatchAnimeResponse> getAnimeAndWatchStatus(int anitubeAnimeId) {
        return getAnimeFromHikka(anitubeAnimeId)
                .flatMap(animeResponse -> getWatchStatus(animeResponse.getSlug()))
                .subscribeOn(Schedulers.io());
    }

    public Single<WatchAnimeResponse> getWatchStatus(String slug) {
        return hikkaApi.getWatchStatus(slug).compose(handleErrors())
                .subscribeOn(Schedulers.io());
    }

    public Single<WatchAnimeResponse> changeWatchStatus(String slug, AddWatchRequest addWatchRequest) {
        return hikkaApi.addOrUpdateWatchStatus(slug, addWatchRequest).compose(handleErrors())
                .subscribeOn(Schedulers.io());
    }

    public Single<SuccesResponse> deleteWatchStatus(String slug) {
        return hikkaApi.deleteWatchStatus(slug).compose(handleErrors())
                .subscribeOn(Schedulers.io());
    }

    private <T> SingleTransformer<T, T> handleErrors() {
        return upstream -> upstream.onErrorResumeNext(throwable -> {
            if (throwable instanceof HttpException) {
                HttpException httpException = (HttpException) throwable;
                int code = httpException.code();
                if (code >= 400 && code < 600) {
                    try {
                        String errorBody = httpException.response().errorBody().string();
                        Gson gson = new Gson();
                        ErrorResponse errorResponse = gson.fromJson(errorBody, ErrorResponse.class);
                        return Single.error(new CustomException(errorResponse));
                    } catch (IOException e) {
                        return Single.error(e);
                    }
                }
            }
            return Single.error(throwable);
        });
    }

    public static class CustomException extends Throwable {
        private final ErrorResponse errorResponse;

        public CustomException(ErrorResponse errorResponse) {
            this.errorResponse = errorResponse;
        }

        public ErrorResponse getErrorResponse() {
            return errorResponse;
        }

        @Override
        public String getMessage() {
            return errorResponse.toString();
        }
    }
}
