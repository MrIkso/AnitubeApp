package com.mrikso.anitube.app.model;

import com.google.common.base.Objects;

public class FranchiseModel extends BaseAnimeModel {
    private boolean isCurrent;
    private String episodes;
    private String releaseYear;

    public FranchiseModel(int animeId, String title, String posterUrl, String animeUrl, boolean isCurrent) {
        super(animeId, title, posterUrl, animeUrl);
        this.isCurrent = isCurrent;
    }

    public boolean isCurrent() {
        return this.isCurrent;
    }

    public void setIsCurrent(boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public String getEpisodes() {
        return this.episodes;
    }

    public void setEpisodes(String episodes) {
        this.episodes = episodes;
    }

    public String getReleaseYear() {
        return this.releaseYear;
    }

    public void setReleaseYear(String releaseYear) {
        this.releaseYear = releaseYear;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FranchiseModel that = (FranchiseModel) o;
        return isCurrent == that.isCurrent && Objects.equal(episodes, that.episodes) && Objects.equal(releaseYear, that.releaseYear);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(isCurrent, episodes, releaseYear);
    }
}
