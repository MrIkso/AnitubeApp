package com.mrikso.anitube.app.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OkHttpUtils {
    private static OkHttpClient mOkHttpClient = null;

    private static OkHttpClient withOkHttpClient() {
        if (mOkHttpClient == null) {
            mOkHttpClient = new OkHttpClient();
        }
        return mOkHttpClient;
    }

    public static void downloadFile(final String fileUrl, final File file, final FileCallback fileCallback) {

        final Request request = new Request.Builder().url(fileUrl).build();
        final Call call = withOkHttpClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    InputStream is = null;
                    byte[] buf = new byte[1024];
                    int len = 0;
                    FileOutputStream fos = null;
                    try {
                        ResponseBody responseBody = response.body();
                        if (responseBody != null) {
                            is = responseBody.byteStream();
                            fos = new FileOutputStream(file);
                            while ((len = is.read(buf)) != -1) {
                                fos.write(buf, 0, len);
                            }
                            fos.flush();
                            fileCallback.success();
                        } else {
                            fileCallback.fail(-1, null);
                        }
                    } catch (IOException e) {
                        fileCallback.fail(-1, e);
                        e.printStackTrace();
                    } finally {
                        try {
                            if (is != null) {
                                is.close();
                            }
                            if (fos != null) {
                                fos.close();
                            }
                        } catch (IOException e) {
                            fileCallback.fail(-1, e);
                            e.printStackTrace();
                        }
                    }
                } else {
                    fileCallback.fail(response.code(), null);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                fileCallback.fail(-1, e);
            }
        });
    }

    public interface FileCallback {
        void success();

        void fail(int code, Exception e);
    }

    public static Map<String, String> headersToMap(Headers headers) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            String name = headers.name(i);
            String value = headers.value(i);
            map.put(name, value);
        }
        return map;
    }

    private static boolean hasNetworkPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context != null
                    && PackageManager.PERMISSION_GRANTED
                    == context.checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE)
                    && PackageManager.PERMISSION_GRANTED == context.checkSelfPermission(Manifest.permission.INTERNET);
        }
        return true;
    }
}
