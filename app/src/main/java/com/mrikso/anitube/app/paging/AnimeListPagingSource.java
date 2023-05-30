package com.mrikso.anitube.app.paging;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.parser.AnimeReleasesMapper;
import com.mrikso.anitube.app.ui.library.LibaryRepository;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AnimeListPagingSource extends RxPagingSource<Integer, AnimeReleaseModel> {
    @NonNull
    private final LibaryRepository repository;

    private final AnimeReleasesMapper mapper;
    private final int type;

    public AnimeListPagingSource(@NonNull LibaryRepository repository, @NonNull AnimeReleasesMapper mapper, int type) {
        this.repository = repository;
        this.mapper = mapper;
        this.type = type;
        //  Log.d("AnimelistPagingSource", "construcrot called");
    }

    @NotNull
    @Override
    public Single<LoadResult<Integer, AnimeReleaseModel>> loadSingle(@NotNull LoadParams<Integer> loadParams) {

        // If page number is already there then init page variable with it otherwise we are loading
        // fist page
        int page = loadParams.getKey() != null ? loadParams.getKey() : 1;

        //  Log.d("AnimelistPagingSource", "loadSingle: loading page " + page);

        // Send request to server with page number
        return repository
                .getPaggingData(page, type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(data -> Single.just(mapper.transform(data)))
                .map(data -> {
                    int maxPage = data.getMaxPage();
                    return new Pair<>(maxPage, data.getAnimeReleases());
                })
                .map(animes -> toLoadResult(animes.second, page, animes.first))
                .doOnError((t) -> {
                    t.printStackTrace();
                })
                .onErrorReturn(LoadResult.Error::new);
    }

    // Method to map AnimeReleaseModel to LoadResult object
    private LoadResult<Integer, AnimeReleaseModel> toLoadResult(List<AnimeReleaseModel> data, int page, int maxPage) {
        Log.d("AnimelistPagingSource", "page: " + page + "maxPage: " + maxPage);
        Integer nextKey = data != null && !data.isEmpty() && (page < maxPage) && (page != maxPage) ? page + 1 : null;
        return new LoadResult.Page<>(data, null, nextKey);
    }

    @Nullable
    @Override
    public Integer getRefreshKey(@NotNull PagingState<Integer, AnimeReleaseModel> state) {
        Integer anchorPosition = state.getAnchorPosition();
        if (anchorPosition == null) {
            return null;
        }

        LoadResult.Page<Integer, AnimeReleaseModel> anchorPage = state.closestPageToPosition(anchorPosition);
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
