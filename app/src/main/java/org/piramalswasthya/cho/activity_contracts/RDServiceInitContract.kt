package org.piramalswasthya.cho.activity_contracts


import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class RDServiceInitContract: ActivityResultContract<Unit, String?>() {
    val ACTION_RDINIT = "in.secugen.rdservice.INIT"

    override fun createIntent(context: Context, input: Unit): Intent {
        val intent = Intent()
        intent.action = ACTION_RDINIT
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        intent?.let {
            resultCode
        }
        return null
    }

}