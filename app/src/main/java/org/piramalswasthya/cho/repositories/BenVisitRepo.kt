package org.piramalswasthya.cho.repositories

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.BenFlowDao
import org.piramalswasthya.cho.database.room.dao.CaseRecordeDao
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.database.room.dao.PrescriptionDao
import org.piramalswasthya.cho.database.room.dao.ProcedureDao
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.model.ComponentResultDTO
import org.piramalswasthya.cho.model.LabResultDTO
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.PatientDoctorFormUpsync
import org.piramalswasthya.cho.model.PatientVisitInformation
import org.piramalswasthya.cho.model.PharmacistItemStockExitDataRequest
import org.piramalswasthya.cho.model.PharmacistPatientIssueDataRequest
import org.piramalswasthya.cho.model.PrescriptionBatchDTO
import org.piramalswasthya.cho.model.PrescriptionDTO
import org.piramalswasthya.cho.model.PrescriptionItemDTO
import org.piramalswasthya.cho.model.ProcedureResultDTO
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.NetworkResponse
import org.piramalswasthya.cho.network.NetworkResult
import org.piramalswasthya.cho.network.NurseDataResponse
import org.piramalswasthya.cho.network.networkResultInterceptor
import org.piramalswasthya.cho.network.refreshTokenInterceptor
import org.piramalswasthya.cho.network.socketTimeoutException
import timber.log.Timber
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
    private val patientDao: PatientDao,
    private val benFlowDao: BenFlowDao,
    private val userDao: UserDao,
    private val procedureDao: ProcedureDao,
    private val prescriptionDao: PrescriptionDao

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

    suspend fun updateDoctorData(patientDoctorForm: PatientDoctorFormUpsync): NetworkResult<NetworkResponse> {

        return networkResultInterceptor {
            val response = apiService.updateDoctorData(patientDoctorForm)
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

    private suspend fun registerLabData(labResultDTO: LabResultDTO): NetworkResult<NetworkResponse> {

        return networkResultInterceptor {
            val response = apiService.saveLabData(labResultDTO)
            val responseBody = response.body()?.string()
            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
                    Timber.i("lab data submitted",  response.body()?.string() ?: "")
                    NetworkResult.Success(NetworkResponse())
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    registerLabData(labResultDTO)
                },
            )
        }

    }

    private suspend fun registerPharmacistData(patientIssue: PharmacistPatientIssueDataRequest): NetworkResult<NetworkResponse> {

        return networkResultInterceptor {
            val response = apiService.savePharmacistData(patientIssue)
            val responseBody = response.body()?.string()
            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
                    val jsonObj = JSONObject(responseBody)
                    Timber.i("lab data submitted",  jsonObj)
                    NetworkResult.Success(NetworkResponse())
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    registerPharmacistData(patientIssue)
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
//                                    patientRepo.updateNurseSubmitted(it.patientVisitInfoSync.patientID)
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


    suspend fun processUnsyncedDoctorDataPendingTest() : Boolean{

        val patientDoctorDataUnSyncList = patientVisitInfoSyncRepo.getPatientDoctorDataPendingTestUnsynced()
        val user = userRepo.getLoggedInUser()

        patientDoctorDataUnSyncList.forEach {

            if(it.patient.beneficiaryRegID != null){
                withContext(Dispatchers.IO){

                    val benFlow = benFlowRepo.getBenFlowByBenRegIdAndBenVisitNo(it.patient.beneficiaryRegID!!, it.patientVisitInfoSync.benVisitNo)
                    if(benFlow != null && benFlow.nurseFlag == 9 && benFlow.doctorFlag == 1){

                        val diagnosisCaseRecordVal = caseRecordeRepo.getDiagnosisCaseRecordByPatientIDAndBenVisitNo(patientID = it.patientVisitInfoSync.patientID, benVisitNo = it.patientVisitInfoSync.benVisitNo)
                        val investigationCaseRecordVal = caseRecordeRepo.getInvestigationCaseRecordByPatientIDAndBenVisitNo(patientID = it.patientVisitInfoSync.patientID, benVisitNo = it.patientVisitInfoSync.benVisitNo)
                        val prescriptionCaseRecordVal = caseRecordeRepo.getPrescriptionCaseRecordeByPatientIDAndBenVisitNo(patientID = it.patientVisitInfoSync.patientID, benVisitNo = it.patientVisitInfoSync.benVisitNo)
                        val procedureList = historyRepo.getProceduresList(investigationCaseRecordVal?.investigationCaseRecord?.newTestIds)

                        val patientDoctorForm = PatientDoctorFormUpsync(
                            user = user,
                            benFlow = benFlow,
                            diagnosisList = diagnosisCaseRecordVal,
                            investigation = investigationCaseRecordVal,
                            prescriptionList = prescriptionCaseRecordVal,
                            procedureList = procedureList,
                            prescriptionID = null
                        )

                        patientVisitInfoSyncRepo.updatePatientDoctorDataSyncSyncing(it.patient.patientID, it.patientVisitInfoSync.benVisitNo)

                        when(val response = registerDoctorData(patientDoctorForm)){
                            is NetworkResult.Success -> {
                                benFlowRepo.updateDoctorFlag(benFlowID = benFlow.benFlowID, doctorFlag = 2)
                                patientVisitInfoSyncRepo.updatePatientDoctorDataSyncSuccess(it.patient.patientID, it.patientVisitInfoSync.benVisitNo)
                            }
                            is NetworkResult.Error -> {
                                patientVisitInfoSyncRepo.updatePatientDoctorDataSyncFailed(it.patient.patientID, it.patientVisitInfoSync.benVisitNo)
                                if(response.code == socketTimeoutException){
                                    throw SocketTimeoutException("This is an example exception message")
                                }
                            }
                            else -> {}
                        }
                    } else if (benFlow != null && benFlow.nurseFlag == 9 && benFlow.doctorFlag == 2) {

                    }
                }
            }
        }

        return true

    }

    suspend fun processUnsyncedDoctorDataWithoutTest() : Boolean{

        val patientDoctorDataUnSyncList = patientVisitInfoSyncRepo.getPatientDoctorDataWithoutTestUnsynced()
        val user = userRepo.getLoggedInUser()

        patientDoctorDataUnSyncList.forEach {

            if(it.patient.beneficiaryRegID != null){
                withContext(Dispatchers.IO){

                    val benFlow = benFlowRepo.getBenFlowByBenRegIdAndBenVisitNo(it.patient.beneficiaryRegID!!, it.patientVisitInfoSync.benVisitNo)
                    if(benFlow != null && benFlow.nurseFlag == 9 && benFlow.doctorFlag == 1){

                        val diagnosisCaseRecordVal = caseRecordeRepo.getDiagnosisCaseRecordByPatientIDAndBenVisitNo(patientID = it.patientVisitInfoSync.patientID, benVisitNo = it.patientVisitInfoSync.benVisitNo)
                        val investigationCaseRecordVal = caseRecordeRepo.getInvestigationCaseRecordByPatientIDAndBenVisitNo(patientID = it.patientVisitInfoSync.patientID, benVisitNo = it.patientVisitInfoSync.benVisitNo)
                        val prescriptionCaseRecordVal = caseRecordeRepo.getPrescriptionCaseRecordeByPatientIDAndBenVisitNo(patientID = it.patientVisitInfoSync.patientID, benVisitNo = it.patientVisitInfoSync.benVisitNo)
                        val procedureList = historyRepo.getProceduresList(investigationCaseRecordVal?.investigationCaseRecord?.newTestIds)

                        val patientDoctorForm = PatientDoctorFormUpsync(
                            user = user,
                            benFlow = benFlow,
                            diagnosisList = diagnosisCaseRecordVal,
                            investigation = investigationCaseRecordVal,
                            prescriptionList = prescriptionCaseRecordVal,
                            procedureList = procedureList,
                            prescriptionID = null
                        )

                        patientVisitInfoSyncRepo.updatePatientDoctorDataSyncSyncing(it.patient.patientID, it.patientVisitInfoSync.benVisitNo)

                        when(val response = registerDoctorData(patientDoctorForm)){
                            is NetworkResult.Success -> {
                                benFlowRepo.updateDoctorFlag(benFlowID = benFlow.benFlowID, doctorFlag = 9)
                                patientVisitInfoSyncRepo.updatePatientDoctorDataSyncSuccess(it.patient.patientID, it.patientVisitInfoSync.benVisitNo)
                            }
                            is NetworkResult.Error -> {
                                patientVisitInfoSyncRepo.updatePatientDoctorDataSyncFailed(it.patient.patientID, it.patientVisitInfoSync.benVisitNo)
                                if(response.code == socketTimeoutException){
                                    throw SocketTimeoutException("This is an example exception message")
                                }
                            }
                            else -> {}
                        }
                    } else if (benFlow != null && benFlow.nurseFlag == 9 && benFlow.doctorFlag == 2) {

                    }
                }
            }
        }

        return true

    }

    suspend fun processUnsyncedDoctorDataAfterTest() : Boolean{

        val patientDoctorDataUnSyncList = patientVisitInfoSyncRepo.getPatientDoctorDataAfterTestUnsynced()
        val user = userRepo.getLoggedInUser()

        patientDoctorDataUnSyncList.forEach {

            if(it.patient.beneficiaryRegID != null){
                withContext(Dispatchers.IO){

                    val benFlow = benFlowRepo.getBenFlowByBenRegIdAndBenVisitNo(it.patient.beneficiaryRegID!!, it.patientVisitInfoSync.benVisitNo)
                    if(benFlow != null){
                        val diagnosisCaseRecordVal = caseRecordeRepo.getDiagnosisCaseRecordByPatientIDAndBenVisitNo(patientID = it.patientVisitInfoSync.patientID, benVisitNo = it.patientVisitInfoSync.benVisitNo)
                        val investigationCaseRecordVal = caseRecordeRepo.getInvestigationCaseRecordByPatientIDAndBenVisitNo(patientID = it.patientVisitInfoSync.patientID, benVisitNo = it.patientVisitInfoSync.benVisitNo)
                        val prescriptionCaseRecordVal = caseRecordeRepo.getPrescriptionCaseRecordeByPatientIDAndBenVisitNo(patientID = it.patientVisitInfoSync.patientID, benVisitNo = it.patientVisitInfoSync.benVisitNo)
                        val procedureList = historyRepo.getProceduresList(investigationCaseRecordVal?.investigationCaseRecord?.newTestIds)

                        val patientDoctorForm = PatientDoctorFormUpsync(
                            user = user,
                            benFlow = benFlow,
                            diagnosisList = diagnosisCaseRecordVal,
                            investigation = investigationCaseRecordVal,
                            prescriptionList = prescriptionCaseRecordVal,
                            procedureList = procedureList,
                            prescriptionID = it.patientVisitInfoSync.prescriptionID
                        )

                        patientVisitInfoSyncRepo.updatePatientDoctorDataSyncSyncing(it.patient.patientID, it.patientVisitInfoSync.benVisitNo)

                        when(val response = updateDoctorData(patientDoctorForm)){
                            is NetworkResult.Success -> {
                                benFlowRepo.updateDoctorFlag(benFlowID = benFlow.benFlowID, doctorFlag = 9)
                                patientVisitInfoSyncRepo.updatePatientDoctorDataSyncSuccess(it.patient.patientID, it.patientVisitInfoSync.benVisitNo)
                            }
                            is NetworkResult.Error -> {
                                patientVisitInfoSyncRepo.updatePatientDoctorDataSyncFailed(it.patient.patientID, it.patientVisitInfoSync.benVisitNo)
                                if(response.code == socketTimeoutException){
                                    throw SocketTimeoutException("This is an example exception message")
                                }
                            }
                            else -> {}
                        }
                    } else if (benFlow != null && benFlow.nurseFlag == 9 && benFlow.doctorFlag == 2) {

                    }
                }
            }
        }

        return true

    }

    suspend fun processUnsyncedLabData(): Boolean{

        val labDataUnsyncedList = patientVisitInfoSyncRepo.getPatientLabDataUnsynced()
        val user = userRepo.getLoggedInUser()

        labDataUnsyncedList.forEach {

            if(it.patient.beneficiaryRegID != null){
                withContext(Dispatchers.IO){

                    val benFlow = benFlowRepo.getBenFlowByBenRegIdAndBenVisitNo(it.patient.beneficiaryRegID!!, it.patientVisitInfoSync.benVisitNo)
                    if(benFlow != null){

                        val procedureResultDTOs : MutableList<ProcedureResultDTO> = mutableListOf()

                        val procedures = procedureDao.getProceduresByPatientIdAndBenVisitNo(it.patient.patientID, it.patientVisitInfoSync.benVisitNo)
                        procedures?.forEach { procedure ->
                            val compListDetails: MutableList<ComponentResultDTO> = mutableListOf()
                            val procedureDTO = ProcedureResultDTO(
                                prescriptionID = procedure.prescriptionID,
                                procedureID = procedure.procedureID,
                                compList = compListDetails
                            )

                            val components = procedureDao.getComponentDetails(procedure.id)
                            components?.forEach { componentDetails ->
                                val componentResultDTO = ComponentResultDTO(
                                    testComponentID = componentDetails.testComponentID,
                                    testResultValue = componentDetails.testResultValue,
                                    testResultUnit = componentDetails.measurementUnit,
                                    remarks = componentDetails.remarks
                                )
                                compListDetails += componentResultDTO
                            }
                            procedureDTO.compList = compListDetails
                            procedureResultDTOs += procedureDTO
                        }


                        val labResultDTO = LabResultDTO(
                            labTestResults = procedureResultDTOs,
                            radiologyTestResults = mutableListOf(),
                            labCompleted =  true,
                            createdBy = user?.userName!!,
                             doctorFlag = "2",
                             nurseFlag = "9",
                             beneficiaryRegID = benFlow.beneficiaryRegID,
                             beneficiaryID = benFlow.beneficiaryID,
                             benFlowID = benFlow.benFlowID,
                             visitID = benFlow.benVisitID,
                             visitCode = benFlow.visitCode,
                             providerServiceMapID = benFlow.providerServiceMapId,
                             specialist_flag = null,
                             vanID = benFlow.vanID,
                             parkingPlaceID = benFlow.parkingPlaceID
                        )

                        if (labResultDTO.labTestResults.isNotEmpty()) {
                            patientVisitInfoSyncRepo.updateLabDataSyncState(it.patient.patientID, it.patientVisitInfoSync.benVisitNo, SyncState.SYNCING)

                            when(val response = registerLabData(labResultDTO)){
                                is NetworkResult.Success -> {
//                                patientRepo.updateDoctorSubmitted(it.patientID)
//                                benFlowRepo.updateDoctorCompleted(benFlowID = benFlow.benFlowID)
                                    patientVisitInfoSyncRepo.updateLabDataSyncState(it.patient.patientID, it.patientVisitInfoSync.benVisitNo, SyncState.SYNCED)
                                }
                                is NetworkResult.Error -> {
                                    patientVisitInfoSyncRepo.updateLabDataSyncState(it.patient.patientID, it.patientVisitInfoSync.benVisitNo, SyncState.UNSYNCED)
                                    if(response.code == socketTimeoutException){
                                        throw SocketTimeoutException("caught exception")
                                    }
                                    return@withContext false;
                                }
                                else -> {}
                            }
                        } else {
                            patientVisitInfoSyncRepo.updateLabDataSyncState(it.patient.patientID, it.patientVisitInfoSync.benVisitNo, SyncState.SYNCED)
                        }

                    } else { }
                }
            }
        }

        return true

    }

    suspend fun processUnsyncedPharmacistData(): Boolean{

        val pharmacistDataUnsyncedList = patientVisitInfoSyncRepo.getPatientPharmacistDataUnsynced()
        val user = userRepo.getLoggedInUser()

        val allBeneficiaryRegIDNotNull = pharmacistDataUnsyncedList.none { it.patient.beneficiaryRegID == null }
        if (allBeneficiaryRegIDNotNull) {
            // Continue processing pharmacist data
            Log.d("WU", "processUnsyncedPharmacistData: success1 ")
            pharmacistDataUnsyncedList.forEach {
                Log.d("WU", "processUnsyncedPharmacistData: success11 ")

                if(it.patient.beneficiaryRegID != null){
                    Log.d("WU", "processUnsyncedPharmacistData: success2 ")

                    withContext(Dispatchers.IO){

                        val benFlow = benFlowRepo.getBenFlowByBenRegIdAndBenVisitNo(it.patient.beneficiaryRegID!!, it.patientVisitInfoSync.benVisitNo)
                        if(benFlow != null){

                            val old_prescriptions = prescriptionDao.getPrescriptionsByPatientIdAndBenVisitNo(it.patient.patientID, it.patientVisitInfoSync.benVisitNo)
                            old_prescriptions?.forEach {prescription->
                                val updatedPrescription = prescription.copy(
                                    beneficiaryRegID = it.patient.beneficiaryRegID!!,
                                    benFlowID = it.patientVisitInfoSync.benFlowID,
                                    visitCode = benFlow.visitCode!!
                                )
                                prescriptionDao.updatePharmacistPrescription(updatedPrescription)
                            }

                            val prescriptionResultDTOs : MutableList<PrescriptionDTO> = mutableListOf()

                            val prescriptions = prescriptionDao.getPrescriptionsByPatientIdAndBenVisitNo(it.patient.patientID, it.patientVisitInfoSync.benVisitNo)
//                        val patient = patientDao.getPatientByBenRegId(it.patient.beneficiaryRegID!!)
                            prescriptions?.forEach { prescription ->
                                val prescriptionItemList: MutableList<PrescriptionItemDTO> = mutableListOf()
                                val prescriptionDTO = PrescriptionDTO(
                                    beneficiaryRegID = prescription.beneficiaryRegID!!,
                                    consultantName = prescription.consultantName,
                                    prescriptionID = prescription.prescriptionID,
                                    visitCode = prescription.visitCode,
                                    itemList = prescriptionItemList
                                )

                                val prescribedDrugsList = prescriptionDao.getPrescribedDrugs(prescription.id)
                                prescribedDrugsList?.forEach { prescribedDrugs ->
                                    val batchList: MutableList<PrescriptionBatchDTO> = mutableListOf()
                                    val prescriptionItemDTO = PrescriptionItemDTO(
                                        id = prescribedDrugs.id,
                                        drugID = prescribedDrugs.drugID,
                                        dose = prescribedDrugs.dose,
                                        drugForm = prescribedDrugs.drugForm,
                                        duration = prescribedDrugs.duration,
                                        durationUnit = prescribedDrugs.durationUnit,
                                        frequency = prescribedDrugs.frequency,
                                        genericDrugName = prescribedDrugs.genericDrugName,
                                        drugStrength = prescribedDrugs.drugStrength,
                                        isEDL = prescribedDrugs.isEDL,
                                        qtyPrescribed = prescribedDrugs.qtyPrescribed,
                                        route = prescribedDrugs.route,
                                        instructions = prescribedDrugs.instructions,
                                        batchList = batchList
                                    )

                                    val prescribedDrugsBatchList = prescriptionDao.getPrescribedDrugsBatch(prescribedDrugs.id)
                                    prescribedDrugsBatchList?.forEach { prescribedDrugsBatch ->
                                        val prescriptionBatchDTO = PrescriptionBatchDTO(
                                            expiresIn = prescribedDrugsBatch.expiresIn,
                                            batchNo = prescribedDrugsBatch.batchNo,
                                            expiryDate = prescribedDrugsBatch.expiryDate,
                                            itemStockEntryID = prescribedDrugsBatch.itemStockEntryID,
                                            qty = prescribedDrugsBatch.qty,
                                        )
                                        batchList += prescriptionBatchDTO
                                    }
                                    prescriptionItemDTO.batchList = batchList
                                    prescriptionItemList += prescriptionItemDTO
                                }
                                prescriptionDTO.itemList = prescriptionItemList
                                prescriptionResultDTOs += prescriptionDTO
                            }

                            val itemStockExitList : MutableList<PharmacistItemStockExitDataRequest> = mutableListOf()
                            prescriptionResultDTOs.get(0)?.itemList?.forEach { prescriptionItemDTO ->
                                prescriptionItemDTO.batchList.forEach { item ->
                                    val pharmacistPatientIssueDataRequest = PharmacistItemStockExitDataRequest(
                                        itemID = prescriptionItemDTO.drugID,
                                        itemStockEntryID = item.itemStockEntryID,
                                        quantity = prescriptionItemDTO.qtyPrescribed,
                                        createdBy = user?.userName!!
                                    )

                                    itemStockExitList+=pharmacistPatientIssueDataRequest
                                }
                            }


                            val pharmacistPatientIssueDataRequest = PharmacistPatientIssueDataRequest(
                                issuedBy = "MMU",
                                visitCode = benFlow.visitCode,
                                facilityID = userDao.getLoggedInUserFacilityID(),
                                age = it.patient.age,
                                beneficiaryID = it.patient.beneficiaryID,
                                benRegID = it.patient.beneficiaryRegID!!,
                                createdBy = user?.userName!!,
                                providerServiceMapID = benFlow.providerServiceMapId,
                                doctorName = prescriptionResultDTOs.get(0)?.consultantName,
                                gender = benFlow.genderName,
                                issueType = prescriptionResultDTOs[0]?.issueType,
                                patientName = it.patient.firstName+" "+it.patient.lastName,
                                prescriptionID = prescriptionResultDTOs?.get(0)?.prescriptionID,
                                reference = "Prescribed by "+user?.userName!!+" from MMU",
                                visitID = benFlow.benVisitID,
                                visitDate = benFlow.visitDate,
                                parkingPlaceID = benFlow.parkingPlaceID,
                                vanID = benFlow.vanID,
                                itemStockExit = itemStockExitList
                            )
                            if (pharmacistPatientIssueDataRequest.itemStockExit.isNotEmpty()) {
                                patientVisitInfoSyncRepo.updatePharmacistDataSyncState(it.patient.patientID, it.patientVisitInfoSync.benVisitNo, SyncState.SYNCING)

                                when(val response = registerPharmacistData(pharmacistPatientIssueDataRequest)){
                                    is NetworkResult.Success -> {
                                        Log.d("WU", "processUnsyncedPharmacistData: success3 ")
                                        patientVisitInfoSyncRepo.updatePharmacistDataSyncState(it.patient.patientID, it.patientVisitInfoSync.benVisitNo, SyncState.SYNCED)
                                    }
                                    is NetworkResult.Error -> {
                                        Log.d("WU", "processUnsyncedPharmacistData: success4 ")
                                        patientVisitInfoSyncRepo.updatePharmacistDataSyncState(it.patient.patientID, it.patientVisitInfoSync.benVisitNo, SyncState.UNSYNCED)
                                        if(response.code == socketTimeoutException){
                                            throw SocketTimeoutException("caught exception")
                                        }
                                        return@withContext false;
                                    }
                                    else -> {}
                                }
                            } else {
                                Log.d("WU", "processUnsyncedPharmacistData: success6")
                                patientVisitInfoSyncRepo.updatePharmacistDataSyncState(it.patient.patientID, it.patientVisitInfoSync.benVisitNo, SyncState.SYNCED)
                            }

                        } else { }
                    }
                }
            }
        } else {
            // Handle the case where some or all beneficiaryRegID are null
            Log.d("WU", "processUnsyncedPharmacistData: success5 ")
            return false
        }

        return true

    }

    suspend fun savePharmacistData(dtos: PrescriptionDTO?, benVisitInfo: PatientDisplayWithVisitInfo): Boolean{
        val user = userRepo.getLoggedInUser()
        var resp : Boolean = false
        if(benVisitInfo.patient.beneficiaryRegID != null){
            withContext(Dispatchers.IO){

                val benFlow = benFlowRepo.getBenFlowByBenRegIdAndBenVisitNo(benVisitInfo.patient.beneficiaryRegID!!, benVisitInfo.benVisitNo!!)
                if(benFlow != null){

                    val itemStockExitList : MutableList<PharmacistItemStockExitDataRequest> = mutableListOf()
                    dtos?.itemList?.forEach { prescriptionItemDTO ->
                        prescriptionItemDTO.batchList.forEach { item ->
                            val pharmacistPatientIssueDataRequest = PharmacistItemStockExitDataRequest(
                                itemID = prescriptionItemDTO.drugID,
                                itemStockEntryID = item.itemStockEntryID,
                                quantity = prescriptionItemDTO.qtyPrescribed,
                                createdBy = user?.userName!!
                            )

                            itemStockExitList+=pharmacistPatientIssueDataRequest
                        }
                    }


                    val pharmacistPatientIssueDataRequest = PharmacistPatientIssueDataRequest(
                        issuedBy = "MMU",
                        visitCode = benFlow.visitCode,
                        facilityID = userDao.getLoggedInUserFacilityID(),
                        age = benVisitInfo.patient.age,
                        beneficiaryID = benVisitInfo.patient.beneficiaryID,
                        benRegID = benVisitInfo.patient.beneficiaryRegID!!,
                        createdBy = user?.userName!!,
                        providerServiceMapID = benFlow.providerServiceMapId,
                        doctorName = dtos?.consultantName,
                        gender = benFlow.genderName,
                        issueType = dtos?.issueType,
                        patientName = benVisitInfo.patient.firstName+" "+benVisitInfo.patient.lastName,
                        prescriptionID = dtos?.prescriptionID,
                        reference = "Prescribed by "+user?.userName!!+" from MMU",
                        visitID = benFlow.benVisitID,
                        visitDate = benFlow.visitDate,
                        parkingPlaceID = benFlow.parkingPlaceID,
                        vanID = benFlow.vanID,
                        itemStockExit = itemStockExitList
                    )

//                    Timber.d("*******************DAta Prescription DTO************** ",pharmacistPatientIssueDataRequest)
                    when(val response = registerPharmacistData(pharmacistPatientIssueDataRequest)){
                        is NetworkResult.Success -> {
//                            Timber.d("*******************DAta Prescription DTO************** ",response)
                            resp = true
                        }
                        is NetworkResult.Error -> {
                            resp = false
                        }
                        else -> {}
                    }

                } else { }
            }
        }

        return resp

    }


}