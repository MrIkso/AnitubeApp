package com.mrikso.anitube.app.repository;

import com.mrikso.anitube.app.model.ChangeStatusResponse;
import com.mrikso.anitube.app.model.CommentsResponse;
import com.mrikso.anitube.app.network.AnitubeApiService;
import com.mrikso.anitube.app.network.ApiClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Response;

public class AnitubeRepository {
    private final AnitubeApiService anitubeApi;

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

    public Single<Document> getMobilePage(String url) {
        return Single.create(emitter -> {
            OkHttpClient client = new OkHttpClient();
            Request request1 = new Request.Builder()
                    .url(url)
                    .header("User-Agent", ApiClient.MOBILE_USER_AGENT)
                    .build();

            try  {
                okhttp3.Response response = client.newCall(request1).execute();
                if (response.isSuccessful() || response.code() == 200) {
                    emitter.onSuccess(Jsoup.parse(response.body().string()));
                }
            }
            catch (Exception ex){
                emitter.onError(ex);
            }
        });
       // return anitubeApi.getMobilePage(ApiClient.MOBILE_USER_AGENT, url);
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

    public Single<String> getPlaylist(int animeId, String dleHash) {
        return anitubeApi.getPlaylist(animeId, "playlist", dleHash);
    }

    public Single<CommentsResponse> getCommentsForAnime(int page, int animeId) {
        return anitubeApi.getCommentsForAnime(page, animeId);
    }


    /*public Single<Document> checkName(String name, String dleHash) {
        return anitubeApi.checkName(name, dleHash);
    }

    public Single<Document> resetPassword(String lostName, String recaptchaResponse) {
        return anitubeApi.resetPassword(lostName, recaptchaResponse, "", "submit_lost");
    }

    public Single<Document> register(String name, String password, String email, String recaptchaResponse) {
        return anitubeApi.register(name, password, password, email, recaptchaResponse, "", "submit_reg", "register");
    }*/
}
