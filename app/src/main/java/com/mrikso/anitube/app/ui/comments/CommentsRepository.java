package com.mrikso.anitube.app.ui.comments;

import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.mrikso.anitube.app.model.CommentModel;
import com.mrikso.anitube.app.network.AnitubeApiService;
import com.mrikso.anitube.app.paging.CommentsListPagingSource;
import com.mrikso.anitube.app.parser.CommentsParser;

import io.reactivex.rxjava3.core.Flowable;

import javax.inject.Inject;

public class CommentsRepository {

    private AnitubeApiService apiService;
    private CommentsParser mapper;

    @Inject
    public CommentsRepository(AnitubeApiService apiService, CommentsParser mapper) {
        this.apiService = apiService;
        this.mapper = mapper;
    }

    public Flowable<PagingData<CommentModel>> loadComments(int animeId) {
        // Create new paging config
        PagingConfig config = new PagingConfig(
                11, //  Count of items in one page
                11, //  Number of items to prefetch
                false, // Enable placeholders for data which is not yet loaded
                11, // initialLoadSize - Count of items to be loaded initially
                11 * 600);
        // Create new Pager
        Pager<Integer, CommentModel> pager = new Pager<>(
                config, () -> new CommentsListPagingSource(animeId, apiService, mapper)); // et paging source

        return PagingRx.getFlowable(pager).doOnError(t -> t.printStackTrace());
    }
}
