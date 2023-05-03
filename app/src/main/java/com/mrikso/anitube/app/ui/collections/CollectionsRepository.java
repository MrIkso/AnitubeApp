package com.mrikso.anitube.app.ui.collections;

import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.mrikso.anitube.app.model.CollectionModel;
import com.mrikso.anitube.app.network.AnitubeApiService;
import com.mrikso.anitube.app.paging.CollectionsPagingSource;
import com.mrikso.anitube.app.parser.CollectionsParser;

import io.reactivex.rxjava3.core.Flowable;

import javax.inject.Inject;

public class CollectionsRepository {
    private AnitubeApiService apiService;
    private CollectionsParser mapper;

    @Inject
    public CollectionsRepository(AnitubeApiService apiService, CollectionsParser mapper) {
        this.apiService = apiService;
        this.mapper = mapper;
    }

    public Flowable<PagingData<CollectionModel>> getCollections() {
        // Create new paging config
        PagingConfig config =
                new PagingConfig(
                        11, //  Count of items in one page
                        11, //  Number of items to prefetch
                        false, // Enable placeholders for data which is not yet loaded
                        11, // initialLoadSize - Count of items to be loaded initially
                        11 * 600);
        // Create new Pager
        Pager<Integer, CollectionModel> pager =
                new Pager<>(
                        config,
                        //  null,
                        //  new AnimeReleaseRemoteMediator(apiService, database, new
                        // AnimeReleasesMapper(preferences)),
                        () -> new CollectionsPagingSource(apiService, mapper)); // et paging source

        return PagingRx.getFlowable(pager).doOnError(t -> t.printStackTrace());
    }
}
