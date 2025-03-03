package com.mrikso.anitube.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

import java.util.HashSet;
import java.util.Set;

public class PreferenceUtils {

    private PreferenceUtils() {
        // utility class
    }

    public static void setOnPreferenceChangeListener(@Nullable final Preference preference, @Nullable final Preference.OnPreferenceChangeListener changeListener) {
        if (preference != null) {
            preference.setOnPreferenceChangeListener(changeListener);
        }
    }

    public static void setOnPreferenceClickListener(@Nullable final Preference preference, @Nullable final Preference.OnPreferenceClickListener clickListener) {
        if (preference != null) {
            preference.setOnPreferenceClickListener(clickListener);
        }
    }

    public static void setEnabled(@Nullable final Preference preference, final boolean enabled) {
        if (preference != null) {
            preference.setEnabled(enabled);
        }
    }

    public static void setSummary(@Nullable final Preference preference, @Nullable final CharSequence summary) {
        if (preference != null) {
            preference.setSummary(summary);
        }
    }

    public static String getPrefString(Context context, String key, final String defaultValue) {
        final SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return settings.getString(key, defaultValue);
    }

    public static Set<String> getPrefStringSet(Context context, String key, final Set<String> defaultValue) {
        final SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return settings.getStringSet(key, defaultValue);
    }

    public static void setPrefStringSet(Context context, String key, final HashSet<String> value) {
        final SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        settings.edit().putStringSet(key, value).commit();
    }

    public static void setPrefString(Context context, final String key, final String value) {
        final SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        settings.edit().putString(key, value).commit();
    }

    public static boolean getPrefBoolean(Context context, final String key, final boolean defaultValue) {
        final SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return settings.getBoolean(key, defaultValue);
    }

    public static boolean hasKey(Context context, final String key) {
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
                .contains(key);
    }

    public static void setPrefBoolean(Context context, final String key, final boolean value) {
        final SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        settings.edit().putBoolean(key, value).commit();
    }

    public static void setPrefInt(Context context, final String key, final int value) {
        final SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        settings.edit().putInt(key, value).commit();
    }

    public static int getPrefInt(Context context, final String key, final int defaultValue) {
        final SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return settings.getInt(key, defaultValue);
    }

    public static void setPrefFloat(Context context, final String key, final float value) {
        final SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        settings.edit().putFloat(key, value).commit();
    }

    public static float getPrefFloat(Context context, final String key, final float defaultValue) {
        final SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return settings.getFloat(key, defaultValue);
    }

    public static void setSettingLong(Context context, final String key, final long value) {
        final SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        settings.edit().putLong(key, value).commit();
    }

    public static long getPrefLong(Context context, final String key, final long defaultValue) {
        final SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return settings.getLong(key, defaultValue);
    }

    public static void clearPreference(Context context, final SharedPreferences p) {
        final SharedPreferences.Editor editor = p.edit();
        editor.clear();
        editor.commit();
    }
}
