package com.mrikso.anitube.app;

import android.app.Application;

import com.google.android.material.color.DynamicColors;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class App extends Application {
    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        if (DynamicColors.isDynamicColorAvailable()) {
            DynamicColors.applyToActivitiesIfAvailable(this);
        }
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
    }

    public static App getApplication() {
        return instance;
    }
}
