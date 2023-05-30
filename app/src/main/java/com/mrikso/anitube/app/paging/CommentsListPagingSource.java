package com.mrikso.anitube.app.paging;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import com.mrikso.anitube.app.model.CommentModel;
import com.mrikso.anitube.app.network.AnitubeApiService;
import com.mrikso.anitube.app.parser.CommentsParser;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommentsListPagingSource extends RxPagingSource<Integer, CommentModel> {
    @NonNull
    private final AnitubeApiService service;

    private final CommentsParser mapper;
    private final int animeId;

    public CommentsListPagingSource(int animeId, @NonNull AnitubeApiService service, @NonNull CommentsParser mapper) {
        this.animeId = animeId;
        this.service = service;
        this.mapper = mapper;
    }

    @NotNull
    @Override
    public Single<LoadResult<Integer, CommentModel>> loadSingle(@NotNull LoadParams<Integer> loadParams) {

        // If page number is already there then init page variable with it otherwise we are loading
        // fist page
        int page = loadParams.getKey() != null ? loadParams.getKey() : 1;

        // Log.d("CommentsListPagingSource", "loadSingle: loading page " + page);

        // Send request to server with page number
        return service.getCommentsForAnime(page, animeId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(data -> Single.just(mapper.transform(data.getComments())))
                .map(comments -> toLoadResult(comments, page))
                .doOnError((t) -> {
                    t.printStackTrace();
                })
                .onErrorReturn(LoadResult.Error::new);
    }

    // Method to map CommentModel to LoadResult object
    private LoadResult<Integer, CommentModel> toLoadResult(List<CommentModel> data, int page) {
        // Log.i("CommentsListPagingSource", "maxPage" + maxPage);

        return new LoadResult.Page<>(data, null, (data != null && !data.isEmpty()) ? page + 1 : null);
    }

    @Nullable
    @Override
    public Integer getRefreshKey(@NotNull PagingState<Integer, CommentModel> state) {
        Integer anchorPosition = state.getAnchorPosition();
        if (anchorPosition == null) {
            return null;
        }

        LoadResult.Page<Integer, CommentModel> anchorPage = state.closestPageToPosition(anchorPosition);
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
