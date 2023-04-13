package com.mrikso.anitube.app.extractors;

import com.google.gson.Gson;
import com.mrikso.anitube.app.model.PlayerjsModel;
import com.mrikso.anitube.app.utils.ParserUtils;

import java.io.IOException;

public class AhsdiVideosExtractor extends BaseVideoLinkExtracror {
    private final String TAG = "AhsdiVideosExtractor";
    private final String PLAYER_JS_PATTERN = "Playerjs\\(([^)]+)\\)";

    public AhsdiVideosExtractor(String url) throws IOException {
        super(url);
    }

    @Override
    public String extract() {
        try {

            Gson gson = new Gson();
            //  String jsCode = getDocument().selectFirst("script").data();
            ///  Log.i(TAG, " " + getDocument().bo());
            String json = ParserUtils.getMatcherResult(PLAYER_JS_PATTERN, getDocument().data(), 1);
            //  Log.i(TAG, " " + json);
            PlayerjsModel playerJs = gson.fromJson(json, PlayerjsModel.class);
            return playerJs.getFile();
        } catch (Exception err) {
            err.printStackTrace();
        }
        return null;
    }
}
