package com.mrikso.anitube.app.utils;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.network.ApiClient;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.Map;

public class DownloadUtils {

    public static void downloadFile(Activity context, String url) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        String fileExtenstion = MimeTypeMap.getFileExtensionFromUrl(url);
        CookieParser cookieParser =
                new CookieParser(new ArrayList<>(PreferencesHelper.getInstance().getCooikes()));

        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        for (Map.Entry<String, String> cookie : cookieParser.getCookiesMap().entrySet()) {
            request.addRequestHeader(cookie.getKey(), cookie.getValue());
        }

        // We must have long pressed on a link or image to download it. We
        // are not sure of the mimetype in this case, so do a head request
        final Disposable disposable = new FetchUrlMimeType(
                        downloadManager, request, url, cookieParser.toString(), ApiClient.DESKTOP_USER_AGENT)
                .create()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    switch (result) {
                        case FAILURE_ENQUEUE:
                            ViewUtils.showSnackbar(context, R.string.cannot_download);
                            break;
                        case FAILURE_LOCATION:
                            ViewUtils.showSnackbar(context, R.string.problem_location_download);
                            break;
                        case SUCCESS:
                            ViewUtils.showSnackbar(context, R.string.download_pending);
                            break;
                    }
                });
    }
}
