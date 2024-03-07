package org.piramalswasthya.cho.ui.web_view_activity.web_view

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentWebViewBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@AndroidEntryPoint
class WebViewFragment constructor(
    private val webUrl: String,
): Fragment(R.layout.fragment_web_view) {

    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private val MIC_PERMISSION_REQUEST_CODE = 101
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

    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var mediaFile: File? = null

    private val filePickerLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            handleSelectedFiles(result.resultCode, result.data)
        } else {
            filePathCallback?.onReceiveValue(null)
            filePathCallback = null
        }
    }

    private val cameraCaptureLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val mediaUri: Uri = Uri.fromFile(mediaFile)
            val results = arrayOf(mediaUri)
            filePathCallback?.onReceiveValue(results)
            filePathCallback = null
        } else {
            filePathCallback?.onReceiveValue(null)
            filePathCallback = null
        }
    }


    private val mediaCaptureLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val mediaUri: Uri = Uri.fromFile(mediaFile)
            val results = arrayOf(mediaUri)
            filePathCallback?.onReceiveValue(results)
            filePathCallback = null
        } else {
            filePathCallback?.onReceiveValue(null)
            filePathCallback = null
        }
    }

    private fun handleSelectedFiles(resultCode: Int, data: Intent?) {
        val results = WebChromeClient.FileChooserParams.parseResult(resultCode, data)
        filePathCallback?.onReceiveValue(results)
        filePathCallback = null
    }

    private fun createAudioCaptureIntent(): Intent? {
        val audioCaptureIntent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
        if (audioCaptureIntent.resolveActivity(requireActivity().packageManager) != null) {
            try {
                mediaFile = createMediaFile("AUDIO", ".3gp")
                val audioUri = Uri.fromFile(mediaFile)
                audioCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, audioUri)
            } catch (e: IOException) {
                e.printStackTrace()
                filePathCallback?.onReceiveValue(null)
                filePathCallback = null
                return null
            }
        }
        return audioCaptureIntent
    }

    private fun createVideoCaptureIntent(): Intent? {
        val videoCaptureIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        if (videoCaptureIntent.resolveActivity(requireActivity().packageManager) != null) {
            try {
                mediaFile = createMediaFile("VIDEO", ".mp4")
                val videoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "org.piramalswasthya.cho.fileprovider",
                    mediaFile!!
                )
                videoCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
            } catch (e: IOException) {
                e.printStackTrace()
                filePathCallback?.onReceiveValue(null)
                filePathCallback = null
                return null
            }
        }
        return videoCaptureIntent
    }

    private fun createImageCaptureIntent(): Intent? {
        val videoCaptureIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        if (videoCaptureIntent.resolveActivity(requireActivity().packageManager) != null) {
            try {
                mediaFile = createMediaFile("IMAGE", ".png")
                val videoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "your.package.name.fileprovider",
                    mediaFile!!
                )
                videoCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
            } catch (e: IOException) {
                e.printStackTrace()
                filePathCallback?.onReceiveValue(null)
                filePathCallback = null
                return null
            }
        }
        return videoCaptureIntent
    }

    private fun createMediaFile(prefix: String, extension: String): File {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val mediaFileName = "$prefix-$timeStamp"
        val storageDir: File = requireContext().getExternalFilesDir(Environment.DIRECTORY_DCIM)!!
        return File.createTempFile(mediaFileName, extension, storageDir)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        progressDialog = ProgressDialog.show(activity, "Loading", "Loading eSanjeevani Please wait...", true)
        progressDialog.setCancelable(false)
        binding.webview.settings.javaScriptEnabled = true
        binding.webview.settings.domStorageEnabled = true
        binding.webview.settings.loadWithOverviewMode = true
        binding.webview.settings.mediaPlaybackRequiresUserGesture = false
        binding.webview.settings.useWideViewPort = true
        binding.webview.settings.allowContentAccess = true

        binding.webview.settings.allowFileAccess = true
        binding.webview.settings.allowFileAccessFromFileURLs = true
        binding.webview.settings.allowUniversalAccessFromFileURLs = true
        binding.webview.settings.loadsImagesAutomatically = true


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
                this@WebViewFragment.filePathCallback = filePathCallback

                val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                contentSelectionIntent.type = fileChooserParams?.acceptTypes?.firstOrNull() ?: "*/*"

                val captureIntent: Intent? = createVideoCaptureIntent()

                val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Choose an action")
                chooserIntent.putExtra(
                    Intent.EXTRA_INITIAL_INTENTS,
                    if (captureIntent != null) arrayOf(captureIntent) else emptyArray()
                )

                filePickerLauncher.launch(chooserIntent)
                return true
            }

            override fun onPermissionRequest(request: PermissionRequest) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    when {
                        request.resources.contains("android.webkit.resource.VIDEO_CAPTURE") -> {
                            requestCameraPermission()
                        }
                        request.resources.contains("android.webkit.resource.AUDIO_CAPTURE") -> {
                            requestMicPermission()
                        }
                    }
                }
            }

        }


        binding.webview.loadUrl(webUrl)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE, MIC_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, reload the WebView or perform necessary actions
                    binding.webview.reload()
                } else {
                    // Permission denied, handle accordingly
                    // You may want to show a message or take other actions
                    Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    private fun requestMicPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO), MIC_PERMISSION_REQUEST_CODE)
        }
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