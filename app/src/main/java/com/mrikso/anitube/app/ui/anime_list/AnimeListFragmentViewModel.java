package com.mrikso.anitube.app.ui.anime_list;

import android.util.Log;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.mrikso.anitube.app.model.AnimeReleaseModel;

import dagger.hilt.android.lifecycle.HiltViewModel;

import io.reactivex.rxjava3.core.Flowable;

import kotlinx.coroutines.CoroutineScope;

import javax.inject.Inject;

@HiltViewModel
public class AnimeListFragmentViewModel extends ViewModel {
    private final String TAG = "AnimeListFragmentViewModel";

    private AnimeListRepository repository;
    public Flowable<PagingData<AnimeReleaseModel>> animePagingDataFlowable;

    @Inject
    public AnimeListFragmentViewModel(AnimeListRepository repository) {
        this.repository = repository;
		if(animePagingDataFlowable==null){
        init();
		}
    }

    private void init() {
        Log.d(TAG, "init called");
        CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(this);
        animePagingDataFlowable = repository.getAnimeListByPage();
        PagingRx.cachedIn(animePagingDataFlowable, viewModelScope);
    }
}
