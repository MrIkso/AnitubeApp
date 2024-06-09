package com.mrikso.anitube.app.ui.webview;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.mrikso.anitube.app.databinding.FragmentWebviewBinding;

public class WebviewFragment extends Fragment {
    private FragmentWebviewBinding binding;
    private WebView webView = null;

    public WebviewFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWebviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        initViews();
        initToolbar();
        loadPage();
    }

    private void initToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(requireView()).popBackStack());
    }

    private void initViews() {
        webView = binding.webView;
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                binding.linearIndicator.setIndeterminate(true);
                binding.linearIndicator.setVisibility(View.VISIBLE);
            }

            public void onPageFinished(WebView view, String url) {
                binding.linearIndicator.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        webView = null;
        binding = null;
    }

    @Override
    public void onPause() {
        if (webView != null) {
            webView.onPause();
            webView.pauseTimers();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (webView != null) {
            webView.resumeTimers();
            webView.onResume();
        }
    }

    @Override
    public void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }

    private void loadPage() {
        WebviewFragmentArgs args = WebviewFragmentArgs.fromBundle(getArguments());
        binding.toolbar.setTitle(args.getToolbarName());
        webView.loadUrl(args.getUrlPage());
    }

}
