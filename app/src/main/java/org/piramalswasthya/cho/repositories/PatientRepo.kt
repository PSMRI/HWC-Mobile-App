package org.piramalswasthya.cho.repositories

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.cho.database.room.InAppDb
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.BlockMasterDao
import org.piramalswasthya.cho.database.room.dao.CaseRecordeDao
import org.piramalswasthya.cho.database.room.dao.DistrictMasterDao
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.database.room.dao.ProcedureDao
import org.piramalswasthya.cho.database.room.dao.RegistrarMasterDataDao
import org.piramalswasthya.cho.database.room.dao.StateMasterDao
import org.piramalswasthya.cho.database.room.dao.VillageMasterDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.BenHealthIdDetails
import org.piramalswasthya.cho.model.BeneficiariesDTO
import org.piramalswasthya.cho.database.room.dao.VisitReasonsAndCategoriesDao
import org.piramalswasthya.cho.database.room.dao.VitalsDao
import org.piramalswasthya.cho.model.Address
import org.piramalswasthya.cho.model.ComponentDetailDTO
import org.piramalswasthya.cho.model.ComponentOptionDTO
import org.piramalswasthya.cho.model.BenFlow
import org.piramalswasthya.cho.model.BlockMaster
import org.piramalswasthya.cho.model.DistrictMaster
import org.piramalswasthya.cho.model.ComponentDetails
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.PatientNetwork
import org.piramalswasthya.cho.model.Procedure
import org.piramalswasthya.cho.model.ProcedureDTO
import org.piramalswasthya.cho.model.StateMaster
import org.piramalswasthya.cho.model.UserDomain
import org.piramalswasthya.cho.model.VillageMaster
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.BenHealthDetails
import org.piramalswasthya.cho.network.BenificiarySaveResponse
import org.piramalswasthya.cho.network.DistrictList
import org.piramalswasthya.cho.network.GetBenHealthIdRequest
import org.piramalswasthya.cho.network.NetworkResponse
import org.piramalswasthya.cho.network.NetworkResult
import org.piramalswasthya.cho.network.VillageIdList
import org.piramalswasthya.cho.network.networkResultInterceptor
import org.piramalswasthya.cho.network.refreshTokenInterceptor
import org.piramalswasthya.cho.network.socketTimeoutException
import org.piramalswasthya.cho.utils.DateTimeUtil
import org.piramalswasthya.cho.utils.generateUuid
import timber.log.Timber
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
    private val registrarMasterDataDao: RegistrarMasterDataDao
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

        return networkResultInterceptor {
            val patNet = PatientNetwork(patient, user)
            Timber.d("patient register is ", patNet.toString())
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

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun downloadAndSyncPatientRecords(): Boolean {
        val user = userRepo.getLoggedInUser()

        val villageList = VillageIdList(
            convertStringToIntList(user?.assignVillageIds ?: ""),
            preferenceDao.getLastSyncTime()
        )

        return when(val response = downloadRegisterPatientFromServer(villageList)){
            is NetworkResult.Success -> {
                true
            }

            is NetworkResult.Error -> {
                if(response.code == socketTimeoutException){
                    throw SocketTimeoutException("This is an example exception message")
                }
                true
            }

            else -> {
                true
            }
        }
    }
    private fun convertStringToIntList(villageIds : String) : List<Int>{
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

                        for(beneficiary in beneficiariesDTO){
                            var benHealthIdDetails: BenHealthIdDetails? = null
                            if(beneficiary.abhaDetails != null){
                                benHealthIdDetails = BenHealthIdDetails(
                                    healthId = beneficiary.abhaDetails[0].HealthID,
                                    healthIdNumber = beneficiary.abhaDetails[0].HealthIDNumber
                                )
                            }

                            downloadLocationMasterData(beneficiary.currentAddress)

                            val patient = Patient(
                                patientID = generateUuid(),
                                firstName = beneficiary.beneficiaryDetails?.firstName,
                                lastName = beneficiary.beneficiaryDetails?.lastName,
                                dob = beneficiary.beneficiaryDetails?.dob,
                                maritalStatusID = beneficiary.beneficiaryDetails?.maritalStatusId,
                                spouseName = beneficiary.beneficiaryDetails?.spouseName,
                                ageAtMarriage = beneficiary.ageAtMarriage,
                                phoneNo = beneficiary.preferredPhoneNum,
                                genderID = beneficiary.beneficiaryDetails?.genderId,
                                registrationDate = beneficiary.createdDate,
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
                                healthIdDetails = benHealthIdDetails
                            )

                            setPatientAge(patient)

                            // check if patient is present or not
                            if(patientDao.getCountByBenId(beneficiary.benId!!.toLong()) > 0){
                                patientDao.updatePatient(patient)
                            } else {
                                patientDao.insertPatient(patient)
                            }
                        }

                        NetworkResult.Success(NetworkResponse())
                    },
                    onTokenExpired = {
                        val user = userRepo.getLoggedInUser()!!
                        userRepo.refreshTokenTmc(user.userName, user.password)
                        downloadRegisterPatientFromServer(villageList)
                })
            }
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

    fun getPatientDisplayListForNurse() : Flow<List<PatientDisplayWithVisitInfo>> {
        return patientDao.getPatientDisplayListForNurse()
    }

    suspend fun getProcedures(patientId: String): List<ProcedureDTO>? {
        val dtos: MutableList<ProcedureDTO> = mutableListOf()
        return withContext(Dispatchers.IO) {
            try {
                patientDao.getPatient(patientId).beneficiaryRegID?.let {
                    val procedures = procedureDao.getProcedures(it)
                    procedures?.forEach { procedure ->
                        val compListDetails: MutableList<ComponentDetailDTO> = mutableListOf()
                        val procedureDTO = ProcedureDTO(
                            benRegId = it,
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
                }
                dtos
            } catch (e: Exception) {
                Timber.d("get failed due to $e")
                null
            }
        }

    }

    suspend fun getProcedure(benRegId: Long, procedureID: Long): Procedure {
        return withContext(Dispatchers.IO) {
            procedureDao.getProcedure(benRegId, procedureID)
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


}