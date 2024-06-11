package com.mrikso.anitube.app.downloader;

public enum DownloaderMode {
    ADM(0),
    IDM(1),
    BROWSER(2);

    int mode;

    private DownloaderMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }
}
