package org.piramalswasthya.cho.activity_contracts


import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.util.Base64
import androidx.activity.result.contract.ActivityResultContract
import java.nio.charset.StandardCharsets

class RDServiceCapturePIDContract : ActivityResultContract<Unit, String?>() {
    val ACTION_RDCAPTURE = "in.gov.uidai.rdservice.fp.CAPTURE"
    val RDCAPTURE_REQUEST = 2 // The request code

    override fun createIntent(context: Context, input: Unit): Intent {
        val intent = Intent()
        intent.action = ACTION_RDCAPTURE
        val pidOptions = "<PidOptions><Opts fCount=\"1\" fType=\"2\" iCount=\"0\" iType=\"\" pCount=\"0\" pType=\"\" format=\"0\" pidVer=\"2.0\" timeout=\"20000\" env=\"P\" wadh=\"E0jzJ/P8UopUHAieZn8CKqS4WPMi5ZSYXgfnlfkWjrc=\" posh=\"UNKNOWN\"/></PidOptions>"
//        val pidOptions = "<PidOptions ver=\"1.0\"><Opts env=\"PP\" fCount=\"1\" fType=\"0\" format=\"0\" pidVer=\"2.0\"></Opts><Demo><Pi ms=\"E\" gender=\"M\"></Pi></Demo><CustOpts></CustOpts></PidOptions>"
        intent.putExtra("PID_OPTIONS", pidOptions)

        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        intent?.let {
            if (resultCode == RESULT_OK) {
                //val a = it.getStringExtra("PID_DATA")
                //                val md: MessageDigest = MessageDigest.getInstance("SHA-256")
                //                var aBytes = a?.toByteArray(StandardCharsets.UTF_8)?.let { it1 -> md.digest(it1) }
                //
                //                val b = Base64.encodeToString(aBytes, Base64.NO_WRAP)
                val a = it.getStringExtra("PID_DATA")
                return Base64.encodeToString(a?.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
            }
        }
        return null
    }
}