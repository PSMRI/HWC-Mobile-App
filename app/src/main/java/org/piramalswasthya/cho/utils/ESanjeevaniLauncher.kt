package org.piramalswasthya.cho.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import org.json.JSONObject
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.model.EsanjeevniPatient
import org.piramalswasthya.cho.model.NetworkBody
import org.piramalswasthya.cho.network.ESanjeevaniApiService
import java.security.MessageDigest

object ESanjeevaniLauncher {

    suspend fun launch(
        context: Context,
        patientDao: PatientDao,
        apiService: ESanjeevaniApiService,
        patientId: String,
        username: String,
        password: String
    ) {
        val passWord = encryptSHA512(encryptSHA512(password) + encryptSHA512("token"))
        val networkBody = NetworkBody(username, passWord, "token", "11001")

        try {
            val patientById = patientDao.getPatientById(patientId)
            val patient = EsanjeevniPatient(patientById)
            val savePatientResponse = apiService.savePatient(patient)
            val responseBody = savePatientResponse.body()?.string()
            if (responseBody.let { JSONObject(it).getInt("msgCode") } != 1) {
                Toast.makeText(
                    context,
                    responseBody.let { JSONObject(it).getString("message") },
                    Toast.LENGTH_LONG
                ).show()
            } else {
                val response = apiService.getAuthRefIdForWebView(networkBody)
                val referenceId = response.model.referenceId
                val url = context.getString(R.string.url_uat_esanjeevani) + referenceId
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "No browser found", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                context.getString(R.string.something_went_wrong),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun encryptSHA512(input: String): String {
        val digest = MessageDigest.getInstance("SHA-512")
        val hashBytes = digest.digest(input.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
