package com.mrikso.anitube.app.model;

import com.google.common.base.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScreenshotModel that = (ScreenshotModel) o;
        return Objects.equal(previewUrl, that.previewUrl) && Objects.equal(fullUrl, that.fullUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(previewUrl, fullUrl);
    }
}
