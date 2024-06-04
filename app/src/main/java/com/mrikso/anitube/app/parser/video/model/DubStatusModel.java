package com.mrikso.anitube.app.parser.video.model;

import androidx.annotation.NonNull;

import com.mrikso.anitube.app.model.BaseModel;

import java.util.List;

public class DubStatusModel extends BaseModel {

    private List<VoicerModel> voicers;

    public DubStatusModel(String id, String name, List<VoicerModel> voicers) {
        super(id, name);
        this.voicers = voicers;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + " DubStatusModel[voicers=" + voicers + "]";
    }

    public List<VoicerModel> getVoicers() {
        return this.voicers;
    }
}
