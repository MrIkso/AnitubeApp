package com.mrikso.anitube.app.ui.search;

import androidx.lifecycle.LiveData;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.mrikso.anitube.app.data.search.RecentSearch;
import com.mrikso.anitube.app.data.search.SearchDao;
import com.mrikso.anitube.app.data.search.SearchDatabase;
import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.model.SimpleModel;
import com.mrikso.anitube.app.network.AnitubeApiService;
import com.mrikso.anitube.app.paging.AnimeSearchPagingSource;
import com.mrikso.anitube.app.parser.AnimeReleasesMapper;
import com.mrikso.anitube.app.parser.QuickSearchResultParser;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Inject;

public class SearchRepository {

    private AnitubeApiService apiService;
    private SearchDatabase searchDatabase;
    private SearchDao searchDao;
    private AnimeReleasesMapper mapper;

    @Inject
    public SearchRepository(
            AnitubeApiService apiService,
            SearchDatabase searchDatabase,
            AnimeReleasesMapper mapper) {
        this.apiService = apiService;
        this.searchDatabase = searchDatabase;
        this.mapper = mapper;
        this.searchDao = searchDatabase.searchDao();
    }

    public LiveData<List<RecentSearch>> getRecentSearch() {
        return searchDao.getAllRecentSearch();
    }

    public void addRecentSearch(String query) {
        Executors.newSingleThreadExecutor()
                .execute(
                        () -> {
                            if (searchDao.getSearchesByName(query) == null) {
                                searchDao.insertSearch(new RecentSearch(query));
                            }
                        });
    }

    public void removeRecentSearch(String query) {
        Executors.newSingleThreadExecutor()
                .execute(
                        () -> {
                            searchDao.deleteSearchesByName(query);
                        });
    }

    public void clearAllRecentSearch() {
        Executors.newSingleThreadExecutor()
                .execute(
                        () -> {
                            searchDao.clearSearches();
                        });
    }

    public Single<List<SimpleModel>> runQickSearch(String query, String dle_login_hash) {
        return apiService
                .quickSearch(query, dle_login_hash)
                .observeOn(Schedulers.io())
                .map(data -> new QuickSearchResultParser().getQuickSearchResults(data));
    }

    public Flowable<PagingData<AnimeReleaseModel>> getSearchResult(String query) {

        // Create new paging config
        PagingConfig config =
                new PagingConfig(
                        11, //  Count of items in one page
                        11, //  Number of items to prefetch
                        false, // Enable placeholders for data which is not yet loaded
                        11, // initialLoadSize - Count of items to be loaded initially
                        11 * 600);
        // Create new Pager
        Pager<Integer, AnimeReleaseModel> pager =
                new Pager<>(
                        config,
                        /*  null,
                        new AnimeSearchRemoteMediator(apiService, animeDatabase,query, new AnimeReleasesMapper(preferences)),*/
                        () ->
                                new AnimeSearchPagingSource(
                                        apiService, mapper, query)); // et paging source

        return PagingRx.getFlowable(pager).doOnError(t -> t.printStackTrace());
    }
}
