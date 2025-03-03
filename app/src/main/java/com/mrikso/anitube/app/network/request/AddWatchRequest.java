package com.mrikso.anitube.app.network.request;

import com.google.gson.annotations.SerializedName;

public class AddWatchRequest {

    @SerializedName("note")
    private String note;

    @SerializedName("score")
    private int score;

    @SerializedName("rewatches")
    private int rewatches;

    @SerializedName("episodes")
    private int episodes;

    @SerializedName("status")
    private String status;

    public String getNote() {
        return note;
    }

    public int getScore() {
        return score;
    }

    public int getRewatches() {
        return rewatches;
    }

    public int getEpisodes() {
        return episodes;
    }

    public String getStatus() {
        return status;
    }
}