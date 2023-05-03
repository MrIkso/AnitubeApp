package com.mrikso.anitube.app.utils;

public class StringUtils {
    public static String removeLastChar(String str) {
        return removeChars(str, 1);
    }

    public static String removeChars(String str, int numberOfCharactersToRemove) {
        if (str != null && !str.trim().isEmpty()) {
            return str.substring(0, str.length() - numberOfCharactersToRemove);
        }
        return "";
    }
}
