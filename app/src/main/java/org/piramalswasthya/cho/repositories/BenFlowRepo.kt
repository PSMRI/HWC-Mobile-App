package org.piramalswasthya.cho.repositories

import android.util.Log
import androidx.room.Transaction
import com.google.gson.Gson
import org.json.JSONObject
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.BenFlowDao
import org.piramalswasthya.cho.database.room.dao.CaseRecordeDao
import org.piramalswasthya.cho.database.room.dao.InvestigationDao
import org.piramalswasthya.cho.database.room.dao.PatientVisitInfoSyncDao
import org.piramalswasthya.cho.database.room.dao.PrescriptionDao
import org.piramalswasthya.cho.database.room.dao.ProcedureDao
import org.piramalswasthya.cho.database.room.dao.VisitReasonsAndCategoriesDao
import org.piramalswasthya.cho.database.room.dao.VitalsDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.BenDetailsDownsync
import org.piramalswasthya.cho.model.BenFlow
import org.piramalswasthya.cho.model.BenNewFlow
import org.piramalswasthya.cho.model.ChiefComplaintDB
import org.piramalswasthya.cho.model.ComponentDetails
import org.piramalswasthya.cho.model.ComponentOption
import org.piramalswasthya.cho.model.DiagnosisCaseRecord
import org.piramalswasthya.cho.model.DoctorDataDownSync
import org.piramalswasthya.cho.model.InvestigationCaseRecord
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.model.PatientVitalsModel
import org.piramalswasthya.cho.model.PrescriptionCaseRecord
import org.piramalswasthya.cho.model.Procedure
import org.piramalswasthya.cho.model.ProcedureDTO
import org.piramalswasthya.cho.model.UserDomain
import org.piramalswasthya.cho.model.VisitDB
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.LabProceduresDataRequest
import org.piramalswasthya.cho.network.NetworkResponse
import org.piramalswasthya.cho.network.NetworkResult
import org.piramalswasthya.cho.network.NurseDataRequest
import org.piramalswasthya.cho.network.NurseDataResponse
import org.piramalswasthya.cho.network.VillageIdList
import org.piramalswasthya.cho.network.networkResultInterceptor
import org.piramalswasthya.cho.network.refreshTokenInterceptor
import org.piramalswasthya.cho.network.socketTimeoutException
import org.piramalswasthya.cho.utils.generateUuid
import java.net.SocketTimeoutException
import javax.inject.Inject

