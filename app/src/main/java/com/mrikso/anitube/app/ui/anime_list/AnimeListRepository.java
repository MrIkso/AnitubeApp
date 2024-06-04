package com.mrikso.anitube.app.ui.anime_list;

import androidx.core.util.Pair;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.model.UserModel;
import com.mrikso.anitube.app.network.AnitubeApiService;
import com.mrikso.anitube.app.paging.AnimeReleasePagingSource;
import com.mrikso.anitube.app.parser.AnimeReleasesMapper;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.subjects.Subject;

import javax.inject.Inject;

public class AnimeListRepository {
    private AnitubeApiService apiService;
    private AnimeReleasesMapper mapper;

    @Inject
    public AnimeListRepository(AnitubeApiService apiService, AnimeReleasesMapper mapper) {
        this.apiService = apiService;
        this.mapper = mapper;
    }

    public UserModel getUserData() {
        return mapper.getUserData();
    }

    public Flowable<PagingData<AnimeReleaseModel>> getAnimeListByPage() {
        // Create new paging config
        PagingConfig config = new PagingConfig(
                11, //  Count of items in one page
                11, //  Number of items to prefetch
                false, // Enable placeholders for data which is not yet loaded
                11, // initialLoadSize - Count of items to be loaded initially
                11 * 600);
        // Create new Pager
        Pager<Integer, AnimeReleaseModel> pager = new Pager<>(
                config,
                //  null,
                //  new AnimeReleaseRemoteMediator(apiService, database, new
                // AnimeReleasesMapper(preferences)),
                () -> new AnimeReleasePagingSource(apiService, mapper)); // et paging source

        return PagingRx.getFlowable(pager).doOnError(t -> t.printStackTrace());
    }
}
