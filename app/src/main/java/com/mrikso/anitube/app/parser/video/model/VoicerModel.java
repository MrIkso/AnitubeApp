package com.mrikso.anitube.app.parser.video.model;

import com.mrikso.anitube.app.model.BaseModel;

import java.util.List;

public class VoicerModel extends BaseModel {
    private List<PlayerModel> players;
    private List<EpisodeModel> episodes;

    public VoicerModel(String id, String name) {
        super(id, name);
        this.episodes = null;
        this.players = null;
    }

    public List<PlayerModel> getPlayers() {
        return this.players;
    }

    public void setPlayers(List<PlayerModel> players) {
        this.players = players;
    }

    public List<EpisodeModel> getEpisodes() {
        return this.episodes;
    }

    public void setEpisodes(List<EpisodeModel> episodes) {
        this.episodes = episodes;
    }

    @Override
    public String toString() {
        return super.toString() + "VoicerModel[players=" + players + ", episodes=" + episodes + "]";
    }
}
