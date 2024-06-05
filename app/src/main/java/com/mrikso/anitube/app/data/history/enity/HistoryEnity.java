package com.mrikso.anitube.app.data.history.enity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.common.base.Objects;

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
    private int episodeId;

    @ColumnInfo(name = "total_watch_time")
    private long totalWatchTime;

    @ColumnInfo(name = "total_episode_time")
    private long totalEpisodeTime;

    @ColumnInfo(name = "watch_date")
    private long watchDate;

    @ColumnInfo(name = "source_path")
    private String sourcePath;

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getEpisodeId() {
        return this.episodeId;
    }

    public void setEpisodeId(int episodeId) {
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

    public String getSourcePath() {
        return this.sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoryEnity that = (HistoryEnity) o;
        return id == that.id && animeId == that.animeId && episodeId == that.episodeId && totalWatchTime == that.totalWatchTime && totalEpisodeTime == that.totalEpisodeTime && watchDate == that.watchDate && Objects.equal(name, that.name) && Objects.equal(animeUrl, that.animeUrl) && Objects.equal(posterUrl, that.posterUrl) && Objects.equal(sourcePath, that.sourcePath);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, animeId, name, animeUrl, posterUrl, episodeId, totalWatchTime, totalEpisodeTime, watchDate, sourcePath);
    }
}
