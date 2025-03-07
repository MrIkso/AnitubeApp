package com.mrikso.anitube.app.network;

import android.util.Log;

import androidx.annotation.NonNull;

import com.mrikso.anitube.app.utils.PreferencesHelper;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AddTokenInterceptor implements Interceptor {

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder requestBuilder = chain.request().newBuilder();
        PreferencesHelper helper = PreferencesHelper.getInstance();
        if (helper.isLogginedToHikka()) {
            String authToken = helper.getHikkaToken();
            Log.v("TAG", "added auth token: " + authToken);
            requestBuilder.addHeader("auth", authToken);
        }
        requestBuilder.header("Content-Type", "application/json");
        requestBuilder.header("Accept", "application/json");
        return chain.proceed(requestBuilder.build());
    }
}
