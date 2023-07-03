package org.piramalswasthya.cho.ui.web_view_activity.web_view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
//import org.piramalswasthya.cho.ui.WebView.WebViewFragmentArgs
import timber.log.Timber


@AndroidEntryPoint
class WebViewFragment: Fragment(R.layout.fragment_web_view) {


    private val webUrl by lazy {
        WebViewFragmentArgs.fromBundle(requireArguments()).webUrl
    }

    private lateinit var webView : WebView
    lateinit var btn : Button
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view : View = inflater.inflate(R.layout.fragment_web_view, container, false)
        webView = view.findViewById(R.id.webview)

        btn = view.findViewById(R.id.back_btn)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.d("here is the url: $webUrl")
        Log.d("aaaaaaaaaaaa","adfdf")
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.loadUrl(webUrl)
        btn.setOnClickListener{
//            findNavController().navigate(
//                WebViewFragmentDirections.backtopreviousFragment()
//            )
        }
    }
}