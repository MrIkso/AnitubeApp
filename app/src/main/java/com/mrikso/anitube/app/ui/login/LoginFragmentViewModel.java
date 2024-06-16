package com.mrikso.anitube.app.ui.login;

import android.util.Log;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.common.base.Strings;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.UserModel;
import com.mrikso.anitube.app.parser.HomePageParser;
import com.mrikso.anitube.app.repository.AnitubeRepository;
import com.mrikso.anitube.app.utils.CookieParser;
import com.mrikso.anitube.app.utils.PreferencesHelper;
import com.mrikso.anitube.app.viewmodel.UserProfileRepository;

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
    private final AnitubeRepository repository;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final MutableLiveData<Pair<LoadState, UserModel>> loadSate = new MutableLiveData<>(null);
    private final HomePageParser homePageParser = HomePageParser.getInstance();

    @Inject
    public LoginFragmentViewModel(AnitubeRepository repository) {
        this.repository = repository;
    }

    public void login(String username, String password) {
        compositeDisposable.add(repository
                .login(username, password)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(v -> {
                    loadSate.setValue(new Pair<>(LoadState.LOADING, null));
                    //Log.d(TAG, "start loading");
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            Executors.newSingleThreadExecutor().execute(() -> {
                                saveCookies(response);
                            });
                        },
                        throwable -> {
                            throwable.printStackTrace();
                            loadSate.postValue(new Pair<>(LoadState.ERROR, null));
                        }));
    }

    public LiveData<Pair<LoadState, UserModel>> getLoadState() {
        return loadSate;
    }

    private void saveCookies(Response<Document> response) {
        List<String> cookielist = response.headers().values("Set-Cookie");
        CookieParser cp = new CookieParser(cookielist);
        HashSet<String> cookies = new HashSet<>();

        if (Strings.isNullOrEmpty(cp.getValue("dle_user_id"))
                || "deleted".equals(cp.getValue("dle_user_id")) && Strings.isNullOrEmpty(cp.getValue("dle_password"))
                || "deleted".equals(cp.getValue("dle_password"))) {
            loadSate.postValue(new Pair<>(LoadState.ERROR, null));
            return;
        }

        for (String header : cookielist) {
            //Log.i(TAG, header);
            if (header.contains("dle_user_id")
                    || header.contains("dle_password")
                    || header.contains("dle_newpm")
                    || header.contains("PHPSESSID")) {
                cookies.add(header);
            }
        }
        if (!cookies.isEmpty()) {
            PreferencesHelper.getInstance().saveCookies(cookies);
            PreferencesHelper.getInstance().setLogin(true);
            UserModel userDataPair = homePageParser.getUserData(response.body());
            PreferencesHelper.getInstance().setUserLogin(userDataPair.getUserName());
            UserProfileRepository.getInstance().setUserModel(userDataPair);
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
