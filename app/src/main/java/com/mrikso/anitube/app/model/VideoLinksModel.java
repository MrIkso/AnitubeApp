package com.mrikso.anitube.app.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class VideoLinksModel implements Serializable {
    private Map<String, String> linksQuality = new HashMap<>();
    private Map<String, String> httpHeaders = new HashMap<>();
    private final String ifRameUrl;
    private String singleDirectUrl;
    private String subtileUrl;
    private String defaultQuality;
    private boolean ignoreSSL;

    public VideoLinksModel(String ifRameUrl) {
        this.ifRameUrl = ifRameUrl;
    }

    public Map<String, String> getLinksQuality() {
        return this.linksQuality;
    }

    public void setLinksQuality(Map<String, String> linksQuality) {
        this.linksQuality = linksQuality;
    }

    public Map<String, String> getHeaders() {
        return this.httpHeaders;
    }

    public void setHeaders(Map<String, String> httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    public String getIfRameUrl() {
        return this.ifRameUrl;
    }

    public String getSingleDirectUrl() {
        return this.singleDirectUrl;
    }

    public void setSingleDirectUrl(String singleDirectUrl) {
        this.singleDirectUrl = singleDirectUrl;
    }

    public String getSubtileUrl() {
        return this.subtileUrl;
    }

    public void setSubtileUrl(String subtileUrl) {
        this.subtileUrl = subtileUrl;
    }

    public String getDefaultQuality() {
        return this.defaultQuality;
    }

    public void setDefaultQuality(String defaultQuality) {
        this.defaultQuality = defaultQuality;
    }

    public boolean isIgnoreSSL() {
        return this.ignoreSSL;
    }

    public void setIgnoreSSL(boolean ignoreSSL) {
        this.ignoreSSL = ignoreSSL;
    }
}
