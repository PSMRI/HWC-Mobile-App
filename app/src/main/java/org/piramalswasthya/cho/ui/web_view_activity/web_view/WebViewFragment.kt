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
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentUsernameBinding
import org.piramalswasthya.cho.databinding.FragmentWebViewBinding
import org.piramalswasthya.cho.model.PatientDetails
//import org.piramalswasthya.cho.ui.WebView.WebViewFragmentArgs
import timber.log.Timber


@AndroidEntryPoint
class WebViewFragment constructor(
    private val webUrl: String,
): Fragment(R.layout.fragment_web_view) {

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
        Log.d("here is the url","$webUrl")
        Log.d("aaaaaaaaaaaa","adfdf")
        binding.webview.settings.javaScriptEnabled = true
        binding.webview.settings.domStorageEnabled = true
        binding.webview.webViewClient = WebViewClient()
        binding.webview.loadUrl(webUrl)
//        findNavController(){
//
//        }
//        activity?.onBackPressedDispatcher()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("destryed", "fsdfdsfds");
    }
}