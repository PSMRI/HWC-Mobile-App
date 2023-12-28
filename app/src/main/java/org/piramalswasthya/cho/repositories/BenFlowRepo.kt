package org.piramalswasthya.cho.repositories

import android.util.Log
import androidx.room.Transaction
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.BenFlowDao
import org.piramalswasthya.cho.database.room.dao.CaseRecordeDao
import org.piramalswasthya.cho.database.room.dao.InvestigationDao
import org.piramalswasthya.cho.database.room.dao.PatientVisitInfoSyncDao
import org.piramalswasthya.cho.database.room.dao.PrescriptionDao
import org.piramalswasthya.cho.database.room.dao.ProcedureDao
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.room.dao.VisitReasonsAndCategoriesDao
import org.piramalswasthya.cho.database.room.dao.VitalsDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.AllocationItemDataRequest
import org.piramalswasthya.cho.model.BenDetailsDownsync
import org.piramalswasthya.cho.model.BenFlow
import org.piramalswasthya.cho.model.BenNewFlow
import org.piramalswasthya.cho.model.ChiefComplaintDB
import org.piramalswasthya.cho.model.ComponentDataDownsync
import org.piramalswasthya.cho.model.ComponentDetails
import org.piramalswasthya.cho.model.ComponentOption
import org.piramalswasthya.cho.model.DiagnosisCaseRecord
import org.piramalswasthya.cho.model.DoctorDataDownSync
import org.piramalswasthya.cho.model.InvestigationCaseRecord
import org.piramalswasthya.cho.model.LabReportData
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.model.PatientVitalsModel
import org.piramalswasthya.cho.model.PrescribedDrugs
import org.piramalswasthya.cho.model.PrescribedDrugsBatch
import org.piramalswasthya.cho.model.PrescribedMedicineDataRequest
import org.piramalswasthya.cho.model.Prescription
import org.piramalswasthya.cho.model.PrescriptionBatchApiDTO
import org.piramalswasthya.cho.model.PrescriptionCaseRecord
import org.piramalswasthya.cho.model.PrescriptionDTO
import org.piramalswasthya.cho.model.PrescriptionItemDTO
import org.piramalswasthya.cho.model.Procedure
import org.piramalswasthya.cho.model.ProcedureDTO
import org.piramalswasthya.cho.model.ProcedureDataDownsync
import org.piramalswasthya.cho.model.UserDomain
import org.piramalswasthya.cho.model.VisitDB
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.CountDownSync
import org.piramalswasthya.cho.network.DownsyncSuccess
import org.piramalswasthya.cho.network.LabProceduresDataRequest
import org.piramalswasthya.cho.network.NetworkResponse
import org.piramalswasthya.cho.network.NetworkResult
import org.piramalswasthya.cho.network.NurseDataRequest
import org.piramalswasthya.cho.network.VillageIdList
import org.piramalswasthya.cho.network.networkResultInterceptor
import org.piramalswasthya.cho.network.refreshTokenInterceptor
import org.piramalswasthya.cho.network.socketTimeoutException
import org.piramalswasthya.cho.utils.DateTimeUtil
import org.piramalswasthya.cho.work.WorkerUtils
import timber.log.Timber
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
    private val userDao: UserDao,
    private val patientVisitInfoSyncDao: PatientVisitInfoSyncDao,
    private val investigationDao: InvestigationDao,
    private val prescriptionDao: PrescriptionDao,
    private val procedureDao: ProcedureDao,
    private val caseRecordeDao: CaseRecordeDao,
) {


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
            preferenceDao.getLastBenflowSyncTime()
        )

        when(val response = getBenFlowCountToDownload(villageList)){
            is NetworkResult.Success -> {

            }
            is NetworkResult.Error -> {
                if(response.code == socketTimeoutException){
                    throw SocketTimeoutException("This is an example exception message")
                }
            }
            else -> {}
        }

        when(val response = syncFlowIds(villageList)){
            is NetworkResult.Success -> {
                return true
//                return (response.data as DownsyncSuccess).isSuccess
            }
            is NetworkResult.Error -> {
                Log.d("error code is", response.code.toString())
                Log.d("error code message is", response.message)
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
    suspend fun refreshDoctorData(prescriptionCaseRecord: List<PrescriptionCaseRecord>?, investigationCaseRecord: InvestigationCaseRecord, diagnosisCaseRecords : List<DiagnosisCaseRecord>,patient: Patient, benFlow: BenFlow, patientVisitInfoSync: PatientVisitInfoSync,docData:DoctorDataDownSync){

        prescriptionDao.deletePrescriptionByPatientIdAndBenVisitNo(patient.patientID, patientVisitInfoSync.benVisitNo)
        prescriptionCaseRecord?.let {
            prescriptionDao.insertAll(it)
        }

        investigationDao.deleteInvestigationCaseRecordByPatientIdAndBenVisitNo(patient.patientID, patientVisitInfoSync.benVisitNo)
        investigationDao.insertInvestigation(investigationCaseRecord)

        caseRecordeDao.deleteDiagnosisByPatientIdAndBenVisitNo(patient.patientID, patientVisitInfoSync.benVisitNo)
        diagnosisCaseRecords.let {
            caseRecordeDao.insertAll(it)
        }

        patientVisitInfoSyncDao.updateAfterDoctorDataDownSync(patientVisitInfoSync.doctorFlag!!, patientVisitInfoSync.patientID, patientVisitInfoSync.benVisitNo)
        if(docData.diagnosis?.prescriptionID != null){
            patientVisitInfoSyncDao.updatePrescriptionID(docData.diagnosis?.prescriptionID, patientVisitInfoSync.patientID, patientVisitInfoSync.benVisitNo)
        }

//        patientVisitInfoSync.doctorDataSynced = SyncState.SYNCED
//        patientVisitInfoSync.prescriptionID = docData.diagnosis?.prescriptionID
//        patientVisitInfoSyncDao.insertPatientVisitInfoSync(patientVisitInfoSync)

    }

    @Transaction
    suspend fun refreshLabData(labReportData: List<LabReportData>, patientVisitInfoSync: PatientVisitInfoSync){

        procedureDao.deleteProcedureDownsyncByPatientIdAndVisitNo(patientID = patientVisitInfoSync.patientID, benVisitNo = patientVisitInfoSync.benVisitNo)

        labReportData.forEach {

            val procedure = ProcedureDataDownsync( labReportData = it, patientVisitInfoSync = patientVisitInfoSync)

            val procedureDataID = procedureDao.insert(procedure)
            it.componentList?.let { it1 ->
                it1.forEach { it2 ->
                    val component = ComponentDataDownsync(componentData = it2, procedureDataID = procedureDataID)
                    procedureDao.insert(component)
                }
            }

        }
        
    }


    private suspend fun getAndSaveDoctorDataToDb(benFlow: BenFlow, patient: Patient, patientVisitInfoSync: PatientVisitInfoSync): NetworkResult<NetworkResponse> {

        return networkResultInterceptor {
            val doctorDataRequest = NurseDataRequest(benRegID = benFlow.beneficiaryRegID!!, visitCode = benFlow.visitCode!!)

            val response = apiService.getDoctorData(doctorDataRequest)
            val responseBody = response.body()?.string()

            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
                    val data = responseBody.let { JSONObject(it).getString("data") }
                    val docData = Gson().fromJson(data, DoctorDataDownSync::class.java)
                    val prescriptionCaseRecords = docData.prescription?.map{
                        PrescriptionCaseRecord(patient = patient, benFlow = benFlow, prescriptionData = it)
                    }
                    val investigationCaseRecord  =  InvestigationCaseRecord(docData = docData, patient = patient, benFlow = benFlow)

                    val diagnosisCaseRecords = docData.diagnosis?.provisionalDiagnosisList?.map {
                        DiagnosisCaseRecord(patient = patient, benFlow = benFlow, provisionalDiagnosisUpsync = it)
                    } ?: emptyList()

                    refreshDoctorData(prescriptionCaseRecord = prescriptionCaseRecords, investigationCaseRecord, diagnosisCaseRecords, patient = patient, benFlow = benFlow, patientVisitInfoSync = patientVisitInfoSync, docData = docData)

                    if(docData.Refer?.referredToInstituteName != null || docData.Refer?.referralReason != null){
                        patientRepo.updatePatientReferData(DateTimeUtil.formatVisitDateString(benFlow.visitDate), docData.Refer?.referredToInstituteName, docData.Refer?.referralReason, benFlow.beneficiaryRegID);
                    }

                    if(!docData.LabReport.isNullOrEmpty()){
                        refreshLabData(labReportData = docData.LabReport, patientVisitInfoSync = patientVisitInfoSync)
                    }
                    NetworkResult.Success(NetworkResponse())
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    getAndSaveDoctorDataToDb(benFlow, patient, patientVisitInfoSync)
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

        patientVisitInfoSyncDao.updateAfterNurseDataDownSync(patientVisitInfoSync.patientID, patientVisitInfoSync.benVisitNo, DateTimeUtil.formatVisitDate(benFlow.visitDate))
//        patientVisitInfoSync.nurseDataSynced = SyncState.SYNCED
//        patientVisitInfoSyncDao.insertPatientVisitInfoSync(patientVisitInfoSync)

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
        if(patientVisitInfoSync != null && benFlow.nurseFlag!! == 9 && patientVisitInfoSync.nurseFlag!! == 1){
            patientVisitInfoSync.nurseFlag = 9
            patientVisitInfoSync.doctorFlag = 1
            getAndSaveNurseDataToDb(benFlow, patient, patientVisitInfoSync)
        }
    }

    suspend fun checkAndDownsyncDoctorData(benFlow: BenFlow, patient: Patient){
        val patientVisitInfoSync = patientVisitInfoSyncDao.getPatientVisitInfoSyncByPatientIdAndBenVisitNo(patientID = patient.patientID, benVisitNo = benFlow.benVisitNo!!)
        if(patientVisitInfoSync != null && benFlow.doctorFlag!! > 1 && patientVisitInfoSync.doctorDataSynced != SyncState.UNSYNCED && patientVisitInfoSync.labDataSynced != SyncState.UNSYNCED){
            patientVisitInfoSync.doctorFlag = benFlow.doctorFlag
            getAndSaveDoctorDataToDb(benFlow, patient, patientVisitInfoSync)
        }
//        if(patientVisitInfoSync != null && benFlow.doctorFlag!! > 1 &&
//            ((benFlow.doctorFlag > patientVisitInfoSync.doctorFlag!!) ||
//             (benFlow.doctorFlag == 2 && patientVisitInfoSync.doctorFlag == 3 && patientVisitInfoSync.labDataSynced == SyncState.UNSYNCED))){
//            patientVisitInfoSync.doctorFlag = benFlow.doctorFlag
//            getAndSaveDoctorDataToDb(benFlow, patient, patientVisitInfoSync)
//        }
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
            benVisitNo = benFlow.benVisitNo!!,
            pharmacistFlag = benFlow.pharmacist_flag!!,
            visitCategory = benFlow.VisitCategory ?: ""
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
                    var isSuccess = true

                    var totalDownloaded = 0

                    for (i in 0 until benflowArray.length()) {

                        totalDownloaded++
                        if(WorkerUtils.totalRecordsToDownload > 0 && totalDownloaded <= WorkerUtils.totalRecordsToDownload){
                            withContext(Dispatchers.Main) {
                                WorkerUtils.totalPercentageCompleted.value = ((totalDownloaded.toDouble() / WorkerUtils.totalRecordsToDownload.toDouble())*100).toInt()
                            }
                        }

                        try {
                            val data = benflowArray.getString(i)
                            val benFlow = Gson().fromJson(data, BenFlow::class.java)
                            val patient = patientRepo.getPatientByBenRegId(benFlow.beneficiaryRegID!!)
                            benFlowDao.insertBenFlow(benFlow)
                            if(patient != null){
                                checkAndAddNewVisitInfo(benFlow, patient)
                                updateBenFlowId(benFlow, patient)
                                checkAndDownsyncNurseData(benFlow, patient)
                                checkAndDownsyncDoctorData(benFlow, patient)
                            }
                        } catch (e : Exception){
                            isSuccess = false
                        }
                    }
                    NetworkResult.Success(DownsyncSuccess(isSuccess))
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    syncFlowIds(villageList)
                },
            )
        }

    }

    private suspend fun getBenFlowCountToDownload(villageList: VillageIdList): NetworkResult<NetworkResponse>{
        return networkResultInterceptor {
            val response = apiService.getBeneficiariesCount(villageList)
            val responseBody = response.body()?.string()
            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
                    val data = responseBody.let { JSONObject(it).getString("data") }
                    val result = Gson().fromJson(data, CountDownSync::class.java)
                    WorkerUtils.totalRecordsToDownload = result.response.toInt()
                    NetworkResult.Success(NetworkResponse())
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    getBenFlowCountToDownload(villageList)
                })
        }
    }

    private suspend fun getAndSaveLabTechnicianDataToDb(benFlow: BenFlow, benVisitInfo: PatientDisplayWithVisitInfo): NetworkResult<NetworkResponse> {
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

                    procedureDao.deleteProcedureByPatientIDAndBenVisitNo(benVisitInfo.patient.patientID, benVisitInfo.benVisitNo!!)
                    procedureDTO.forEach { dto ->
                        val procedure = Procedure(
                            procedureID = dto.procedureID,
                            procedureDesc = dto.procedureDesc,
                            procedureType = dto.procedureType,
                            procedureName = dto.procedureName,
                            prescriptionID = dto.prescriptionID,
                            isMandatory = dto.isMandatory,
                            patientID = benVisitInfo.patient.patientID,
                            benVisitNo = benVisitInfo.benVisitNo
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

//                        val patientVisitInfoSync = patientVisitInfoSyncDao.getPatientVisitInfoSyncByPatientIdAndBenVisitNo(benVisitInfo.patient.patientID, benVisitInfo.benVisitNo!!)!!
//                        patientVisitInfoSync.labDataSynced = SyncState.NOT_ADDED
//                        patientVisitInfoSyncDao.insertPatientVisitInfoSync(patientVisitInfoSync)

                    }

                    NetworkResult.Success(NetworkResponse())
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    getAndSaveLabTechnicianDataToDb(benFlow, benVisitInfo)
                },
            )
        }
    }

    suspend fun updateNurseCompletedAndVisitCode(visitCode: Long, benVisitID: Long, benFlowID: Long){
        benFlowDao.updateNurseCompleted(visitCode, benVisitID, benFlowID)
    }

    suspend fun updateDoctorFlag(benFlowID: Long, doctorFlag: Int){
        benFlowDao.updateDoctorFlag(benFlowID, doctorFlag)
    }

    suspend fun pullLabProcedureData(benVisitInfo : PatientDisplayWithVisitInfo): Boolean {

        return try {

            val benFlow = benFlowDao.getBenFlowByBenRegIdAndBenVisitNo(benVisitInfo.patient.beneficiaryRegID!!, benVisitInfo.benVisitNo!!)
            if (benFlow != null) {
                getAndSaveLabTechnicianDataToDb(benFlow = benFlow, benVisitInfo = benVisitInfo)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

//    private suspend fun getBenDetailsForPharmacist(benFlow: BenFlow): NetworkResult<NetworkResponse> {
//        return networkResultInterceptor {
//            val pharmacistPatientDataRequest = PharmacistPatientDataRequest(
//                benFlowID = benFlow.benFlowID!!,
//                beneficiaryRegID = benFlow.beneficiaryRegID!!
//            )
//
//            val response = apiService.getPharmacistPatientDetails(pharmacistPatientDataRequest)
//            val responseBody = response.body()?.string()
//
//            refreshTokenInterceptor(
//                responseBody = responseBody,
//                onSuccess = {
//                    val jsonObj = JSONObject(responseBody)
//                    val data = jsonObj.getJSONObject("data")
//                        .toString()
////                    val benFlowDetails = Gson().fromJson(data, Array<ProcedureDTO>::class.java)
//
//                    NetworkResult.Success(NetworkResponse())
//                },
//                onTokenExpired = {
//                    val user = userRepo.getLoggedInUser()!!
//                    userRepo.refreshTokenTmc(user.userName, user.password)
//                    getBenDetailsForPharmacist(benFlow)
//                },
//            )
//        }
//    }

        suspend fun getAllocationItemForPharmacist(prescriptionDTO: PrescriptionDTO): NetworkResult<NetworkResponse> {
            return networkResultInterceptor {
                val listAllocation: MutableList<AllocationItemDataRequest> = mutableListOf()
                prescriptionDTO.itemList.forEach { prescriptionItemDTO ->
                    val allocationItemDataRequest = AllocationItemDataRequest(
                        itemID = prescriptionItemDTO.drugID,
                        quantity = prescriptionItemDTO.qtyPrescribed
                    )
                    listAllocation.add(allocationItemDataRequest)
                }
                Timber.d("******************* prescriptionBatchDTO Item DTO************** ",listAllocation)
                val facilityID = userDao.getLoggedInUserFacilityID()
                val response = apiService.getPharmacistAllocationItemList(listAllocation, facilityID)
                val responseBody = response.body()?.string()

                refreshTokenInterceptor(
                    responseBody = responseBody,
                    onSuccess = {
                        val jsonObj = JSONObject(responseBody)
                        val data = jsonObj.getJSONObject("data").toString()
                        val prescriptionBatchApiDTO = Gson().fromJson(data, PrescriptionBatchApiDTO::class.java)
                        Timber.d("******************* prescriptionBatchDTO Item DTO************** ",prescriptionBatchApiDTO)

                        NetworkResult.Success(NetworkResponse())
                    },
                    onTokenExpired = {
                        val user = userRepo.getLoggedInUser()!!
                        userRepo.refreshTokenTmc(user.userName, user.password)
                        getAllocationItemForPharmacist(prescriptionDTO)
                    },
                )
            }
    }

    private suspend fun getPrescriptionsListForPharmacist(benFlow: BenFlow, benVisitInfo: PatientDisplayWithVisitInfo, facilityID: Int): NetworkResult<NetworkResponse> {
//        Log.i("Location From home is", "${benFlow!!}")
        return networkResultInterceptor {
            val prescribedMedicineDataRequest = PrescribedMedicineDataRequest(
                beneficiaryRegID = benFlow.beneficiaryRegID!!,
                facilityID = facilityID!!,
                visitCode = benFlow.visitCode!!
            )

            val response = apiService.getPharmacistPrescriptionList(prescribedMedicineDataRequest)
            val responseBody = response.body()?.string()

            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
                    val jsonObj = JSONObject(responseBody)
                    val data = jsonObj.getJSONObject("data")
                        .toString()
                    val prescriptionDTO = Gson().fromJson(data, PrescriptionDTO::class.java)
                    prescriptionDao.deletePrescriptionByPatientIDAndBenVisitNo(benVisitInfo.patient.patientID, benVisitInfo.benVisitNo!!)
                    prescriptionDTO.let { dto ->
                        val prescription = Prescription(
                            prescriptionID = dto.prescriptionID,
                            beneficiaryRegID = prescribedMedicineDataRequest.beneficiaryRegID,
                            visitCode = dto.visitCode,
                            consultantName = dto.consultantName,
                            patientID = benVisitInfo.patient.patientID,
                            benFlowID = benFlow.benFlowID,
                            benVisitNo = benVisitInfo.benVisitNo
                        )

                        val prescriptionID = prescriptionDao.insert(prescription = prescription)
                        dto.itemList.forEach { prescriptionItemDTO ->
                            Timber.d("*******************Prescription Item DTO************** ",prescriptionItemDTO)
                            val prescribedDrugs = PrescribedDrugs(
                                drugID = prescriptionItemDTO.drugID,
                                prescriptionID = prescriptionID,
                                dose = prescriptionItemDTO.dose,
                                drugForm = prescriptionItemDTO.drugForm,
                                drugStrength = prescriptionItemDTO.drugStrength,
                                duration = prescriptionItemDTO.duration,
                                durationUnit = prescriptionItemDTO.durationUnit,
                                frequency = prescriptionItemDTO.frequency,
                                genericDrugName = prescriptionItemDTO.genericDrugName,
                                isEDL = prescriptionItemDTO.isEDL,
                                qtyPrescribed = prescriptionItemDTO.qtyPrescribed,
                                route = prescriptionItemDTO.route,
                                instructions = prescriptionItemDTO.instructions
                            )
                            Timber.d("*******************prescribedDrugs Item DTO************** ",prescribedDrugs)
                            var prescribedDrugsId = prescriptionDao.insert(prescribedDrugs)
//                            getAllocationItemForPharmacist(prescriptionItemDTO, prescribedDrugsId)
                            prescriptionItemDTO.batchList.forEach { prescriptionBatchDTO ->
                                Timber.d("******************* prescriptionBatchDTO Item DTO************** ",prescriptionBatchDTO)
                                val prescribedDrugsBatch = PrescribedDrugsBatch(
                                    drugID = prescribedDrugsId,
                                    expiryDate = prescriptionBatchDTO.expiryDate,
                                    expiresIn = prescriptionBatchDTO.expiresIn,
                                    batchNo = prescriptionBatchDTO.batchNo,
                                    itemStockEntryID = prescriptionBatchDTO.itemStockEntryID,
                                    qty = prescriptionBatchDTO.qty
                                )
                                Timber.d("******************* prescriptionBatchDTO Item DTO************** ",prescribedDrugsBatch)
                                prescriptionDao.insert(prescribedDrugsBatch)
                            }
                        }

//                        patientVisitInfoSyncDao.insertPatientVisitInfoSync(patientVisitInfoSync)
                    }

                    NetworkResult.Success(NetworkResponse())
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    getPrescriptionsListForPharmacist(benFlow, benVisitInfo, facilityID)
                },
            )
        }
    }

    suspend fun pullPrescriptionListData(benVisitInfo : PatientDisplayWithVisitInfo): Boolean {

        return try {
            val facilityID = userDao.getLoggedInUserFacilityID()
            val benFlow = benFlowDao.getBenFlowByBenRegIdAndBenVisitNo(benVisitInfo.patient.beneficiaryRegID!!, benVisitInfo.benVisitNo!!)
            if (benFlow != null) {
                getPrescriptionsListForPharmacist(benFlow = benFlow, benVisitInfo = benVisitInfo, facilityID)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

//    suspend fun getBenDetailsForPharmacist(benFlowID: Long) : BenFlow {
//        return benFlowDao.getBenFlowByBenFlowID(benFlowID)
//    }

//    suspend fun getBenDetailsForPharmacist(patientId: String?): BenFlow? {
//        return try {
//            var benFlowBD: BenFlow? = null
//            patientId?.let { patientID ->
//                val patient = patientRepo.getPatient(patientId)
//                val benFlow = benFlowDao.getBenFlowByBenRegId(patient.beneficiaryRegID!!)
//                if (benFlow != null) {
//                    benFlowBD = benFlowDao.getBenFlowByBenFlowID(benFlow.benFlowID)
//                }
//            }
//            benFlowBD
//        } catch (e: Exception) {
//            var benFlowBD: BenFlow? = null
//            benFlowBD
//        }
//    }

}