package com.mrikso.anitube.app.extractors.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DomainsModel {
    @SerializedName("streamsb")
    private List<String> streamsb;

    @SerializedName("peertube")
    private List<String> peertube;

    public List<String> getStreamsb() {
        return streamsb;
    }

    public List<String> getPeertube() {
        return peertube;
    }
}
