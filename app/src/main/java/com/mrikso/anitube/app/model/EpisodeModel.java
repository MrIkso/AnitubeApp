package com.mrikso.anitube.app.model;

public class EpisodeModel extends BaseModel {
    private final String episodeUrl;

    public EpisodeModel(String name, String episodeUrl) {
        super(null, name);
        this.episodeUrl = episodeUrl;
    }

    public EpisodeModel(String id, String name, String episodeUrl) {
        super(id, name);
        this.episodeUrl = episodeUrl;
    }

    public String getEpisodeUrl() {
        return this.episodeUrl;
    }

    @Override
    public String toString() {
        return super.toString() + " EpisodeModel[episodeUrl=" + episodeUrl + "]";
    }
}
