package com.mrikso.anitube.app.model;

import java.util.List;

public class DubStatusModel extends BaseModel {

    private List<VoicerModel> voicers;

    public DubStatusModel(String id, String name, List<VoicerModel> voicers) {
        super(id, name);
        this.voicers = voicers;
    }

    @Override
    public String toString() {
        return super.toString() + " DubStatusModel[voicers=" + voicers + "]";
    }

    public List<VoicerModel> getVoicers() {
        return this.voicers;
    }
}
