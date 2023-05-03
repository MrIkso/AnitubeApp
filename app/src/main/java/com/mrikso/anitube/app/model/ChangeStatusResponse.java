package com.mrikso.anitube.app.model;

import com.google.gson.annotations.SerializedName;

public class ChangeStatusResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
