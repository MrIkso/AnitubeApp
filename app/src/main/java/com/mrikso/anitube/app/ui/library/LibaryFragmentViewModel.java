package com.mrikso.anitube.app.ui.library;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.mrikso.anitube.app.model.AnimeReleaseModel;

import dagger.hilt.android.lifecycle.HiltViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import javax.inject.Inject;

import kotlinx.coroutines.CoroutineScope;

@HiltViewModel
public class LibaryFragmentViewModel extends ViewModel {
    private final String TAG = "SearchFragmentViewModel";
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Flowable<PagingData<AnimeReleaseModel>> animePagingDataFlowable;
    private boolean singleLoad;

    private MutableLiveData<PagingData<AnimeReleaseModel>> animePagingData =
            new MutableLiveData<>();

    private final LibaryRepository repository;

    @Inject
    public LibaryFragmentViewModel(LibaryRepository repository) {
        this.repository = repository;
    }

    public void loadData(int mode) {
        if (!singleLoad) {
            CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(this);
            animePagingDataFlowable =
                    PagingRx.cachedIn(repository.getAnimeListByPage(mode), viewModelScope);

            compositeDisposable.add(
                    animePagingDataFlowable
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(animePagingData::setValue));
            singleLoad = true;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }

    public LiveData<PagingData<AnimeReleaseModel>> getAnimePagingData() {
        return animePagingData;
    }
}
