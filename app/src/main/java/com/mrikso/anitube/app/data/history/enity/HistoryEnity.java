package com.mrikso.anitube.app.data.history.enity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "watch_history")
public class HistoryEnity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "anime_id")
    private int animeId;

    @ColumnInfo(name = "anime_name")
    private String name;

    @ColumnInfo(name = "anime_url")
    private String animeUrl;

    @ColumnInfo(name = "poster_url")
    private String posterUrl;

    @ColumnInfo(name = "episode_id")
    private String episodeId;

    @ColumnInfo(name = "total_watch_time")
    private long totalWatchTime;

    @ColumnInfo(name = "total_episode_time")
    private long totalEpisodeTime;

    @ColumnInfo(name = "watch_date")
    private long watchDate;

    public HistoryEnity(int animeId, String name) {
        this.animeId = animeId;
        this.name = name;
    }

    public int getAnimeId() {
        return this.animeId;
    }

    public void setAnimeId(int animeId) {
        this.animeId = animeId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAnimeUrl() {
        return this.animeUrl;
    }

    public void setAnimeUrl(String animeUrl) {
        this.animeUrl = animeUrl;
    }

    public String getPosterUrl() {
        return this.posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getEpisodeId() {
        return this.episodeId;
    }

    public void setEpisodeId(String episodeId) {
        this.episodeId = episodeId;
    }

    public long getTotalWatchTime() {
        return this.totalWatchTime;
    }

    public void setTotalWatchTime(long totalWatchTime) {
        this.totalWatchTime = totalWatchTime;
    }

    public long getTotalEpisodeTime() {
        return this.totalEpisodeTime;
    }

    public void setTotalEpisodeTime(long totalEpisodeTime) {
        this.totalEpisodeTime = totalEpisodeTime;
    }

    public long getWatchDate() {
        return this.watchDate;
    }

    public void setWatchDate(long watchDate) {
        this.watchDate = watchDate;
    }
}
