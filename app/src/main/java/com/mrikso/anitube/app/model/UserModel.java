package com.mrikso.anitube.app.model;

import com.google.common.base.Objects;

import java.io.Serializable;

public class UserModel implements Serializable {

    private final String userName;
    private final String userUrl;
    private final String userAvatar;

    public String getUserName() {
        return userName;
    }

    public UserModel(String userName, String userUrl, String userAvatar) {
        this.userName = userName;
        this.userUrl = userUrl;
        this.userAvatar = userAvatar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserModel userModel = (UserModel) o;
        return Objects.equal(userName, userModel.userName) && Objects.equal(userUrl, userModel.userUrl) && Objects.equal(userAvatar, userModel.userAvatar);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userName, userUrl, userAvatar);
    }

    public String getUserUrl() {
        return userUrl;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

}
