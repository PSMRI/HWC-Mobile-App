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
import org.piramalswasthya.cho.model.Diagnosis
import org.piramalswasthya.cho.model.DiagnosisCaseRecord
import org.piramalswasthya.cho.model.Investigation
import org.piramalswasthya.cho.model.InvestigationCaseRecord
import org.piramalswasthya.cho.model.Laboratory
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.model.PatientDoctorForm
import org.piramalswasthya.cho.model.PatientNetwork
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.model.PatientVisitInformation
import org.piramalswasthya.cho.model.Prescription
import org.piramalswasthya.cho.model.PrescriptionCaseRecord
import org.piramalswasthya.cho.model.ProvisionalDiagnosis
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

    suspend fun registerDoctorData(patientDoctorForm: PatientDoctorForm): NetworkResult<NetworkResponse> {

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

            if(it.beneficiaryRegID != null){
                withContext(Dispatchers.IO){

                    val benFlow = benFlowRepo.getBenFlowByBenRegIdAndBenVisitNo(it.beneficiaryRegID!!, it.benVisitNo!!)
                    if(benFlow != null && benFlow.nurseFlag == 1){

                        val visit = visitReasonsAndCategoriesRepo.getVisitDbByBenRegIdAndBenVisitNo(beneficiaryRegID = benFlow.beneficiaryRegID!!, benVisitNo = benFlow.benVisitNo!!)
                        val chiefComplaints = visitReasonsAndCategoriesRepo.getChiefComplaintsByBenRegIdAndBenVisitNo(beneficiaryRegID = benFlow.beneficiaryRegID!!, benVisitNo = benFlow.benVisitNo!!)
                        val vitals = vitalsRepo.getPatientVitalsByBenRegIdAndBenVisitNo(beneficiaryRegID = benFlow.beneficiaryRegID!!, benVisitNo = benFlow.benVisitNo!!)

                        val patientVisitInfo = PatientVisitInformation(
                            user = user,
                            visit = visit,
                            chiefComplaints = chiefComplaints,
                            vitals = vitals,
                            benFlow = benFlow
                        )

                        patientVisitInfoSyncRepo.updatePatientNurseDataSyncSyncing(it.patientID)

                        when(val response = registerNurseData(patientVisitInfo)){
                            is NetworkResult.Success -> {
                                val nurseDataResponse = response.data as NurseDataResponse
                                val visitCode = nurseDataResponse.visitCode.toLong()
                                val benVisitID = nurseDataResponse.visitID
                                patientRepo.updateNurseSubmitted(it.patientID)
                                benFlowRepo.updateNurseCompletedAndVisitCode(visitCode = visitCode, benVisitID = benVisitID, benFlowID = benFlow.benFlowID)
                                patientVisitInfoSyncRepo.updatePatientNurseDataSyncSuccess(it.patientID)
                            }
                            is NetworkResult.Error -> {
                                patientVisitInfoSyncRepo.updatePatientNurseDataSyncFailed(it.patientID)
                                if(response.code == socketTimeoutException){
                                    throw SocketTimeoutException("This is an example exception message")
                                }
                                return@withContext false;
                            }
                            else -> {}
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

            if(it.beneficiaryRegID != null){
                withContext(Dispatchers.IO){

                    val benFlow = benFlowRepo.getBenFlowByBenRegId(it.beneficiaryRegID!!)
                    if(benFlow != null && benFlow.nurseFlag == 9 && benFlow.doctorFlag == 1){

                        val diagnosisCaseRecordVal = caseRecordeRepo.getDiagnosisCaseRecordByBenRegIdAndPatientID(beneficiaryRegID = benFlow.beneficiaryRegID!!, patientID = patient.id!!)
                        val investigationCaseRecordVal = caseRecordeRepo.getInvestigationCaseRecordByBenRegIdAndPatientID(beneficiaryRegID = benFlow.beneficiaryRegID!!,patientID = patient.id!!)
                        val prescriptionCaseRecordVal = caseRecordeRepo.getPrescriptionCaseRecordeByBenRegIdAndPatientID(beneficiaryRegID = benFlow.beneficiaryRegID!!,patientID = patient.id!!)
//                        val patientVisitInfo = PatientVisitInformation(
//                            user = user,
//                            visit = visit,
//                            chiefComplaints = chiefComplaints,
//                            vitals = vitals,
//                            benFlow = benFlow
//                        )
                        val diagnosisList = diagnosisCaseRecordVal?.map { diagnosisCaseRecord ->
                            val provisionalDiagnosis =
                                ProvisionalDiagnosis(term = diagnosisCaseRecord.diagnosis)

                            // Create a Diagnosis object based on the diagnosisCaseRecord and other data
                            Diagnosis(
                                prescriptionID = null, // Fill this as needed
                                vanID = user?.vanId,
                                parkingPlaceID = user?.parkingPlaceId,
                                provisionalDiagnosisList = listOf(provisionalDiagnosis), // Add the provisionalDiagnosis to the list
                                beneficiaryRegID = benFlow?.beneficiaryID.toString(),
                                benVisitID = benFlow?.benVisitID.toString(),
                                visitCode = benFlow?.visitCode.toString(),
                                providerServiceMapID = user?.serviceMapId.toString(),
                                createdBy = user?.userName,
                                isSpecialist = false // Update this value as needed
                            )
                        }
                        val investigationIDs = investigationCaseRecordVal?.testIds?.split(",")?.map { it.toInt() }

                        val laboratoryList = mutableListOf<Laboratory>()

                        if (investigationIDs != null) {
                            for (investigationID in investigationIDs) {
                                val laboratoryData = historyRepo.getProcedureByProcedureId(investigationID)

                                val laboratory = Laboratory(
                                    procedureID = laboratoryData.procedureID,
                                    procedureName = laboratoryData.procedureName,
                                    procedureDesc = laboratoryData.procedureDesc,
                                    procedureType = laboratoryData.procedureType,
                                    gender = laboratoryData.gender,
                                    providerServiceMapID = laboratoryData.providerServiceMapID
                                )
                                laboratoryList.add(laboratory)
                            }
                        }

                        val investigationCaseRecord = Investigation(
                            externalInvestigations = investigationCaseRecordVal?.externalInvestigation,
                            vanID = user?.vanId,
                            parkingPlaceID = user?.parkingPlaceId,
                            beneficiaryRegID = benFlow?.beneficiaryRegID.toString(),
                            benVisitID = benFlow?.benVisitID.toString(),
                            visitCode = benFlow?.visitCode.toString(),
                            providerServiceMapID = user?.serviceMapId.toString(),
                            createdBy = user?.userName,
                            isSpecialist = false,
                            laboratoryList = laboratoryList
                        )

                        val prescriptionList = mutableListOf<Prescription>()

                        if (prescriptionCaseRecordVal != null) {
                            for (prescriptionRecord in prescriptionCaseRecordVal) {

                                val itemMasterData = prescriptionRecord.itemId?.let { it1 ->
                                    doctorMasterDataMaleRepo.getItemMasterListById(
                                        it1
                                    )
                                }

                                val prescription = Prescription(
                                    id = null,
                                    drugID = itemMasterData?.id,
                                    drugName = itemMasterData?.itemName,
                                    drugStrength = itemMasterData?.strength,
                                    formName = itemMasterData?.itemName,
                                    formID = itemMasterData?.itemFormID,
                                    dose = prescriptionRecord.instruciton,
                                    qtyPrescribed = prescriptionRecord?.unit?.toInt(),
                                    frequency = prescriptionRecord.frequency,
                                    duration = prescriptionRecord?.duration?.toInt(),
                                    route = null,
                                    durationView = null,
                                    unit = null,
                                    instructions = prescriptionRecord.instruciton,
                                    sctCode = null,
                                    sctTerm = null,
                                    createdBy = user?.userName,
                                    vanID = user?.vanId,
                                    parkingPlaceID = user?.parkingPlaceId,
                                    isEDL = itemMasterData?.isEDL
                                )

                                // Add the Prescription object to the list
                                prescriptionList.add(prescription)
                            }
                        }

// Now you have a list of Prescription objects


                        val patientDoctorForm = PatientDoctorForm(
                            user = user,
                            benFlow = benFlow,
                            diagnosis = diagnosisList,
                            investigation=investigationCaseRecord,
                            prescription = prescriptionList,
                            refer = investigationCaseRecord.insti
                        )

                        patientVisitInfoSyncRepo.updatePatientDoctorDataSyncSyncing(it.patientID)

                        when(val response = registerDoctorData(patientDoctorForm)){
                            is NetworkResult.Success -> {
                                patientRepo.updateDoctorSubmitted(it.patientID)
                                benFlowRepo.updateDoctorCompleted(benFlowID = benFlow.benFlowID)
                                patientVisitInfoSyncRepo.updatePatientDoctorDataSyncSuccess(it.patientID)
                            }
                            is NetworkResult.Error -> {
                                patientVisitInfoSyncRepo.updatePatientDoctorDataSyncFailed(it.patientID)
                                if(response.code == socketTimeoutException){
                                    throw SocketTimeoutException("This is an example exception message")
                                }
                                return@withContext false;
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