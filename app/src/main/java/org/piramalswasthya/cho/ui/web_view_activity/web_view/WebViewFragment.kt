package org.piramalswasthya.cho.ui.web_view_activity.web_view

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentWebViewBinding


@AndroidEntryPoint
class WebViewFragment constructor(
    private val webUrl: String,
): Fragment(R.layout.fragment_web_view) {
    private lateinit var progressDialog: ProgressDialog

    private var _binding: FragmentWebViewBinding? = null
    private val binding: FragmentWebViewBinding
        get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentWebViewBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        progressDialog = ProgressDialog.show(activity, "Loading", "Loading eSanjeevani Please wait...", true)
        progressDialog.setCancelable(false)
        binding.webview.settings.javaScriptEnabled = true
        binding.webview.settings.domStorageEnabled = true
        binding.webview.settings.loadWithOverviewMode = true
        binding.webview.settings.mediaPlaybackRequiresUserGesture = false
        binding.webview.settings.useWideViewPort = true
        binding.webview.webViewClient = WebViewClient()

        binding.webview.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                progressDialog.show()
                view.loadUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                progressDialog.dismiss()
            }
        }

        binding.webview.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                // Handle file upload (photo capture) here
                return true
            }
        }
        binding.webview.loadUrl(webUrl)
    }

    override fun onDestroyView() {
        // Destroy the WebView when the fragment view is destroyed
         binding?.webview?.let {
            it.stopLoading()
            it.destroy()
        }
        super.onDestroyView()
    }
}