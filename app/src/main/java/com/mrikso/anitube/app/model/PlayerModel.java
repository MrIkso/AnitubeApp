package com.mrikso.anitube.app.model;

import java.util.List;

public class PlayerModel extends BaseModel {
    private List<EpisodeModel> episodes;

    public PlayerModel(String id, String name, List<EpisodeModel> episodes) {
        super(id, name);
        this.episodes = episodes;
    }

    public List<EpisodeModel> getEpisodes() {
        return this.episodes;
    }

    @Override
    public String toString() {
        return super.toString() + " PlayerModel[episodes=" + episodes + "]";
    }
}
