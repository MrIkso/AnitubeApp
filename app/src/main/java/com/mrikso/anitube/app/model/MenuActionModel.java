package com.mrikso.anitube.app.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.StringRes;

import com.google.common.base.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuActionModel that = (MenuActionModel) o;
        return id == that.id && name == that.name && icon == that.icon;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, name, icon);
    }
}
