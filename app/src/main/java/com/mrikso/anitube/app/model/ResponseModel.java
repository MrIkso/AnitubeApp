package com.mrikso.anitube.app.model;

import com.google.gson.annotations.SerializedName;

public class ResponseModel {
    @SerializedName("success")
    private boolean success;

    @SerializedName("response")
    private String response;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public String getResponse() {
        return response;
    }

    public String getMessage() {
        return this.message;
    }
}
