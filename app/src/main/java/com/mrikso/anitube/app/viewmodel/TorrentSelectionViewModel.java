package com.mrikso.anitube.app.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.TorrentModel;
import com.mrikso.anitube.app.parser.TorrentsPageParser;
import com.mrikso.anitube.app.repository.AnitubeRepository;

import org.jsoup.nodes.Document;

import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class TorrentSelectionViewModel extends ViewModel {
    private final String TAG = "TorrentSelectionViewModel";
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final AnitubeRepository repository;
    private final MutableLiveData<List<TorrentModel>> torrentList = new MutableLiveData<>();
    public final MutableLiveData<LoadState> loadSate = new MutableLiveData<>(LoadState.LOADING);

    @Inject
    public TorrentSelectionViewModel(AnitubeRepository repository) {
        this.repository = repository;
    }

    public void loadTorrents(String url) {
        compositeDisposable.add(repository
                .getPage(url)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(v -> {
                    loadSate.setValue(LoadState.LOADING);
                    Log.d(TAG, "start loading");
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            Executors.newSingleThreadExecutor().execute(() -> {
                                parsePage(response);
                            });
                        },
                        throwable -> {
                            throwable.printStackTrace();
                            loadSate.postValue(LoadState.ERROR);
                        }));
    }

    private void parsePage(Document response) {
        TorrentsPageParser parser = new TorrentsPageParser();
        compositeDisposable.add(
                parser.parsePage(response).subscribeOn(Schedulers.io()).subscribe(v -> {
                    torrentList.postValue(v);
                    loadSate.postValue(LoadState.DONE);
                }));
    }

    public LiveData<List<TorrentModel>> getTorrentList() {
        return torrentList;
    }

    public LiveData<LoadState> getLoadSate() {
        return loadSate;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }
}
