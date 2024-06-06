package com.mrikso.anitube.app.model;

import com.google.common.base.Objects;

public class InteresingModel {
    private final String posterUrl;
    private final String animeUrl;

    public InteresingModel(String posterUrl, String animeUrl) {
        this.posterUrl = posterUrl;
        this.animeUrl = animeUrl;
    }

    public String getPosterUrl() {
        return this.posterUrl;
    }

    public String getAnimeUrl() {
        return this.animeUrl;
    }

    @Override
    public String toString() {
        return "InterestingModel[posterUrl=" + posterUrl + ", animeUrl=" + animeUrl + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InteresingModel that = (InteresingModel) o;
        return Objects.equal(posterUrl, that.posterUrl) && Objects.equal(animeUrl, that.animeUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(posterUrl, animeUrl);
    }
}
