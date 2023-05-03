package com.mrikso.anitube.app.ui.home;

import android.util.Log;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.model.BaseAnimeModel;
import com.mrikso.anitube.app.model.InteresingModel;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.parser.HomePageParser;
import com.mrikso.anitube.app.repository.AnitubeRepository;

import dagger.hilt.android.lifecycle.HiltViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

import org.jsoup.nodes.Document;

import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Inject;

@HiltViewModel
public class HomeFragmentViewModel extends ViewModel {
    private final String TAG = "HomeFragmentViewModel";

    private AnitubeRepository repository;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private MutableLiveData<LoadState> loadSate = new MutableLiveData<>(LoadState.LOADING);
    private MutableLiveData<List<InteresingModel>> _interesingAnime = new MutableLiveData<>(null);
    private MutableLiveData<List<BaseAnimeModel>> _bestAnime = new MutableLiveData<>(null);
    private MutableLiveData<List<AnimeReleaseModel>> _newAnime = new MutableLiveData<>(null);
    private MutableLiveData<Pair<String, String>> _userData = new MutableLiveData<>(null);
    private MutableLiveData<String> dleHash = new MutableLiveData<>();

    private boolean singleLoad = false;

    @Inject
    public HomeFragmentViewModel(AnitubeRepository repository) {
        this.repository = repository;
    }

    public void loadHome() {
        if (!singleLoad) {
            loadData();
            singleLoad = true;
        }
    }

    public void reloadHome() {
        loadData();
    }

    private void loadData() {

        Disposable disposable =
                repository
                        .getHome()
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe(
                                new Consumer<Disposable>() {
                                    @Override
                                    public void accept(Disposable disposable) throws Throwable {
                                        loadSate.setValue(LoadState.LOADING);
                                        Log.d(TAG, "start loading");
                                    }
                                })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                new Consumer<Document>() {
                                    @Override
                                    public void accept(Document response) throws Throwable {
                                        Executors.newSingleThreadExecutor()
                                                .execute(
                                                        () -> {
                                                            parseHomePage(response);
                                                        });
                                    }
                                },
                                new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Throwable {
                                        Log.d(TAG, throwable.toString());
                                        //	isDataLoading.setValue(false);
                                        loadSate.setValue(LoadState.ERROR);
                                    }
                                });

        compositeDisposable.add(disposable);
    }

    private void parseHomePage(Document home) {
        HomePageParser homePageParser = new HomePageParser(home);
        homePageParser.parseHome();

        _interesingAnime.postValue(homePageParser.getInteresingAnime());
        _bestAnime.postValue(homePageParser.getBestAnime());
        _newAnime.postValue(homePageParser.getLatestReleasesAnime());
        _userData.postValue(homePageParser.getUser());
        loadSate.postValue(homePageParser.getLoadState());
    }

    public LiveData<LoadState> getLoadState() {
        return loadSate;
    }

    public LiveData<List<InteresingModel>> getInteresingAnime() {
        return _interesingAnime;
    }

    public LiveData<List<BaseAnimeModel>> getBestAnime() {
        return _bestAnime;
    }

    public LiveData<List<AnimeReleaseModel>> getNewAnime() {
        return _newAnime;
    }

    public LiveData<Pair<String, String>> getUserData() {
        return _userData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }
}
