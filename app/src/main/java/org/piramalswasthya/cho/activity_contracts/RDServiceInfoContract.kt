package org.piramalswasthya.cho.activity_contracts

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Base64
import androidx.activity.result.contract.ActivityResultContract
import java.nio.charset.StandardCharsets


class RDServiceInfoContract : ActivityResultContract<Unit, String?>() {

    val ACTION_RDINFO = "in.gov.uidai.rdservice.fp.INFO"

    override fun createIntent(context: Context, input: Unit): Intent {
        val sendIntent = Intent()
        sendIntent.action = ACTION_RDINFO
        return sendIntent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        intent?.let {
            if (resultCode == Activity.RESULT_OK) {

                val deviceInfo : String? = it.getStringExtra("DEVICE_INFO")
                val rdServiceInfo: String? = it.getStringExtra("RD_SERVICE_INFO")
                val a = it.getStringExtra("PID_DATA")
                return Base64.encodeToString(a?.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
            }
        }
        return null
    }
}