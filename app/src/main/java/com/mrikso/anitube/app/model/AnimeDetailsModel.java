package com.mrikso.anitube.app.model;

import java.util.List;

public class AnimeDetailsModel extends AnimeReleaseModel {

    private String originalTitle;
    private List<ScreenshotModel> screenshotsModel;
    private ScreenshotModel trailerModel;
    private String studio;
    private String director;
    private String age;
    private List<SimpleModel> genres;
    private List<SimpleModel> translators;
    private List<SimpleModel> voicers;
    private List<DubbersTeam> dubbersTeamList;
    private List<BaseAnimeModel> similarAnimeList;

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

    @Override
    public String toString() {
        return super.toString()
                + " AnimeDetailsModel[originalTitle="
                + originalTitle
                + ", screenshotsModel="
                + screenshotsModel
                + ", trailerModel="
                + trailerModel
                + ", studio="
                + studio
                + ", director="
                + director
                + ", age="
                + age
                + ", genres="
                + genres
                + ", translators="
                + translators
                + ", voicers="
                + voicers
                + ", dubbersTeamList="
                + dubbersTeamList
                + ", similarAnimeList="
                + similarAnimeList
                + "]";
    }
}
