package org.piramalswasthya.cho.repositories

import android.net.Network
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.cho.database.room.InAppDb
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.model.LocationRequest
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
    private val database: InAppDb,
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

    suspend fun deletePreviousAndInsertNew(patient: Patient, newPatientId: String){
        patientDao.deletePatient(patient.patientID);
        patient.patientID = newPatientId;
        patient.syncState = SyncState.SYNCED;
        patientDao.insertPatient(patient);
    }

    suspend fun getPatientList() : List<PatientDisplay>{
        return patientDao.getPatientList()
    }

    suspend fun registerNewPatient(patient : PatientDisplay, user: UserDomain?): NetworkResult<NetworkResponse> {

        return networkResultInterceptor {
            val patNet = PatientNetwork(patient, user)
            val response = apiService.saveBenificiaryDetails(patNet)
            val responseBody = response.body()?.string()
            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
                    val data = responseBody.let { JSONObject(it).getString("data") }
                    Log.i("data saved is", data)
                    val result = BenificiarySaveResponse(data)
                    NetworkResult.Success(result)
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    registerNewPatient(patient, user)
                },
            )
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
                    var newPatientID = benificiarySaveResponse.response.split("}").first().split(" ").last()
                    newPatientID = newPatientID.substring(0, newPatientID.length - 1)
                    deletePreviousAndInsertNew(it.patient, newPatientID)
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