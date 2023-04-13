package com.mrikso.anitube.app.model;

public class BaseAnimeModel {
    public final String title;
    public final String imageUrl;
    public final String animeUrl;

    public BaseAnimeModel(String title, String imageUrl, String animeUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.animeUrl = animeUrl;
    }

    @Override
    public String toString() {
        return "BaseAnimeModel[title="
                + title
                + ", imageUrl="
                + imageUrl
                + ", animeUrl="
                + animeUrl
                + "]";
    }
}
