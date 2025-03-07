package com.mrikso.anitube.app.network.response;

import com.google.gson.annotations.SerializedName;

public class WatchAnimeResponse {
    @SerializedName("reference")
    private String reference;

    @SerializedName("anime")
    private Anime anime;

    @SerializedName("status")
    private String status;

    public String getReference() {
        return reference;
    }

    public Anime getAnime() {
        return anime;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "WatchAnimeResponse{" +
                "reference='" + reference + '\'' +
                ", anime=" + anime +
                ", status='" + status + '\'' +
                '}';
    }
}