package org.piramalswasthya.cho.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.cho.database.room.dao.CaseRecordeDao
import org.piramalswasthya.cho.database.room.dao.HistoryDao
import org.piramalswasthya.cho.database.room.dao.PatientVisitInfoSyncDao
import org.piramalswasthya.cho.model.BenFlow

import org.piramalswasthya.cho.model.PatientDoctorFormUpsync
import org.piramalswasthya.cho.model.PatientNetwork
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.model.PatientVisitInformation
import org.piramalswasthya.cho.model.PrescriptionCaseRecord
import org.piramalswasthya.cho.model.UserDomain
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.BenificiarySaveResponse
import org.piramalswasthya.cho.network.NetworkResponse
import org.piramalswasthya.cho.network.NetworkResult
import org.piramalswasthya.cho.network.NurseDataResponse
import org.piramalswasthya.cho.network.networkResultInterceptor
import org.piramalswasthya.cho.network.refreshTokenInterceptor
import org.piramalswasthya.cho.network.socketTimeoutException
import org.piramalswasthya.cho.patient.patient
import org.piramalswasthya.cho.utils.nullIfEmpty
import timber.log.Timber
import java.lang.Exception
import java.net.SocketTimeoutException
import javax.inject.Inject


class BenVisitRepo @Inject constructor(
    private val caseRecordDao: CaseRecordeDao,
    private val visitReasonsAndCategoriesRepo: VisitReasonsAndCategoriesRepo,
    private val vitalsRepo: VitalsRepo,
    private val doctorMasterDataMaleRepo: DoctorMasterDataMaleRepo,
    private val historyRepo: HistoryRepo,
    private val caseRecordeRepo: CaseRecordeRepo,
    private val userRepo: UserRepo,
    private val apiService: AmritApiService,
    private val patientVisitInfoSyncRepo: PatientVisitInfoSyncRepo,
    private val benFlowRepo: BenFlowRepo,
    private val patientRepo: PatientRepo,

) {

    suspend fun registerNurseData(patientVisitInfo: PatientVisitInformation): NetworkResult<NetworkResponse> {

        return networkResultInterceptor {
            val response = apiService.saveNurseData(patientVisitInfo)
            val responseBody = response.body()?.string()
            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
                    val data = responseBody.let { JSONObject(it).getString("data") }
                    val result = Gson().fromJson(data, NurseDataResponse::class.java)
                    NetworkResult.Success(result)
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    registerNurseData(patientVisitInfo)
                },
            )
        }

    }

    suspend fun registerDoctorData(patientDoctorForm: PatientDoctorFormUpsync): NetworkResult<NetworkResponse> {

        return networkResultInterceptor {
            val response = apiService.saveDoctorData(patientDoctorForm)
            val responseBody = response.body()?.string()
            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
                    Log.i("doctor data submitted",  response.body()?.string() ?: "")
//                    val data = responseBody.let { JSONObject(it).getString("data") }
//                    val result = Gson().fromJson(data, NurseDataResponse::class.java)
                    NetworkResult.Success(NetworkResponse())
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    registerDoctorData(patientDoctorForm)
                },
            )
        }

    }

    suspend fun processUnsyncedNurseData() : Boolean{

        val patientNurseDataUnSyncList = patientVisitInfoSyncRepo.getPatientNurseDataUnsynced()
        val user = userRepo.getLoggedInUser()

        patientNurseDataUnSyncList.forEach {

            if(it.patient.beneficiaryRegID != null){

                withContext(Dispatchers.IO){

                    val benFlow = benFlowRepo.getBenFlowByBenRegIdAndBenVisitNo(it.patient.beneficiaryRegID!!, it.patientVisitInfoSync.benVisitNo!!)

                    if(benFlow != null && benFlow.nurseFlag == 1){

                        val visit = visitReasonsAndCategoriesRepo.getVisitDbByPatientIDAndBenVisitNo(patientID = it.patient.patientID, benVisitNo = benFlow.benVisitNo!!)
                        val chiefComplaints = visitReasonsAndCategoriesRepo.getChiefComplaintsByPatientIDAndBenVisitNo(patientID = it.patient.patientID, benVisitNo = benFlow.benVisitNo!!)
                        val vitals = vitalsRepo.getPatientVitalsByPatientIDAndBenVisitNo(patientID = it.patient.patientID, benVisitNo = benFlow.benVisitNo!!)

                        val patientVisitInfo = PatientVisitInformation(
                            user = user,
                            visit = visit,
                            chiefComplaints = chiefComplaints,
                            vitals = vitals,
                            benFlow = benFlow
                        )

                        patientVisitInfoSyncRepo.updatePatientNurseDataSyncSyncing(it.patientVisitInfoSync.patientID, it.patientVisitInfoSync.benVisitNo)

                        when(val response = registerNurseData(patientVisitInfo)){
                            is NetworkResult.Success -> {
                                val nurseDataResponse = response.data as NurseDataResponse
                                if(nurseDataResponse.visitCode != null && nurseDataResponse.visitID != null){
                                    val visitCode = nurseDataResponse.visitCode.toLong()
                                    val benVisitID = nurseDataResponse.visitID
                                    patientRepo.updateNurseSubmitted(it.patientVisitInfoSync.patientID)
                                    benFlowRepo.updateNurseCompletedAndVisitCode(visitCode = visitCode, benVisitID = benVisitID, benFlowID = benFlow.benFlowID)
                                    patientVisitInfoSyncRepo.updatePatientNurseDataSyncSuccess(it.patientVisitInfoSync.patientID, it.patientVisitInfoSync.benVisitNo)
                                } else {
                                    patientVisitInfoSyncRepo.updatePatientNurseDataSyncFailed(it.patientVisitInfoSync.patientID, it.patientVisitInfoSync.benVisitNo)
                                }
                            }
                            is NetworkResult.Error -> {
                                patientVisitInfoSyncRepo.updatePatientNurseDataSyncFailed(it.patientVisitInfoSync.patientID, it.patientVisitInfoSync.benVisitNo)
                                if(response.code == socketTimeoutException){
                                    throw SocketTimeoutException("This is an example exception message")
                                }
//                                return@withContext false;
                            }
                            else -> { }
                        }

                    } else { }
                }
            }
        }

        return true

    }


    suspend fun processUnsyncedDoctorData() : Boolean{

        val patientDoctorDataUnSyncList = patientVisitInfoSyncRepo.getPatientDoctorDataUnsynced()
        val user = userRepo.getLoggedInUser()

        patientDoctorDataUnSyncList.forEach {

            if(it.patient.beneficiaryRegID != null){
                withContext(Dispatchers.IO){

                    val benFlow = benFlowRepo.getBenFlowByBenRegIdAndBenVisitNo(it.patient.beneficiaryRegID!!, it.patientVisitInfoSync.benVisitNo)
                    if(benFlow != null && benFlow.nurseFlag == 9 && benFlow.doctorFlag == 1){

                        val diagnosisCaseRecordVal = caseRecordeRepo.getDiagnosisCaseRecordByPatientIDAndBenVisitNo(patientID = it.patientVisitInfoSync.patientID, benVisitNo = it.patientVisitInfoSync.benVisitNo)
                        val investigationCaseRecordVal = caseRecordeRepo.getInvestigationCaseRecordByPatientIDAndBenVisitNo(patientID = it.patientVisitInfoSync.patientID, benVisitNo = it.patientVisitInfoSync.benVisitNo)
                        val prescriptionCaseRecordVal = caseRecordeRepo.getPrescriptionCaseRecordeByPatientIDAndBenVisitNo(patientID = it.patientVisitInfoSync.patientID, benVisitNo = it.patientVisitInfoSync.benVisitNo)
                        val procedureList = historyRepo.getProceduresList(investigationCaseRecordVal?.investigationCaseRecord?.testIds)

//                        val patientVisitInfo = PatientVisitInformation(
//                            user = user,
//                            visit = visit,
//                            chiefComplaints = chiefComplaints,
//                            vitals = vitals,
//                            benFlow = benFlow
//                        )

                        val patientDoctorForm = PatientDoctorFormUpsync(
                            user = user,
                            benFlow = benFlow,
                            diagnosisList = diagnosisCaseRecordVal,
                            investigation = investigationCaseRecordVal,
                            prescriptionList = prescriptionCaseRecordVal,
                            procedureList = procedureList
                        )

                        patientVisitInfoSyncRepo.updatePatientDoctorDataSyncSyncing(it.patient.patientID)

                        when(val response = registerDoctorData(patientDoctorForm)){
                            is NetworkResult.Success -> {
                                patientRepo.updateDoctorSubmitted(it.patient.patientID)
                                benFlowRepo.updateDoctorCompleted(benFlowID = benFlow.benFlowID)
                                patientVisitInfoSyncRepo.updatePatientDoctorDataSyncSuccess(it.patient.patientID)
                            }
                            is NetworkResult.Error -> {
                                patientVisitInfoSyncRepo.updatePatientDoctorDataSyncFailed(it.patient.patientID)
                                if(response.code == socketTimeoutException){
                                    throw SocketTimeoutException("This is an example exception message")
                                }
//                                return@withContext false;
                            }
                            else -> {}
                        }
                    } else { }
                }
            }
        }

        return true

    }


}