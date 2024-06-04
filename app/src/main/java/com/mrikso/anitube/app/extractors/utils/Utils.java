package com.mrikso.anitube.app.extractors.utils;

import android.util.Base64;

import androidx.annotation.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static boolean startWithNumber(String string) {
        final String regex = "^[0-9][A-Za-z0-9-\\s,]*$"; // start with number and can contain space or comma (
        // 480p , ENG)
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(string);
        return matcher.find();
    }

    @Nullable
    public static String getDomainFromURL(String url) {
        String regex = "^(?:https?:\\/\\/)?(?:[^@\\n]+@)?(?:www\\.)?([^:\\/\\n?]+)";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }

    public static String B64Encode(String text) {
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        return Base64.encodeToString(data, Base64.DEFAULT).replace("=", "");
    }

    public static String getID(String data) {
        if (data.contains(".html")) {
            data = data.replace(".html", "");
            return data.substring(data.lastIndexOf("/") + 1);
        } else {
            return data.substring(data.lastIndexOf("/") + 1);
        }
    }
}
