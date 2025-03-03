package com.mrikso.anitube.app.network.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TokenResponse {
    @SerializedName("expiration")
    @Expose
    private int expiration;
    @SerializedName("created")
    @Expose
    private int created;
    @SerializedName("secret")
    @Expose
    private String secret;

    public int getExpiration() {
        return expiration;
    }

    public int getCreated() {
        return created;
    }

    public String getSecret() {
        return secret;
    }

    @Override
    public String toString() {
        return "TokenResponse{" +
                "expiration=" + expiration +
                ", created=" + created +
                ", secret='" + secret + '\'' +
                '}';
    }
}
