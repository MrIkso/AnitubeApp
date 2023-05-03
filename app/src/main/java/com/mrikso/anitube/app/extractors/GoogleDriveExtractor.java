package com.mrikso.anitube.app.extractors;

import androidx.core.util.Pair;

import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.VideoLinksModel;
import com.mrikso.anitube.app.utils.ParserUtils;

import io.reactivex.rxjava3.core.Single;

public class GoogleDriveExtractor extends BaseVideoLinkExtracror {
    private final String TAG = "GoogleDriveExtractor";
    private final String ID_PATTERN = "\\/d\\/(.*?)\\/";

    public GoogleDriveExtractor(String ifRame) {
        super(ifRame);
    }

    @Override
    public Single<Pair<LoadState, VideoLinksModel>> parse() {
        return Single.fromCallable(
                () -> {
                    String id = ParserUtils.getMatcherResult(ID_PATTERN, getUrl(), 1);
                    String directUrl =
                            String.format("https://drive.google.com/uc?id=%s&export=download", id);
                    VideoLinksModel model = new VideoLinksModel(getUrl());
                    model.setSingleDirectUrl(directUrl);
                    return new Pair<>(LoadState.DONE, model);
                });
    }
}
