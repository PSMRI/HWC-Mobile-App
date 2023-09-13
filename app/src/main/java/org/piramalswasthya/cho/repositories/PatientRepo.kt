package org.piramalswasthya.cho.repositories

import android.util.Log
import com.google.gson.Gson
import org.json.JSONObject
import org.piramalswasthya.cho.database.room.InAppDb
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.model.PatientNetwork
import org.piramalswasthya.cho.model.UserDomain
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.BenificiarySaveResponse
import org.piramalswasthya.cho.network.DistrictList
import org.piramalswasthya.cho.network.NetworkResponse
import org.piramalswasthya.cho.network.NetworkResult
import org.piramalswasthya.cho.network.StateList
import org.piramalswasthya.cho.network.networkResultInterceptor
import org.piramalswasthya.cho.network.refreshTokenInterceptor
import org.piramalswasthya.cho.network.socketTimeoutException
import org.piramalswasthya.cho.ui.login_activity.login_settings.LoginSettingsViewModel
import java.net.SocketTimeoutException
import javax.inject.Inject

class PatientRepo  @Inject constructor(
    private val patientDao: PatientDao,
    private val userRepo: UserRepo,
    private val apiService: AmritApiService
) {

    suspend fun insertPatient(patient: Patient) {
        patientDao.insertPatient(patient)
    }

    suspend fun updatePatientSyncing(patient: Patient) {
        patientDao.updatePatientSyncing(SyncState.SYNCING, patient.patientID)
    }

    suspend fun updatePatientSyncingFailed(patient: Patient) {
        patientDao.updatePatientSyncFailed(SyncState.UNSYNCED, patient.patientID)
    }

    suspend fun updatePatientSyncSuccess(patient: Patient, benificiarySaveResponse: BenificiarySaveResponse){
        patientDao.updatePatientSynced(SyncState.SYNCED, benificiarySaveResponse.beneficiaryID, benificiarySaveResponse.beneficiaryRegID, patient.patientID)
    }

    suspend fun getPatientList() : List<PatientDisplay>{
        return patientDao.getPatientList()
    }

    suspend fun registerNewPatient(patient : PatientDisplay, user: UserDomain?): NetworkResult<NetworkResponse> {

        return networkResultInterceptor {
            val patNet = PatientNetwork(patient, user)
            val response = apiService.saveBenificiaryDetails(patNet)
            val responseBody = response.body()?.string()

            val result = Gson().fromJson(responseBody!!, BenificiarySaveResponse::class.java)
            NetworkResult.Success(result)

//            refreshTokenInterceptor(
//                responseBody = responseBody,
//                onSuccess = {
//                    val beneficiaryID = responseBody.let { JSONObject(it).getLong("beneficiaryID") }
//                    val beneficiaryRegID = responseBody.let { JSONObject(it).getLong("beneficiaryRegID") }
//                    val benID = beneficiaryID;
//                    val benRegId = beneficiaryRegID;
//                    Log.i("beneficiaryID", benID.toString())
//                    Log.i("beneficiaryRegID", benRegId.toString())
//
////                    val result = Gson().fromJson(data, StateList::class.java)
////                    val data = responseBody.let { JSONObject(it).getString("data") }
////                    val resp = responseBody
//                    val result = Gson().fromJson(responseBody!!, BenificiarySaveResponse::class.java)
////                    Log.i("fdgdgsdfgsfd", responseBody ?: "")
//////                    val result = BenificiarySaveResponse(" ")
//                    NetworkResult.Success(result)
//                },
//                onTokenExpired = {
//                    val user = userRepo.getLoggedInUser()!!
//                    userRepo.refreshTokenTmc(user.userName, user.password)
//                    registerNewPatient(patient, user)
//                },
//            )
        }

    }

    suspend fun processUnsyncedData() : Boolean{

        val patientList = patientDao.getPatientListUnsynced();
        val user = userRepo.getLoggedInUser()

        patientList.forEach {
            updatePatientSyncing(it.patient)
            when(val response = registerNewPatient(it, user)){
                is NetworkResult.Success -> {
                    val benificiarySaveResponse = response.data as BenificiarySaveResponse
                    updatePatientSyncSuccess(it.patient, benificiarySaveResponse)
                }
                is NetworkResult.Error -> {
                    updatePatientSyncingFailed(it.patient)
                    if(response.code == socketTimeoutException){
                        throw SocketTimeoutException("This is an example exception message")
                    }
                    return false;
                }
                else -> {}
            }
        }

        return true;

    }



}