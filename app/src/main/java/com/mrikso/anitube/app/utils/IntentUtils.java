package com.mrikso.anitube.app.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.ContextCompat;

public class IntentUtils {

    public static void openInBrowser(Context context, String link) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // browserIntent.addCategory(Intent.CATEGORY_APP_BROWSER);
        try {
            ContextCompat.startActivity(context, browserIntent, null);
            return;
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
