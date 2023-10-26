package org.piramalswasthya.cho.ui.web_view_activity

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.databinding.ActivityWebViewBinding
import org.piramalswasthya.cho.model.EsanjeevniPatient
import org.piramalswasthya.cho.model.NetworkBody
import org.piramalswasthya.cho.network.ESanjeevaniApiService
import org.piramalswasthya.cho.ui.web_view_activity.web_view.WebViewFragment
import java.security.MessageDigest
import javax.inject.Inject

@AndroidEntryPoint
class WebViewActivity : AppCompatActivity() {
    @Inject
    lateinit var patientDao: PatientDao

    @Inject
    lateinit var apiService : ESanjeevaniApiService


    private var _binding : ActivityWebViewBinding? = null


    private val binding  : ActivityWebViewBinding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        launchESanjeenvani()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun launchESanjeenvani(){
        var username = intent.getStringExtra("usernameEs")!!
        var passWord = encryptSHA512(encryptSHA512(intent.getStringExtra("passwordEs")!!) + encryptSHA512("token"))

        //creating object using encrypted Password and other details
        var networkBody = NetworkBody(
            username,
            passWord,
            "token",
            "11001"
        )

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val patientId = intent.getStringExtra("patientId")!!
                val patientById = patientDao.getPatientById(patientId)
                val patient = EsanjeevniPatient(patientById)
                val savePatientResponse = apiService.savePatient(patient)
                val responseBody = savePatientResponse.body()?.string()
                if(responseBody.let { JSONObject(it).getInt("msgCode") }!=1)
                {
                    Toast.makeText(baseContext, responseBody.let { JSONObject(it).getString("message") }, Toast.LENGTH_LONG).show()
                    finish()
                }
                else {
                    val response = apiService.getAuthRefIdForWebView(networkBody)
                    if (response != null) {
                        val referenceId = response.model.referenceId
                        val url =
                            getString(R.string.url_uat_esanjeevani) + referenceId
                        val fragmentWebView = WebViewFragment(url);
                        supportFragmentManager.beginTransaction()
                            .replace(binding.webView.id, fragmentWebView).commit()
                    } else {
                        finish()
                        Toast.makeText(baseContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            catch (e: Exception){
                finish()
                Toast.makeText(baseContext, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    suspend fun apicall(){


    }

    override fun onBackPressed() {
        finish()
//        onDestroy()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun encryptSHA512(input: String): String {
        val digest = MessageDigest.getInstance("SHA-512")
        val hashBytes = digest.digest(input.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

}