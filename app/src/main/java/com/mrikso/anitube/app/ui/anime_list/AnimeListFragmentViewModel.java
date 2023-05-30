package com.mrikso.anitube.app.ui.anime_list;

import androidx.core.util.Pair;
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
public class AnimeListFragmentViewModel extends ViewModel {
    private final String TAG = "AnimeListFragmentViewModel";
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private AnimeListRepository repository;
    private MutableLiveData<PagingData<AnimeReleaseModel>> animePagingData = new MutableLiveData<>();
    private Flowable<PagingData<AnimeReleaseModel>> animePagingDataFlowable;
    private MutableLiveData<Pair<String, String>> userData = new MutableLiveData<>(null);

    @Inject
    public AnimeListFragmentViewModel(AnimeListRepository repository) {
        this.repository = repository;
        init();
    }

    private void init() {
        CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(this);
        animePagingDataFlowable = PagingRx.cachedIn(repository.getAnimeListByPage(), viewModelScope);

        compositeDisposable.add(animePagingDataFlowable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(animePagingData::setValue));
        compositeDisposable.add(repository
                .getUserData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(results -> {
                    if (results != null && results.first != null && results.second != null) {
                        userData.postValue(results);
                    }
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

    public LiveData<Pair<String, String>> getUserData() {
        return userData;
    }
}
