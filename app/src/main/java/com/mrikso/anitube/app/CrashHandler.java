package com.mrikso.anitube.app;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Process;

import androidx.core.content.ContextCompat;

import com.mrikso.anitube.app.ui.CrashActivity;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private String newLine = "\n";
    private StringBuilder errorMessage = new StringBuilder();
    private StringBuilder softwareInfo = new StringBuilder();
    private StringBuilder dateInfo = new StringBuilder();
    private final Context context;

    public CrashHandler(Context context) {
        this.context = context;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable exception) {
        var stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));

        errorMessage.append(stackTrace.toString());

        softwareInfo.append("SDK: ");
        softwareInfo.append(Build.VERSION.SDK_INT);
        softwareInfo.append(newLine);
        softwareInfo.append("Release: ");
        softwareInfo.append(Build.VERSION.RELEASE);
        softwareInfo.append(newLine);
        softwareInfo.append("Incremental: ");
        softwareInfo.append(Build.VERSION.INCREMENTAL);
        softwareInfo.append(newLine);

        dateInfo.append(Calendar.getInstance().getTime());
        dateInfo.append(newLine);

        // Log.e("Error", errorMessage.toString());
        // Log.d("Software", softwareInfo.toString());
        // Log.d("Date", dateInfo.toString());

        var intent = new Intent(context, CrashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("Error", errorMessage.toString());
        intent.putExtra("Software", softwareInfo.toString());
        intent.putExtra("Date", dateInfo.toString());

        ContextCompat.startActivity(context, intent, null);

        Process.killProcess(Process.myPid());
        System.exit(2);
    }
}
