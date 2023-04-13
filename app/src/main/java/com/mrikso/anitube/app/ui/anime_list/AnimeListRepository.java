package com.mrikso.anitube.app.ui.anime_list;

import android.util.Log;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;
import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.network.AnitubeApiService;
import com.mrikso.anitube.app.paging.AnimeReleasePagingSource;
import io.reactivex.rxjava3.core.Flowable;
import javax.inject.Inject;

public class AnimeListRepository {
    private AnitubeApiService apiService;

    @Inject
    public AnimeListRepository(AnitubeApiService apiService) {
        this.apiService = apiService;
    }

    public Flowable<PagingData<AnimeReleaseModel>> getAnimeListByPage() {
        Log.i("AnimeListRepository", "getAnimeListByPage");
        // Create new Pager
        Pager<Integer, AnimeReleaseModel> pager =
                new Pager<>(
                        // Create new paging config
                        new PagingConfig(
                                11, //  Count of items in one page
                                11, //  Number of items to prefetch
                                false, // Enable placeholders for data which is not yet loaded
                                11, // initialLoadSize - Count of items to be loaded initially
                                11 * 499), // maxSize - Count of total items to be shown in
                        // recyclerview
                        () -> new AnimeReleasePagingSource(apiService)); // et paging source

        return PagingRx.getFlowable(pager).doOnError(t -> t.printStackTrace());
    }
}
