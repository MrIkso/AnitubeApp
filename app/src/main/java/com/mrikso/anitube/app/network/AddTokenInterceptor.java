package com.mrikso.anitube.app.network;

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
        Request.Builder builder = chain.request().newBuilder();

        if (PreferencesHelper.getInstance().isLogginedToHikka()) {
            String authToken = PreferencesHelper.getInstance().getHikkaToken();
            builder.addHeader("auth", authToken);
        }
        return chain.proceed(builder.build());
    }
}
