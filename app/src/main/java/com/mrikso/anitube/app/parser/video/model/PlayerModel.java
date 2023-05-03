package com.mrikso.anitube.app.parser.video.model;

import com.mrikso.anitube.app.model.BaseModel;

import java.io.Serializable;
import java.util.List;

public class PlayerModel extends BaseModel implements Serializable {
    private List<EpisodeModel> episodes;

    public PlayerModel(String id, String name) {
        super(id, name);
    }

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

    public void setEpisodes(List<EpisodeModel> episodes) {
        this.episodes = episodes;
    }
}
