package com.mrikso.anitube.app.ui.collections;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.model.CollectionModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import javax.inject.Inject;
import kotlinx.coroutines.CoroutineScope;

public class CollectionsFragmentViewModel extends ViewModel {
    private final String TAG = "AnimeListFragmentViewModel";
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private CollectionsRepository repository;
    private MutableLiveData<PagingData<CollectionModel>> animePagingData = new MutableLiveData<>();
    private Flowable<PagingData<CollectionModel>> animePagingDataFlowable;

    @Inject
    public CollectionsFragmentViewModel(CollectionsRepository repository) {
        this.repository = repository;
        init();
    }

    private void init() {
        CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(this);
        animePagingDataFlowable = PagingRx.cachedIn(repository.getCollections(), viewModelScope);

        compositeDisposable.add(
                animePagingDataFlowable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(animePagingData::setValue));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }

    public LiveData<PagingData<CollectionModel>> getAnimePagingData() {
        return animePagingData;
    }
}
