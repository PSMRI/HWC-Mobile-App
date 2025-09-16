package org.piramalswasthya.cho.ui.register_patient_activity.scanAadhaar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import org.piramalswasthya.cho.utils.ImgUtils


class ScanAadhaarActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scanCode()

        // Registering receiver to get Notify once scanning Barcode.

        val br: BroadcastReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {

                if(intent.extras?.get("data")!=null && intent.extras?.get("data").toString().trim()!="" ) {
                    unregisterReceiver(this)
                    setResult(2, intent)
                    finish()
                }
            }
        }

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION).apply {
            addAction("com.example.broadcast.MY_NOTIFICATION")
        }

        // I have commented this because getting crash and add below condition
//        registerReceiver(br, filter)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(br, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(br, filter)
        }


    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

    override fun onResume() {
        super.onResume()
        // finishing the activity when moving back from capture activity with out any action.
        if (ImgUtils.canFinishActivity) {
            ImgUtils.canFinishActivity = false
            finish()
        }

    }
    private fun scanCode() {
        val options = ScanOptions()
        options.setBeepEnabled(true)
        options.setOrientationLocked(false)
        options.captureActivity = CaptureAct::class.java
        barCodeLauncher.launch(options)
    }

    private var barCodeLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->

        if (result.contents != null) {
            Intent().also { intent ->
                intent.action = "com.example.broadcast.MY_NOTIFICATION"
                intent.putExtra("data", result.contents)
                sendBroadcast(intent)
            }
        }
    }
}