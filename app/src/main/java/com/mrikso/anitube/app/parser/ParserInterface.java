package com.mrikso.anitube.app.parser;

import androidx.core.util.Pair;

import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.VideoLinksModel;

import io.reactivex.rxjava3.core.Single;

public interface ParserInterface {
    public Single<Pair<LoadState, VideoLinksModel>> parse();
}
