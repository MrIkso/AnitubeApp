package com.mrikso.anitube.app.parser;

import com.mrikso.anitube.app.utils.ParserUtils;

public class DleHashParser {
    private static final String DLE_HASH_PATTERN = "dle_login_hash = '([^']+)'";

    public static String getHash(String content) {

        String hash = ParserUtils.getMatcherResult(DLE_HASH_PATTERN, content, 1);
        // Log.i("DleHashParser", "" + hash);
        return hash;
    }
}
