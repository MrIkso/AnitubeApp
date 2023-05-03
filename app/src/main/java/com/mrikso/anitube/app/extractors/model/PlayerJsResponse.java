package com.mrikso.anitube.app.extractors.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PlayerJsResponse {
    @Expose
    @SerializedName("id")
    private String id;

    @Expose
    @SerializedName("file")
    private String file;

    @Expose
    @SerializedName("poster")
    private String poster;

    @Expose
    @SerializedName("subtitle")
    private String subtitle;

    @Expose
    @SerializedName("forbidden_quality")
    private String forbiddenQuality;

    @Expose
    @SerializedName("default_quality")
    private String defaultQuality;

    public String getId() {
        return id;
    }

    public String getFile() {
        return file;
    }

    public String getPoster() {
        return poster;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getForbiddenQuality() {
        return forbiddenQuality;
    }

    public String getDefaultQuality() {
        return defaultQuality;
    }
}
