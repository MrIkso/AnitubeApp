package com.mrikso.anitube.app.model;

import java.io.Serializable;

public class BaseAnimeModel implements Serializable {
    private final int animeId;
    private final String title;
    private String posterUrl;
    private final String animeUrl;

    public BaseAnimeModel(int animeId, String title, String animeUrl) {
        this.animeId = animeId;
        this.title = title;
        this.animeUrl = animeUrl;
    }

    public BaseAnimeModel(int animeId, String title, String posterUrl, String animeUrl) {
        this.animeId = animeId;
        this.title = title;
        this.animeUrl = animeUrl;
        this.posterUrl = posterUrl;
    }

    public String getTitle() {
        return this.title;
    }

    public String getAnimeUrl() {
        return this.animeUrl;
    }

    public int getAnimeId() {
        return this.animeId;
    }

    @Override
    public String toString() {
        return "BaseAnimeModel[title="
                + title
                + ", posterUrl="
                + posterUrl
                + ", animeUrl="
                + animeUrl
                + ", animeId="
                + animeId
                + "]";
    }

    public String getPosterUrl() {
        return this.posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }
}
