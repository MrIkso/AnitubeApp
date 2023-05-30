package com.mrikso.anitube.app.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.app.ShareCompat;
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

    public static void shareLink(Context context, String url) {
        new ShareCompat.IntentBuilder(context)
                .setType("text/plain")
                .setText(url)
                .startChooser();
    }

    public static void openWitch(Context context, String content, String mimeType) {
        new ShareCompat.IntentBuilder(context)
                .setType("text/plain")
                .setText(content)
                .startChooser();
    }
}
