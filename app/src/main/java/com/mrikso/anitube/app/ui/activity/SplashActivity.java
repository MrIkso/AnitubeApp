package com.mrikso.anitube.app.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.mrikso.anitube.app.BuildConfig;
import com.mrikso.anitube.app.databinding.ActivitySplashBinding;
import com.mrikso.anitube.app.network.ApiClient;
import com.mrikso.anitube.app.ui.MainActivity;
import com.mrikso.anitube.app.utils.PreferencesHelper;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SplashActivity extends AppCompatActivity {

    private final Object mutex = new Object();
    private boolean mHasLoaded;
    ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        splashScreen.setKeepOnScreenCondition(() -> true);

        /* Use a WebView to bypass cloudflare challenge (Retrieve cookie) */
        // any api url is fine :|

        binding = ActivitySplashBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        WebView webView = binding.webView;
        // new WebView(this);

        // uncomment this part to simulate no-cookie state, for debugging
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();

        WebSettings webSettings = webView.getSettings();
        //  webSettings.setBuiltInZoomControls(true);
        // webSettings.setDisplayZoomControls(false);

        // webView.getSettings().setUserAgentString(ApiClient.MOBILE_USER_AGENT);

        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);

        if (BuildConfig.DEBUG) webView.setWebContentsDebuggingEnabled(true);

        webView.loadUrl(ApiClient.BASE_URL);

        // saveCookie();
        // Intent refreshCookieIntent = new Intent(SplashActivity.this, MainActivity.class);
        // startActivity(refreshCookieIntent);
        // finish();

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                saveCookie();
            }
        });
    }

    private void saveCookie() {
        Handler handler = new Handler();
        handler.postDelayed(
                () -> {
                    checkCookie();
                },
                5 * 1000); // 2 seconds
    }

    private void checkCookie() {
        // handle Cloudflare challenge
        String cookies = CookieManager.getInstance().getCookie(ApiClient.BASE_URL);
        // Log.d("SplashActivitiy", "url: " + url);
        Log.d("SplashActivitiy", "Got cookie: " + cookies);

        if (cookies == null || !cookies.contains("cf_clearance=")) {
            Log.e("SplashActivitiy", "Not found required cookie: cf_clearance, I think API call won't work");
            //  closeActivity();
            saveCookie();
        } else {
            String[] parts = cookies.split(";");
            PreferencesHelper.getInstance().saveCooikes(Arrays.stream(parts).collect(Collectors.toSet()));

            Intent refreshCookieIntent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(refreshCookieIntent);
            finish();
        }
    }

    private void closeActivity() {
        Toast.makeText(getApplicationContext(), "Failed to bypass human checking, use manual mode", Toast.LENGTH_LONG)
                .show();
        finish();
    }
}
