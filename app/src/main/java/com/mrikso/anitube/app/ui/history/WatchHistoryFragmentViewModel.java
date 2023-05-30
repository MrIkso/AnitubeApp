package com.mrikso.anitube.app.ui.history;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mrikso.anitube.app.data.history.enity.HistoryEnity;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.ui.watch.WatchAnimeRepository;

import dagger.hilt.android.lifecycle.HiltViewModel;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Inject;

@HiltViewModel
public class WatchHistoryFragmentViewModel extends ViewModel {
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final MutableLiveData<LoadState> loadSate = new MutableLiveData<>(LoadState.LOADING);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private WatchAnimeRepository repository;

    @Inject
    public WatchHistoryFragmentViewModel(WatchAnimeRepository repository) {
        this.repository = repository;
        loadData();
    }

    public void loadData() {
        //	repository.getAllHistory();
    }

    public void deleteItem(int animeId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            repository.deleteHistoryEnity(animeId);
        });
    }

    public LiveData<List<HistoryEnity>> getHistory() {
        return repository.getAllHistory();
    }

    public LiveData<LoadState> getLoadState() {
        return loadSate;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }
}
