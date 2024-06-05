package com.mrikso.anitube.app.model;

import com.google.common.base.Objects;

public class SimpleModel {
    private final String text;
    private final String url;

    public SimpleModel(String text, String url) {
        this.text = text;
        this.url = url;
    }

    public String getText() {
        return this.text;
    }

    public String getUrl() {
        return this.url;
    }

    @Override
    public String toString() {
        return "SimpleModel[text=" + text + ", url=" + url + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleModel that = (SimpleModel) o;
        return Objects.equal(text, that.text) && Objects.equal(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(text, url);
    }
}
