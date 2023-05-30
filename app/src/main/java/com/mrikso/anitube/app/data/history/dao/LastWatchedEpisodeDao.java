package com.mrikso.anitube.app.data.history.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.mrikso.anitube.app.data.history.enity.LastWatchedEpisodeEnity;

import java.util.List;

@Dao
public interface LastWatchedEpisodeDao {
    @Query("SELECT * FROM last_watched_episode ORDER BY id DESC")
    LiveData<List<LastWatchedEpisodeEnity>> getAllHistory();

    @Query("SELECT * FROM last_watched_episode WHERE anime_id = :animeId AND episode_id=:episodeId")
    LastWatchedEpisodeEnity getById(int animeId, String episodeId);

    @Query("DELETE FROM last_watched_episode WHERE anime_id = :animeId AND episode_id=:episodeId")
    void deleteById(int animeId, String episodeId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(LastWatchedEpisodeEnity recentSearch);

    @Query("DELETE FROM last_watched_episode")
    void deleteAll();

    @Update
    void update(LastWatchedEpisodeEnity item);
}
