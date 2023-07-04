package org.piramalswasthya.cho.ui.web_view_activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.piramalswasthya.cho.R
import java.security.MessageDigest

class WebViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)



    }

    override fun onBackPressed() {
        finish()
    }

    private fun encryptSHA512(input: String): String {
        val digest = MessageDigest.getInstance("SHA-512")
        val hashBytes = digest.digest(input.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

}