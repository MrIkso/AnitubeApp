package com.mrikso.anitube.app.ui.library;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.mrikso.anitube.app.App;
import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.utils.InternetConnection;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlinx.coroutines.CoroutineScope;

@HiltViewModel
public class LibaryFragmentViewModel extends ViewModel {
    private final String TAG = "SearchFragmentViewModel";
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Flowable<PagingData<AnimeReleaseModel>> animePagingDataFlowable;
    private boolean singleLoad;

    private MutableLiveData<PagingData<AnimeReleaseModel>> animePagingData = new MutableLiveData<>();
    private final MutableLiveData<LoadState> loadSate = new MutableLiveData<>(LoadState.LOADING);

    private final LibaryRepository repository;

    @Inject
    public LibaryFragmentViewModel(LibaryRepository repository) {
        this.repository = repository;
    }

    public void loadData(int mode) {
        if (!InternetConnection.isNetworkAvailable(App.getApplication())) {
            loadSate.postValue(LoadState.NO_NETWORK);
            return;
        }
        loadSate.postValue(LoadState.LOADING);
        CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(this);
        animePagingDataFlowable = PagingRx.cachedIn(repository.getAnimeListByPage(mode), viewModelScope);

        compositeDisposable.add(animePagingDataFlowable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pagingData -> {
                    animePagingData.setValue(pagingData);
                }));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }

    public LiveData<PagingData<AnimeReleaseModel>> getAnimePagingData() {
        return animePagingData;
    }

    public LiveData<LoadState> getLoadState() {
        return loadSate;
    }
}
