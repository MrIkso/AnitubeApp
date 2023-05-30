package com.mrikso.anitube.app.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    @Nullable
    public static String guessFileExtension(@NonNull String filename) {
        int lastIndex = filename.lastIndexOf('.') + 1;
        if (lastIndex > 0 && filename.length() > lastIndex) {
            return filename.substring(lastIndex);
        }
        return null;
    }
}
