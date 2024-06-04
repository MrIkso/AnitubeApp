package com.mrikso.anitube.app.ui.library;

import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.network.AnitubeApiService;
import com.mrikso.anitube.app.paging.AnimeListPagingSource;
import com.mrikso.anitube.app.parser.AnimeReleasesMapper;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

import org.jsoup.nodes.Document;


import java.util.function.Function;

import javax.inject.Inject;

public class LibaryRepository {

    private AnitubeApiService apiService;
    private AnimeReleasesMapper mapper;

    @Inject
    public LibaryRepository(AnitubeApiService apiService, AnimeReleasesMapper mapper) {
        this.apiService = apiService;
        this.mapper = mapper;
    }

    public Single<Document> getPaggingData(int page, int listType) {
        String userName = mapper.getUserData().getUserName();
        switch (listType) {
            case AnimeListType.LIST_ALL:
                return apiService.getAllMyLists(userName, page);
            case AnimeListType.LIST_ADAND:
                return apiService.getAbandList(userName, page);
            case AnimeListType.LIST_FAVORITES:
                return apiService.getFavorites(page);
            case AnimeListType.LIST_PONED:
                return apiService.getPonedList(userName, page);
            case AnimeListType.LIST_SEEN:
                return apiService.getSeenList(userName, page);
            case AnimeListType.LIST_WATCH:
                return apiService.getWatchList(userName, page);
            case AnimeListType.LIST_WILL:
                return apiService.getWllList(userName, page);
        }
        return null;
    }


    public Flowable<PagingData<AnimeReleaseModel>> getAnimeListByPage(int listType) {
        // Create new paging config
        PagingConfig config = new PagingConfig(
                11, //  Count of items in one page
                11, //  Number of items to prefetch
                false, // Enable placeholders for data which is not yet loaded
                11, // initialLoadSize - Count of items to be loaded initially
                11 * 600);
        // Create new Pager
        Pager<Integer, AnimeReleaseModel> pager =
                new Pager<>(config, () -> new AnimeListPagingSource(this, mapper, listType)); // et paging source

        return PagingRx.getFlowable(pager).doOnError(t -> t.printStackTrace());
    }
}
