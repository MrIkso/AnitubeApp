package com.mrikso.anitube.app.network.response;

import com.google.gson.annotations.SerializedName;

public class SuccesResponse {

    @SerializedName("success")
    private boolean success;

    public boolean isSuccess() {
        return success;
    }
}