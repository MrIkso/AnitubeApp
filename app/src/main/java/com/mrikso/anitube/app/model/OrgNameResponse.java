package com.mrikso.anitube.app.model;

import com.google.gson.annotations.SerializedName;

public class OrgNameResponse {
    @SerializedName("nameorg")
    private String nameorg;

    public String getNameorg() {
        return nameorg;
    }

    public void setNameorg(String nameorg) {
        this.nameorg = nameorg;
    }
}
