package com.mrikso.anitube.app.model;

import java.util.List;

public class AnimeListReleases {
    private List<AnimeReleaseModel> animeReleases;
    private int currentPage;
    private int maxPage;

    public AnimeListReleases(List<AnimeReleaseModel> animeReleases) {
        this.animeReleases = animeReleases;
    }

    public List<AnimeReleaseModel> getAnimeReleases() {
        return this.animeReleases;
    }

    public int getCurrentPage() {
        return this.currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getMaxPage() {
        return this.maxPage;
    }

    public void setMaxPage(int maxPage) {
        this.maxPage = maxPage;
    }

    public boolean isEndOfPage() {
        return maxPage == currentPage;
    }
}
