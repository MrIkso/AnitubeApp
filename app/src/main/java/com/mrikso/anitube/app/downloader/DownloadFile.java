package com.mrikso.anitube.app.downloader;

import static com.blankj.utilcode.util.MapUtils.isEmpty;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.mrikso.anitube.app.R;

import java.util.Map;


public class DownloadFile {

    public static void download(
            Context context,
            DownloaderMode downloaderMode,
            String link,
            String fileName,
            Map<String, String> headers,
            boolean batchMode) {

        boolean isDVGetInstalled = false;
        boolean isIDMInstalled = false;
        // adm - dvget
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        switch (downloaderMode) {
            case ADM:
                if (isPackageInstalled("com.dv.adm", context.getPackageManager())) {
                    intent.setClassName("com.dv.adm", "com.dv.adm.AEditor");
                    isDVGetInstalled = true;
                } else if (isPackageInstalled("com.dv.get", context.getPackageManager())) {
                    intent.setClassName("com.dv.get", "com.dv.get.AEditor");
                    isDVGetInstalled = true;
                } else if (isPackageInstalled("com.dv.adm.pay", context.getPackageManager())) {
                    intent.setClassName("com.dv.adm.pay", "com.dv.adm.pay.AEditor");
                    isDVGetInstalled = true;
                }

                if (isDVGetInstalled) {
                    if (batchMode) {
                        intent.putExtra("com.dv.get.ACTION_LIST_ADD", link);
                    } else {
                        intent.putExtra("android.intent.extra.TEXT", link);
                        intent.putExtra("com.android.extra.filename", fileName);
                    }
                    ContextCompat.startActivity(context, intent, null);
                } else {
                    Toast.makeText(
                                    context,
                                    context.getString(R.string.dvget_not_found),
                                    Toast.LENGTH_LONG)
                            .show();
                    String url = "https://play.google.com/store/apps/details?id=com.dv.adm";

                    openInBrowser(context, url);
                }
                break;

                // IDM
            case IDM:
                if (isPackageInstalled(
                        "idm.internet.download.manager.adm.lite", context.getPackageManager())) {
                    isIDMInstalled = true;
                    intent.setClassName(
                            "idm.internet.download.manager.adm.lite",
                            "idm.internet.download.manager.Downloader");
                } else if (isPackageInstalled(
                        "idm.internet.download.manager.plus", context.getPackageManager())) {
                    isIDMInstalled = true;
                    intent.setClassName(
                            "idm.internet.download.manager.plus",
                            "idm.internet.download.manager.Downloader");
                } else if (isPackageInstalled(
                        "idm.internet.download.manager", context.getPackageManager())) {
                    isIDMInstalled = true;
                    intent.setClassName(
                            "idm.internet.download.manager",
                            "idm.internet.download.manager.Downloader");
                }

                if (isIDMInstalled) {
                    if (!isEmpty(headers)) {
                        Bundle extra = new Bundle();
                        for (Map.Entry<String, String> entry : headers.entrySet())
                            extra.putString(entry.getKey(), entry.getValue());
                        intent.putExtra("extra_headers", extra);
                    }
                    intent.putExtra("secure_uri", link);
                    intent.setData(Uri.parse(link));
                    intent.putExtra("title", fileName);
                    intent.putExtra("filename", fileName);

                    ContextCompat.startActivity(context, intent, null);
                } else {
                    Toast.makeText(
                                    context,
                                    context.getString(R.string.idm_not_found),
                                    Toast.LENGTH_LONG)
                            .show();
                    String url =
                            "https://play.google.com/store/apps/details?id=idm.internet.download.manager";

                    openInBrowser(context, url);
                }
                break;

            case BROWSER:
                openInBrowser(context, link);
                break;
        }
    }

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

    private static boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            return packageManager.getApplicationInfo(packageName, 0).enabled;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
