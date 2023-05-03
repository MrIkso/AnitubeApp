package com.mrikso.anitube.app.utils;

import android.content.SharedPreferences;

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
        preferences =
                PreferenceManager.getDefaultSharedPreferences(
                        App.getApplication().getApplicationContext());
    }

    public void saveCooikes(Set<String> cookies) {
        preferences.edit().putStringSet(PreferenceKeys.PREF_KEY_COOKIES, cookies).apply();
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

    public String getDleHash() {
        return preferences.getString(PreferenceKeys.PREF_KEY_DLE_HASH, "");
    }

    public void setDleHash(String hash) {
        preferences.edit().putString(PreferenceKeys.PREF_KEY_DLE_HASH, hash).apply();
    }
}
