package com.mrikso.anitube.app.ui.dialogs.captcha

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.annotation.Keep

import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mrikso.anitube.app.databinding.DialogCapchaResolverBottomSheetBinding
import com.mrikso.anitube.app.network.ApiClient
import java.io.InputStream

class CaptchaResolverFragment : BottomSheetDialogFragment() {
    private lateinit var binding: DialogCapchaResolverBottomSheetBinding
    private lateinit var callback: CaptchaResolvedCallback

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val sheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        try {
            sheetDialog.setOnShowListener {
                val bottomSheet = sheetDialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)!!
                val behavior = BottomSheetBehavior.from(bottomSheet)
                behavior.skipCollapsed = true
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        } catch (t: Throwable) {
            //Timber.d(t, "Hum...")
        }
        return sheetDialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return DialogCapchaResolverBottomSheetBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val inputStream: InputStream = requireContext().assets.open("captcha.html")
        val captchaHTML = inputStream.bufferedReader().use{it.readText()}
        binding.webView.apply {
            loadDataWithBaseURL(
                ApiClient.BASE_URL,
                captchaHTML.replace("#site_key#", ApiClient.SIGN_UP_URL),
                "text/html; charset=utf-8",
                "UTF-8",
                null
            )
            settings.apply {
                javaScriptEnabled = true
                userAgentString = ApiClient.DESKTOP_USER_AGENT
                loadWithOverviewMode = true
                // useWideViewPort = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
            }
            scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
            isScrollbarFadingEnabled = false
            addJavascriptInterface(this@CaptchaResolverFragment, "BridgeWebView")
        }
    }

    @Keep
    @JavascriptInterface
    fun reCaptchaCallback(token: String) {
        //Timber.d("reCaptcha token $token")
        dismiss()
        callback.onCaptchaResolved(token)
    }

    fun setCallback(callback: CaptchaResolvedCallback) {
        this.callback = callback
    }

    interface CaptchaResolvedCallback {
        fun onCaptchaResolved(token: String)
    }

    companion object {
        const val TAG = "CaptchaResolverFragment"
        private const val RECAPTCHA_HTML = "<html>\n" +
                "    <head>\n" +
                "      <title>reCAPTCHA</title>\n" +
                "      <meta content='width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=0' name='viewport' />\n" +
                "      <script src=\"https://www.google.com/recaptcha/api.js\" async defer></script>\n" +
                "<script type=\"text/javascript\">\n" +
                "    function captchaResponse(token){\n" +
                "        BridgeWebView.reCaptchaCallback(token);\n" +
                "    }\n" +
                "</script>" +
                "      <script>\n" +
                "        function onSubmit() {\n" +
                "          var response = grecaptcha.getResponse();\n" +
                "          console.log(response)\n" +
                "          window.postMessage(response);\n" +
                "        }\n" +
                "      </script>\n" +
                "      <script>\n" +
                "        window.onload = function (e) {\n" +
                "          grecaptcha.execute()\n" +
                "        }\n" +
                "      </script>\n" +
                "      <style>\n" +
                "        html, body {\n" +
                "          max-width: 100%;\n" +
                "          overflow-x: hidden;\n" +
                "        }\n" +
                "        .status-message {\n" +
                "          background-color: #00000000;\n" +
                "          margin-bottom: 10px;\n" +
                "          text-align: center;\n" +
                "        }\n" +
                "        textarea {\n" +
                "          margin: 10px 0;\n" +
                "          resize: none;\n" +
                "        }\n" +
                "        .g-recaptcha {\n" +
                "          transform:scale(1.2);\n" +
                "          transform-origin:0 0;\n" +
                "        }\n" +
                "        .hide-all {\n" +
                "          display: none;\n" +
                "        }\n" +
                "      </style>\n" +
                "    </head>\n" +
                "    <body>\n" +
                "        <div class=\"g-recaptcha\"\n" +
                "          data-sitekey=\"" + ApiClient.SIGN_UP_URL + "\"\n" +
                "          data-callback=\"captchaResponse\">\n" +
                "        </div>\n" +
                "    </body>\n" +
                "</html>"
    }
}