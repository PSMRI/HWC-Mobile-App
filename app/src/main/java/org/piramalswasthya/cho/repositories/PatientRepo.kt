package org.piramalswasthya.cho.repositories

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.cho.database.room.InAppDb
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.CaseRecordeDao
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.database.room.dao.VisitReasonsAndCategoriesDao
import org.piramalswasthya.cho.database.room.dao.VitalsDao
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.model.PatientNetwork
import org.piramalswasthya.cho.model.UserDomain
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.BenHealthDetails
import org.piramalswasthya.cho.network.BenificiarySaveResponse
import org.piramalswasthya.cho.network.DistrictList
import org.piramalswasthya.cho.network.GetBenHealthIdRequest
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
    private val apiService: AmritApiService,
    private val caseRecordeRepo: CaseRecordeRepo,
    private val visitReasonsAndCategoriesRepo: VisitReasonsAndCategoriesRepo,
    private val vitalsRepo: VitalsRepo,
    private val patientVisitInfoSyncRepo: PatientVisitInfoSyncRepo
) {

    suspend fun insertPatient(patient: Patient) {
        patientDao.insertPatient(patient)
    }
    suspend fun getBenFromId(benId: Long): Patient? {
        return withContext(Dispatchers.IO) {
            patientDao.getBen(benId)
        }
    }
    suspend fun updateRecord(it: Patient) {
        withContext(Dispatchers.IO) {
            patientDao.updatePatient(it)
        }
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
//    suspend fun getPatientListFlow() : Flow<List<PatientDisplay>> {
//        return patientDao.getPatientListFlow()
//    }

    suspend fun getPatient(patientId : String) : Patient{
        return patientDao.getPatient(patientId)
    }

    suspend fun updateNurseSubmitted(patientId : String) {
        patientDao.updateNurseSubmitted(patientId)
    }

    suspend fun updateDoctorSubmitted(patientId : String) {
        patientDao.updateDoctorSubmitted(patientId)
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
                    val result = Gson().fromJson(data, BenificiarySaveResponse::class.java)
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
                    updatePatientSyncSuccess(it.patient, benificiarySaveResponse)
                    caseRecordeRepo.updateBenIdAndBenRegId(
                        beneficiaryID = benificiarySaveResponse.beneficiaryID,
                        beneficiaryRegID = benificiarySaveResponse.beneficiaryRegID,
                        patientID = it.patient.patientID
                    )
                    visitReasonsAndCategoriesRepo.updateBenIdAndBenRegId(
                        beneficiaryID = benificiarySaveResponse.beneficiaryID,
                        beneficiaryRegID = benificiarySaveResponse.beneficiaryRegID,
                        patientID = it.patient.patientID
                    )
                    vitalsRepo.updateBenIdBenRegId(
                        beneficiaryID = benificiarySaveResponse.beneficiaryID,
                        beneficiaryRegID = benificiarySaveResponse.beneficiaryRegID,
                        patientID = it.patient.patientID
                    )
                    patientVisitInfoSyncRepo.updatePatientVisitInfoBenIdAndBenRegId(
                        beneficiaryID = benificiarySaveResponse.beneficiaryID,
                        beneficiaryRegID = benificiarySaveResponse.beneficiaryRegID,
                        patientID = it.patient.patientID
                    )
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

    suspend fun getBeneficiaryWithId(benRegId: Long): BenHealthDetails? {
        try {
            val response = apiService
                .getBenHealthID(GetBenHealthIdRequest(benRegId, null))
            if (response.isSuccessful) {
                val responseBody = response.body()?.string()

                when (responseBody?.let { JSONObject(it).getInt("statusCode") }) {
                    200 -> {
                        val jsonObj = JSONObject(responseBody)
                        val data = jsonObj.getJSONObject("data").getJSONArray("BenHealthDetails")
                            .toString()
                        val bens = Gson().fromJson(data, Array<BenHealthDetails>::class.java)
                        return if (bens.isNotEmpty()) {
                            bens.last()
                        } else {
                            null
                        }
                    }

                    5000, 5002 -> {
                        if (JSONObject(responseBody).getString("errorMessage")
                                .contentEquals("Invalid login key or session is expired")
                        ) {
                            val user = userRepo.getLoggedInUser()!!
                            userRepo.refreshTokenTmc(user.userName, user.password)
                            return getBeneficiaryWithId(benRegId)
                        } else {
                            NetworkResult.Error(
                                0,
                                JSONObject(responseBody).getString("errorMessage")
                            )
                        }
                    }

                    else -> {
                        NetworkResult.Error(0, responseBody.toString())
                    }
                }
            }
        } catch (_: java.lang.Exception) {
        }
        return null
    }




}