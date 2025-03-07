package com.mrikso.anitube.app.network.response;

import com.google.gson.annotations.SerializedName;

public class ErrorResponse {

    @SerializedName("code")
    private String code;

    @SerializedName("message")
    private String message;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}