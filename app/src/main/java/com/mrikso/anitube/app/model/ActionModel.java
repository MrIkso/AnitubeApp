package com.mrikso.anitube.app.model;

import androidx.annotation.StringRes;

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
}
