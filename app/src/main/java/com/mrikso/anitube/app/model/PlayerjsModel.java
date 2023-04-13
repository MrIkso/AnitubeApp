package com.mrikso.anitube.app.model;

import com.google.gson.annotations.SerializedName;

public class PlayerjsModel {

    @SerializedName("id")
    private String id;

    @SerializedName("file")
    private String file;

    @SerializedName("poster")
    private String poster;

    @SerializedName("subtitle")
    private String subtitle;

    @SerializedName("forbidden_quality")
    private String forbiddenQuality;

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
