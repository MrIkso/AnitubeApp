package com.mrikso.anitube.app.paging;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.network.AnitubeApiService;
import com.mrikso.anitube.app.parser.AnimeReleasesMapper;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AnimeSearchPagingSource extends RxPagingSource<Integer, AnimeReleaseModel> {
    @NonNull private final AnitubeApiService service;
    private final AnimeReleasesMapper mapper;
    private final String query;

    int maxPage = -1;

    public AnimeSearchPagingSource(
            @NonNull AnitubeApiService service, @NonNull AnimeReleasesMapper mapper, String query) {
        this.service = service;
        this.mapper = mapper;
        this.query = query;
        //  Log.d("AnimeSearchPagingSource", "construcrot called");
    }

    @NotNull
    @Override
    public Single<LoadResult<Integer, AnimeReleaseModel>> loadSingle(
            @NotNull LoadParams<Integer> loadParams) {

        // If page number is already there then init page variable with it otherwise we are loading
        // fist page
        int page = loadParams.getKey() != null ? loadParams.getKey() : 1;

        //  Log.d("AnimeSearchPagingSource", "loadSingle: loading page " + page);

        // Send request to server with page number
        return service.search(query, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(data -> Single.just(mapper.transform(data)))
                .map(
                        data -> {
                            maxPage = data.getMaxPage();
                            return data.getAnimeReleases();
                        })
                .map(animes -> toLoadResult(animes, page))
                .doOnError(
                        (t) -> {
                            t.printStackTrace();
                        })
                .onErrorReturn(LoadResult.Error::new);
    }

    // Method to map AnimeReleaseModel to LoadResult object
    private LoadResult<Integer, AnimeReleaseModel> toLoadResult(
            List<AnimeReleaseModel> data, int page) {
        //  Log.d("AnimeSearchPagingSource", "toLoadResult:data size " + data.size());

        return new LoadResult.Page<>(
                data, null, (data != null && !data.isEmpty() && page <= maxPage) ? page + 1 : null);
    }

    @Nullable
    @Override
    public Integer getRefreshKey(@NotNull PagingState<Integer, AnimeReleaseModel> state) {
        Integer anchorPosition = state.getAnchorPosition();
        if (anchorPosition == null) {
            return null;
        }

        LoadResult.Page<Integer, AnimeReleaseModel> anchorPage =
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
