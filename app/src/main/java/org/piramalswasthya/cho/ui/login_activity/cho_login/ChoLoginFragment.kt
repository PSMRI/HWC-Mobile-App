package org.piramalswasthya.cho.ui.login_activity.cho_login

import SecuGen.FDxSDKPro.JSGFPLib
import SecuGen.FDxSDKPro.SGDeviceInfoParam
import SecuGen.FDxSDKPro.SGFDxDeviceName
import SecuGen.FDxSDKPro.SGFDxErrorCode
import SecuGen.FDxSDKPro.SGFDxSecurityLevel
import SecuGen.FDxSDKPro.SGFDxTemplateFormat
import SecuGen.FDxSDKPro.SGFingerInfo
import SecuGen.FDxSDKPro.SGImpressionType
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.registerReceiver
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentChoLoginBinding
import org.piramalswasthya.cho.model.FingerPrint
import org.piramalswasthya.cho.model.LoginSettingsData
import org.piramalswasthya.cho.repositories.LoginSettingsDataRepository
import org.piramalswasthya.cho.ui.login_activity.cho_login.hwc.HwcFragment
import org.piramalswasthya.cho.ui.login_activity.cho_login.outreach.OutreachFragment
import timber.log.Timber
import java.nio.ByteBuffer
import javax.inject.Inject
import android.util.Base64
import androidx.navigation.fragment.findNavController


@AndroidEntryPoint
class ChoLoginFragment : Fragment() {

    @Inject
    lateinit var prefDao: PreferenceDao
    @Inject
    lateinit var loginSettingsDataRepository: LoginSettingsDataRepository
    private var loginSettingsData: LoginSettingsData? = null
    private var _binding: FragmentChoLoginBinding? = null
    private val viewModel: ChoLoginViewModel by viewModels()

    private val binding: FragmentChoLoginBinding
        get() = _binding!!
    private lateinit var sgfplib: JSGFPLib
    private var mPermissionIntent: PendingIntent? = null
    private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    private var mImageWidth = 0
    private var mImageHeight = 0
    private var mScanImage: ByteArray? = null
    private var mVerifyImage: ByteArray? = null
    private var mScanTemplate: ByteArray? = null
    private var mVerifyTemplate: ByteArray? = null
    private var mMaxTemplateSize: IntArray? = null
    private var fingerPrintList : List<FingerPrint>? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChoLoginBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    private fun setActivityContainer(programId: Int){
        val userName = (arguments?.getString("userName", ""))!!;
        val rememberUsername:Boolean = (arguments?.getBoolean("rememberUsername"))!!
        val fragmentManager : FragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction : FragmentTransaction = fragmentManager.beginTransaction()
        val hwcFragment  = HwcFragment(userName)
        val outreachFragment = OutreachFragment(userName,rememberUsername);
        when (programId){
            binding.btnHwc.id -> {
                binding.fpLoginLayout.visibility = View.VISIBLE
                fragmentTransaction.replace(binding.selectActivityContainer.id, hwcFragment)
                fragmentTransaction.commit()
            }
            binding.btnOutreach.id -> {
                binding.fpLoginLayout.visibility = View.GONE
                fragmentTransaction.replace(binding.selectActivityContainer.id, outreachFragment)
                fragmentTransaction.commit()
            }
            else -> {
                fragmentTransaction.replace(binding.selectActivityContainer.id, hwcFragment)
                fragmentTransaction.commit()
            }
        }
    }

//    fun setUserName(){
//        userName = arguments?.getString("userName", "");
//        if(userName != null) {
//            Timber.tag("tag").i(userName)
//        }
//        else{
//            Timber.tag("tag").i("null")
//        }
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setActivityContainer(binding.selectProgram.id)
        binding.selectProgram.setOnCheckedChangeListener { _, programId ->
            setActivityContainer(programId)
        }

