package com.mrikso.anitube.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class QickSearchResponse {
    @Expose
    @SerializedName("text")
    private String text;

    @Expose
    @SerializedName("error")
    private String error;

    @Expose
    @SerializedName("content")
    private String content;

    public String getContent() {
        return content;
    }

    public String getText() {
        return text;
    }

    public String getError() {
        return error;
    }
}
