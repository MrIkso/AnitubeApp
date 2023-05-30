package com.mrikso.anitube.app.model;

import androidx.annotation.ColorRes;

import com.mrikso.anitube.app.repository.ViewStatusAnime;

public class WatchAnimeStatusModel {
    private String status;

    @ColorRes
    private int color;

    private ViewStatusAnime viewStatus;
    private boolean selected;

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @ColorRes
    public int getColor() {
        return this.color;
    }

    public void setColor(@ColorRes int color) {
        this.color = color;
    }

    public ViewStatusAnime getViewStatus() {
        return this.viewStatus;
    }

    public void setViewStatus(ViewStatusAnime viewStatus) {
        this.viewStatus = viewStatus;
    }

    public boolean getSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
