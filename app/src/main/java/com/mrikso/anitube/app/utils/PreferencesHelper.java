package com.mrikso.anitube.app.utils;

import android.content.SharedPreferences;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.mrikso.anitube.app.App;

import java.util.Set;

public class PreferencesHelper {
    public static PreferencesHelper instance;
    final SharedPreferences preferences;

    public static PreferencesHelper getInstance() {
        if (instance == null) {
            instance = new PreferencesHelper();
        }
        return instance;
    }

    private PreferencesHelper() {
        preferences = PreferenceManager.getDefaultSharedPreferences(
                App.getApplication().getApplicationContext());
    }

    public void saveCooikes(Set<String> cookies) {
        preferences
                .edit()
                .putStringSet(PreferenceKeys.PREF_KEY_COOKIES, cookies)
                .apply();
    }

    public Set<String> getCooikes() {
        return preferences.getStringSet(PreferenceKeys.PREF_KEY_COOKIES, null);
    }

    public boolean isLogin() {
        return preferences.getBoolean(PreferenceKeys.PREF_KEY_IS_LOGIN, false);
    }

    public void setLogin(boolean isLogin) {
        preferences.edit().putBoolean(PreferenceKeys.PREF_KEY_IS_LOGIN, isLogin).apply();
    }

    public boolean isReverseEpisodeList() {
        return preferences.getBoolean(PreferenceKeys.PREF_KEY_REVERSE_EPISODE_LIST, false);
    }

    public void setReverseEpisodeList(boolean isReverse) {
        preferences
                .edit()
                .putBoolean(PreferenceKeys.PREF_KEY_REVERSE_EPISODE_LIST, isReverse)
                .apply();
    }

    public boolean isDynamicColorsEnabled() {
        return preferences.getBoolean(PreferenceKeys.PREF_KEY_DYNAMIC_COLORS, false);
    }

    public void setDynamicColorsEnabled(boolean enable) {
        preferences
                .edit()
                .putBoolean(PreferenceKeys.PREF_KEY_DYNAMIC_COLORS, enable)
                .apply();
    }

    public String getThemeMode() {
        return preferences.getString(PreferenceKeys.PREF_KEY_THEME, "follow_system");
    }

    public void setThemeMode(String themeMode) {
        preferences.edit().putString(PreferenceKeys.PREF_KEY_THEME, themeMode).apply();
    }

    public String getDleHash() {
        return preferences.getString(PreferenceKeys.PREF_KEY_DLE_HASH, "");
    }

    public void setDleHash(String hash) {
        preferences.edit().putString(PreferenceKeys.PREF_KEY_DLE_HASH, hash).apply();
    }

    public void applyThemeMode(String mode) {
        switch (mode) {
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "follow_system":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                }
                break;
        }
    }
}
