package com.mrikso.anitube.app.paging;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.model.CollectionModel;
import com.mrikso.anitube.app.network.AnitubeApiService;
import com.mrikso.anitube.app.parser.AnimeReleasesMapper;
import com.mrikso.anitube.app.parser.CollectionsParser;
import com.mrikso.anitube.app.ui.library.LibaryRepository;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CollectionsPagingSource extends RxPagingSource<Integer, CollectionModel> {
     @NonNull private final AnitubeApiService service;
    private final CollectionsParser mapper;

    public CollectionsPagingSource(
            @NonNull AnitubeApiService service, @NonNull CollectionsParser mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @NotNull
    @Override
    public Single<LoadResult<Integer, CollectionModel>> loadSingle(
            @NotNull LoadParams<Integer> loadParams) {

        // If page number is already there then init page variable with it otherwise we are loading
        // fist page
        int page = loadParams.getKey() != null ? loadParams.getKey() : 1;

        //  Log.d("AnimelistPagingSource", "loadSingle: loading page " + page);

        // Send request to server with page number
        return service
                .getCollections(page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(data -> Single.just(mapper.transform(data)))
                .map(animes -> toLoadResult(animes.second, page, animes.first))
                .doOnError(
                        (t) -> {
                            t.printStackTrace();
                        })
                .onErrorReturn(LoadResult.Error::new);
    }

    // Method to map CollectionModel to LoadResult object
    private LoadResult<Integer, CollectionModel> toLoadResult(
            List<CollectionModel> data, int page, int maxPage) {
        
        return new LoadResult.Page<>(
                data,
                null,
                (data != null && !data.isEmpty() && !(page <= maxPage)) ? page + 1 : null);
    }

    @Nullable
    @Override
    public Integer getRefreshKey(@NotNull PagingState<Integer, CollectionModel> state) {
        Integer anchorPosition = state.getAnchorPosition();
        if (anchorPosition == null) {
            return null;
        }

        LoadResult.Page<Integer, CollectionModel> anchorPage =
                state.closestPageToPosition(anchorPosition);
        if (anchorPage == null) {
            return null;
        }

        Integer prevKey = anchorPage.getPrevKey();
        if (prevKey != null) {
            return prevKey + 1;
        }

        Integer nextKey = anchorPage.getNextKey();
        if (nextKey != null) {
            return nextKey - 1;
        }

        return null;
    }
}

