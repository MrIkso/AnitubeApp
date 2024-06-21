package com.mrikso.anitube.app.viewmodel;

import androidx.core.util.Pair;
import androidx.lifecycle.ViewModel;

import com.mrikso.anitube.app.data.history.enity.HistoryEnity;
import com.mrikso.anitube.app.data.history.enity.LastWatchedEpisodeEnity;
import com.mrikso.anitube.app.model.BaseAnimeModel;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.VideoLinksModel;
import com.mrikso.anitube.app.parser.DirectVideoUrlParser;
import com.mrikso.anitube.app.parser.video.model.EpisodeModel;
import com.mrikso.anitube.app.repository.ListRepository;
import com.mrikso.anitube.app.ui.watch.WatchAnimeRepository;

import dagger.hilt.android.lifecycle.HiltViewModel;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

import okhttp3.OkHttpClient;

import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Named;

@HiltViewModel
public class SharedViewModel extends ViewModel {
    private final String TAG = "SharedViewModel";
    private final ListRepository listRepo;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final WatchAnimeRepository watchAnimeRepository;
    private final OkHttpClient client;

    @Inject
    public SharedViewModel(@Named("Normal") OkHttpClient client, WatchAnimeRepository watchAnimeRepository) {
        this.client = client;
        this.watchAnimeRepository = watchAnimeRepository;
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

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }
}
