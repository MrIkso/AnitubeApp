package com.mrikso.anitube.app.viewmodel;

import android.util.Log;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.repository.AnitubeRepository;

import dagger.hilt.android.lifecycle.HiltViewModel;

import io.reactivex.rxjava3.core.Flowable;

import javax.inject.Inject;

import kotlinx.coroutines.CoroutineScope;

@HiltViewModel
public class SharedViewModel extends ViewModel {
    private final String TAG = "SharedViewModel";

    // private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private AnitubeRepository repository;
    // private final MutableLiveData<LoadState> loadSate = new MutableLiveData<>(LoadState.LOADING);
    public Flowable<PagingData<AnimeReleaseModel>> animePagingDataFlowable;

    @Inject
    public SharedViewModel(AnitubeRepository repository) {
        this.repository = repository;
        // init();
    }

    public void getMyData() {
        // if (animePagingDataFlowable == null) {
     //   CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(this);
     //   animePagingDataFlowable = repository.getAnimeListByPage();
      //  PagingRx.cachedIn(animePagingDataFlowable, viewModelScope);
        // }
        ///    return animePagingDataFlowable;
    }
    // Init ViewModel Data
    public void loadData() {
        Log.d(TAG, "loadData called");

        //        CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(this);
        //        Pager<Integer, AnimeReleaseModel> pager =
        //            new Pager<>(
        //                        new PagingConfig(
        //                                11, //  Count of items in one page
        //                                11, //  Number of items to prefetch
        //                                false, // Enable placeholders for data which is not yet
        // loaded
        //                                11, // initialLoadSize - Count of items to be loaded
        // initially
        //                                11 * 499), // maxSize - Count of total items to be shown
        // in
        //                        // recyclerview
        //                        () -> new AnimeReleasePagingSource(repository));
        //
        //        animePagingDataFlowable = pager.getFlow();
        //        PagingRx.cachedIn(animePagingDataFlowable, viewModelScope);
        //
        //        // Define Paging Source
        //        animePagingDataFlowable.doOnError(
        //                t -> {
        //                    t.printStackTrace();
        //                });

    }
}
