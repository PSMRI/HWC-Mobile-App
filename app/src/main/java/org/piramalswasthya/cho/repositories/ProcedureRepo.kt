package org.piramalswasthya.cho.repositories

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.cho.database.room.InAppDb
import org.piramalswasthya.cho.database.room.dao.ProcedureDao
import org.piramalswasthya.cho.database.room.dao.ProcedureMasterDao
import org.piramalswasthya.cho.model.ComponentDetails
import org.piramalswasthya.cho.model.ComponentDetailsMaster
import org.piramalswasthya.cho.model.ComponentOption
import org.piramalswasthya.cho.model.ComponentOptionsMaster
import org.piramalswasthya.cho.model.MasterLabProceduresRequestModel
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.Procedure
import org.piramalswasthya.cho.model.ProcedureDataDownsync
import org.piramalswasthya.cho.model.ProcedureDataWithComponent
import org.piramalswasthya.cho.model.ProcedureMaster
import org.piramalswasthya.cho.model.ComponentDataDownsync
import org.piramalswasthya.cho.model.ProcedureMasterDTO
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.NetworkResponse
import org.piramalswasthya.cho.network.NetworkResult
import org.piramalswasthya.cho.network.networkResultInterceptor
import org.piramalswasthya.cho.network.refreshTokenInterceptor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


