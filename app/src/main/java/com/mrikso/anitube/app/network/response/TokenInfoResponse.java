package com.mrikso.anitube.app.network.response;

import com.google.gson.annotations.SerializedName;

public class TokenInfoResponse {

    @SerializedName("reference")
    private String reference;

    @SerializedName("created")
    private int created;

    @SerializedName("expiration")
    private int expiration;

    @SerializedName("used")
    private int used;

    public String getReference() {
        return reference;
    }

    public int getCreated() {
        return created;
    }

    public int getExpiration() {
        return expiration;
    }

    public int getUsed() {
        return used;
    }
}