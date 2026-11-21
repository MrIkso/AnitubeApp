package com.mrikso.anitube.app.utils;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import io.reactivex.rxjava3.core.Observable;

/**
 * This class is used to pull down the http headers of a given URL so that we can analyse the
 * mimetype and make any correction needed before we give the URL to the download manager. This
 * operation is needed when the user long-clicks on a link or image and we don't know the mimetype.
 * If the user just clicks on the link, we will do the same steps of correcting the mimetype down in
 * android.os.webkit.LoadListener rather than handling it here.
 */
public class FetchUrlMimeType {

    private static final String TAG = "FetchUrlMimeType";

    private final Context mContext;
    private final DownloadManager.Request mRequest;
    private final DownloadManager mDownloadManager;
    private final String mUri;
    private final String mCookies;
    private final String mUserAgent;

    public FetchUrlMimeType(
            @NonNull Context context,
            @NonNull DownloadManager downloadManager,
            @NonNull DownloadManager.Request request,
            String uri,
            String cookies,
            String userAgent) {
        mContext = context.getApplicationContext();
        mRequest = request;
        mDownloadManager = downloadManager;
        mUri = uri;
        mCookies = cookies;
        mUserAgent = userAgent;
    }

    public Observable<Result> create() {
        return Observable.create(emitter -> {
            // User agent is likely to be null, though the AndroidHttpClient
            // seems ok with that.
            String mimeType = null;
            String contentDisposition = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(mUri);
                connection = (HttpURLConnection) url.openConnection();
                if (mCookies != null && !mCookies.isEmpty()) {
                    connection.addRequestProperty("Cookie", mCookies);
                    connection.setRequestProperty("User-Agent", mUserAgent);
                }
                connection.connect();
                // We could get a redirect here, but if we do lets let
                // the download manager take care of it, and thus trust that
                // the server sends the right mimetype
                if (connection.getResponseCode() == 200) {
                    String header = connection.getHeaderField("Content-Type");
                    if (header != null) {
                        mimeType = header;
                        final int semicolonIndex = mimeType.indexOf(';');
                        if (semicolonIndex != -1) {
                            mimeType = mimeType.substring(0, semicolonIndex);
                        }
                    }
                    String contentDispositionHeader = connection.getHeaderField("Content-Disposition");
                    if (contentDispositionHeader != null) {
                        contentDisposition = contentDispositionHeader;
                    }
                }
            } catch (@NonNull IllegalArgumentException | IOException ex) {
                Log.e(TAG, "Error fetching headers", ex);
            } finally {
                if (connection != null) connection.disconnect();
            }

            if (mimeType != null) {
                if (mimeType.equalsIgnoreCase("text/plain") || mimeType.equalsIgnoreCase("application/octet-stream")) {
                    String newMimeType =
                            MimeTypeMap.getSingleton().getMimeTypeFromExtension(StringUtils.guessFileExtension(mUri));
                    if (newMimeType != null) {
                        mRequest.setMimeType(newMimeType);
                    }
                }
                final String filename = URLUtil.guessFileName(mUri, contentDisposition, mimeType);
                mRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
            }

            // Start the download
            long downloadId;
            try {
                downloadId = mDownloadManager.enqueue(mRequest);
                emitter.onNext(Result.SUCCESS);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Unable to enqueue request", e);
                emitter.onNext(Result.FAILURE_ENQUEUE);
                emitter.onComplete();
                return;
            } catch (SecurityException e) {
                emitter.onNext(Result.FAILURE_LOCATION);
                emitter.onComplete();
                return;
            }

            BroadcastReceiver onCompleteReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    Log.d(TAG, "Broadcast received for id: " + id + ", target: " + downloadId);

                    if (id == downloadId) {
                        DownloadManager.Query query = new DownloadManager.Query();
                        query.setFilterById(downloadId);

                        try (Cursor cursor = mDownloadManager.query(query)) {
                            if (cursor != null && cursor.moveToFirst()) {
                                int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                                if (columnIndex != -1) {
                                    int status = cursor.getInt(columnIndex);
                                    Log.d(TAG, "Download status: " + status);

                                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                        emitter.onNext(Result.COMPLETE);
                                    } else {
                                        emitter.onNext(Result.FAILURE_ENQUEUE);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to query download status", e);
                        } finally {
                            emitter.onComplete();
                            safeUnregisterReceiver(this);
                        }
                    }
                }
            };

            IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

            Handler mainHandler = new Handler(Looper.getMainLooper());
            ContextCompat.registerReceiver(mContext, onCompleteReceiver, filter, null, mainHandler, ContextCompat.RECEIVER_EXPORTED);
            emitter.setCancellable(() -> safeUnregisterReceiver(onCompleteReceiver));
        });
    }

    private void safeUnregisterReceiver(BroadcastReceiver receiver) {
        try {
            mContext.unregisterReceiver(receiver);
        } catch (IllegalArgumentException ignored) {
        }
    }

    public enum Result {
        FAILURE_ENQUEUE,
        FAILURE_LOCATION,
        SUCCESS,
        COMPLETE
    }
}