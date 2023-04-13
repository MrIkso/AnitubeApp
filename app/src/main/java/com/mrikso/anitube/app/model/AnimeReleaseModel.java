package com.mrikso.anitube.app.model;

import android.os.Parcelable;
import java.io.Serializable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "animes")
public class AnimeReleaseModel {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String description;
    private SimpleModel releaseYear = new SimpleModel(null, null);
    private String status; // на майбутнє
    private String posterUrl;
    private String animeUrl;
    private String episodes;
    private String rating;
    private int animeId;

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPosterUrl() {
        return this.posterUrl;
    }

    public void setPosterUrl(String imageUrl) {
        this.posterUrl = imageUrl;
    }

    public String getEpisodes() {
        return this.episodes;
    }

    public void setEpisodes(String episodes) {
        this.episodes = episodes;
    }

    public String getAnimeUrl() {
        return this.animeUrl;
    }

    public void setAnimeUrl(String animeUrl) {
        this.animeUrl = animeUrl;
    }

    public String getRating() {
        return this.rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "AnimeReleaseModel[id="
                + id
                + ", title="
                + title
                + ", description="
                + description
                + ", releaseYear="
                + releaseYear.toString()
                + ", status="
                + status
                + ", posterUrl="
                + posterUrl
                + ", animeUrl="
                + animeUrl
                + ", episodes="
                + episodes
                + ", rating="
                + rating
                + "]";
    }

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
}
