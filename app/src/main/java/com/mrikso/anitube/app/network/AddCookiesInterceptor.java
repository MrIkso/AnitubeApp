package com.mrikso.anitube.app.network;

import android.util.Log;

import com.mrikso.anitube.app.utils.PreferencesHelper;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Set;

public class AddCookiesInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();

        if (PreferencesHelper.getInstance().isLogin()) {
            Set<String> preferences = PreferencesHelper.getInstance().getCooikes();
            for (String cookie : preferences) {
                builder.addHeader("Cookie", cookie);
                Log.v("OkHttp", "Adding Header: " + cookie); // This is done so I know which headers are being added;
                // this
                // interceptor is used after the normal logging of OkHttp
            }
        }
        return chain.proceed(builder.build());
    }
}
