package com.mrikso.anitube.app.extractors;

import androidx.core.util.Pair;

import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.VideoLinksModel;

import io.reactivex.rxjava3.core.Single;

public class VeohVideodExtractor extends BaseVideoLinkExtracror {

    public VeohVideodExtractor(String url) {
        super(url);
    }

    @Override
    public Single<Pair<LoadState, VideoLinksModel>> parse() {
        return Single.fromCallable(() -> {
            VideoLinksModel model = new VideoLinksModel(getUrl());
            model.setSingleDirectUrl(getUrl());
            return new Pair<>(LoadState.DONE, model);
        });
    }
}
