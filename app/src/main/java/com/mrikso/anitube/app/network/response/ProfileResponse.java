package com.mrikso.anitube.app.network.response;

import com.google.gson.annotations.SerializedName;

public class ProfileResponse {

    @SerializedName("reference")
    private String reference;

    @SerializedName("cover")
    private String cover;

    @SerializedName("role")
    private String role;

    @SerializedName("created")
    private int created;

    @SerializedName("description")
    private String description;

    @SerializedName("active")
    private boolean active;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("updated")
    private int updated;

    @SerializedName("email")
    private String email;

    @SerializedName("username")
    private String username;

    public String getReference() {
        return reference;
    }

    public String getCover() {
        return cover;
    }

    public String getRole() {
        return role;
    }

    public int getCreated() {
        return created;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }

    public String getAvatar() {
        return avatar;
    }

    public int getUpdated() {
        return updated;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }
}