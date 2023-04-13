package com.mrikso.anitube.app.model;

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
}
