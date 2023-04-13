package com.mrikso.anitube.app.model;

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
}
