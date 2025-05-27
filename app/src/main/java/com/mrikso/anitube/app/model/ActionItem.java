package com.mrikso.anitube.app.model;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.google.common.base.Objects;

public class ActionItem {
    private final ID id;
    @DrawableRes
    private int defaultIconResId;
    @StringRes
    private int defaultTextResId;
    private boolean isVisible;
    @DrawableRes
    private int currentIconResId;
    @ColorRes
    private int currentIconColor;
    private String currentDisplayText;

    public enum ID {
        BOOKMARK,
        STATUS,
        SHARE,
        TORRENT,
        COMMENT,
        OPEN_ON_HIKKA
    }

    public ActionItem(ID id, @DrawableRes int defaultIconResId, @StringRes int defaultTextResId, boolean isVisible) {
        this.id = id;
        this.defaultIconResId = defaultIconResId;
        this.defaultTextResId = defaultTextResId;
        this.isVisible = isVisible;
        this.currentIconResId = defaultIconResId;
        this.currentIconColor = 0;
        this.currentDisplayText = null;
    }

    public ID getId() {
        return id;
    }

    public int getDefaultIconResId() {
        return defaultIconResId;
    }

    @StringRes
    public int getDefaultTextResId() {
        return defaultTextResId;
    }

    public boolean isVisible() {
        return isVisible;
    }

    @DrawableRes
    public int getCurrentIconResId() {
        return currentIconResId;
    }

    @ColorRes
    public int getCurrentIconColor() {
        return currentIconColor;
    }

    public String getCurrentDisplayText() {
        return currentDisplayText;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public void setCurrentIconResId(@DrawableRes int currentIconResId) {
        this.currentIconResId = currentIconResId;
    }

    public void setCurrentIconColor(@ColorRes int currentIconColor) {
        this.currentIconColor = currentIconColor;
    }

    public void setCurrentDisplayText(String currentDisplayText) {
        this.currentDisplayText = currentDisplayText;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ActionItem that = (ActionItem) o;
        return defaultIconResId == that.defaultIconResId && defaultTextResId == that.defaultTextResId && isVisible == that.isVisible && currentIconResId == that.currentIconResId && currentIconColor == that.currentIconColor && Objects.equal(id, that.id) && Objects.equal(currentDisplayText, that.currentDisplayText);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, defaultIconResId, defaultTextResId, isVisible, currentIconResId, currentIconColor, currentDisplayText);
    }
}