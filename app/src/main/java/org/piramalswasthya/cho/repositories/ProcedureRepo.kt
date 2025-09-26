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
import org.piramalswasthya.cho.model.ProcedureDataWithComponent
import org.piramalswasthya.cho.model.ProcedureMaster
import org.piramalswasthya.cho.model.ProcedureMasterDTO
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.NetworkResponse
import org.piramalswasthya.cho.network.NetworkResult
import org.piramalswasthya.cho.network.networkResultInterceptor
import org.piramalswasthya.cho.network.refreshTokenInterceptor
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

    suspend fun pullLabProcedureMasterData(): Boolean {
        return try {
            getProcedureMasterData();
            true
        } catch (e: Exception) {
            false
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
        var procedureMaster = procedureMasterDao.getMasterProcedureById(procedureID)
        procedureMaster?.let {
            val procedure = Procedure(
                patientID = benVisitInfo.patient.patientID,
                benVisitNo = benVisitInfo.benVisitNo!!,
                procedureID = procedureMaster.procedureID,
                procedureDesc = procedureMaster.procedureDesc,
                procedureType = procedureMaster.procedureType,
                procedureName = procedureMaster.procedureName,
                prescriptionID = procedureMaster.prescriptionID,
                isMandatory = procedureMaster.isMandatory
            )
            val procedureId = procedureDao.insert(procedure)
            val componentDetailsMasterList = procedureMasterDao.getComponentDetails(procedureId)
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
                val componentOptionsMaster = procedureMasterDao.getComponentOptions(componentId)
                componentOptionsMaster?.forEach { componentOptionsMaster ->
                        val componentOption = ComponentOption(
                            componentDetailsId = componentOptionsMaster.componentDetailsId,
                            name = componentOptionsMaster.name
                        )
                        procedureDao.insert(componentOption)
                }
            }
        }

    }

}