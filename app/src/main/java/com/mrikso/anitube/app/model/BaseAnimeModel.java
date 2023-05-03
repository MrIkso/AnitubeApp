package com.mrikso.anitube.app.model;

public class BaseAnimeModel {
    public final String title;
    public final String posterUrl;
    public final String animeUrl;

    public BaseAnimeModel(String title, String posterUrl, String animeUrl) {
        this.title = title;
        this.posterUrl = posterUrl;
        this.animeUrl = animeUrl;
    }

    @Override
    public String toString() {
        return "BaseAnimeModel[title="
                + title
                + ", posterUrl="
                + posterUrl
                + ", animeUrl="
                + animeUrl
                + "]";
    }

    public String getTitle() {
        return this.title;
    }

    public String getPosterUrl() {
        return this.posterUrl;
    }

    public String getAnimeUrl() {
        return this.animeUrl;
    }
}
