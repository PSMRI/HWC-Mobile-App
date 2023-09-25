package org.piramalswasthya.cho.ui.login_activity.login_settings

//import SecuGen.FDxSDKPro.JSGFPLib
//import SecuGen.FDxSDKPro.SGDeviceInfoParam
//import SecuGen.FDxSDKPro.SGFDxDeviceName
//import SecuGen.FDxSDKPro.SGFDxErrorCode
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
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FpRegisterBinding
import org.piramalswasthya.cho.model.FingerPrint
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.fingerList
import java.nio.ByteBuffer

@AndroidEntryPoint
class FingerPrintRegisterFragment: Fragment() {

    private var _binding:FpRegisterBinding?= null
    private val binding: FpRegisterBinding
        get() = _binding!!
    private val viewModel: FpModel by viewModels()
    private var fingerType: String? = null
    var userName: String? = null
    private var byteArrayData: ByteArray? = null
    private var base64String: String = ""
//    private lateinit var sgfplib: JSGFPLib
    private var mPermissionIntent: PendingIntent? = null
    private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    private var initialPos = 0
    private var fpList = mutableListOf<FingerPrint>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FpRegisterBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userName = (arguments?.getString("userName", ""))!!
        binding.inputUserName.setText(userName)
        initialPos = 0
        binding.fingerDropdownVal.setAdapter(ArrayAdapter(requireContext(), R.layout.drop_down, fingerList))
        if(initialPos < 4){
            binding.fingerDropdownVal.setText(fingerList[initialPos],false)
            fingerType = fingerList[initialPos]
            if(initialPos > 0) binding.enrollFp.text = "Next Finger"
            if(initialPos >= 4){
                binding.enrollFp.visibility = View.GONE
                binding.submitFP.visibility = View.VISIBLE
            }
        }
        binding.fingerDropdownVal.setOnItemClickListener { parent, _, position, _ ->
            fingerType = parent.getItemAtPosition(position).toString()
        }
        binding.enrollFp.setOnClickListener {
            try {
                    registerUsbDevice()
                    initialiseDevice()
            } catch (e : Exception) {
                // go back
                Toast.makeText(context, "Device initialisation failed", Toast.LENGTH_SHORT).show()
                binding.textViewFp.visibility = View.GONE
                binding.textViewFpNot.visibility = View.VISIBLE
            }
        }
        binding.submitFP.setOnClickListener {
            if(initialPos >= 4){
                viewModel.submitFPData(fpList)
                Toast.makeText(activity,"Finger Print Data Saved",Toast.LENGTH_LONG).show()
            }
        }
        binding.moveToLoginSc.setOnClickListener {
            findNavController().navigate(
                FingerPrintRegisterFragmentDirections.actionFingerPrintRegisterFragmentToChoLoginFragment(userName!!)
            )
        }
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
        ContextCompat.registerReceiver(requireContext(), mUsbReceiver, filter, regFlags)
    }


    private fun initialiseDevice() {
//        val pendingFlags: Int = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//        mPermissionIntent = PendingIntent.getBroadcast(
//            activity, 0,
//            Intent(ACTION_USB_PERMISSION), pendingFlags
//        )
//        val usbManager: UsbManager = requireContext().getSystemService(Context.USB_SERVICE) as UsbManager
//        sgfplib = JSGFPLib(this.requireActivity(), usbManager)
//        sgfplib.Init( SGFDxDeviceName.SG_DEV_AUTO)
//        val usbDevice: UsbDevice? = sgfplib.GetUsbDevice()
//        if (usbDevice == null) {
//            val dlgAlert = AlertDialog.Builder(activity)
//            dlgAlert.setMessage("USB Device Not Found Please check your Connection")
//            dlgAlert.setTitle("USB Device Error")
//            dlgAlert.setPositiveButton("OK",
//                DialogInterface.OnClickListener { _, _ ->
//                    return@OnClickListener
//                }
//            )
//            dlgAlert.setCancelable(false)
//            dlgAlert.create().show()
//        }
//        else {
//            var usbPermissionRequested = false
//            var hasPermission: Boolean = sgfplib.GetUsbManager().hasPermission(usbDevice)
//            if (!hasPermission) {
//                if (!usbPermissionRequested) {
//                    sgfplib.GetUsbManager().requestPermission(usbDevice, mPermissionIntent)
//                    usbPermissionRequested = true
//                } else {
//                    //wait up to 20 seconds for the system to grant USB permission
//                    hasPermission = sgfplib.GetUsbManager().hasPermission(usbDevice)
//                    var i = 0
//                    while (!hasPermission && i <= 40) {
//                        ++i
//                        hasPermission = sgfplib.GetUsbManager().hasPermission(usbDevice)
//                        try {
//                            Thread.sleep(500)
//                        } catch (e: InterruptedException) {
//                            e.printStackTrace()
//                        }
//                    }
//                }
//            }
//            if(hasPermission){
//                captureFingerprint()
//            }
//        }
//        sgfplib.AutoOnEnabled()

    }

//    private fun captureFingerprint() {
//        sgfplib.OpenDevice(0)
//        val deviceInfo = SGDeviceInfoParam()
//        sgfplib.GetDeviceInfo(deviceInfo)
//        byteArrayData = ByteArray(deviceInfo.imageWidth * deviceInfo.imageHeight)
//        val quality: Long = 60
//        val timeout: Long = 10000
//        var result = sgfplib.GetImageEx(byteArrayData, timeout, quality)
//        if (result == SGFDxErrorCode.SGFDX_ERROR_NONE) {
//            binding.fpImageReg.setImageBitmap(toGrayscale(byteArrayData!!,deviceInfo))
//            binding.textViewFp.visibility = View.VISIBLE
//            binding.textViewFpNot.visibility = View.GONE
//            base64String = Base64.encodeToString(byteArrayData, Base64.DEFAULT)
//            fpList.add(FingerPrint(initialPos, userName, base64String, fingerType))
//            // closing device after taking the reading
//            sgfplib.CloseDevice()
//
//            initialPos++
//            if(initialPos < 4) {
//                binding.fingerDropdownVal.setText(fingerList[initialPos],false)
//                fingerType = fingerList[initialPos]
//               if(initialPos > 0) binding.enrollFp.text = "Next Finger"
//            }
//            else {
//                binding.enrollFp.visibility = View.GONE
//                binding.submitFP.visibility = View.VISIBLE
//            }
//        }
//    }
//
//    private fun toGrayscale(mImageBuffer: ByteArray, deviceInfoParam: SGDeviceInfoParam): Bitmap? {
//        val bits = ByteArray(mImageBuffer.size * 4)
//        for (i in mImageBuffer.indices) {
//            bits[i * 4 + 2] = mImageBuffer[i]
//            bits[i * 4 + 1] = bits[i * 4 + 2]
//            bits[i * 4] = bits[i * 4 + 1] // Invert the source bits
//            bits[i * 4 + 3] = -1 // 0xff, that's the alpha.
//        }
//
//        val bmpGrayscale = Bitmap.createBitmap(deviceInfoParam.imageWidth, deviceInfoParam.imageHeight,
//            Bitmap.Config.ARGB_8888)
//        bmpGrayscale.copyPixelsFromBuffer(ByteBuffer.wrap(bits))
//        return bmpGrayscale
//    }
}