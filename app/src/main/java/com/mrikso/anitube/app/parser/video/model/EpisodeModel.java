package com.mrikso.anitube.app.parser.video.model;

import com.google.common.base.Objects;

import java.io.Serializable;

public class EpisodeModel implements Serializable, Cloneable {
    private final String playerId;
    private final String episodeId;
    private final String name;
    private final String episodeUrl;
    private long totalWatchTime;
    private long totalEpisodeTime;
    private boolean isWatched;

    public EpisodeModel(String name, String episodeUrl) {
        this.playerId = "";
        this.episodeId = episodeUrl;
        this.name = name;
        this.episodeUrl = episodeUrl;
    }

    public EpisodeModel(String playerId, String name, String episodeUrl) {
        this.playerId = playerId;
        this.episodeId = episodeUrl;
        this.name = name;
        this.episodeUrl = episodeUrl;
    }

    public String getEpisodeUrl() {
        return this.episodeUrl;
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

    public boolean isWatched() {
        return this.isWatched;
    }

    public void setIsWatched(boolean isWatched) {
        this.isWatched = isWatched;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EpisodeModel that = (EpisodeModel) o;

        return totalWatchTime == that.totalWatchTime
                && totalEpisodeTime == that.totalEpisodeTime
                && isWatched == that.isWatched
                && playerId.equals(that.playerId)
                && episodeId.equals(that.episodeId)
                && name.equals(that.name)
                && episodeUrl.equals(that.episodeUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(playerId, episodeId, name, episodeUrl, totalWatchTime, totalEpisodeTime, isWatched);
    }

    public String getPlayerId() {
        return this.playerId;
    }

    public String getName() {
        return this.name;
    }

    public String getEpisodeId() {
        return this.episodeId;
    }

    @Override
    public String toString() {
        return "EpisodeModel[playerId="
                + playerId
                + ", episodeId="
                + episodeId
                + ", name="
                + name
                + ", episodeUrl="
                + episodeUrl
                + ", totalWatchTime="
                + totalWatchTime
                + ", totalEpisodeTime="
                + totalEpisodeTime
                + ", isWatched="
                + isWatched
                + "]";
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
