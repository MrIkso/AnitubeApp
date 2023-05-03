package com.mrikso.anitube.app.extractors;

import androidx.core.util.Pair;

import com.google.gson.Gson;
import com.mrikso.anitube.app.extractors.model.PlayerJsResponse;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.VideoLinksModel;
import com.mrikso.anitube.app.utils.ParserUtils;

import io.reactivex.rxjava3.core.Single;

public class MoonAnimeArtExtractor extends BaseVideoLinkExtracror {

    private final String TAG = "MoonAnimeArtExtractor";
    private final String PLAYER_JS_PATTERN = "Playerjs\\(([^)]+)\\)";

    public MoonAnimeArtExtractor(String url) {
        super(url);
    }

    @Override
    public Single<Pair<LoadState, VideoLinksModel>> parse() {
        return Single.fromCallable(
                () -> {
                    extract();
                    Gson gson = new Gson();
                    String json =
                            ParserUtils.getMatcherResult(
                                    PLAYER_JS_PATTERN, getDocument().data(), 1);
                    int lastIndex = json.lastIndexOf(",");
                    if (lastIndex >= 0) {
                        json = json.substring(0, lastIndex) + "}";
                    }
                    // Log.i(TAG, json);
                    PlayerJsResponse playerJs = gson.fromJson(json, PlayerJsResponse.class);
                    VideoLinksModel model = new VideoLinksModel(url);
                    model.setSingleDirectUrl(playerJs.getFile());
                    return new Pair<>(LoadState.DONE, model);
                });
    }
}
