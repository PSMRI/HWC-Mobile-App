package org.piramalswasthya.cho.ui.commons

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.result.contract.ActivityResultContract
import java.util.Locale

class SpeechToTextContract : ActivityResultContract<Unit, String>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault()
        )
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String {
        return intent?.takeIf { resultCode== RESULT_OK }?.let {
            it.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS)?.first()
        }?:""

    }
}