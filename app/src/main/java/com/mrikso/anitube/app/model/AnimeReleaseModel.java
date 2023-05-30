package com.mrikso.anitube.app.model;

import java.io.Serializable;

public class AnimeReleaseModel extends BaseAnimeModel implements Serializable {
    private String description;
    private SimpleModel releaseYear = new SimpleModel(null, null);
    private WatchAnimeStatusModel watchStatusModdel;
    private String episodes;
    private String rating;
    private boolean isFavorites;

    public AnimeReleaseModel(int animeId, String title, String animeUrl) {
        super(animeId, title, animeUrl);
    }

    public AnimeReleaseModel(int animeId, String title, String posterUrl, String animeUrl) {
        super(animeId, title, posterUrl, animeUrl);
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SimpleModel getReleaseYear() {
        return this.releaseYear;
    }

    public void setReleaseYear(SimpleModel releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getEpisodes() {
        return this.episodes;
    }

    public void setEpisodes(String episodes) {
        this.episodes = episodes;
    }

    public String getRating() {
        return this.rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "AnimeReleaseModel[ description="
                + description
                + ", releaseYear="
                + releaseYear.toString()
                + ", episodes="
                + episodes
                + ", rating="
                + rating
                + "]";
    }

    public boolean isFavorites() {
        return this.isFavorites;
    }

    public void setFavorites(boolean isFavorites) {
        this.isFavorites = isFavorites;
    }

    public WatchAnimeStatusModel getWatchStatusModdel() {
        return this.watchStatusModdel;
    }

    public void setWatchStatusModdel(WatchAnimeStatusModel watchStatusModdel) {
        this.watchStatusModdel = watchStatusModdel;
    }
}
