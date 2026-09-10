package com.mrikso.anitube.app.ui.search_result;

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
public class SearchResultViewModel extends ViewModel {
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private SearchResultRepository repository;
    private MutableLiveData<PagingData<AnimeReleaseModel>> animePagingData = new MutableLiveData<>();
    private Flowable<PagingData<AnimeReleaseModel>> animePagingDataFlowable;
    private final MutableLiveData<LoadState> loadSate = new MutableLiveData<>(LoadState.LOADING);
    private boolean singleLoad = false;

    @Inject
    public SearchResultViewModel(SearchResultRepository repository) {
        this.repository = repository;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }

    public LiveData<PagingData<AnimeReleaseModel>> getAnimePagingData() {
        return animePagingData;
    }

    public void searchByLink(String link) {
        if (!InternetConnection.isNetworkAvailable(App.getApplication())) {
            loadSate.postValue(LoadState.NO_NETWORK);
            return;
        }
        loadSate.postValue(LoadState.LOADING);
        CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(this);
        animePagingDataFlowable = PagingRx.cachedIn(repository.searchByLink(link), viewModelScope);

        compositeDisposable.add(animePagingDataFlowable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pagingData -> {
                    animePagingData.setValue(pagingData);
                }));
    }

    public LiveData<LoadState> getLoadState() {
        return loadSate;
    }
}
