package com.mrikso.anitube.app.ui.profile;

import android.util.Log;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.UserProfileModel;
import com.mrikso.anitube.app.parser.UserProfileParser;
import com.mrikso.anitube.app.repository.AnitubeRepository;

import dagger.hilt.android.lifecycle.HiltViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import org.jsoup.nodes.Document;

import java.util.concurrent.Executors;

import javax.inject.Inject;

@HiltViewModel
public class ProfilePragmentViewModel extends ViewModel {
    private final String TAG = "ProfilePragmentViewModel";
    private AnitubeRepository repository;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private MutableLiveData<Pair<LoadState, UserProfileModel>> loadSate =
            new MutableLiveData<>(null);
    private boolean singleLoad;

    @Inject
    public ProfilePragmentViewModel(AnitubeRepository repository) {
        this.repository = repository;
    }

    public LiveData<Pair<LoadState, UserProfileModel>> getData() {
        return loadSate;
    }

    public void loadData(String url) {
        if (!singleLoad) {
            loadPage(url);
            singleLoad = true;
        }
    }

    public void reloadData(String url) {
        loadPage(url);
    }

    public void loadPage(String url) {
        compositeDisposable.add(
                repository
                        .getPage(url)
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe(
                                v -> {
                                    loadSate.setValue(new Pair<>(LoadState.LOADING, null));
                                    Log.d(TAG, "start loading");
                                })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    Executors.newSingleThreadExecutor()
                                            .execute(
                                                    () -> {
                                                        parsUserPage(response);
                                                    });
                                },
                                throwable -> {
                                    throwable.printStackTrace();
                                    loadSate.postValue(new Pair<>(LoadState.ERROR, null));
                                }));
    }

    private void parsUserPage(final Document response) {
        UserProfileParser parser = new UserProfileParser(response);
        loadSate.postValue(new Pair<>(LoadState.DONE, parser.getUserProfile()));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }
}
