package com.mrikso.anitube.app.ui.registration;

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

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Response;

@HiltViewModel
public class RegistrationFragmentViewModel extends ViewModel {
    private final String TAG = "LoginFragmentViewModel";
    private final AnitubeRepository repository;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final MutableLiveData<Pair<LoadState, UserModel>> loadSate = new MutableLiveData<>(null);
    private final MutableLiveData<Pair<LoadState, String>> checkNameState = new MutableLiveData<>(null);
    private final HomePageParser homePageParser = HomePageParser.getInstance();

    @Inject
    public RegistrationFragmentViewModel(AnitubeRepository repository) {
        this.repository = repository;
    }


    public LiveData<Pair<LoadState, UserModel>> getLoadState() {
        return loadSate;
    }

    public LiveData<Pair<LoadState, String>> getCheckNameState() {
        return checkNameState;
    }

    private void parseCheckUserNameResult(Document document) {

        Element span = document.selectFirst("span");
        String style = span.attr("style");
        String text = span.text();

        if (style.contains("color:red")) {
            checkNameState.postValue(new Pair<>(LoadState.ERROR, text));
        } else if (style.contains("color:green")) {
            checkNameState.postValue(new Pair<>(LoadState.DONE, text));
        }

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
            Log.i(TAG, header);
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
