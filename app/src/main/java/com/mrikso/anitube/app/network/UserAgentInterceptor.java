package com.mrikso.anitube.app.network;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/** Adds a custom {@code User-Agent} header to OkHttp requests. */
public class UserAgentInterceptor implements Interceptor {

    public final String userAgent;

    public UserAgentInterceptor(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request userAgentRequest =
                chain.request().newBuilder().header("User-Agent", userAgent).build();
        return chain.proceed(userAgentRequest);
    }
}
