package com.mrikso.anitube.app.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;
import androidx.paging.rxjava3.PagingRx;

import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.network.AnitubeApiService;
import com.mrikso.anitube.app.paging.AnimeReleasePagingSource;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

import org.jsoup.nodes.Document;

import java.util.Date;

import javax.inject.Inject;

public class AnitubeRepository {
    private AnitubeApiService anitubeApi;

    @Inject
    public AnitubeRepository(AnitubeApiService anitubeApi) {
        this.anitubeApi = anitubeApi;
    }

    public Single<Document> getHome() {
        return anitubeApi.getHome();
    }

    public Single<Document> getAnimePage(String url) {
        return anitubeApi.getAnimePage(url);
    }

    public Single<Document> getAnimeByPage(int page) {
        return anitubeApi.getAnimeByPage(String.valueOf(page));
    }

    public Single<String> getPlaylist(int id) {
        long currentTimeMillis = new Date().getTime();
        return anitubeApi.getPlaylist(id, "playlist", currentTimeMillis);
    }
}
