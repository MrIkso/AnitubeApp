package com.mrikso.anitube.app.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;

import com.mrikso.anitube.app.network.ApiClient;

import java.util.ArrayList;
import java.util.Map;

import io.reactivex.rxjava3.core.Observable;

public class DownloadUtils {

    public static Observable<FetchUrlMimeType.Result> downloadFile(Context context, String url) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        CookieParser cookieParser =
                new CookieParser(new ArrayList<>(PreferencesHelper.getInstance().getCooikes()));

        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        for (Map.Entry<String, String> cookie : cookieParser.getCookiesMap().entrySet()) {
            request.addRequestHeader(cookie.getKey(), cookie.getValue());
        }

        return new FetchUrlMimeType(
                context,
                downloadManager,
                request,
                url,
                cookieParser.toString(),
                ApiClient.DESKTOP_USER_AGENT
        ).create();
    }
}