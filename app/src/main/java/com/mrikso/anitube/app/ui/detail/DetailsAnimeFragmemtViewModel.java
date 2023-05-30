package com.mrikso.anitube.app.ui.detail;

import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mrikso.anitube.app.App;
import com.mrikso.anitube.app.model.AnimeDetailsModel;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.parser.DetailsAnimeParser;
import com.mrikso.anitube.app.repository.AnitubeRepository;
import com.mrikso.anitube.app.utils.FileCache;
import com.mrikso.anitube.app.utils.PreferencesHelper;

import dagger.hilt.android.lifecycle.HiltViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.concurrent.Executors;

import javax.inject.Inject;

@HiltViewModel
public class DetailsAnimeFragmemtViewModel extends ViewModel {
    private final String TAG = "DetailsAnimeFragmemtViewModel";

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private AnitubeRepository repository;
    private MutableLiveData<LoadState> loadSate = new MutableLiveData<>(LoadState.LOADING);
    private MutableLiveData<AnimeDetailsModel> detailsModel;
    private final DetailsAnimeParser parser = new DetailsAnimeParser();

    @Inject
    public DetailsAnimeFragmemtViewModel(AnitubeRepository repository) {
        this.repository = repository;
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
        Disposable disposable = repository
                .getPage(url)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Throwable {
                        loadSate.postValue(LoadState.LOADING);
                        Log.d(TAG, "start loading");
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<Document>() {
                    @Override
                    public void onSuccess(Document response) {
                        Executors.newSingleThreadExecutor().execute(() -> {
                            try {
                                FileCache.writePage(response.html());
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
    }

    public LiveData<LoadState> getLoadState() {
        return loadSate;
    }

    public LiveData<AnimeDetailsModel> getDetails() {
        return detailsModel;
    }

    public void addOrRemoveFromFavorites(int animeId, boolean isAdd) {

        compositeDisposable.add(repository
                .addOrRemoveFromFavorites(
                        animeId, isAdd, PreferencesHelper.getInstance().getDleHash())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(v -> {
                    AnimeDetailsModel newModel = detailsModel.getValue();
                    newModel.setFavorites(isAdd);
                    detailsModel.postValue(newModel);
                }));
    }

    public void changeAnimeStatus(int animeId, int viewStatus) {
        compositeDisposable.add(repository
                .changeAnimeStatus(animeId, viewStatus)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(v -> {
                    Toast.makeText(App.getApplication(), v.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                }));
    }

    public void parseAnimePage(Document document) {

        compositeDisposable.add(
                parser.getDetailsModel(document).subscribeOn(Schedulers.io()).subscribe(v -> {
                    detailsModel.postValue(v);
                    loadSate.postValue(LoadState.DONE);
                }));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }
}
