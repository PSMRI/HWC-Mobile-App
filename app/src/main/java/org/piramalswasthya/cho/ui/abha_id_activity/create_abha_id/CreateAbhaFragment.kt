package org.piramalswasthya.cho.ui.abha_id_activity.create_abha_id

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.work.Operation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.ResponseBody
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentCreateAbhaBinding
import org.piramalswasthya.cho.helpers.AnalyticsHelper
import org.piramalswasthya.cho.network.CreateHIDResponse
import org.piramalswasthya.cho.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.cho.ui.abha_id_activity.aadhaar_id.AadhaarIdViewModel
import org.piramalswasthya.cho.ui.abha_id_activity.create_abha_id.CreateAbhaViewModel.State
import org.piramalswasthya.cho.work.WorkerUtils
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class CreateAbhaFragment : Fragment() {

    private lateinit var navController: NavController

    private var _binding: FragmentCreateAbhaBinding? = null

    private val binding: FragmentCreateAbhaBinding
        get() = _binding!!
    private val viewModel: CreateAbhaViewModel by viewModels()

    private val parentViewModel: AadhaarIdViewModel by viewModels({ requireActivity() })

    private val channelId = "download abha card"

    private var benId: Long = 0
    private var benRegId: Long = 0

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    val args: CreateAbhaFragmentArgs by lazy {
        CreateAbhaFragmentArgs.fromBundle(requireArguments())
    }


    private var timer = object : CountDownTimer(30000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val sec = millisUntilFinished / 1000 % 60
            binding.timerResendOtp.text = sec.toString()
        }

        // When the task is over it will print 00:00:00 there
        override fun onFinish() {
            binding.resendOtp.isEnabled = true
            binding.timerResendOtp.visibility = View.INVISIBLE
        }
    }

    private val onBackPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle back button press here
                Timber.d("handleOnBackPressed")
                exitAlert.show()
            }
        }
    }

    private val exitAlert by lazy {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.exit))
            .setMessage(resources.getString(R.string.do_you_want_to_go_back))
            .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                activity?.finish()
            }
            .setNegativeButton(resources.getString(R.string.no)) { d, _ ->
                d.dismiss()
            }
            .create()
    }

    private val beneficiaryDisclaimer by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle("beneficiary abha mapping.")
            .setMessage("linking abha to beneficiary")
            .setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
            .create()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateAbhaBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        navController = findNavController()


        val intent = requireActivity().intent

        benId = intent.getLongExtra("benId", 0)
        benRegId = intent.getLongExtra("benRegId", 0)

        if (parentViewModel.abhaMode.value == AadhaarIdViewModel.Abha.SEARCH) {
            binding.imageView.setImageResource(R.drawable.ic_exclamation_circle_green)
            binding.textView7.text = getString(R.string.str_here_abha_no)
            binding.clDownloadAbha.visibility = View.INVISIBLE

        }else{
            val timestamp = System.currentTimeMillis()
            analyticsHelper.logCustomTimestampEvent("map_ben_to_health_id_request",timestamp)
        }




        binding.textView2.text = args.name
        binding.textView4.text = args.abhaNumber

        viewModel.abhaResponseLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.isNew == false) {
                    binding.imageView.setImageResource(R.drawable.ic_exclamation_circle)
                    binding.textView7.text = getString(R.string.str_abha_already_exist)
                } else {
                    viewModel.mapBeneficiaryToHealthId(benId, benRegId)
                    binding.imageView.setImageResource(R.drawable.ic_check_circle)
                    binding.textView7.text = getString(R.string.str_abha_successfully_created)
                }
            }
        }

        viewModel.benMapped.observe(viewLifecycleOwner) {
            it?.let {
                binding.abhBenMappedTxt.text = String.format(
                    "%s%s%s",
                    resources.getString(R.string.linked_to_beneficiary),
                    "\n",
                    it
                )
                binding.llAbhaBenMapped.visibility = View.VISIBLE
            }
        }
        binding.tietAadhaarOtp.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                binding.btnVerifyOTP.isEnabled = p0 != null && p0.length == 6
            }
        })

        binding.btnVerifyOTP.setOnClickListener {
            viewModel.verifyOtp(binding.tietAadhaarOtp.text.toString())
        }

        binding.btnDownloadAbhaNo.setOnClickListener {
            binding.txtDownloadAbha.visibility = View.INVISIBLE
            binding.clDownloadAbha.visibility = View.GONE
            requireActivity().finish()
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            it?.let {
                binding.tvErrorTextVerify.text = it
                binding.tvErrorText.text = it
                viewModel.resetErrorMessage()
            }
        }

        viewModel.byteImage.observe(viewLifecycleOwner) {
            it?.let {
                showFileNotification(it)
            }
        }
        binding.btnDownloadAbhaYes.setOnClickListener {
            viewModel.printAbhaCard()
        }

        binding.resendOtp.setOnClickListener {
            viewModel.generateOtp()
            startResendTimer()
        }

        viewModel.state.observe(viewLifecycleOwner) {
            if (it.name == "ABHA_GENERATE_SUCCESS"){
                val timestamp = System.currentTimeMillis()
                analyticsHelper.logCustomTimestampEvent("map_ben_to_health_id_reponse",timestamp)
            }
            if (it.name == "DOWNLOAD_SUCCESS") {
                binding.clDownloadAbha.visibility = View.INVISIBLE
                Snackbar.make(
                    requireView(),
                    getString(R.string.str_abha_card_downloaded), Snackbar.LENGTH_INDEFINITE
                ).setAction(getString(R.string.ok)) {
                    requireActivity().finish()
                }.show()
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        onBackPressedCallback.remove()
        _binding = null
    }

    private fun showFileNotification(fileStr: ResponseBody) {
        val fileName =
            "${benId}_${System.currentTimeMillis()}.png"
        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelId,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        val directory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(directory, fileName)
        FileOutputStream(file).use { stream -> stream.write(fileStr.bytes()) }
        MediaScannerConnection.scanFile(
            requireContext(),
            arrayOf(file.toString()),
            null,
        ) { _, uri ->
            run {
                showDownload(fileName, uri, notificationManager)
            }
        }
    }

    private fun showDownload(fileName: String, uri: Uri, notificationManager: NotificationManager) {
        val notificationBuilder = NotificationCompat.Builder(requireContext(), fileName)
            .setSmallIcon(R.drawable.ic_download)
            .setChannelId(channelId)
            .setContentTitle(fileName)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationBuilder.setContentTitle(fileName)
            .setContentText(fileName)
            .setAutoCancel(true)
            .setOngoing(false)
            .setContentIntent(
                PendingIntent.getActivity(
                    requireContext(),
                    0,
                    Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "image/png")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        notificationManager.notify(1, notificationBuilder.build())

    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as AbhaIdActivity).updateActionBar(
                R.drawable.ic__abha_logo_v1_24,
                getString(R.string.generate_abha)
            )
        }
    }

    private fun startResendTimer() {
        binding.resendOtp.isEnabled = false
        binding.timerResendOtp.visibility = View.VISIBLE
        timer.start()
    }

    fun notifydownload() {
        val fileName =
            "${viewModel.hidResponse.value?.name}_${System.currentTimeMillis()}.pdf"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                channelId, channelId,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)

            val notification = NotificationCompat.Builder(requireContext(), channelId)
                .setContentTitle(resources.getString(R.string.downloading_abha_card))
                .setContentText(resources.getString(R.string.downloading))
                .setSmallIcon(R.drawable.ic_download)
                .setProgress(100, 0, true)
                .build()
            notificationManager.notify(0, notification)
        }

        val state: LiveData<Operation.State> = WorkerUtils
            .triggerDownloadCardWorker(requireContext(), fileName, viewModel.otpTxnID)

        state.observe(viewLifecycleOwner) {
            when (it) {
                is Operation.State.SUCCESS -> {
                    binding.clDownloadAbha.visibility = View.INVISIBLE
                    Snackbar.make(
                        binding.root,
                        "Downloading $fileName ", Snackbar.LENGTH_SHORT
                    ).show()
                }
                is Operation.State.SUCCESS -> {
                    binding.clDownloadAbha.visibility = View.INVISIBLE
                    Snackbar.make(
                        binding.root,
                        "Downloading $fileName ", Snackbar.LENGTH_SHORT
                    ).show()
                }

                is Operation.State.FAILURE -> {
                    Toast.makeText(context, "Failed to download , Please retry", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

}