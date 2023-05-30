package com.mrikso.anitube.app.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.StringRes;

public class MenuActionModel {
    @IdRes
    private final int id;

    @StringRes
    private final int name;

    @DrawableRes
    private final int icon;

    public MenuActionModel(int id, @StringRes int name, @DrawableRes int icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
    }

    @IdRes
    public int getId() {
        return this.id;
    }

    @StringRes
    public int getName() {
        return this.name;
    }

    @DrawableRes
    public int getIcon() {
        return this.icon;
    }
}
