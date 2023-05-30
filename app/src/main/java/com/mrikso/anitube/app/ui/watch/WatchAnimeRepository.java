package com.mrikso.anitube.app.ui.watch;

import androidx.lifecycle.LiveData;

import com.mrikso.anitube.app.data.history.HistoryDatabase;
import com.mrikso.anitube.app.data.history.dao.HistoryDao;
import com.mrikso.anitube.app.data.history.dao.LastWatchedEpisodeDao;
import com.mrikso.anitube.app.data.history.enity.HistoryEnity;
import com.mrikso.anitube.app.data.history.enity.LastWatchedEpisodeEnity;

import java.util.List;

import javax.inject.Inject;

public class WatchAnimeRepository {

    private HistoryDatabase databade;
    private final HistoryDao historyDao;
    private final LastWatchedEpisodeDao lastEpisodeDao;

    @Inject
    public WatchAnimeRepository(HistoryDatabase databade) {
        this.databade = databade;
        this.historyDao = databade.historyDao();
        this.lastEpisodeDao = databade.lastWatchedEpisodeDao();
    }

    public LiveData<List<LastWatchedEpisodeEnity>> getAllWatchedEpisodes() {
        return lastEpisodeDao.getAllHistory();
    }

    public LastWatchedEpisodeEnity getWatchedEpisode(int animeId, String episodeId) {
        return lastEpisodeDao.getById(animeId, episodeId);
    }

    public void toodgeStatusLastWatchedEpisode(boolean isViewed, int animeId, String episodeId) {
        LastWatchedEpisodeEnity episodeWatched = getWatchedEpisode(animeId, episodeId);
        if (episodeWatched != null) {
            episodeWatched.setIsWatched(isViewed);
            addOrUpdateWatchedEpisode(episodeWatched);
        }
    }

    public void addOrUpdateWatchedEpisode(LastWatchedEpisodeEnity historyEnity) {
        if (getWatchedEpisode(historyEnity.getAnimeId(), historyEnity.getEpisodeId()) != null) {
            historyEnity.setId(getWatchedEpisode(historyEnity.getAnimeId(), historyEnity.getEpisodeId())
                    .getId());
            lastEpisodeDao.update(historyEnity);
        } else {
            lastEpisodeDao.insert(historyEnity);
        }
    }

    public void deleteWatchedEpisode(int animeId, String episodeId) {
        lastEpisodeDao.deleteById(animeId, episodeId);
    }

    public void deleteAllWatchedEpisodes() {
        lastEpisodeDao.deleteAll();
    }

    public LiveData<List<HistoryEnity>> getAllHistory() {
        return historyDao.getAllHistory();
    }

    public HistoryEnity getHistoryEnity(int animeId) {
        return historyDao.getById(animeId);
    }

    public void addOrUpdateHistoryItem(HistoryEnity historyEnity) {
        if (getHistoryEnity(historyEnity.getAnimeId()) != null) {
            historyEnity.setId(getHistoryEnity(historyEnity.getAnimeId()).getId());
            historyDao.update(historyEnity);
        } else {
            historyDao.insert(historyEnity);
        }
    }

    public void deleteHistoryEnity(int animeId) {
        historyDao.deleteById(animeId);
    }

    public void deleteAllHistory() {
        historyDao.deleteAll();
    }
}
