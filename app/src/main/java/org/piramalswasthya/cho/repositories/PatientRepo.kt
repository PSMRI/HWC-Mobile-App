package org.piramalswasthya.cho.repositories

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.piramalswasthya.cho.network.exception
import org.piramalswasthya.cho.network.jsonException
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.BatchDao
import org.piramalswasthya.cho.database.room.dao.BlockMasterDao
import org.piramalswasthya.cho.database.room.dao.DistrictMasterDao
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.database.room.dao.PrescriptionDao
import org.piramalswasthya.cho.database.room.dao.ProcedureDao
import org.piramalswasthya.cho.database.room.dao.RegistrarMasterDataDao
import org.piramalswasthya.cho.database.room.dao.StateMasterDao
import org.piramalswasthya.cho.database.room.dao.VillageMasterDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.Address
import org.piramalswasthya.cho.model.BenHealthIdDetails
import org.piramalswasthya.cho.model.BeneficiariesDTO
import org.piramalswasthya.cho.model.BlockMaster
import org.piramalswasthya.cho.model.ComponentDetailDTO
import org.piramalswasthya.cho.model.ComponentDetails
import org.piramalswasthya.cho.model.ComponentOptionDTO
import org.piramalswasthya.cho.model.DistrictMaster
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.PatientNetwork
import org.piramalswasthya.cho.model.PrescribedDrugsBatch
import org.piramalswasthya.cho.model.Prescription
import org.piramalswasthya.cho.model.PrescriptionBatchDTO
import org.piramalswasthya.cho.model.PrescriptionDTO
import org.piramalswasthya.cho.model.PrescriptionItemDTO
import org.piramalswasthya.cho.model.Procedure
import org.piramalswasthya.cho.model.ProcedureDTO
import org.piramalswasthya.cho.model.StateMaster
import org.piramalswasthya.cho.model.UserDomain
import org.piramalswasthya.cho.model.VillageMaster
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.BenHealthDetails
import org.piramalswasthya.cho.network.BenificiarySaveResponse
import org.piramalswasthya.cho.network.CountDownSync
import org.piramalswasthya.cho.network.DownsyncSuccess
import org.piramalswasthya.cho.network.GenerateOTPForCareContext
import org.piramalswasthya.cho.network.GenerateOTPForCareContextRequest
import org.piramalswasthya.cho.network.GetBenHealthIdRequest
import org.piramalswasthya.cho.network.NetworkResponse
import org.piramalswasthya.cho.network.NetworkResult
import org.piramalswasthya.cho.network.SaveAbdmFacilityId
import org.piramalswasthya.cho.network.ValidateOTPAndCreateCareContextRequest
import org.piramalswasthya.cho.network.ValidateOTPAndCreateCareContextResponse
import org.piramalswasthya.cho.network.VillageIdList
import org.piramalswasthya.cho.network.networkResultInterceptor
import org.piramalswasthya.cho.network.refreshTokenInterceptor
import org.piramalswasthya.cho.network.socketTimeoutException
import org.piramalswasthya.cho.utils.DateTimeUtil
import org.piramalswasthya.cho.utils.generateUuid
import org.piramalswasthya.cho.utils.nullIfEmpty
import org.piramalswasthya.cho.work.WorkerUtils
import timber.log.Timber
import java.io.File
import java.net.SocketTimeoutException
import javax.inject.Inject

