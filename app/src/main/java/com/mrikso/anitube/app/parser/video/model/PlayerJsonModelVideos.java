package com.mrikso.anitube.app.parser.video.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PlayerJsonModelVideos {

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("code")
    @Expose
    private String code;

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }
}
