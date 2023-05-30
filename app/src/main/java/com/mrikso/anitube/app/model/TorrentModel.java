package com.mrikso.anitube.app.model;

import java.io.Serializable;

public class TorrentModel implements Serializable {
    String name;
    String size;
    int seeds;
    int leechers;
    int downloadedCount;
    String torrentUrl;
    String magnetUrl;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return this.size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public int getSeeds() {
        return this.seeds;
    }

    public void setSeeds(int seeds) {
        this.seeds = seeds;
    }

    public int getLeechers() {
        return this.leechers;
    }

    public void setLeechers(int leechers) {
        this.leechers = leechers;
    }

    public String getMagnetUrl() {
        return this.magnetUrl;
    }

    public void setMagnetUrl(String magnetUrl) {
        this.magnetUrl = magnetUrl;
    }

    public int getDownloadedCount() {
        return this.downloadedCount;
    }

    public void setDownloadedCount(int downloadedCount) {
        this.downloadedCount = downloadedCount;
    }

    public String getTorrentUrl() {
        return this.torrentUrl;
    }

    public void setTorrentUrl(String torrentUrl) {
        this.torrentUrl = torrentUrl;
    }
}
