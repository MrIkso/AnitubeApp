package com.mrikso.anitube.app.model;

import com.google.gson.annotations.SerializedName;

public class CommentsResponse {

    @SerializedName("navigation")
    private String navigation;

    @SerializedName("comments")
    private String comments;

    public String getNavigation() {
        return navigation;
    }

    public String getComments() {
        return comments;
    }
}
