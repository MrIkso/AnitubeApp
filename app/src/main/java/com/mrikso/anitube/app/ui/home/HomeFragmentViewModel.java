package com.mrikso.anitube.app.ui.home;

import android.util.Log;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.model.ActionModel;
import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.model.BaseAnimeModel;
import com.mrikso.anitube.app.model.CollectionModel;
import com.mrikso.anitube.app.model.InteresingModel;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.SimpleModel;
import com.mrikso.anitube.app.network.ApiClient;
import com.mrikso.anitube.app.parser.HomePageParser;
import com.mrikso.anitube.app.repository.AnitubeRepository;

import dagger.hilt.android.lifecycle.HiltViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Inject;

@HiltViewModel
public class HomeFragmentViewModel extends ViewModel {
    private final String TAG = "HomeFragmentViewModel";

    private AnitubeRepository repository;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private MutableLiveData<LoadState> loadSate = new MutableLiveData<>(LoadState.LOADING);
    private MutableLiveData<List<InteresingModel>> interesingAnime = new MutableLiveData<>(null);
    private MutableLiveData<List<BaseAnimeModel>> bestAnime = new MutableLiveData<>(null);
    private MutableLiveData<List<AnimeReleaseModel>> newAnime = new MutableLiveData<>(null);
    private MutableLiveData<List<CollectionModel>> newCollections = new MutableLiveData<>(null);
    private MutableLiveData<List<SimpleModel>> yearsList = new MutableLiveData<>(null);
    private MutableLiveData<List<SimpleModel>> genresList = new MutableLiveData<>(null);
    private MutableLiveData<List<ActionModel>> actionList = new MutableLiveData<>(null);
    private MutableLiveData<Pair<String, String>> userData = new MutableLiveData<>(null);
    private MutableLiveData<String> dleHash = new MutableLiveData<>();
    HomePageParser homePageParser;
    private boolean singleLoad = false;

    @Inject
    public HomeFragmentViewModel(AnitubeRepository repository) {
        this.repository = repository;
        homePageParser = HomePageParser.getInstance();
    }

    public void loadHome() {
        if (!singleLoad) {
            loadData();
            createActionList();
            singleLoad = true;
        }
    }

    public void reloadHome() {
        loadData();
    }

    private void loadData() {

        Disposable disposable = repository
                .getHome()
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Consumer<Disposable>() {
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
                                Executors.newSingleThreadExecutor().execute(() -> {
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

        compositeDisposable.add(homePageParser
                .getUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(results -> {
                    if (results != null && results.first != null && results.second != null) {
                        userData.postValue(results);
                    }
                }));
    }

    private void createActionList() {
        List<ActionModel> actionListModel = new ArrayList<>();
        actionListModel.add(new ActionModel(
                ActionMode.ACTION_MODE_YEARS, R.string.release_calendar, ApiClient.ANIME_CALLENDAR_URL));
        actionListModel.add(
                new ActionModel(ActionMode.ACTION_MODE_GENRES, R.string.release_genres, ApiClient.ANIME_GENRES_URL));
        actionListModel.add(new ActionModel(
                ActionMode.ACTION_MODE_RANDOM_ANIME, R.string.random_anime, ApiClient.ANIME_RANDOM_BG_URL));

        this.actionList.postValue(actionListModel);
    }

    private void parseHomePage(Document home) {
        homePageParser.parseHome(home);

        interesingAnime.postValue(homePageParser.getInteresingAnime());
        bestAnime.postValue(homePageParser.getBestAnime());
        newAnime.postValue(homePageParser.getLatestReleasesAnime());
        newCollections.postValue(homePageParser.getNewCollectionsList());
        genresList.postValue(homePageParser.getGenresList());
        yearsList.postValue(homePageParser.getCalendarList());

        // userData.postValue(homePageParser.getUser());
        loadSate.postValue(homePageParser.getLoadState());
    }

    public LiveData<LoadState> getLoadState() {
        return loadSate;
    }

    public LiveData<List<InteresingModel>> getInteresingAnime() {
        return interesingAnime;
    }

    public LiveData<List<BaseAnimeModel>> getBestAnime() {
        return bestAnime;
    }

    public LiveData<List<AnimeReleaseModel>> getNewAnime() {
        return newAnime;
    }

    public LiveData<List<CollectionModel>> getNewACollections() {
        return newCollections;
    }

    public LiveData<List<SimpleModel>> getYearsList() {
        return yearsList;
    }

    public LiveData<List<SimpleModel>> getGenresList() {
        return genresList;
    }

    public LiveData<Pair<String, String>> getUserData() {
        return userData;
    }

    public LiveData<List<ActionModel>> getActionList() {
        return actionList;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }
}
