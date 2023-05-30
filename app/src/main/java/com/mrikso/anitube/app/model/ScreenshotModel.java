package com.mrikso.anitube.app.model;

import java.io.Serializable;

public class ScreenshotModel implements Serializable {
    private final String previewUrl;
    private final String fullUrl;

    public ScreenshotModel(String previewUrl, String fullUrl) {
        this.previewUrl = previewUrl;
        this.fullUrl = fullUrl;
    }

    public String getPreviewUrl() {
        return this.previewUrl;
    }

    public String getFullUrl() {
        return this.fullUrl;
    }

    @Override
    public String toString() {
        return "ScreenshotModel[previewUrl=" + previewUrl + ", fullUrl=" + fullUrl + "]";
    }
}
