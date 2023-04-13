package com.mrikso.anitube.app;

import android.app.Application;
import android.content.Context;

import com.google.android.material.color.DynamicColors;
import com.itsaky.androidide.logsender.LogSender;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class App extends Application {

    @Override
    protected void attachBaseContext(Context arg0) {
        super.attachBaseContext(arg0);
        LogSender.startLogging(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
		if (DynamicColors.isDynamicColorAvailable()) {
            DynamicColors.applyToActivitiesIfAvailable(this);
        }
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
    }
}
