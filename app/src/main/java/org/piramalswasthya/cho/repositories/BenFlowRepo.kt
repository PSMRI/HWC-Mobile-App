package org.piramalswasthya.cho.repositories

import androidx.room.Transaction
import com.google.gson.Gson
import org.json.JSONObject
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.BenFlowDao
import org.piramalswasthya.cho.database.room.dao.PatientVisitInfoSyncDao
import org.piramalswasthya.cho.database.room.dao.VisitReasonsAndCategoriesDao
import org.piramalswasthya.cho.database.room.dao.VitalsDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.BenDetailsDownsync
import org.piramalswasthya.cho.model.BenFlow
import org.piramalswasthya.cho.model.ChiefComplaintDB
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.model.PatientVitalsModel
import org.piramalswasthya.cho.model.VisitDB
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.NetworkResponse
import org.piramalswasthya.cho.network.NetworkResult
import org.piramalswasthya.cho.network.NurseDataRequest
import org.piramalswasthya.cho.network.NurseDataResponse
import org.piramalswasthya.cho.network.VillageIdList
import org.piramalswasthya.cho.network.networkResultInterceptor
import org.piramalswasthya.cho.network.refreshTokenInterceptor
import org.piramalswasthya.cho.network.socketTimeoutException
import java.net.SocketTimeoutException
import javax.inject.Inject

class BenFlowRepo @Inject constructor(
    private val userRepo: UserRepo,
    private val apiService: AmritApiService,
    private val preferenceDao: PreferenceDao,
    private val benFlowDao: BenFlowDao,
    private val visitReasonsAndCategoriesRepo: VisitReasonsAndCategoriesRepo,
    private val visitReasonsAndCategoriesDao: VisitReasonsAndCategoriesDao,
    private val vitalsRepo: VitalsRepo,
    private val vitalsDao: VitalsDao,
    private val patientRepo: PatientRepo,
    private val patientVisitInfoSyncDao: PatientVisitInfoSyncDao,
) {

    suspend fun getBenFlowByBenRegId(beneficiaryRegID: Long) : BenFlow?{
        return benFlowDao.getBenFlowByBenRegId(beneficiaryRegID)
    }

    private fun convertStringToIntList(villageIds : String) : List<Int>{
        return villageIds.split(",").map {
            it.trim().toInt()
        }
    }

    suspend fun downloadAndSyncFlowRecords(): Boolean {

        val user = userRepo.getLoggedInUser()
        val villageList = VillageIdList(
            convertStringToIntList(user?.assignVillageIds ?: ""),
            preferenceDao.getLastSyncTime()
        )

        when(val response = syncFlowIds(villageList)){
            is NetworkResult.Success -> {
                return true
            }
            is NetworkResult.Error -> {
                if(response.code == socketTimeoutException){
                    throw SocketTimeoutException("This is an example exception message")
                }
                return false
            }
            else -> {}
        }
        return true
    }

    @Transaction
    suspend fun refreshNurseData(visit: VisitDB, vitals: PatientVitalsModel, chiefComplaints: List<ChiefComplaintDB>?, patient: Patient, benFlow: BenFlow){
        visitReasonsAndCategoriesDao.deleteVisitDbByPatientId(patient.patientID)
        visitReasonsAndCategoriesDao.insertVisitDB(visit)
        visitReasonsAndCategoriesDao.deleteChiefComplaintsByPatientId(patient.patientID)
        chiefComplaints?.let {
            visitReasonsAndCategoriesDao.insertAll(it)
        }
        vitalsDao.deletePatientVitalsByPatientId(patient.patientID)
        vitalsDao.insertPatientVitals(vitals)
        val patientVisitInfoSync = PatientVisitInfoSync(
            patientID = patient.patientID,
            beneficiaryID = benFlow.beneficiaryID,
            beneficiaryRegID = benFlow.beneficiaryRegID,
            nurseDataSynced = SyncState.SYNCED,
            nurseFlag = benFlow.nurseFlag,
            doctorFlag = benFlow.doctorFlag,
            pharmacist_flag = benFlow.pharmacist_flag
        )
        patientVisitInfoSyncDao.insertPatientVisitInfoSync(patientVisitInfoSync)
    }

    suspend fun getAndSaveNurseDataToDb(benFlow: BenFlow, patient: Patient): NetworkResult<NetworkResponse> {

        return networkResultInterceptor {
            val nurseDataRequest = NurseDataRequest(benRegID = benFlow.beneficiaryRegID!!, visitCode = benFlow.visitCode!!)
            val response = apiService.getNurseData(nurseDataRequest)
            val responseBody = response.body()?.string()

            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
                    val data = responseBody.let { JSONObject(it).getString("data") }
                    val nurseData = Gson().fromJson(data, BenDetailsDownsync::class.java)
                    val visit = VisitDB(nurseData, patient, benFlow)
                    val vitals = PatientVitalsModel(nurseData, patient, benFlow)
                    val chiefComplaints = nurseData.BenChiefComplaints?.map {
                        ChiefComplaintDB(it, patient, benFlow)
                    }
                    refreshNurseData(visit = visit, vitals = vitals, chiefComplaints = chiefComplaints, patient = patient, benFlow = benFlow)
                    NetworkResult.Success(NetworkResponse())
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    getAndSaveNurseDataToDb(benFlow, patient)
                },
            )
        }

    }

    suspend fun syncFlowIds(villageList: VillageIdList): NetworkResult<NetworkResponse> {

        return networkResultInterceptor {
            val response = apiService.getBenFlowRecords(villageList)
            val responseBody = response.body()?.string()
            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
                    val benflowArray = responseBody.let { JSONObject(it).getJSONArray("data") }
                    for (i in 0 until benflowArray.length()) {
                        val data = benflowArray.getString(i)
                        val benFlow = Gson().fromJson(data, BenFlow::class.java)
                        val patient = patientRepo.getPatientByBenRegId(benFlow.beneficiaryRegID!!)
                        benFlowDao.insertBenFlow(benFlow)
                        visitReasonsAndCategoriesRepo.updateBenFlowId(
                            benFlowId = benFlow.benFlowID,
                            beneficiaryRegID = benFlow.beneficiaryRegID!!
                        )
                        vitalsRepo.updateBenFlowId(
                            benFlowId = benFlow.benFlowID,
                            beneficiaryRegID = benFlow.beneficiaryRegID!!
                        )
                        if(benFlow.nurseFlag == 9 && benFlow.beneficiaryRegID != null && benFlow.visitCode != null && patient != null){
                            getAndSaveNurseDataToDb(benFlow, patient)
                        }
                    }
                    NetworkResult.Success(NetworkResponse())
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    syncFlowIds(villageList)
                },
            )
        }

    }

    suspend fun updateNurseCompletedAndVisitCode(visitCode: Long, benVisitID: Long, benFlowID: Long){
        benFlowDao.updateNurseCompleted(visitCode, benVisitID, benFlowID)
    }

    suspend fun updateDoctorCompleted(benFlowID: Long){
        benFlowDao.updateDoctorCompleted(benFlowID)
    }

}