package org.piramalswasthya.cho.ui.web_view_activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.ActivityEditPatientDetailsBinding
import org.piramalswasthya.cho.databinding.ActivityWebViewBinding
import org.piramalswasthya.cho.model.EsanjeevniPatient
import org.piramalswasthya.cho.model.EsanjeevniPatientAddress
import org.piramalswasthya.cho.model.EsanjeevniPatientContactDetails
import org.piramalswasthya.cho.model.NetworkBody
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.ESanjeevaniApiService
import org.piramalswasthya.cho.network.interceptors.TokenESanjeevaniInterceptor
import org.piramalswasthya.cho.network.interceptors.TokenInsertTmcInterceptor
import org.piramalswasthya.cho.ui.commons.fhir_revisit_form.FhirRevisitFormFragment
import org.piramalswasthya.cho.ui.web_view_activity.web_view.WebViewFragment
import timber.log.Timber
import java.security.MessageDigest
import javax.inject.Inject

@AndroidEntryPoint
class WebViewActivity : AppCompatActivity() {

//    @Inject
//    lateinit var apiService : AmritApiService
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

    fun launchESanjeenvani(){

        var user = "Cdac@1234";
        var token = "token"
        var passWord = encryptSHA512(encryptSHA512(user) + encryptSHA512(token))

        //creating object using encrypted Password and other details
        var networkBody = NetworkBody(
            "8501258162",
            passWord,
            "token",
            "11001"
        )


        CoroutineScope(Dispatchers.Main).launch {
            Log.d("Resp token is", "working")
            val response = apiService.getJwtToken(networkBody)
            val token = response.model.access_token;
            TokenESanjeevaniInterceptor.setToken(token)

            val patientAddress = EsanjeevniPatientAddress(
                 "Street 44",
             "Physical",
             "Work",
             0,
             "",
             452,
             "Delhi Cantonment",
             "1",
             "India",
             79,
              "New Delhi",
             "110349",
             7,
             "Delhi"
            )

            val patientContact = EsanjeevniPatientContactDetails(
                 true,
             "Phone",
             "Work",
             "7890667710"
            )

            val patient = EsanjeevniPatient(
                 "",
             "",
             25,
             "1998-05-04",
             "Suraj1 sigh Kumar",
             "Suraj",
             "",
             "Kumar",
             1,
             "Male",
             false,
                patientAddress,
                patientContact,
                "11001"
            )




                Log.d("Resp token is", "$response")
        }


//        CoroutineScope(Dispatchers.Main).launch {
//            try {
//                val response = apiService.getAuthRefIdForWebView(networkBody)
//                Log.d("Resp", "$response")
//                if (response != null) {
//                    var referenceId = response.model.referenceId
//                    var url = "https://uat.esanjeevani.in/#/external-provider-signin/$referenceId"
//                    Log.d("here is act the url","ssds $url")
//                    val fragmentWebView = WebViewFragment(url);
//                    supportFragmentManager.beginTransaction().replace(binding.webView.id, fragmentWebView).commit()
//                }
//
//            }
//            catch (e: Exception){
//                Timber.d("GHere is error $e")
//
//            }
//        }
    }

    suspend fun apicall(){


    }

    override fun onBackPressed() {
        finish()
//        onDestroy()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("activity destroyed", "destroyed")
    }

    private fun encryptSHA512(input: String): String {
        val digest = MessageDigest.getInstance("SHA-512")
        val hashBytes = digest.digest(input.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

}