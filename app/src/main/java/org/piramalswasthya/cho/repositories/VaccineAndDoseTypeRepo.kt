package org.piramalswasthya.cho.repositories

import androidx.lifecycle.LiveData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.cho.database.converters.MasterDataListConverter
import org.piramalswasthya.cho.database.room.dao.ImmunizationDao
import org.piramalswasthya.cho.database.room.dao.VaccinationTypeAndDoseDao
import org.piramalswasthya.cho.database.room.dao.VisitReasonsAndCategoriesDao
import org.piramalswasthya.cho.model.ChildImmunizationCategory
import org.piramalswasthya.cho.model.DoseType
import org.piramalswasthya.cho.model.GenderMaster
import org.piramalswasthya.cho.model.ImmunizationCategory
import org.piramalswasthya.cho.model.Vaccine
import org.piramalswasthya.cho.model.VaccineType
import org.piramalswasthya.cho.model.VisitReason
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.TmcLocationDetailsRequest
import timber.log.Timber
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class VaccineAndDoseTypeRepo @Inject constructor(
    private val vaccinationTypeAndDoseDao: VaccinationTypeAndDoseDao,
    private val apiService: AmritApiService,
    private val vaccineDao: ImmunizationDao,
    private val userRepo: UserRepo,
    private val immunizationDao: ImmunizationDao
){

    suspend fun getVaccineDetailsFromServer(): Int {
        return withContext(Dispatchers.IO) {
            val user = userRepo.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")
            try {
                val response = apiService.getAllChildVaccines(category = "CHILD")
                val statusCode = response.code()
                if (statusCode == 200) {
                    val responseString = response.body()?.string()
                    if (responseString != null) {
                        val jsonObj = JSONObject(responseString)

                        val errorMessage = jsonObj.getString("errorMessage")
                        val responseStatusCode = jsonObj.getInt("statusCode")
                        Timber.d("Pull from amrit child vaccine data : $responseStatusCode")
                        when (responseStatusCode) {
                            200 -> {
                                try {
                                    val dataObj = jsonObj.getString("data")
                                    saveVaccinesFromResponse(dataObj)
                                } catch (e: Exception) {
                                    Timber.d("Child Vaccine entries not synced $e")
                                    return@withContext 0
                                }

                                return@withContext 1
                            }

                            5002 -> {
                                if (userRepo.refreshTokenTmc(
                                        user.userName, user.password
                                    )
                                ) throw SocketTimeoutException("Refreshed Token!")
                                else throw IllegalStateException("User Logged out!!")
                            }

                            5000 -> {
                                if (errorMessage == "No record found") return@withContext 0
                            }

                            else -> {
                                throw IllegalStateException("$responseStatusCode received, don't know what todo!?")
                            }
                        }
                    }
                }

            } catch (e: SocketTimeoutException) {
                Timber.d("get_child_vaccines error : $e")
                getVaccineDetailsFromServer()

            } catch (e: java.lang.IllegalStateException) {
                Timber.d("get_child_vaccines error : $e")
                return@withContext -1
            }
            -1
        }
    }

    private suspend fun saveVaccinesFromResponse(dataObj: String) {
        val vaccineList = Gson().fromJson(dataObj, Array<Vaccine>::class.java).toList()
        vaccineList.forEach { vaccine ->
            val existingVaccine: Vaccine? = immunizationDao.getVaccineByName(vaccine.vaccineName)
            if (existingVaccine == null) {
                immunizationDao.addVaccine(vaccine)
            }
        }
    }



    private suspend fun vaccinationTypeAndDoseService(): JSONObject? {
        val response = apiService.getVaccinationTypeAndDoseTaken()
        val statusCode = response.code()
        if (statusCode == 200) {
            val responseString = response.body()?.string()
            val responseJson = responseString?.let { JSONObject(it) }
            Timber.tag("dataJson").d(responseJson.toString())
            return responseJson?.getJSONObject("data")
        }else{
            throw Exception("Failed to get data!")
        }
    }


    //VACCINE TYPE
    private suspend fun vaccineTypeService(): List<VaccineType> {
        val vaccineTypeList = vaccinationTypeAndDoseService()?.getJSONArray("vaccineType")
        return MasterDataListConverter.toVaccineTypeList(vaccineTypeList.toString())
    }

    suspend fun saveVaccineTypeResponseToCache() {
        vaccineTypeService().forEach { vaccineType: VaccineType ->
            withContext(Dispatchers.IO) {
                vaccinationTypeAndDoseDao.insertVaccineType(vaccineType)
            }
            Timber.tag("vaccineTypeItem").d(vaccineType.toString())
        }
    }

     fun getVaccineTypeCachedResponse(): LiveData<List<VaccineType>> {
        return vaccinationTypeAndDoseDao.getVaccineType()
    }

    //DOSE TYPE
    private suspend fun doseTypeService(): List<DoseType> {
        val doseTypeList = vaccinationTypeAndDoseService()?.getJSONArray("doseType")
        return MasterDataListConverter.toDoseTypeList(doseTypeList.toString())
    }

    suspend fun saveDoseTypeResponseToCache() {
        doseTypeService().forEach { doseType: DoseType ->
            withContext(Dispatchers.IO) {
                vaccinationTypeAndDoseDao.insertDoseType(doseType)
            }
            Timber.tag("doseTypeItem").d(doseType.toString())
        }
    }

     fun getDoseTypeCachedResponse(): LiveData<List<DoseType>> {
        return vaccinationTypeAndDoseDao.getDoseType()
    }

    suspend fun getVaccineTypeByNameMap():Map<Int,String>{
        return vaccinationTypeAndDoseDao.getVaccineTypeMasterMap().associate {
            it.covidVaccineTypeID to it.vaccineType
        }
    }
    suspend fun getDoseTypeByNameMap():Map<Int,String>{
        return vaccinationTypeAndDoseDao.getDoseTypeMasterMap().associate {
            it.covidDoseTypeID to it.doseType
        }
    }

}