class ProcedureRepo @Inject constructor(
    private val procedureDao: ProcedureDao,
    private val procedureMasterDao: ProcedureMasterDao,
    private val inAppDb: InAppDb,
    private val apiService: AmritApiService,
    private val userRepo: UserRepo
) {
    suspend fun getProceduresWithComponent(
        patientID: String,
        benVisitNo: Int
    ): List<ProcedureDataWithComponent> {
        return withContext(Dispatchers.IO) {
            procedureDao.getProceduresWithComponent(patientID, benVisitNo)
        }
    }

    suspend fun syncLabResultsToDownsyncTable(patientID: String, benVisitNo: Int) {
        withContext(Dispatchers.IO) {
            val procedures = procedureDao.getProceduresByPatientIdAndBenVisitNo(patientID, benVisitNo) ?: return@withContext
            procedureDao.deleteProcedureDownsyncByPatientIdAndVisitNo(patientID, benVisitNo)
            val createdDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            procedures.forEach { procedure ->
                val downsync = ProcedureDataDownsync(
                    id = 0,
                    prescriptionID = procedure.prescriptionID.toInt(),
                    procedureID = procedure.procedureID.toInt(),
                    createdDate = createdDate,
                    procedureName = procedure.procedureName,
                    patientID = patientID,
                    benVisitNo = benVisitNo
                )
                val downsyncId = procedureDao.insert(downsync)
                val components = procedureDao.getComponentDetails(procedure.id) ?: emptyList()
                components.forEach { component ->
                    procedureDao.insert(
                        ComponentDataDownsync(
                            id = 0,
                            procedureDataID = downsyncId,
                            testResultValue = component.testResultValue,
                            testResultUnit = component.measurementUnit,
                            testComponentID = component.testComponentID.toInt(),
                            componentName = component.testComponentName,
                            remarks = component.remarks
                        )
                    )
                }
            }
        }
    }

    suspend fun pullLabProcedureMasterData(): Boolean {
        return try {
            getProcedureMasterData();
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun ensureLabProcedureMasterSeed() {
        withContext(Dispatchers.IO) {
            if (procedureMasterDao.getMasterProcedureById(101L) != null) return@withContext
            inAppDb.runSeedLabProcedureMaster()
        }
    }


    private suspend fun getProcedureMasterData(): NetworkResult<NetworkResponse> {
        return networkResultInterceptor {
                val procedureMasterDataRequest = MasterLabProceduresRequestModel(
                    providerServiceMapID = userRepo.getLoggedInUser()?.serviceMapId

                )

            val response = apiService.getMasterLabProceduresDate(procedureMasterDataRequest)
            val responseBody = response.body()?.string()

            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
                    val jsonObj = JSONObject(responseBody)
                    val data = jsonObj.getJSONObject("data").getJSONArray("laboratoryList")
                        .toString()
                    val procedureDTO = Gson().fromJson(data, Array<ProcedureMasterDTO>::class.java)

                    procedureDTO.forEach { dto ->
                        val procedure = ProcedureMaster(
                            procedureID = dto.procedureID,
                            procedureDesc = dto.procedureDesc,
                            procedureType = dto.procedureType,
                            procedureName = dto.procedureName,
                            prescriptionID = dto.prescriptionID,
                            isMandatory = dto.isMandatory
                        )
                        val procedureId = procedureMasterDao.insert(procedure)
                        dto.compListDetails.forEach { componentDetailDTO ->
                            val componentDetails = ComponentDetailsMaster(
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
                            val componentId = procedureMasterDao.insert(componentDetails)
                            componentDetailDTO.compOpt.forEach { option ->
                                option.name?.let {
                                    val compOption = ComponentOptionsMaster(
                                        componentDetailsId = componentId,
                                        name = it
                                    )
                                    procedureMasterDao.insert(compOption)
                                }
                            }
                        }
                    }

                    NetworkResult.Success(NetworkResponse())
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    getProcedureMasterData()
                },
            )
        }
    }

    suspend fun addProcedure(procedureID: Long, benVisitInfo: PatientDisplayWithVisitInfo) {
        addProcedureFromMaster(procedureID, benVisitInfo.patient.patientID, benVisitInfo.benVisitNo!!)
    }

    suspend fun copyProceduresFromMasterForVisit(patientID: String, benVisitNo: Int, newTestIds: String?) {
        if (newTestIds.isNullOrBlank()) return
        withContext(Dispatchers.IO) {
            ensureLabProcedureMasterSeed()
            val existingProcedures = procedureDao.getProceduresByPatientIdAndBenVisitNo(patientID, benVisitNo)
            val existingProcedureIds = existingProcedures?.map { it.procedureID }?.toSet() ?: emptySet()
            newTestIds.split(",").mapNotNull { it.trim().toLongOrNull() }.forEach { procedureID ->
                if (procedureID !in existingProcedureIds) {
                    addProcedureFromMaster(procedureID, patientID, benVisitNo)
                }
            }
        }
    }

    private suspend fun addProcedureFromMaster(procedureID: Long, patientID: String, benVisitNo: Int) {
        val procedureMaster = procedureMasterDao.getMasterProcedureById(procedureID) ?: return
        val procedure = Procedure(
            patientID = patientID,
            benVisitNo = benVisitNo,
            procedureID = procedureMaster.procedureID,
            procedureDesc = procedureMaster.procedureDesc,
            procedureType = procedureMaster.procedureType,
            procedureName = procedureMaster.procedureName,
            prescriptionID = procedureMaster.prescriptionID,
            isMandatory = procedureMaster.isMandatory
        )
        val procedureId = procedureDao.insert(procedure)
        val componentDetailsMasterList = procedureMasterDao.getComponentDetails(procedureMaster.id)
        componentDetailsMasterList.forEach { componentDetailsMaster ->
            val component = ComponentDetails(
                procedureID = procedureId,
                testComponentID = componentDetailsMaster.testComponentID,
                rangeNormalMin = componentDetailsMaster.rangeNormalMin,
                rangeNormalMax = componentDetailsMaster.rangeNormalMax,
                rangeMax = componentDetailsMaster.rangeMax,
                rangeMin = componentDetailsMaster.rangeMin,
                isDecimal = componentDetailsMaster.isDecimal,
                inputType = componentDetailsMaster.inputType,
                measurementUnit = componentDetailsMaster.measurementUnit,
                testComponentName = componentDetailsMaster.testComponentName,
                testComponentDesc = componentDetailsMaster.testComponentDesc,
                testResultValue = null,
                remarks = null
            )
            val componentId = procedureDao.insert(component)
            val componentOptionsMaster = procedureMasterDao.getComponentOptions(componentDetailsMaster.id)
            componentOptionsMaster?.forEach { option ->
                val componentOption = ComponentOption(
                    componentDetailsId = componentId,
                    name = option.name
                )
                procedureDao.insert(componentOption)
            }
        }
    }

}