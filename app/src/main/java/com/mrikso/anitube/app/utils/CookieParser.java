package com.mrikso.anitube.app.utils;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CookieParser {

    private final HashMap<String, String> cookiesMap;

    public CookieParser(List<String> cookies) {
        cookiesMap = new HashMap<>();
        String cookie;
        String[] cookieValue;
        for (String rawCookie : cookies) {
            cookie = rawCookie.substring(0, rawCookie.indexOf(";"));
            cookieValue = cookie.split("=");
            if (cookieValue.length == 2) {
                cookiesMap.put(cookieValue[0], cookieValue[1]);
            }
        }
    }

    @Nullable
    public String getValue(String key) {
        return cookiesMap.get(key);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        for (Map.Entry<String, String> cookie : cookiesMap.entrySet()) {
            res.append(cookie.getKey()).append("=").append(cookie.getValue()).append("; ");
        }
        // res.delete(res.length() - 2, res.length() - 1);
        return res.toString();
    }

    public String toString(String... values) {
        StringBuilder res = new StringBuilder();
        for (String key : values) {
            res.append(key).append("=").append(cookiesMap.get(key)).append("; ");
        }
        return res.substring(0, res.length() - 2);
    }

    public HashMap<String, String> getCookiesMap() {
        return this.cookiesMap;
    }
}
