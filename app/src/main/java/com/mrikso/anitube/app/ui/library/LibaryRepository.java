package com.mrikso.anitube.app.ui.library;

import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.model.UserModel;
import com.mrikso.anitube.app.network.AnitubeApiService;
import com.mrikso.anitube.app.paging.AnimeListPagingSource;
import com.mrikso.anitube.app.parser.AnimeReleasesMapper;
import com.mrikso.anitube.app.utils.PreferencesHelper;

import org.jsoup.nodes.Document;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class LibaryRepository {

    private AnitubeApiService apiService;
    private AnimeReleasesMapper mapper;

    @Inject
    public LibaryRepository(AnitubeApiService apiService, AnimeReleasesMapper mapper) {
        this.apiService = apiService;
        this.mapper = mapper;
    }

    public Single<Document> getPaggingData(int page, int listType) {
        String userName = null;
        UserModel user = mapper.getUserData();
        if (user != null) {
            userName = user.getUserName();
        } else if (PreferencesHelper.getInstance().isLogin()) {
            userName = PreferencesHelper.getInstance().getUserLogin();
        }

        if (userName == null) {
            return Single.error(new NullPointerException("Username is null and not loading favorites"));
        }

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
            default:
                return Single.error(new IllegalArgumentException("Unknown listType: " + listType));
        }
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
