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
import io.reactivex.rxjava3.core.Single;

import org.jsoup.nodes.Document;

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
        switch (listType) {
            case AnimeListType.LIST_ALL:
                return apiService.getAllMyLists(page);
            case AnimeListType.LIST_ADAND:
                return apiService.getAbandList(page);
            case AnimeListType.LIST_FAVORITES:
                return apiService.getFavorites(page);
            case AnimeListType.LIST_PONED:
                return apiService.getPonedList(page);
            case AnimeListType.LIST_SEEN:
                return apiService.getSeenList(page);
            case AnimeListType.LIST_WATCH:
                return apiService.getWatchList(page);
            case AnimeListType.LIST_WILL:
                return apiService.getWllList(page);
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
