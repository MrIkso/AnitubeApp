package com.mrikso.anitube.app;

import android.app.Application;

import com.google.android.material.color.DynamicColors;
import com.mrikso.anitube.app.utils.PreferencesHelper;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class App extends Application {
    private static App instance;
    private PreferencesHelper preferenceHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        preferenceHelper = PreferencesHelper.getInstance();
        if (preferenceHelper.isDynamicColorsEnabled() && DynamicColors.isDynamicColorAvailable()) {
            DynamicColors.applyToActivitiesIfAvailable(this);
        }

        preferenceHelper.applyThemeMode(preferenceHelper.getThemeMode());

        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
    }

    public static App getApplication() {
        return instance;
    }

    public PreferencesHelper getPreferenceHelper() {
        return this.preferenceHelper;
    }
	
}