class BenFlowRepo @Inject constructor(
    private val userRepo: UserRepo,
    private val apiService: AmritApiService,
    private val preferenceDao: PreferenceDao,
    private val benFlowDao: BenFlowDao,
    private val visitReasonsAndCategoriesDao: VisitReasonsAndCategoriesDao,
    private val vitalsDao: VitalsDao,
    private val patientRepo: PatientRepo,
    private val patientVisitInfoSyncDao: PatientVisitInfoSyncDao,
    private val investigationDao: InvestigationDao,
    private val prescriptionDao: PrescriptionDao,
    private val procedureDao: ProcedureDao,
    private val caseRecordeDao: CaseRecordeDao
) {

    suspend fun getBenFlowByBenRegId(beneficiaryRegID: Long) : BenFlow?{
        return benFlowDao.getBenFlowByBenRegId(beneficiaryRegID)
    }

    suspend fun getBenFlowByBenRegIdAndBenVisitNo(beneficiaryRegID: Long, benVisitNo: Int) : BenFlow?{
        return benFlowDao.getBenFlowByBenRegIdAndBenVisitNo(beneficiaryRegID, benVisitNo)
    }

    private fun convertStringToIntList(villageIds : String) : List<Int>{
        return villageIds.split(",").map {
            it.trim().toInt()
        }
    }

    suspend fun createNewBenflow(user: UserDomain, patientDisplay: PatientDisplay, patientVisitInfoSync: PatientVisitInfoSync): NetworkResult<NetworkResponse> {

        return networkResultInterceptor {
            val nurseNewBenflowRequest = BenNewFlow(user = user, patientDisplay = patientDisplay)
            val response = apiService.createBenReVisitToNurse(nurseNewBenflowRequest)
            val responseBody = response.body()?.string()

            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
                    val data = responseBody.let { JSONObject(it).getString("data") }
                    Log.i("create benflow response is", data)
                    patientVisitInfoSyncDao.updateCreateBenflowFlag(patientID = patientDisplay.patient.patientID, benVisitNo = patientVisitInfoSync.benVisitNo)
                    NetworkResult.Success(NetworkResponse())
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    createNewBenflow(user, patientDisplay, patientVisitInfoSync)
                },
            )
        }

    }

    suspend fun createRevisitBenflowRecords(): Boolean {

        val unsyncedRevisitRecords = patientVisitInfoSyncDao.getUnsyncedRevisitRecords()

        unsyncedRevisitRecords.forEach{

            val patientDisplay = patientRepo.getPatientDisplay(it.patientID)
            val user = userRepo.getLoggedInUser()

            if(user != null){
                when(val response = createNewBenflow(user, patientDisplay, it)){
                    is NetworkResult.Success -> {

                    }
                    is NetworkResult.Error -> {
                        if(response.code == socketTimeoutException){
                            throw SocketTimeoutException("This is an example exception message")
                        }
//                        return false
                    }
                    else -> {}
                }
            }
        }

        return true;

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
    suspend fun refreshDoctorData(prescriptionCaseRecord: List<PrescriptionCaseRecord>?, investigationCaseRecord: InvestigationCaseRecord, diagnosisCaseRecords : MutableList<DiagnosisCaseRecord>,patient: Patient, benFlow: BenFlow){

        prescriptionDao.deletePrescriptionByPatientId(patient.patientID)
        prescriptionCaseRecord?.let {
            prescriptionDao.insertAll(it)
        }

        investigationDao.deleteInvestigationCaseRecordByPatientId(patient.patientID)
        investigationDao.insertInvestigation(investigationCaseRecord)

        caseRecordeDao.deleteDiagnosisByPatientId(patient.patientID)
        diagnosisCaseRecords?.let {
            caseRecordeDao.insertAll(it)
        }

        val patientVisitInfoSync = PatientVisitInfoSync(
            patientID = patient.patientID,
            doctorDataSynced = SyncState.SYNCED,
            nurseFlag = benFlow.nurseFlag,
            doctorFlag = benFlow.doctorFlag,
            pharmacist_flag = benFlow.pharmacist_flag
        )
        patientVisitInfoSyncDao.insertPatientVisitInfoSync(patientVisitInfoSync)
    }

    private suspend fun getAndSaveDoctorDataToDb(benFlow: BenFlow, patient: Patient): NetworkResult<NetworkResponse> {

        return networkResultInterceptor {
            val doctorDataRequest = NurseDataRequest(benRegID = benFlow.beneficiaryRegID!!, visitCode = benFlow.visitCode!!)

            val response = apiService.getDoctorData(doctorDataRequest)
            val responseBody = response.body()?.string()

            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
//                    val data = responseBody.let { JSONObject(it).getString("data") }
//                    Log.i("Your response data is","$data")
//
//                    val docData = Gson().fromJson(data, DoctorDataDownSync::class.java)
//                    val prescriptionCaseRecords = docData.prescription?.map{
//                        PrescriptionCaseRecord(
//                            prescriptionCaseRecordId = it.prescriptionID.toString(),
//                            itemId = it.drugID,
//                            frequency = it.frequency,
//                            duration = it.duration,
//                            instruciton = null,
//                            unit = it.unit,
//                            patientID = patient.patientID,
//                            beneficiaryID = patient.beneficiaryID,
//                            beneficiaryRegID = patient.beneficiaryRegID,
//                            benFlowID = benFlow.benFlowID
//                        )
//                    }
//
//                    val investigation = docData.investigation
//                    val investigationVal = InvestigationCaseRecord(
//                        investigationCaseRecordId = generateUuid(),
//                        testIds = null,
//                        externalInvestigation = null,
//                        counsellingTypes = null,
//                        institutionId = docData.Refer?.referredToInstituteID,
//                        patientID = patient.patientID,
//                        beneficiaryID = patient.beneficiaryID,
//                        beneficiaryRegID = patient.beneficiaryRegID,
//                        benFlowID = benFlow.benFlowID)
//
//                    val diagnosisDoc = docData.diagnosis
//                    var diagnosisCaseRecords = mutableListOf<DiagnosisCaseRecord>()
//                    if(diagnosisDoc != null){
//                        diagnosisDoc.provisionalDiagnosisList?.map {
//                            val  diagnosisCaseRecord = DiagnosisCaseRecord(
//                                diagnosisCaseRecordId = generateUuid(),
//                                diagnosis = it.term,
//                                patientID = patient.patientID,
//                                beneficiaryID = patient.beneficiaryID,
//                                beneficiaryRegID = patient.beneficiaryRegID,
//                                benFlowID = benFlow.benFlowID)
//                            Log.i("diagnosisCaseRecord data is ","$diagnosisCaseRecord")
//                            diagnosisCaseRecords.add(diagnosisCaseRecord)
//                        }
//                    }

//                    refreshDoctorData(prescriptionCaseRecord = prescriptionCaseRecords, investigationVal,diagnosisCaseRecords, patient = patient, benFlow = benFlow)
                    NetworkResult.Success(NetworkResponse())
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    getAndSaveDoctorDataToDb(benFlow, patient)
                },
            )
        }

    }

    @Transaction
    suspend fun refreshNurseData(visit: VisitDB, vitals: PatientVitalsModel, chiefComplaints: List<ChiefComplaintDB>?, patient: Patient, benFlow: BenFlow, patientVisitInfoSync: PatientVisitInfoSync){

        visitReasonsAndCategoriesDao.deleteVisitDbByPatientIdAndBenVisitNo(patient.patientID, benFlow.benVisitNo!!)
        visitReasonsAndCategoriesDao.insertVisitDB(visit)
        visitReasonsAndCategoriesDao.deleteChiefComplaintsByPatientIdAndBenVisitNo(patient.patientID, benFlow.benVisitNo!!)
        chiefComplaints?.let {
            visitReasonsAndCategoriesDao.insertAll(it)
        }
        vitalsDao.deletePatientVitalsByPatientIdAndBenVisitNo(patient.patientID, benFlow.benVisitNo!!)
        vitalsDao.insertPatientVitals(vitals)

        patientVisitInfoSync.nurseDataSynced = SyncState.SYNCED
        patientVisitInfoSyncDao.insertPatientVisitInfoSync(patientVisitInfoSync)

//        val existingPatientVisitInfoSync = patientVisitInfoSyncDao.getPatientVisitInfoSync(patient.patientID)
//
//        if(existingPatientVisitInfoSync != null && benFlow.benVisitNo > existingPatientVisitInfoSync.benVisitNo!!){
//            existingPatientVisitInfoSync.benVisitNo = benFlow.benVisitNo
//            existingPatientVisitInfoSync.nurseDataSynced = SyncState.SYNCED
//            existingPatientVisitInfoSync.nurseFlag = benFlow.nurseFlag
//            existingPatientVisitInfoSync.doctorFlag = benFlow.doctorFlag
//            existingPatientVisitInfoSync.pharmacist_flag = benFlow.pharmacist_flag
//            patientVisitInfoSyncDao.insertPatientVisitInfoSync(existingPatientVisitInfoSync)
//        }
//        else if(existingPatientVisitInfoSync == null){
//            val patientVisitInfoSync = PatientVisitInfoSync(
//                patientID = patient.patientID,
//                beneficiaryID = benFlow.beneficiaryID,
//                beneficiaryRegID = benFlow.beneficiaryRegID,
//                nurseDataSynced = SyncState.SYNCED,
//                nurseFlag = benFlow.nurseFlag,
//                doctorFlag = benFlow.doctorFlag,
//                pharmacist_flag = benFlow.pharmacist_flag,
//                benVisitNo = benFlow.benVisitNo
//            )
//            patientVisitInfoSyncDao.insertPatientVisitInfoSync(patientVisitInfoSync)
//        }

    }

    suspend fun getAndSaveNurseDataToDb(benFlow: BenFlow, patient: Patient, patientVisitInfoSync: PatientVisitInfoSync): NetworkResult<NetworkResponse> {

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
                    refreshNurseData(visit = visit, vitals = vitals, chiefComplaints = chiefComplaints, patient = patient, benFlow = benFlow, patientVisitInfoSync = patientVisitInfoSync)
                    NetworkResult.Success(NetworkResponse())
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    getAndSaveNurseDataToDb(benFlow, patient, patientVisitInfoSync)
                },
            )
        }

    }

    suspend fun checkAndDownsyncNurseData(benFlow: BenFlow, patient: Patient){
        val patientVisitInfoSync = patientVisitInfoSyncDao.getPatientVisitInfoSyncByPatientIdAndBenVisitNo(patientID = patient.patientID, benVisitNo = benFlow.benVisitNo!!)
        if(patientVisitInfoSync != null && benFlow.nurseFlag!! > 1 && benFlow.nurseFlag >= patientVisitInfoSync.nurseFlag!!){
            patientVisitInfoSync.nurseFlag = benFlow.nurseFlag
            getAndSaveNurseDataToDb(benFlow, patient, patientVisitInfoSync)
        }
    }

    suspend fun checkAndAddNewVisitInfo(benFlow: BenFlow, patient: Patient){
        val existingPatientVisitInfoSync = patientVisitInfoSyncDao.getPatientVisitInfoSyncByPatientIdAndBenVisitNo(patient.patientID, benFlow.benVisitNo!!)
        if(existingPatientVisitInfoSync == null){
            val patientVisitInfoSync = PatientVisitInfoSync(benFlow, patient)
            patientVisitInfoSyncDao.insertPatientVisitInfoSync(patientVisitInfoSync)
        }
    }

    suspend fun updateBenFlowId(benFlow: BenFlow, patient: Patient){
        patientVisitInfoSyncDao.updateBenFlowIdByPatientIdAndBenVisitNo(
            benFlowId = benFlow.benFlowID!!,
            patientID = patient.patientID,
            benVisitNo = benFlow.benVisitNo!!
        )
    }

    suspend fun insertBenFlow(benFlow: BenFlow) {
        benFlowDao.insertBenFlow(benFlow = benFlow)
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
//                        visitReasonsAndCategoriesRepo.updateBenFlowId(
//                            benFlowId = benFlow.benFlowID,
//                            beneficiaryRegID = benFlow.beneficiaryRegID!!,
//                            benVisitNo = benFlow.benVisitNo!!,
//                        )
//                        vitalsRepo.updateBenFlowId(
//                            benFlowId = benFlow.benFlowID,
//                            beneficiaryRegID = benFlow.beneficiaryRegID!!,
//                            benVisitNo = benFlow.benVisitNo!!,
//                        )
                        if(patient != null){
                            checkAndAddNewVisitInfo(benFlow, patient)
                            updateBenFlowId(benFlow, patient)
//                            checkAndDownsyncNurseData(benFlow, patient)
//                            if(benFlow.nurseFlag == 9 && benFlow.beneficiaryRegID != null && benFlow.visitCode != null){
//                                getAndSaveNurseDataToDb(benFlow, patient)
//                            }
//                            if(benFlow.doctorFlag != null &&  benFlow.doctorFlag > 1 && benFlow.beneficiaryRegID != null && benFlow.visitCode != null){
//                                getAndSaveDoctorDataToDb(benFlow, patient)
//                            }
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

    private suspend fun getAndSaveLabTechnicianDataToDb(benFlow: BenFlow, patient: Patient): NetworkResult<NetworkResponse> {
        return networkResultInterceptor {
            val labProceduresDataRequest = LabProceduresDataRequest(
                beneficiaryRegID = benFlow.beneficiaryRegID!!,
                visitCode = benFlow.visitCode!!,
                benVisitID = benFlow.benVisitID!!,
            )

            val response = apiService.getLabTestPrescribedProceduresList(labProceduresDataRequest)
            val responseBody = response.body()?.string()

            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
                    val jsonObj = JSONObject(responseBody)
                    val data = jsonObj.getJSONObject("data").getJSONArray("laboratoryList")
                        .toString()
                    val procedureDTO = Gson().fromJson(data, Array<ProcedureDTO>::class.java)
                    var patientVisitInfoSync =
                        benFlow.benVisitNo?.let {
                            patientVisitInfoSyncDao.getPatientVisitInfoSyncByPatientIdAndBenVisitNo(patient.patientID,
                                it
                            )
                        }
                    if (patientVisitInfoSync == null) {
                        patientVisitInfoSync = PatientVisitInfoSync(
                            patientID = patient.patientID,
                            doctorDataSynced = SyncState.SYNCED,
                            labDataSynced = SyncState.NOT_ADDED,
                            nurseFlag = benFlow.nurseFlag,
                            doctorFlag = benFlow.doctorFlag,
                            pharmacist_flag = benFlow.pharmacist_flag
                        )
                    }

                    procedureDao.deleteProcedure(labProceduresDataRequest.beneficiaryRegID)
                    procedureDTO.forEach { dto ->
                        val procedure = Procedure(
                            benRegId = labProceduresDataRequest.beneficiaryRegID,
                            procedureID = dto.procedureID,
                            procedureDesc = dto.procedureDesc,
                            procedureType = dto.procedureType,
                            procedureName = dto.procedureName,
                            prescriptionID = dto.prescriptionID,
                            isMandatory = dto.isMandatory
                        )
                        val procedureId = procedureDao.insert(procedure = procedure)
                        dto.compListDetails.forEach { componentDetailDTO ->
                            val componentDetails = ComponentDetails(
                                testComponentID = componentDetailDTO.testComponentID,
                                procedureID = procedureId,
                                rangeNormalMin = componentDetailDTO.range_normal_min,
                                rangeNormalMax = componentDetailDTO.range_normal_max,
                                rangeMax = componentDetailDTO.range_max,
                                rangeMin = componentDetailDTO.range_min,
                                isDecimal = componentDetailDTO.isDecimal,
                                inputType = componentDetailDTO.inputType,
                                measurementUnit = componentDetailDTO.measurementUnit,
                                testComponentDesc = componentDetailDTO.testComponentDesc,
                                testComponentName = componentDetailDTO.testComponentName
                            )
                            var componentId = procedureDao.insert(componentDetails)
                            componentDetailDTO.compOpt.forEach { option ->
                                option.name?.let {
                                    val compOption = ComponentOption(
                                        componentDetailsId = componentId,
                                        name = it
                                    )
                                    procedureDao.insert(compOption)
                                }
                            }
                        }

                        patientVisitInfoSyncDao.insertPatientVisitInfoSync(patientVisitInfoSync)
                    }

                    NetworkResult.Success(NetworkResponse())
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    getAndSaveLabTechnicianDataToDb(benFlow, patient)
                },
            )
        }
    }

    suspend fun updateNurseCompletedAndVisitCode(visitCode: Long, benVisitID: Long, benFlowID: Long){
        benFlowDao.updateNurseCompleted(visitCode, benVisitID, benFlowID)
    }

    suspend fun updateDoctorCompletedWithTest(benFlowID: Long){
        benFlowDao.updateDoctorCompletedWithTest(benFlowID)
    }

    suspend fun updateDoctorCompletedWithoutTest(benFlowID: Long){
        benFlowDao.updateDoctorCompletedWithoutTest(benFlowID)
    }


    suspend fun pullLabProcedureData(patientId: String?): Boolean {

        return try {
            patientId?.let { patientID ->

                val patient = patientRepo.getPatient(patientId)
                val benFlow = benFlowDao.getBenFlowByBenRegId(patient.beneficiaryRegID!!)
                if (benFlow?.visitCode != null && benFlow.benVisitID != null) {
                    getAndSaveLabTechnicianDataToDb(benFlow = benFlow, patient = patient)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

}