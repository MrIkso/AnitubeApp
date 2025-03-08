package com.mrikso.anitube.app.viewmodel;

import android.util.Log;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mrikso.anitube.app.BuildConfig;
import com.mrikso.anitube.app.data.history.enity.HistoryEnity;
import com.mrikso.anitube.app.data.history.enity.LastWatchedEpisodeEnity;
import com.mrikso.anitube.app.model.BaseAnimeModel;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.VideoLinksModel;
import com.mrikso.anitube.app.network.response.ProfileResponse;
import com.mrikso.anitube.app.parser.DirectVideoUrlParser;
import com.mrikso.anitube.app.parser.video.model.EpisodeModel;
import com.mrikso.anitube.app.repository.HikkaRepository;
import com.mrikso.anitube.app.repository.ListRepository;
import com.mrikso.anitube.app.repository.TokenManager;
import com.mrikso.anitube.app.ui.watch.WatchAnimeRepository;
import com.mrikso.anitube.app.utils.PreferencesHelper;

import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.OkHttpClient;

@HiltViewModel
public class SharedViewModel extends ViewModel {
    private final String TAG = "SharedViewModel";
    private final ListRepository listRepo;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final WatchAnimeRepository watchAnimeRepository;
    private final TokenManager tokenManager;
    private final OkHttpClient client;
    private final MutableLiveData<Boolean> hikkaLogin = new MutableLiveData<>();

    private final MutableLiveData<ProfileResponse> hikkaProfileResponse = new MutableLiveData<>();
    private final HikkaRepository hikkaRepository;

    @Inject
    public SharedViewModel(@Named("Normal") OkHttpClient client, WatchAnimeRepository watchAnimeRepository,
                           HikkaRepository hikkaRepository, TokenManager tokenManager) {
        this.client = client;
        this.watchAnimeRepository = watchAnimeRepository;
        this.hikkaRepository = hikkaRepository;
        this.tokenManager = tokenManager;
        listRepo = ListRepository.getInstance();
    }

    // Init ViewModel Data
    public Single<Pair<LoadState, VideoLinksModel>> loadData(String ifRame) {
        return new DirectVideoUrlParser(client).getDirectUrl(ifRame);
    }

    public void addOrUpdateWatchedEpisode(int position, boolean isWatched, BaseAnimeModel animeModel) {
        addOrUpdateWatchedEpisode(position, isWatched, 0L, 0L, animeModel);
    }

    public void addOrUpdateWatchedEpisode(
            int position, boolean isWatched, long totalEpisodeTime, long watchEpisodeTime, BaseAnimeModel animeModel) {
        // List<EpisodeModel> newList = new ArrayList<>();
        //     newList.addAll(listRepo.getList());

        EpisodeModel episodeModel = listRepo.getList().get(position);
        try {
            EpisodeModel newEpisodeModel = (EpisodeModel) episodeModel.clone();
            newEpisodeModel.setIsWatched(isWatched);
            newEpisodeModel.setTotalEpisodeTime(totalEpisodeTime);
            newEpisodeModel.setTotalWatchTime(watchEpisodeTime);
            //Log.i("shared", newEpisodeModel.toString());
            listRepo.updateItem(newEpisodeModel, position);
            addOrUpdateWatchedEpisode(animeModel, newEpisodeModel);
        } catch (CloneNotSupportedException c) {
            c.printStackTrace();
        }
    }

    public void addOrUpdateWatchedAnime(
            BaseAnimeModel baseAnimeModel,
            String sourcePath,
            int episodeNumber,
            long totalEpisodeTime,
            long currentPlayback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            HistoryEnity historyEnity = new HistoryEnity();
            historyEnity.setAnimeId(baseAnimeModel.getAnimeId());
            historyEnity.setAnimeUrl(baseAnimeModel.getAnimeUrl());
            historyEnity.setName(baseAnimeModel.getTitle());
            historyEnity.setPosterUrl(baseAnimeModel.getPosterUrl());
            historyEnity.setWatchDate(System.currentTimeMillis());
            historyEnity.setSourcePath(sourcePath);
            historyEnity.setTotalEpisodeTime(totalEpisodeTime);
            historyEnity.setTotalWatchTime(currentPlayback);
            historyEnity.setEpisodeId(episodeNumber);

            watchAnimeRepository.addOrUpdateHistoryItem(historyEnity);
        });
    }

    public void addOrUpdateWatchedEpisode(BaseAnimeModel baseAnimeModel, EpisodeModel episodeModel) {
        Executors.newSingleThreadExecutor().execute(() -> {
            LastWatchedEpisodeEnity lastWatchEntry = new LastWatchedEpisodeEnity();
            lastWatchEntry.setAnimeId(baseAnimeModel.getAnimeId());
            lastWatchEntry.setIsWatched(episodeModel.isWatched());
            lastWatchEntry.setTotalEpisodeTime(episodeModel.getTotalEpisodeTime());
            lastWatchEntry.setTotalWatchTime(episodeModel.getTotalWatchTime());
            lastWatchEntry.setEpisodeId(episodeModel.getEpisodeId());
            watchAnimeRepository.addOrUpdateWatchedEpisode(lastWatchEntry);
        });
    }

    public void hikkaLogin(String reference) {
        compositeDisposable.add(hikkaRepository.getAuthToken(BuildConfig.CLIENT_SECET, reference)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(tokenResponse -> {
                    hikkaLogin.postValue(true);
                    PreferencesHelper.getInstance().setHikkaToken(tokenResponse.getSecret(), tokenResponse.getExpiration());
                    updateToken(tokenResponse.getExpiration());
                    return hikkaRepository.getMeProfile();
                })
                .subscribe(profileResponse -> {
                    if (profileResponse != null) {
                        hikkaProfileResponse.postValue(profileResponse);
                    }
                }, throwable -> {
                    Log.e(TAG, throwable.getMessage());
                    throwable.printStackTrace();
                }));
    }

    public LiveData<Boolean> hikkaLogin() {
        return hikkaLogin;
    }

    public void loadHikkaProfile() {
        compositeDisposable.add(hikkaRepository.getMeProfile()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(profileResponse -> {
                    if (profileResponse != null) {
                        hikkaProfileResponse.postValue(profileResponse);
                    }
                }, throwable -> {
                    Log.e(TAG, throwable.getMessage());
                    throwable.printStackTrace();
                }));
    }

    public void updateToken(long expirationTime) {
        compositeDisposable.add(tokenManager.updateToken(expirationTime)
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                    // Токен успішно збережено
                }, throwable -> {
                    throwable.printStackTrace();
                }));
    }

    public LiveData<ProfileResponse> getHikkaProfile() {
        return hikkaProfileResponse;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }
}
