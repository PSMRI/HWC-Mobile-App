package org.piramalswasthya.cho.ui.register_patient_activity.scanAadhaar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions


class ScanAadhaarActivity : AppCompatActivity(){

    private var canFinishActivity = false

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
        registerReceiver(br, filter)

    }

    override fun onResume() {

        super.onResume()
        // finishing the activity when moving back from capture activity with out any action.

        if (canFinishActivity) {
            canFinishActivity = false
            finish()
        }

    }

    private fun scanCode() {

        val options = ScanOptions()
        options.setBeepEnabled(true)
        options.setOrientationLocked(false)
//        options.captureActivity = CaptureAct::class.java
        barCodeLauncher.launch(options)

    }

// Once getting the result , passing the result to MainActivity.

    private var barCodeLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->

        if (result.contents != null) {
            Intent().also { intent ->
                Log.e("content", "" + result.contents)
                intent.action = "com.example.broadcast.MY_NOTIFICATION"
                intent.putExtra("data", result.contents)
                sendBroadcast(intent)
            }
        }
    }
}