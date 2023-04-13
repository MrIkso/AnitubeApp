package com.mrikso.anitube.app.model;

import java.util.List;

public class AnimeListReleases {
    private List<AnimeReleaseModel> animeReleases;
    private String currentPage;
    private String maxPage;

    public AnimeListReleases(List<AnimeReleaseModel> animeReleases) {
        this.animeReleases = animeReleases;
    }

    public List<AnimeReleaseModel> getAnimeReleases() {
        return this.animeReleases;
    }

    public String getCurrentPage() {
        return this.currentPage;
    }

    public void setCurrentPage(String currentPage) {
        this.currentPage = currentPage;
    }

    public String getMaxPage() {
        return this.maxPage;
    }

    public void setMaxPage(String maxPage) {
        this.maxPage = maxPage;
    }
}
