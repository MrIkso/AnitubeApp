package com.mrikso.anitube.app.ui.detail;

import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mrikso.anitube.app.App;
import com.mrikso.anitube.app.model.AnimeDetailsModel;
import com.mrikso.anitube.app.model.AnimeMobileDetailsModel;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.parser.DetailsAnimeParser;
import com.mrikso.anitube.app.repository.AnitubeRepository;
import com.mrikso.anitube.app.repository.HikkaRepository;
import com.mrikso.anitube.app.utils.FileCache;
import com.mrikso.anitube.app.utils.InternetConnection;
import com.mrikso.anitube.app.utils.ParserUtils;
import com.mrikso.anitube.app.utils.PreferencesHelper;

import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class DetailsAnimeFragmemtViewModel extends ViewModel {
    private final String TAG = "DetailsAnimeFragmemtViewModel";

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final AnitubeRepository anitubeRepository;
    private final HikkaRepository hikkaRepository;
    private final MutableLiveData<LoadState> loadSate = new MutableLiveData<>(LoadState.LOADING);
    private MutableLiveData<AnimeDetailsModel> detailsModel ;
    private final MutableLiveData<AnimeMobileDetailsModel> mobileDetailsModelMutableLiveData = new MutableLiveData<>(new AnimeMobileDetailsModel());
    private final DetailsAnimeParser parser = new DetailsAnimeParser();
    private final PreferencesHelper preferencesHelper;
    @Inject
    public DetailsAnimeFragmemtViewModel(AnitubeRepository repository, HikkaRepository hikkaRepository, PreferencesHelper preferencesHelper) {
        this.anitubeRepository = repository;
        this.hikkaRepository = hikkaRepository;
        this.preferencesHelper = preferencesHelper;
    }

    public void loadData(String url) {
        Log.d(TAG, "loadData called");
        if (detailsModel == null) {
            detailsModel = new MutableLiveData<>();
            loadAnime(url);
            Log.d(TAG, "loadAnime called");
        }
    }

    public void loadAnime(String url) {
        if (!InternetConnection.isNetworkAvailable(App.getApplication())) {
            loadSate.setValue(LoadState.NO_NETWORK);
            return;
        }
        Disposable disposable = anitubeRepository
                .getPage(url)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(disposable1 -> {
                    loadSate.postValue(LoadState.LOADING);
                    Log.d(TAG, "start loading");
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<Document>() {
                    @Override
                    public void onSuccess(Document response) {
                        Executors.newSingleThreadExecutor().execute(() -> {
                            try {
                                FileCache.writePage(response.html());
                                ParserUtils.parseDleHash(response.html());
                            } catch (IOException err) {
                                err.printStackTrace();
                            }
                        });
                        Executors.newSingleThreadExecutor().execute(() -> {
                            parseAnimePage(response);
                        });
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        loadSate.postValue(LoadState.ERROR);
                    }
                });

        compositeDisposable.add(disposable);

        if (preferencesHelper.isLoadAnimeAdditionalInfo()) {
            loadMobileAnimeDetails(url);
        }
    }

    private void loadMobileAnimeDetails(String url){
        compositeDisposable.add(anitubeRepository
                .getMobilePage(url)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(v -> {
                   // loadSate.setValue(new Pair<>(LoadState.LOADING, null));
                    Log.d(TAG, "start mobile loading");
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            Executors.newSingleThreadExecutor().execute(() -> {
                                parseMobileDetailAnime(response);
                            });
                        },
                        throwable -> {
                            throwable.printStackTrace();
                            //loadSate.postValue(new Pair<>(LoadState.ERROR, null));
                        }));
    }

    public LiveData<LoadState> getLoadState() {
        return loadSate;
    }

    public LiveData<AnimeDetailsModel> getDetails() {
        return detailsModel;
    }

    public LiveData<AnimeMobileDetailsModel> getMobileDetails(){
        return mobileDetailsModelMutableLiveData;
    }

    public void addOrRemoveFromFavorites(int animeId, boolean isAdd) {

        compositeDisposable.add(anitubeRepository
                .addOrRemoveFromFavorites(
                        animeId, isAdd, preferencesHelper.getDleHash())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(v -> {
                    AnimeDetailsModel newModel = detailsModel.getValue();
                    newModel.setFavorites(isAdd);
                    detailsModel.postValue(newModel);
                }));
    }

    public void changeAnimeStatus(int animeId, int viewStatus) {
        changeAnimeStatusOnAnitube(animeId, viewStatus);

        if (preferencesHelper.isLogginedToHikka()) {
            changeAnimeStatusOnHikka(animeId, viewStatus);
        }
    }

    private void changeAnimeStatusOnAnitube(int anitubeId, int viewStatus) {
        compositeDisposable.add(anitubeRepository
                .changeAnimeStatus(anitubeId, viewStatus)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(v -> {
                    Toast.makeText(App.getApplication(), v.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                }));
    }

    private void changeAnimeStatusOnHikka(int anitubeId, int viewStatus) {
        compositeDisposable.add(hikkaRepository
                .getAnimeAndWatchStatus(anitubeId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(v -> {
                            Log.d(TAG, v.toString());
                            //   Toast.makeText(App.getApplication(), v.getMessage(), Toast.LENGTH_SHORT)
                            //        .show();
                        },
                        throwable -> {
                            // Обробка помилки
                            Log.e(TAG, ("Error: " + throwable.getMessage()));
                        }));
    }

    public void parseAnimePage(Document document) {
        compositeDisposable.add(
                parser.getDetailsModel(document).subscribeOn(Schedulers.io()).subscribe(v -> {
                    detailsModel.postValue(v);
                    loadSate.postValue(LoadState.DONE);
                }));
    }

    private void parseMobileDetailAnime(Document response) {

        compositeDisposable.add(
                parser.getMobileDetailsModel(response).subscribeOn(Schedulers.io()).subscribe(v -> {
                    mobileDetailsModelMutableLiveData.postValue(v);
                    loadSate.postValue(LoadState.DONE);
                }));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }
}
