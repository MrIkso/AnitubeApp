package com.mrikso.anitube.app.model;

import java.util.List;

public class AnimeDetailsModel extends AnimeReleaseModel {

    private String originalTitle;
    private List<ScreenshotModel> screenshotsModel;
    private ScreenshotModel trailerModel;
    private String studio;
    private String director;
    private String age;
    private String lastUpdateTime;
    private SimpleModel animeSeason;
    private List<SimpleModel> genres;
    private List<SimpleModel> translators;
    private List<SimpleModel> voicers;
    private List<DubbersTeam> dubbersTeamList;
    private List<FranchiseModel> franchiseList;
    private List<BaseAnimeModel> similarAnimeList;
    private boolean hasTorrent;
    private String torrentPageUrl;
    private boolean isHavePlaylistsAjax;

    public AnimeDetailsModel(int animeId, String title, String animeUrl) {
        super(animeId, title, animeUrl);
    }

    public AnimeDetailsModel(int animeId, String title, String posterUrl, String animeUrl) {
        super(animeId, title, posterUrl, animeUrl);
    }

    public String getOriginalTitle() {
        return this.originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public List<ScreenshotModel> getScreenshotsModel() {
        return this.screenshotsModel;
    }

    public void setScreenshotsModel(List<ScreenshotModel> screenshotsModel) {
        this.screenshotsModel = screenshotsModel;
    }

    public ScreenshotModel getTrailerModel() {
        return this.trailerModel;
    }

    public void setTrailerModel(ScreenshotModel trailerModel) {
        this.trailerModel = trailerModel;
    }

    public String getStudio() {
        return this.studio;
    }

    public void setStudio(String studio) {
        this.studio = studio;
    }

    public String getDirector() {
        return this.director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public List<SimpleModel> getTranslators() {
        return this.translators;
    }

    public void setTranslators(List<SimpleModel> translators) {
        this.translators = translators;
    }

    public List<SimpleModel> getGenres() {
        return this.genres;
    }

    public void setGenres(List<SimpleModel> genres) {
        this.genres = genres;
    }

    public List<BaseAnimeModel> getSimilarAnimeList() {
        return this.similarAnimeList;
    }

    public void setSimilarAnimeList(List<BaseAnimeModel> similarAnimeList) {
        this.similarAnimeList = similarAnimeList;
    }

    public List<SimpleModel> getVoicers() {
        return this.voicers;
    }

    public void setVoicers(List<SimpleModel> voicers) {
        this.voicers = voicers;
    }

    public String getAge() {
        return this.age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public List<DubbersTeam> getDubbersTeamList() {
        return this.dubbersTeamList;
    }

    public void setDubbersTeamList(List<DubbersTeam> dubbersTeamList) {
        this.dubbersTeamList = dubbersTeamList;
    }

    public List<FranchiseModel> getFranchiseList() {
        return this.franchiseList;
    }

    public void setFranchiseList(List<FranchiseModel> franchiseList) {
        this.franchiseList = franchiseList;
    }

    public boolean isHavePlaylistsAjax() {
        return this.isHavePlaylistsAjax;
    }

    public void setHavePlaylistsAjax(boolean isHavePlaylistsAjax) {
        this.isHavePlaylistsAjax = isHavePlaylistsAjax;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public SimpleModel getAnimeSeason() {
        return animeSeason;
    }

    public void setAnimeSeason(SimpleModel animeSeason) {
        this.animeSeason = animeSeason;
    }

    public boolean isHasTorrent() {
        return hasTorrent;
    }

    public void setHasTorrent(boolean hasTorrent) {
        this.hasTorrent = hasTorrent;
    }

    public void setTorrentPageUrl(String torrentPageUrl) {
        this.torrentPageUrl = torrentPageUrl;
    }

    public String getTorrentPageUrl() {
        return torrentPageUrl;
    }
}
