package com.mrikso.anitube.app.model;

public class FranchiseModel extends BaseAnimeModel {
    private boolean isCurrent;
    private String episodes;
    private String releaseYear;

    public FranchiseModel(String title, String posterUrl, String animeUrl, boolean isCurrent) {
        super(title, posterUrl, animeUrl);
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
}
