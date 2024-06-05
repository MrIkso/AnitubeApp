package com.mrikso.anitube.app.model;

import androidx.annotation.StringRes;

import com.google.common.base.Objects;

public class ActionModel {
    private final int type;

    @StringRes
    private final int name;

    private final String bgUrl;

    public ActionModel(int type, @StringRes int name, String bgUrl) {
        this.type = type;
        this.name = name;
        this.bgUrl = bgUrl;
    }

    public int getType() {
        return this.type;
    }

    @StringRes
    public int getName() {
        return this.name;
    }

    public String getBgUrl() {
        return this.bgUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActionModel that = (ActionModel) o;
        return type == that.type && name == that.name && Objects.equal(bgUrl, that.bgUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type, name, bgUrl);
    }
}
