package com.mrikso.anitube.app.network.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TokenRequest {
    @SerializedName("request_reference")
    @Expose
    private String requestReference;
    @SerializedName("client_secret")
    @Expose
    private String clientSecret;

    public TokenRequest(String clientSecret, String requestReference) {
        this.clientSecret = clientSecret;
        this.requestReference = requestReference;
    }

    public String getRequestReference() {
        return requestReference;
    }

    public String getClientSecret() {
        return clientSecret;
    }


}
