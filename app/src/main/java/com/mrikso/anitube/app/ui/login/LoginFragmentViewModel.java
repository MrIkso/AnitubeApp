package com.mrikso.anitube.app.ui.login;

import android.util.Log;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.parser.HomePageParser;
import com.mrikso.anitube.app.repository.AnitubeRepository;
import com.mrikso.anitube.app.utils.PreferencesHelper;

import dagger.hilt.android.lifecycle.HiltViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import org.jsoup.nodes.Document;

import retrofit2.Response;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Inject;

@HiltViewModel
public class LoginFragmentViewModel extends ViewModel {
    private final String TAG = "LoginFragmentViewModel";
    private AnitubeRepository repository;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private MutableLiveData<Pair<LoadState, Pair<String, String>>> loadSate =
            new MutableLiveData<>(null);

    @Inject
    public LoginFragmentViewModel(AnitubeRepository repository) {
        this.repository = repository;
    }

    public void login(String username, String password) {
        compositeDisposable.add(
                repository
                        .login(username, password)
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe(
                                v -> {
                                    loadSate.setValue(
                                            new Pair<>(LoadState.LOADING, new Pair<>(null, null)));
                                    Log.d(TAG, "start loading");
                                })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    Executors.newSingleThreadExecutor()
                                            .execute(
                                                    () -> {
                                                        saveCookies(response);
                                                    });
                                },
                                throwable -> {
                                    throwable.printStackTrace();
                                    loadSate.postValue(
                                            new Pair<>(
                                                    LoadState.ERROR,
                                                    new Pair<>(throwable.getMessage(), null)));
                                }));
    }

    public LiveData<Pair<LoadState, Pair<String, String>>> getLoadState() {
        return loadSate;
    }

    private void saveCookies(Response<Document> response) {
        HomePageParser homePageParser = new HomePageParser(response.body());
        Pair<String, String> userDataPair = homePageParser.getUserData();
        List<String> cookielist = response.headers().values("Set-Cookie");
        HashSet<String> cookies = new HashSet<>();

        for (String header : cookielist) {
            Log.i(TAG, header);
            if (header.contains("dle_user_id")
                    || header.contains("dle_password")
                    || header.contains("dle_newpm")
                    || header.contains("PHPSESSID")) {
                cookies.add(header);
            }
        }
        if (!cookies.isEmpty()) {
            PreferencesHelper.getInstance().saveCooikes(cookies);
            PreferencesHelper.getInstance().setLogin(true);
            loadSate.postValue(new Pair<>(LoadState.DONE, userDataPair));
        } else {
            loadSate.postValue(new Pair<>(LoadState.ERROR, null));
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }
}