class PatientRepo @Inject constructor(
    private val patientDao: PatientDao,
    private val userRepo: UserRepo,
    private val apiService: AmritApiService,
    private val caseRecordeRepo: CaseRecordeRepo,
    private val visitReasonsAndCategoriesRepo: VisitReasonsAndCategoriesRepo,
    private val vitalsRepo: VitalsRepo,
    private val patientVisitInfoSyncRepo: PatientVisitInfoSyncRepo,
    private val preferenceDao: PreferenceDao,
    private val stateMasterDao: StateMasterDao,
    private val districtMasterDao: DistrictMasterDao,
    private val blockMasterDao: BlockMasterDao,
    private val villageMasterDao: VillageMasterDao,
    private val procedureDao: ProcedureDao,
    private val prescriptionDao: PrescriptionDao,
    private val registrarMasterDataDao: RegistrarMasterDataDao,
    private val batchDao: BatchDao,
) {

    private var abdmFacilityId: String = ""
    private var abdmFacilityName: String = ""

    suspend fun insertPatient(patient: Patient) {
        // Ensure master data exists before inserting patient
        ensureMasterDataExists(patient)
        patientDao.insertPatient(patient)
    }

    /**
     * Ensures all master data (State, District, Block, Village, Gender) exists before inserting patient
     */
    private suspend fun ensureMasterDataExists(patient: Patient) {
        // Cache IDs in local vals to satisfy smart-cast requirements
        val stateId = patient.stateID
        val districtId = patient.districtID
        val blockId = patient.blockID
        val villageId = patient.districtBranchID

        // Ensure State exists
        if (stateId != null) {
            if (stateMasterDao.getStateById(stateId) == null) {
                stateMasterDao.insertStates(
                    StateMaster(
                        stateID = stateId,
                        stateName = "", // Name not available from patient data
                        govtLGDStateID = null
                    )
                )
            }
        }

        // Ensure District exists
        if (districtId != null && stateId != null) {
            if (districtMasterDao.getDistrictById(districtId) == null) {
                districtMasterDao.insertDistrict(
                    DistrictMaster(
                        districtID = districtId,
                        stateID = stateId,
                        govtLGDStateID = null,
                        govtLGDDistrictID = null,
                        districtName = ""
                    )
                )
            }
        }

        // Ensure Block exists
        if (blockId != null && districtId != null) {
            if (blockMasterDao.getBlockById(blockId) == null) {
                blockMasterDao.insertBlock(
                    BlockMaster(
                        blockID = blockId,
                        districtID = districtId,
                        govtLGDDistrictID = null,
                        govLGDSubDistrictID = null,
                        blockName = ""
                    )
                )
            }
        }

        // Ensure Village exists
        if (villageId != null && blockId != null) {
            if (villageMasterDao.getVillageById(villageId) == null) {
                villageMasterDao.insertVillage(
                    VillageMaster(
                        districtBranchID = villageId,
                        blockID = blockId,
                        govtLGDVillageID = null,
                        govtLGDSubDistrictID = null,
                        villageName = ""
                    )
                )
            }
        }
    }

    suspend fun getBenFromId(benId: Long): Patient? {
        return patientDao.getBen(benId)
    }

    suspend fun updateRecord(it: Patient) {
        withContext(Dispatchers.IO) {
            patientDao.updatePatient(it)
        }
    }

    suspend fun updatePatientSyncing(patient: Patient) {
        patientDao.updatePatientSyncing(SyncState.SYNCING, patient.patientID)
    }

//    suspend fun updatePatientReferData(referDate: String?, referTo: String?, referralReason: String?, benRegId: Long) {
//        patientDao.updatePatientReferData(referDate, referTo, referralReason, benRegId);
//    }

    suspend fun updatePatientSyncingFailed(patient: Patient) {
        patientDao.updatePatientSyncFailed(SyncState.UNSYNCED, patient.patientID)
    }

    suspend fun updatePatientSyncSuccess(patient: Patient, benificiarySaveResponse: BenificiarySaveResponse){
        patientDao.updatePatientSynced(SyncState.SYNCED, benificiarySaveResponse.beneficiaryID, benificiarySaveResponse.beneficiaryRegID, patient.patientID)
    }

    suspend fun getPatientList() : List<PatientDisplay>{
        return patientDao.getPatientList()
    }
    fun getPatientListFlow() : Flow<List<PatientDisplay>> {
        return patientDao.getPatientListFlow()
    }

    fun getPatientListFlowForNurse() : Flow<List<PatientDisplay>> {
        return patientDao.getPatientListFlowForNurse()
    }

    fun getPatientListFlowForDoctor() : Flow<List<PatientDisplay>> {
        return patientDao.getPatientListFlowForDoctor()
    }

    fun getPatientListFlowForLab() : Flow<List<PatientDisplay>> {
        return patientDao.getPatientListFlowForLab()
    }

//    suspend fun updateFlagsByBenRegId(benFlow: BenFlow) {
//        val patient = patientDao.getPatientByBenRegId(benFlow.beneficiaryRegID!!)
//        if(patient != null && benFlow.nurseFlag!! >= patient.nurseFlag!! && benFlow.doctorFlag!! >= patient.doctorFlag!!){
//            patientDao.updateFlagsByBenRegId(nurseFlag = benFlow.nurseFlag!!, doctorFlag = benFlow.doctorFlag!!, beneficiaryRegID = benFlow.beneficiaryRegID!!)
//        }
//    }
//    suspend fun updateFlagsByBenRegId(benFlow: BenFlow) {
//        val patient = patientDao.getPatientByBenRegId(benFlow.beneficiaryRegID!!)
//        if(patient != null && benFlow.nurseFlag!! >= patient.nurseFlag!! && benFlow.doctorFlag!! >= patient.doctorFlag!!){
//            patientDao.updateFlagsByBenRegId(nurseFlag = benFlow.nurseFlag!!, doctorFlag = benFlow.doctorFlag!!, beneficiaryRegID = benFlow.beneficiaryRegID!!)
//        }
//    }

    suspend fun getPatient(patientId : String) : Patient{
        return patientDao.getPatient(patientId)
    }

    suspend fun getPatientDisplay(patientId : String) : PatientDisplay{
        return patientDao.getPatientDisplay(patientId)
    }

//    suspend fun updateNurseSubmitted(patientId : String) {
//        patientDao.updateNurseSubmitted(patientId)
//    }

//    suspend fun updateDoctorSubmitted(patientId : String) {
//        patientDao.updateDoctorSubmitted(patientId)
//    }

    suspend fun getPatientByBenRegId(beneficiaryRegID : Long) : Patient?{
        return patientDao.getPatientByBenRegId(beneficiaryRegID)
    }

    suspend fun registerNewPatient(patient : PatientDisplay, user: UserDomain?): NetworkResult<NetworkResponse> {

        // 🔹 Basic validation – this API needs mandatory fields which are missing
        val p = patient.patient
        if (p.dob == null || p.genderID == null || p.districtBranchID == null) {
            // These are the minimum fields required downstream in PatientNetwork/Bendemographics
            return NetworkResult.Error(
                0,
                "Cannot register – missing mandatory data (DOB / gender / village). Please complete registration form first."
            )
        }

        return networkResultInterceptor {
            val patNet = PatientNetwork(patient, user)
//            Timber.d("patient register is ", patNet.toString())
            val response = apiService.saveBenificiaryDetails(patNet)
            
            // Check if response is successful
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                Timber.e("Registration failed - HTTP ${response.code()}: $errorBody")
                
                // Try to parse error message from response
                val errorMessage = try {
                    if (errorBody != null) {
                        val errorJson = JSONObject(errorBody)
                        if (errorJson.has("errorMessage")) {
                            errorJson.getString("errorMessage")
                        } else if (errorJson.has("message")) {
                            errorJson.getString("message")
                        } else {
                            "Registration failed with status code ${response.code()}"
                        }
                    } else {
                        "Registration failed with status code ${response.code()}"
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error parsing error response")
                    errorBody ?: "Registration failed with status code ${response.code()}"
                }
                
                return@networkResultInterceptor NetworkResult.Error(response.code(), errorMessage)
            }
            
            val responseBody = response.body()?.string()
            
            if (responseBody == null) {
                Timber.e("Registration failed - Response body is null")
                return@networkResultInterceptor NetworkResult.Error(0, "Server returned empty response")
            }
            
            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
                    try {
                        val jsonResponse = JSONObject(responseBody)
                        
                        // Check statusCode in response
                        if (jsonResponse.has("statusCode")) {
                            val statusCode = jsonResponse.getInt("statusCode")
                            if (statusCode != 200) {
                                val errorMessage = if (jsonResponse.has("errorMessage")) {
                                    jsonResponse.getString("errorMessage")
                                } else if (jsonResponse.has("message")) {
                                    jsonResponse.getString("message")
                                } else {
                                    "Registration failed with status code $statusCode"
                                }
                                Timber.e("Registration failed - Status code: $statusCode, Message: $errorMessage")
                                NetworkResult.Error(statusCode, errorMessage)
                            } else {
                                val data = jsonResponse.getString("data")
                                val result = Gson().fromJson(data, BenificiarySaveResponse::class.java)
                                NetworkResult.Success(result)
                            }
                        } else {
                            // No statusCode field, try to parse data directly
                            val data = jsonResponse.getString("data")
                            val result = Gson().fromJson(data, BenificiarySaveResponse::class.java)
                            NetworkResult.Success(result)
                        }
                    } catch (e: JSONException) {
                        Timber.e(e, "Error parsing registration response: $responseBody")
                        NetworkResult.Error(jsonException, "Invalid response format from server")
                    } catch (e: Exception) {
                        Timber.e(e, "Unexpected error parsing registration response")
                        NetworkResult.Error(exception, "Error processing server response: ${e.message}")
                    }
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    registerNewPatient(patient, user)
                },
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun downloadAndSyncPatientRecords(): Boolean {

        val user = userRepo.getLoggedInUser()

        val villageList = VillageIdList(
            convertStringToIntList(user?.assignVillageIds ?: ""),
            preferenceDao.getLastPatientSyncTime()
        )

        when(val response = getPatientsCountToDownload(villageList)){
            is NetworkResult.Success -> {

            }
            is NetworkResult.Error -> {
                if(response.code == socketTimeoutException){
                    throw SocketTimeoutException("This is an example exception message")
                }
            }
            else -> {}
        }

        when(val response = downloadRegisterPatientFromServer(villageList)){
            is NetworkResult.Success -> {
                return true
//                return (response.data as DownsyncSuccess).isSuccess
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


    private fun convertStringToIntList(villageIds : String) : List<Int>{
        if(villageIds.trim().nullIfEmpty() == null){
            return emptyList();
        }
        return villageIds.split(",").map {
            it.trim().toInt()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend  fun setPatientAge(patient: Patient){
        val age = DateTimeUtil.calculateAge(patient.dob!!)
        val start = DateTimeUtil.ageUnitMap.get(age.unit)
        val ageUnit = registrarMasterDataDao.getAgeUnit(start!!)
        patient.age = age.value
        patient.ageUnitID = ageUnit.id
    }

    private suspend fun downloadStateMasterData(currentAddress: Address?){
        if(currentAddress?.stateId != null){
            if(stateMasterDao.getStateById(currentAddress.stateId.toInt()) == null ){
                stateMasterDao.insertStates(
                    StateMaster(
                        stateID = currentAddress.stateId.toInt(),
                        stateName = currentAddress.state ?: "",
                        null
                    )
                )
            }
        }
    }

    private suspend fun downloadDistrictMasterData(currentAddress: Address?){
        if(currentAddress?.districtId != null && currentAddress.stateId != null){
            if(districtMasterDao.getDistrictById(currentAddress.districtId.toInt()) == null ){
                districtMasterDao.insertDistrict(
                    DistrictMaster(
                        districtID = currentAddress.districtId.toInt(),
                        stateID = currentAddress.stateId.toInt(),
                        null,
                        null,
                        districtName = currentAddress.district ?: "",
                    )
                )
            }
        }
    }

    private suspend fun downloadBlockMasterData(currentAddress: Address?){
        if(currentAddress?.subDistrictId != null && currentAddress.districtId != null){
            if(blockMasterDao.getBlockById(currentAddress.subDistrictId.toInt()) == null ){
                blockMasterDao.insertBlock(
                    BlockMaster(
                        blockID = currentAddress.subDistrictId.toInt(),
                        districtID = currentAddress.districtId.toInt(),
                        null,
                        null,
                        blockName = currentAddress.subDistrict ?: "",
                    )
                )
            }
        }
    }

    private suspend fun downloadVillageMasterData(currentAddress: Address?){
        if(currentAddress?.villageId != null && currentAddress.subDistrictId != null){
            if(villageMasterDao.getVillageById(currentAddress.villageId.toInt()) == null ){
                villageMasterDao.insertVillage(
                    VillageMaster(
                        districtBranchID = currentAddress.villageId.toInt(),
                        blockID = currentAddress.subDistrictId.toInt(),
                        null,
                        null,
                        villageName = currentAddress.village ?: "",
                    )
                )
            }
        }
    }

    private suspend fun downloadLocationMasterData(currentAddress: Address?){
        downloadStateMasterData(currentAddress)
        downloadDistrictMasterData(currentAddress)
        downloadBlockMasterData(currentAddress)
        downloadVillageMasterData(currentAddress)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun downloadRegisterPatientFromServer(villageList: VillageIdList): NetworkResult<NetworkResponse>{
        return networkResultInterceptor {
                val response = apiService.downloadBeneficiariesFromServer(villageList)
                val responseBody = response.body()?.string()
                refreshTokenInterceptor(
                    responseBody = responseBody,
                    onSuccess = {
                        val data = responseBody.let { JSONObject(it).getString("data") }
                        val gson = Gson()
                        val dataListType = object : TypeToken<List<BeneficiariesDTO>>() {}.type
                        val beneficiariesDTO : List<BeneficiariesDTO> = gson.fromJson(data, dataListType)
                        var isSuccess = true

                        var totalDownloaded = 0

                        for(beneficiary in beneficiariesDTO){

                            totalDownloaded++
                            if(WorkerUtils.totalRecordsToDownload > 0 && totalDownloaded <= WorkerUtils.totalRecordsToDownload){
                                withContext(Dispatchers.Main) {
                                    WorkerUtils.totalPercentageCompleted.value = ((totalDownloaded.toDouble() / WorkerUtils.totalRecordsToDownload.toDouble())*100).toInt()
                                }
                            }

                            try{
                                var benHealthIdDetails: BenHealthIdDetails? = null
                                if(beneficiary.abhaDetails != null){
                                    benHealthIdDetails = BenHealthIdDetails(
                                        healthId = beneficiary.abhaDetails[0].HealthID!!,
                                        healthIdNumber = beneficiary.abhaDetails[0].HealthIDNumber!!
                                    )
                                }

                                downloadLocationMasterData(beneficiary.currentAddress)
                                val patient = Patient(
                                    patientID = generateUuid(),
                                    firstName = beneficiary.beneficiaryDetails?.firstName,
                                    lastName = beneficiary.beneficiaryDetails?.lastName,
                                    dob = DateTimeUtil.convertTimestampToISTDate(beneficiary.beneficiaryDetails?.dob),
                                    maritalStatusID = beneficiary.beneficiaryDetails?.maritalStatusId,
                                    spouseName = beneficiary.beneficiaryDetails?.spouseName,
                                    ageAtMarriage = beneficiary.ageAtMarriage,
                                    phoneNo = beneficiary.preferredPhoneNum,
                                    genderID = beneficiary.beneficiaryDetails?.genderId,
                                    registrationDate = DateTimeUtil.convertTimestampToISTDate(beneficiary.lastModDate),
                                    stateID = beneficiary.currentAddress?.stateId,
                                    districtID = beneficiary.currentAddress?.districtId,
                                    blockID = beneficiary.currentAddress?.subDistrictId,
                                    districtBranchID = beneficiary.currentAddress?.villageId,
                                    communityID = beneficiary.beneficiaryDetails?.communityId,
                                    religionID = beneficiary.beneficiaryDetails?.religionId,
                                    parentName = null,
                                    syncState = SyncState.SYNCED,
                                    beneficiaryID = beneficiary.benId?.toLong(),
                                    beneficiaryRegID = beneficiary.benRegId?.toLong(),
                                    healthIdDetails = benHealthIdDetails ,
                                    faceEmbedding = beneficiary.faceEmbedding
                                )

                                setPatientAge(patient)
                                val matchedPatient = patientDao.findSharedOfflinePatient(
                                    syncState = SyncState.SHARED_OFFLINE,
                                    firstName = patient.firstName,
                                    lastName = patient.lastName ?: null,  // Handle null case
                                    phoneNumber = patient.phoneNo ?: null  // Handle null case
                                )
                                if (matchedPatient != null) {
                                    patient.patientID = matchedPatient.patientID
                                    patient.syncState = SyncState.SYNCED
                                    patientDao.updatePatient(patient)  // Update the matched patient with the new data
                                }else {
                                    // check if patient is present or not
                                    if (patientDao.getCountByBenId(beneficiary.benId!!.toLong()) > 0) {
                                        patientDao.updatePatient(patient)
                                    } else {
                                        patientDao.insertPatient(patient)
                                    }
                                }
                            } catch (e: Exception){
                                isSuccess = false
                            }
                        }

                        NetworkResult.Success(DownsyncSuccess(isSuccess))
                    },
                    onTokenExpired = {
                        val user = userRepo.getLoggedInUser()!!
                        userRepo.refreshTokenTmc(user.userName, user.password)
                        downloadRegisterPatientFromServer(villageList)
                })
            }
    }

    private suspend fun getPatientsCountToDownload(villageList: VillageIdList): NetworkResult<NetworkResponse>{
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
                    getPatientsCountToDownload(villageList)
                })
        }
    }

    suspend fun getPatientDisplayListForNurseByPatient(patientID: String): PatientDisplayWithVisitInfo{
        return patientDao.getPatientDisplayListForNurseByPatient(patientID)
    }

    suspend fun processUnsyncedData() : Boolean{

//        Log.d("hey", "ya")

        val patientList = patientDao.getPatientListUnsynced();
        val user = userRepo.getLoggedInUser()

        patientList.forEach {
            updatePatientSyncing(it.patient)
            when(val response = registerNewPatient(it, user)){
                is NetworkResult.Success -> {
                    val benificiarySaveResponse = response.data as BenificiarySaveResponse
                    updatePatientSyncSuccess(it.patient, benificiarySaveResponse)
//                    caseRecordeRepo.updateBenIdAndBenRegId(
//                        beneficiaryID = benificiarySaveResponse.beneficiaryID,
//                        beneficiaryRegID = benificiarySaveResponse.beneficiaryRegID,
//                        patientID = it.patient.patientID
//                    )
//                    visitReasonsAndCategoriesRepo.updateBenIdAndBenRegId(
//                        beneficiaryID = benificiarySaveResponse.beneficiaryID,
//                        beneficiaryRegID = benificiarySaveResponse.beneficiaryRegID,
//                        patientID = it.patient.patientID
//                    )
//                    vitalsRepo.updateBenIdBenRegId(
//                        beneficiaryID = benificiarySaveResponse.beneficiaryID,
//                        beneficiaryRegID = benificiarySaveResponse.beneficiaryRegID,
//                        patientID = it.patient.patientID
//                    )
//                    patientVisitInfoSyncRepo.updatePatientVisitInfoBenIdAndBenRegId(
//                        beneficiaryID = benificiarySaveResponse.beneficiaryID,
//                        beneficiaryRegID = benificiarySaveResponse.beneficiaryRegID,
//                        patientID = it.patient.patientID
//                    )
                }
                is NetworkResult.Error -> {
                    updatePatientSyncingFailed(it.patient)
                    if(response.code == socketTimeoutException){
                        throw SocketTimeoutException("This is an example exception message")
                    }
                }
                else -> {

                }
            }
        }

        return true;

    }

    suspend fun getWorkLocationMappedAbdmFacility(visitCode: Long? = 0L, benId: Long? = 0L, benRegId: Long? = 0L): NetworkResult<NetworkResponse> {

        return networkResultInterceptor {
            val response = apiService
                .getWorkLocationMappedAbdmFacility(preferenceDao.getWorkingLocationID().toString())
            val responseBody = response.body()?.string()
            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
                    val jsonObj = JSONObject(responseBody)
                    val data = jsonObj.getJSONObject("data")
                    abdmFacilityId = data.getString("abdmFacilityID")
                    abdmFacilityName = data.getString("abdmFacilityName")
//                    val result = Gson().fromJson(data, BenificiarySaveResponse::class.java)
//                    NetworkResult.Success(result)
                    saveAbdmFacilityId(visitCode, abdmFacilityId, benId, benRegId)
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    getWorkLocationMappedAbdmFacility(visitCode = visitCode, benId = benId, benRegId = benRegId)
                },
            )
        }

    }

    suspend fun saveAbdmFacilityId(visitCode: Long? = 0L, abdmFacilityId: String? = "", benId: Long? = 0L, benRegId: Long? = 0L): NetworkResult<NetworkResponse> {

        return networkResultInterceptor {
            val response = apiService
                .saveAbdmFacilityId(SaveAbdmFacilityId(visitCode, abdmFacilityId))
            val responseBody = response.body()?.string()
            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
                    getBeneficiaryHealthId(benId, benRegId)
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    saveAbdmFacilityId(visitCode, abdmFacilityId, benId, benRegId)
                },
            )
        }

    }

    suspend fun getBeneficiaryHealthId(benId: Long? = 0L, benRegId: Long? = 0L): NetworkResult<NetworkResponse> {

        return networkResultInterceptor {
            val response = apiService
                .getBenHealthID(GetBenHealthIdRequest(benRegId, benId))
            val responseBody = response.body()?.string()
            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
                    val jsonObj = JSONObject(responseBody)
                    val data = jsonObj.getJSONObject("data").getJSONArray("BenHealthDetails")
                        .toString()
                    val bens = Gson().fromJson(data, Array<BenHealthDetails>::class.java)
                    if (bens.isNotEmpty()) {
                        NetworkResult.Success(bens.last())
                    } else {
                        NetworkResult.Error(0, "No data")
                    }
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    getBeneficiaryHealthId(benId = benId, benRegId = benRegId)
                },
            )
        }

    }

    suspend fun generateOTPForCareContext(healthID: String, healthIdNumber: String): NetworkResult<NetworkResponse> {

        return networkResultInterceptor {
            val response = apiService
                .generateOTPForCareContext(GenerateOTPForCareContextRequest(healthID, healthIdNumber, abdmFacilityId, abdmFacilityName))
            val responseBody = response.body()?.string()
            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
                    val jsonObj = JSONObject(responseBody)
                    val data = jsonObj.getJSONObject("data").toString()
                    val data2 = Gson().fromJson(data, GenerateOTPForCareContext::class.java)
                    if (!data2.txnId.isNullOrEmpty()) {
                        NetworkResult.Success(data2)
                    } else {
                        NetworkResult.Error(0, "No data")
                    }
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    generateOTPForCareContext(healthID, healthIdNumber)
                },
            )
        }

    }

    suspend fun validateOTPAndCreateCareContext(otp: String, txnId: String, beneficiaryID: Long, healthID: String, healthIdNumber: String, visitCode: Long, visitCategory: String): NetworkResult<NetworkResponse> {

        return networkResultInterceptor {
            val response = apiService
                .validateOTPAndCreateCareContext(ValidateOTPAndCreateCareContextRequest(otp, txnId, beneficiaryID, healthID, healthIdNumber, visitCode, visitCategory, abdmFacilityId, abdmFacilityName))
            val responseBody = response.body()?.string()
            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
                    val jsonObj = JSONObject(responseBody)
                    val data = jsonObj.getJSONObject("data").toString()
                    val data2 = Gson().fromJson(data, ValidateOTPAndCreateCareContextResponse::class.java)
                    if (!data2.response.isNullOrEmpty()) {
                        NetworkResult.Success(data2)
                    } else {
                        NetworkResult.Error(0, "No data")
                    }
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    generateOTPForCareContext(healthID, healthIdNumber)
                },
            )
        }

    }

    fun getPatientDisplayListForNurse() : Flow<List<PatientDisplayWithVisitInfo>> {
        return patientDao.getPatientDisplayListForNurse()
    }

    fun getPatientDisplayListForDoctor() : Flow<List<PatientDisplayWithVisitInfo>> {
        return patientDao.getPatientDisplayListForDoctor()
    }

    suspend fun getProcedures(benVisitInfo: PatientDisplayWithVisitInfo): List<ProcedureDTO>? {
        val dtos: MutableList<ProcedureDTO> = mutableListOf()
        return withContext(Dispatchers.IO) {
            try {

                val procedures = procedureDao.getProceduresByPatientIdAndBenVisitNo(benVisitInfo.patient.patientID, benVisitInfo.benVisitNo!!)
                procedures?.forEach { procedure ->
                    val compListDetails: MutableList<ComponentDetailDTO> = mutableListOf()
                    val procedureDTO = ProcedureDTO(
                        benRegId = benVisitInfo.patient.beneficiaryRegID!!,
                        procedureDesc = procedure.procedureDesc,
                        procedureType = procedure.procedureType,
                        prescriptionID = procedure.prescriptionID,
                        procedureID = procedure.procedureID,
                        procedureName = procedure.procedureName,
                        compListDetails = compListDetails,
                        isMandatory = procedure.isMandatory
                    )

                    val components = procedureDao.getComponentDetails(procedure.id)
                    components?.forEach { componentDetails ->
                        val componentOptionDTOs: MutableList<ComponentOptionDTO> = mutableListOf()
                        val componentDetailDTO = ComponentDetailDTO(
                            id = componentDetails.id,
                            range_normal_min = componentDetails.rangeNormalMin,
                            range_normal_max = componentDetails.rangeNormalMax,
                            range_min = componentDetails.rangeMin,
                            range_max = componentDetails.rangeMax,
                            isDecimal = componentDetails.isDecimal,
                            inputType = componentDetails.inputType,
                            testComponentID = componentDetails.testComponentID,
                            measurementUnit = componentDetails.measurementUnit,
                            testComponentName = componentDetails.testComponentName,
                            testComponentDesc = componentDetails.testComponentDesc,
                            testResultValue = componentDetails.testResultValue,
                            remarks = componentDetails.remarks,
                            compOpt = componentOptionDTOs
                        )

                        val componentOptions = procedureDao.getComponentOptions(componentDetails.id)
                        componentOptions?.forEach { option ->
                            val componentOptionDTO = ComponentOptionDTO(
                                name = option.name
                            )
                            componentOptionDTOs += componentOptionDTO
                        }
                        componentDetailDTO.compOpt = componentOptionDTOs
                        compListDetails += componentDetailDTO
                    }
                    procedureDTO.compListDetails = compListDetails
                    dtos += procedureDTO
                }

                dtos
            } catch (e: Exception) {
                Timber.d("get failed due to $e")
                null
            }
        }

    }

    suspend fun getProcedure(patientID: String, benVisitNo: Int, procedureID: Long): Procedure {
        return withContext(Dispatchers.IO) {
            procedureDao.getProcedure(patientID, benVisitNo, procedureID)
        }
    }

    suspend fun getComponent(procedureId: Long, testComponentID: Long): ComponentDetails {
        return withContext(Dispatchers.IO) {
            procedureDao.getComponentDetails(procedureId, testComponentID)
        }
    }

    suspend fun updateComponentDetails(componentDetails: ComponentDetails): Long {
        return withContext(Dispatchers.IO) {
            val componentDetailsId = componentDetails.id

            val updatedId = procedureDao.insert(componentDetails)

            procedureDao.getComponentOptions(componentDetailsId)?.forEach { componentOption ->
                componentOption.componentDetailsId = updatedId
                procedureDao.insert(componentOption)
            }
            updatedId
        }
    }

    suspend fun updatePrescription(prescription: Prescription): Int {
        return withContext(Dispatchers.IO) {
            val updatedId = prescriptionDao.updatePrescription(prescription.issueType, prescription.prescriptionID)

            updatedId
        }
    }

    suspend fun getPrescriptions(benVisitInfo : PatientDisplayWithVisitInfo): List<PrescriptionDTO>? {
        val dtos: MutableList<PrescriptionDTO> = mutableListOf()
        return withContext(Dispatchers.IO) {
            try {
                val prescriptions = prescriptionDao.getPrescriptionsByPatientIdAndBenVisitNo(benVisitInfo.patient.patientID, benVisitInfo.benVisitNo!!)
                Log.d("MyPrescription", "Prescription1 ${prescriptions}")
                prescriptions?.forEach { prescription ->
                    val prescriptionItemList: MutableList<PrescriptionItemDTO> = mutableListOf()
                    val prescriptionDTO = PrescriptionDTO(
                        beneficiaryRegID = benVisitInfo.patient.beneficiaryRegID?:null,
                        consultantName = prescription.consultantName,
                        prescriptionID = prescription.prescriptionID,
                        visitCode = prescription.visitCode,
                        itemList = prescriptionItemList
                    )

                    val prescribedDrugsList = prescriptionDao.getPrescribedDrugs(prescription.id)
                    Log.d("MyPrescription", "Prescription11 ${prescribedDrugsList}")
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
                        Log.d("MyPrescription", "Prescription2 ${prescribedDrugsBatchList}")
                        val batches = batchDao.getBatchesByItemID(prescribedDrugs.drugID.toInt())
                        Log.d("MyPrescription", "Prescription3 ${batches}")
                        val batchMap = batches.associateBy { it.batchNo }

                        // List to store updated PrescribedDrugsBatch entries
                        val updatedPrescribedDrugsBatches = mutableListOf<PrescribedDrugsBatch>()
                        val updatedBatchNumbers = mutableSetOf<String>()
                        prescribedDrugsBatchList?.forEach { prescribedDrugsBatch ->
                            val correspondingBatch = batchMap[prescribedDrugsBatch.batchNo]
                            if (correspondingBatch != null) {
                                // Update existing entry
                                val updatedPrescribedDrugsBatch = prescribedDrugsBatch.copy(
                                    qty = correspondingBatch.quantityInHand
                                )
                                updatedPrescribedDrugsBatches.add(updatedPrescribedDrugsBatch)
                                updatedBatchNumbers.add(prescribedDrugsBatch.batchNo)
                            }
                            // If correspondingBatch is null, this entry will be omitted (effectively removed)
                        }

                        val batchesToDelete = prescribedDrugsBatchList?.filter { it.batchNo !in updatedBatchNumbers } ?: emptyList()
                        Log.d("MyPrescription", "Prescription4 ${batchesToDelete}")

                        batchesToDelete.forEach { batchToDelete ->
                            prescriptionDao.deletePrescribedDrugsBatch(batchToDelete)
                        }

                        Log.d("MyPrescription", "Prescription5 ${updatedPrescribedDrugsBatches}")

                        // Perform the update or insert operations
                        updatedPrescribedDrugsBatches.forEach { updatedBatch ->
                            prescriptionDao.updatePrescribedDrugsBatch(updatedBatch)
                        }


                        updatedPrescribedDrugsBatches?.forEach { prescribedDrugsBatch ->
                            val prescriptionBatchDTO = PrescriptionBatchDTO(
                                expiresIn = prescribedDrugsBatch.expiresIn,
                                batchNo = prescribedDrugsBatch.batchNo,
                                expiryDate = prescribedDrugsBatch.expiryDate,
                                itemStockEntryID = prescribedDrugsBatch.itemStockEntryID,
                                qty = prescribedDrugsBatch.qty,
                                isSelected = false,
                                dispenseQuantity = 0
                            )
                            batchList += prescriptionBatchDTO
                        }
                        prescriptionItemDTO.batchList = batchList
                        prescriptionItemList += prescriptionItemDTO
                    }
                    prescriptionDTO.itemList = prescriptionItemList
                    dtos += prescriptionDTO
                }
                dtos
            } catch (e: Exception) {
                Timber.d("get failed due to $e")
                Log.d("MyPrescription", "Prescription12 ${e}")
                null
            }
        }
    }

    suspend fun getPrescription(patientID: String, benVisitNo:Int, prescriptionID: Long): Prescription {
        return withContext(Dispatchers.IO) {
            prescriptionDao.getPrescription(patientID, benVisitNo, prescriptionID)
        }
    }
}