        // calling to get all registered finger print data
        viewModel.getFingerPrintData()
        viewModel.fingerListData.observe(viewLifecycleOwner){
            fingerPrintList = it
        }
        binding.scanButton.setOnClickListener{
            try {
                registerUsbDevice()
                initialiseDevice()
            } catch (e : Exception) {
                // go back
                Toast.makeText(context, "Device initialisation failed", Toast.LENGTH_SHORT).show()
                if(sgfplib.DeviceInUse()){
                    sgfplib.CloseDevice()
                }
            }
        }

//        val userName = (arguments?.getString("userName", ""))!!;
//
//        lifecycleScope.launch {
//            loginSettingsData =  loginSettingsDataRepository.getLoginSettingsDataByUsername(userName)
//
//            if (loginSettingsData==null) {
//                binding.loginSettings.visibility = View.VISIBLE
//
//                binding.loginSettings.setOnClickListener{
//                    try {
//                        findNavController().navigate(
//                            ChoLoginFragmentDirections.actionChoLoginToLoginSettings(userName),
//                        )
//                    }catch (e: Exception){
//                        Timber.d("Failed to navigate"+e.message)
//                    }
//
//                }
//            } else {
//                binding.loginSettings.visibility = View.INVISIBLE
//            }
//        }



    }
    private fun registerUsbDevice() {
        var mUsbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (ACTION_USB_PERMISSION == action) {
                    synchronized(this) {
                        val device = intent.getParcelableExtra<Parcelable>(
                            UsbManager.EXTRA_DEVICE
                        ) as UsbDevice?
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            if (device != null) {
                                Toast.makeText(activity, "registration $device", Toast.LENGTH_LONG)
                                    .show()
                            } else {
                                Toast.makeText(activity, "null device", Toast.LENGTH_LONG)
                                    .show()
                            }
                        } else Toast.makeText(activity, "permission denied device", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        }
        val pendingFlags: Int = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        mPermissionIntent = PendingIntent.getBroadcast(
            activity, 0,
            Intent(ACTION_USB_PERMISSION), pendingFlags
        )

        val filter = IntentFilter(ACTION_USB_PERMISSION)
        val regFlags: Int = ContextCompat.RECEIVER_EXPORTED
        registerReceiver(requireContext(), mUsbReceiver, filter, regFlags)
    }


    private fun initialiseDevice() {
        val pendingFlags: Int = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        mPermissionIntent = PendingIntent.getBroadcast(
            activity, 0,
            Intent(ACTION_USB_PERMISSION), pendingFlags
        )
        val usbManager: UsbManager = requireContext().getSystemService(Context.USB_SERVICE) as UsbManager
        sgfplib = JSGFPLib(this.requireActivity(), usbManager)
        sgfplib.Init( SGFDxDeviceName.SG_DEV_AUTO)
        val usbDevice: UsbDevice? = sgfplib.GetUsbDevice()
        if (usbDevice == null) {
            val dlgAlert = AlertDialog.Builder(activity)
            dlgAlert.setMessage("USB Device Not Found Please check your Connection")
            dlgAlert.setTitle("USB Device Error")
            dlgAlert.setPositiveButton("OK",
                DialogInterface.OnClickListener { _, _ ->
                    binding.scanButton.text = "Scan For Login"
                    return@OnClickListener
                }
            )
            dlgAlert.setCancelable(false)
            dlgAlert.create().show()
        }
        else {
            var usbPermissionRequested = false
            var hasPermission: Boolean = sgfplib.GetUsbManager().hasPermission(usbDevice)
            if (!hasPermission) {
                if (!usbPermissionRequested) {
                    sgfplib.GetUsbManager().requestPermission(usbDevice, mPermissionIntent)
                    usbPermissionRequested = true
                    Timber.i("value of permission $usbPermissionRequested")
                } else {
                    //wait up to 20 seconds for the system to grant USB permission
                    hasPermission = sgfplib.GetUsbManager().hasPermission(usbDevice)
                    var i = 0
                    while (!hasPermission && i <= 40) {
                        ++i
                        hasPermission = sgfplib.GetUsbManager().hasPermission(usbDevice)
                        try {
                            Thread.sleep(500)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            if(hasPermission){
                captureFingerprint()
            }
        }
        sgfplib.AutoOnEnabled()

    }
    private fun captureFingerprint() {
        mMaxTemplateSize = IntArray(1)
        sgfplib.OpenDevice(0)
        val deviceInfo = SGDeviceInfoParam()
        sgfplib.GetDeviceInfo(deviceInfo)
        val buffer = ByteArray(deviceInfo.imageWidth * deviceInfo.imageHeight)
        mImageWidth = deviceInfo.imageWidth
        mImageHeight = deviceInfo.imageHeight
        val quality: Long = 60
        val timeout: Long = 10000
        var result = sgfplib.GetImageEx(buffer, timeout, quality)
        if (result == SGFDxErrorCode.SGFDX_ERROR_NONE) {
            binding.fpImage.setImageBitmap(toGrayscale(buffer,deviceInfo))
            binding.scanButton.text = "Re-Scan for Login"
            binding.scanButton.isClickable = true
            binding.scanButton.isEnabled = true
            mScanImage = buffer
            //calling method to verify the scanned print
            verifyFp()
        }
    }

    private fun toGrayscale(mImageBuffer: ByteArray, deviceInfoParam: SGDeviceInfoParam): Bitmap? {
        val bits = ByteArray(mImageBuffer.size * 4)
        for (i in mImageBuffer.indices) {
            bits[i * 4 + 2] = mImageBuffer[i]
            bits[i * 4 + 1] = bits[i * 4 + 2]
            bits[i * 4] = bits[i * 4 + 1] // Invert the source bits
            bits[i * 4 + 3] = -1 // 0xff, that's the alpha.
        }

        val bmpGrayscale = Bitmap.createBitmap(deviceInfoParam.imageWidth, deviceInfoParam.imageHeight,
            Bitmap.Config.ARGB_8888)
        bmpGrayscale.copyPixelsFromBuffer(ByteBuffer.wrap(bits))
        return bmpGrayscale
    }

    private fun verifyFp(){
        val tempFormat = sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_ISO19794)
        Log.i("Template Format","$tempFormat")
        sgfplib.GetMaxTemplateSize(mMaxTemplateSize)
        mScanTemplate = ByteArray(mMaxTemplateSize!![0])
        mVerifyTemplate = ByteArray(mMaxTemplateSize!![0])

        // current scan finger print
        val qualityS = IntArray(1)
        val getQualityS  = sgfplib.GetImageQuality(mImageWidth.toLong(), mImageHeight.toLong(), mScanImage, qualityS)
        Log.i("Scanned Image Quality"," $getQualityS")

        val fpInfo = SGFingerInfo()
        fpInfo.FingerNumber = 1
        fpInfo.ImageQuality = qualityS[0]
        fpInfo.ImpressionType = SGImpressionType.SG_IMPTYPE_LP
        fpInfo.ViewNumber = 1

        for (i in mScanTemplate!!.indices) mScanTemplate!![i] = 0

        val scanTemplate = sgfplib.CreateTemplate(fpInfo, mScanImage, mScanTemplate)
        Log.i("Scan Template"," $scanTemplate")

        var matchBool = false
        if(fingerPrintList!!.isNotEmpty()){
            for (item in fingerPrintList!!) {
                for (i in mVerifyTemplate!!.indices) mVerifyTemplate!![i] = 0
                val decodedByteArray = Base64.decode(item.fpVal, Base64.DEFAULT)
                val registerDataTemp = sgfplib.CreateTemplate(fpInfo, decodedByteArray, mVerifyTemplate)
                Log.i("Register Image Template"," $registerDataTemp")

                val matched = BooleanArray(1)
                val match = sgfplib.MatchTemplate(mScanTemplate, mVerifyTemplate, SGFDxSecurityLevel.SL_NORMAL, matched)
                Log.i("Register Image Matching value"," $match")


                if(matched[0]){
                    Toast.makeText(activity,"Finger Print Matched !!",Toast.LENGTH_SHORT).show()
                    sgfplib.CloseDevice()
                    matchBool = true
                    // navigate to Main screen
                    findNavController().navigate(ChoLoginFragmentDirections.actionSignInToHomeFromCho())
                    break
                } else {
                    binding.scanButton.text = "Re-Scan for Login"
                    matchBool = false
                }
            }
            if(!matchBool){
                Toast.makeText(activity,"Finger Print Does Not Matched !!",Toast.LENGTH_SHORT).show()
            }
        } else{
            Toast.makeText(activity,"Register Finger Print Data Not Found",Toast.LENGTH_SHORT).show()
        }
        // closing device after taking the reading
        sgfplib.CloseDevice()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}