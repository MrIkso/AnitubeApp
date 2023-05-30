package com.mrikso.anitube.app.data.history.enity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "last_watched_episode")
public class LastWatchedEpisodeEnity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "anime_id")
    private int animeId;

    @ColumnInfo(name = "episode_id")
    private String episodeId;

    @ColumnInfo(name = "total_watch_time")
    private long totalWatchTime;

    @ColumnInfo(name = "total_episode_time")
    private long totalEpisodeTime;

    @ColumnInfo(name = "isWatched")
    private boolean isWatched;

    public int getAnimeId() {
        return this.animeId;
    }

    public void setAnimeId(int animeId) {
        this.animeId = animeId;
    }

    public long getTotalWatchTime() {
        return this.totalWatchTime;
    }

    public void setTotalWatchTime(long totalWatchTime) {
        this.totalWatchTime = totalWatchTime;
    }

    public String getEpisodeId() {
        return this.episodeId;
    }

    public void setEpisodeId(String episodeId) {
        this.episodeId = episodeId;
    }

    public long getTotalEpisodeTime() {
        return this.totalEpisodeTime;
    }

    public void setTotalEpisodeTime(long totalEpisodeTime) {
        this.totalEpisodeTime = totalEpisodeTime;
    }

    public boolean isWatched() {
        return this.isWatched;
    }

    public void setIsWatched(boolean isWatched) {
        this.isWatched = isWatched;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
