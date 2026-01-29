package org.piramalswasthya.cho.repositories

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
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

    /**
     * Sync lab results from procedure + component_details to PROCEDURE_DATA_DOWNSYNC
     * so the doctor's case record page can show submitted lab report data.
     */
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

    /**
     * Ensures procedure_master has the 7 lab procedures (seed data).
     * Runs when migration didn't (e.g. fresh install at version 111) so lab form can render from DB without API.
     */
    suspend fun ensureLabProcedureMasterSeed() {
        withContext(Dispatchers.IO) {
            if (procedureMasterDao.getMasterProcedureById(101L) != null) return@withContext
            seedLabProcedureMasterData()
        }
    }

    private suspend fun seedLabProcedureMasterData() {
        val procedures = listOf(
            Triple(101L, "Random Blood Glucose (RBS)", "Laboratory"),
            Triple(104L, "RPR Card Test for Syphilis", "Laboratory"),
            Triple(105L, "HIV-1 & HIV-2 (RDT)", "Laboratory"),
            Triple(106L, "Serum Uric Acid", "Laboratory"),
            Triple(107L, "HBsAg (RDT)", "Laboratory"),
            Triple(108L, "Serum Total Cholesterol", "Laboratory"),
            Triple(110L, "Hemoglobin", "Laboratory")
        )
        val components = listOf(
            arrayOf(102L, 41, 140, 40, 500, true, "TextBox", "mg/dl", "Random Blood Glucose (RBS)", "Random Blood Glucose (RBS)"),
            arrayOf(105L, null, null, null, null, false, "RadioButton", null, "RPR Card Test for Syphilis", "RPR Card Test for Syphilis"),
            arrayOf(106L, null, null, null, null, false, "RadioButton", null, "HIV-1 & HIV-2 (RDT)", "HIV-1 & HIV-2 (RDT)"),
            arrayOf(107L, 3, 7, 0, 30, true, "TextBox", "mg/dl", "Serum Uric Acid", "Serum Uric Acid"),
            arrayOf(108L, null, null, null, null, false, "RadioButton", null, "HBsAg (RDT)", "HBsAg (RDT)"),
            arrayOf(109L, 100, 200, 99, 400, true, "TextBox", "mg/dl", "Serum Total Cholesterol", "Serum Total Cholesterol"),
            arrayOf(111L, 4, 15, 1, 18, true, "TextBox", "g/dL", "Hemoglobin", "Hemoglobin")
        )
                val componentOptions = listOf(
            emptyList(),
            listOf("Negative", "Positive"),
            listOf("Negative", "Positive"),
            emptyList(),
            listOf("Negative", "Positive"),
            emptyList(),
            emptyList()
        )
        procedures.forEachIndexed { i, (procId, name, procType) ->
            val procedure = ProcedureMaster(
                procedureID = procId,
                procedureDesc = name,
                procedureType = procType,
                prescriptionID = 2802381L,
                procedureName = name,
                isMandatory = false
            )
            val procedureMasterId = procedureMasterDao.insert(procedure)
            val comp = components[i]
            val compDetails = ComponentDetailsMaster(
                testComponentID = comp[0] as Long,
                procedureID = procedureMasterId,
                rangeNormalMin = comp[1] as? Int,
                rangeNormalMax = comp[2] as? Int,
                rangeMin = comp[3] as? Int,
                rangeMax = comp[4] as? Int,
                isDecimal = comp[5] as Boolean,
                inputType = comp[6] as String,
                measurementUnit = comp[7] as? String,
                testComponentName = comp[8] as String,
                testComponentDesc = comp[9] as String
            )
            val compDetailsId = procedureMasterDao.insert(compDetails)
            componentOptions[i].forEach { optName ->
                procedureMasterDao.insert(ComponentOptionsMaster(componentDetailsId = compDetailsId, name = optName))
            }
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

    /**
     * Copy prescribed lab procedures from ProcedureMaster to Procedure table for this visit.
     * Called when doctor saves investigation with newTestIds so lab technician can render form from DB without API.
     * Replaces existing procedures for this visit with only the selected tests (so only those forms show in lab record).
     */
    suspend fun copyProceduresFromMasterForVisit(patientID: String, benVisitNo: Int, newTestIds: String?) {
        if (newTestIds.isNullOrBlank()) return
        withContext(Dispatchers.IO) {
            ensureLabProcedureMasterSeed()
            procedureDao.deleteProcedureByPatientIDAndBenVisitNo(patientID, benVisitNo)
            procedureDao.deleteProcedureDownsyncByPatientIdAndVisitNo(patientID, benVisitNo)
            newTestIds.split(",").mapNotNull { it.trim().toLongOrNull() }.forEach { procedureID ->
                addProcedureFromMaster(procedureID, patientID, benVisitNo)
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