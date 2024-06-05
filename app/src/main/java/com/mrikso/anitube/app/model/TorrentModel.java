package com.mrikso.anitube.app.model;

import com.google.common.base.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TorrentModel that = (TorrentModel) o;
        return seeds == that.seeds && leechers == that.leechers && downloadedCount == that.downloadedCount && Objects.equal(name, that.name) && Objects.equal(size, that.size) && Objects.equal(torrentUrl, that.torrentUrl) && Objects.equal(magnetUrl, that.magnetUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, size, seeds, leechers, downloadedCount, torrentUrl, magnetUrl);
    }
}
