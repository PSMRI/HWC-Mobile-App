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

//    suspend fun checkAndAddVaccines() {
//        if (vaccineDao.vaccinesLoaded())
//            return
//        val vaccineList = arrayOf(
//            ////------------------CHILD-----------------///////////////
//            //ChildImmunizationCategory.BIRTH
//            Vaccine(
//                id = 1,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.BIRTH,
//                name = "OPV 0",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(0),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(15),
//                overdueDurationSinceMinInMillis = TimeUnit.DAYS.toMillis(1),
//            ),
//            Vaccine(
//                id = 2,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.BIRTH,
//                name = "BCG 0",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(0),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(365),
//                overdueDurationSinceMinInMillis = TimeUnit.DAYS.toMillis(1),
//
//                ),
//            Vaccine(
//                id = 3,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.BIRTH,
//                name = "Hepatitis B 0",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(0),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(1),
//
//                ),
//            Vaccine(
//                id = 4,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.BIRTH,
//                name = "Vit K",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(0),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(1),
//            ),
//            //Week 6
//            Vaccine(
//                id = 5,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.WEEK_6,
//                name = "OPV 1",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(6 * 7),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(365 * 2),
////                overdueDurationSinceMinInMillis =
//            ),
//            Vaccine(
//                id = 6,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.WEEK_6,
//                name = "Pentavalent 1",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(6 * 7),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(365),
//            ),
//            Vaccine(
//                id = 7,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.WEEK_6,
//                name = "ROTA 1",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(6 * 7),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(365),
//            ),
//            Vaccine(
//                id = 8,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.WEEK_6,
//                name = "IPV 1",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(6 * 7),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(365),
//            ),
//            //Week 10
//            Vaccine(
//                id = 9,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.WEEK_10,
//                name = "OPV 2",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(10 * 7),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(365 * 2),
//                dependantVaccineId = 5,
//                dependantCoolDuration = TimeUnit.DAYS.toMillis(28)
//            ),
//            Vaccine(
//                id = 10,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.WEEK_10,
//                name = "Pentavalent 2",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(10 * 7),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(365),
//                dependantVaccineId = 5,
//                dependantCoolDuration = TimeUnit.DAYS.toMillis(28)
//            ),
//            Vaccine(
//                id = 11,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.WEEK_14,
//                name = "OPV 3",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(14 * 7),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(365 * 2),
//                dependantVaccineId = 9,
//                dependantCoolDuration = TimeUnit.DAYS.toMillis(28)
//            ),
//            Vaccine(
//                id = 12,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.WEEK_14,
//                name = "Pentavalent 3",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(14 * 7),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(365),
//                dependantVaccineId = 10,
//                dependantCoolDuration = TimeUnit.DAYS.toMillis(28)
//            ),
//            Vaccine(
//                id = 13,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.WEEK_10,
//                name = "ROTA 2",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(10 * 7),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(365 * 1),
//                dependantVaccineId = 7,
//                dependantCoolDuration = TimeUnit.DAYS.toMillis(28)
//            ),
//            Vaccine(
//                id = 14,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.WEEK_14,
//                name = "ROTA 3",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(14 * 7),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(365 * 1),
//                dependantVaccineId = 7,
//                dependantCoolDuration = TimeUnit.DAYS.toMillis(28)
//            ),
//            Vaccine(
//                id = 15,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.WEEK_14,
//                name = "IPV 2",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(8 * 7),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(365 * 1),
//                dependantVaccineId = 8,
//                dependantCoolDuration = TimeUnit.DAYS.toMillis(28)
//            ),
//            Vaccine(
//                id = 16,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.MONTH_16_24,
//                name = "OPV Booster 1",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(487),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(365 * 2),
//            ),
//            Vaccine(
//                id = 17,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.MONTH_16_24,
//                name = "DPT Booster 1",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(487),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(365 * 7),
//            ),
//            Vaccine(
//                id = 18,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.YEAR_5_6,
//                name = "DPT Booster 2",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(356 * 5),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(365 * 7),
//            ),
//
//            Vaccine(
//                id = 19,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.MONTH_9_12,
//                name = "Measles 1",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(274),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(365 * 5),
//            ),
//            Vaccine(
//                id = 20,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.MONTH_16_24,
//                name = "Measles 2",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(487),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(365 * 5),
//            ),
//            Vaccine(
//                id = 21,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.MONTH_9_12,
//                name = "JE Vaccine – 1",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(274),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(365),
//            ),
//            Vaccine(
//                id = 22,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.MONTH_16_24,
//                name = "JE Vaccine – 2",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(487),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(365 * 5),
//            ),
//            Vaccine(
//                id = 23,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.MONTH_9_12,
//                name = "Vitamin A – 1",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(274),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(365 * 5),
//            ),
//            Vaccine(
//                id = 24,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.MONTH_16_24,
//                name = "Vitamin A – 2",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(487),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(365 * 5),
//                dependantVaccineId = 23,
//                dependantCoolDuration = TimeUnit.DAYS.toMillis(274)
//            ),
//            Vaccine(
//                id = 25,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.YEAR_5_6,
//                name = "Vitamin A – 3",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(365 * 2),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(365 * 5),
//                dependantVaccineId = 24,
//                dependantCoolDuration = TimeUnit.DAYS.toMillis(183)
//            ),
//            Vaccine(
//                id = 26,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.YEAR_5_6,
//                name = "Vitamin A – 4",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(913),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(365 * 5),
//                dependantVaccineId = 25,
//                dependantCoolDuration = TimeUnit.DAYS.toMillis(183)
//            ),
//            Vaccine(
//                id = 27,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.YEAR_5_6,
//                name = "Vitamin A – 5",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(1095),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(365 * 5),
//                dependantVaccineId = 26,
//                dependantCoolDuration = TimeUnit.DAYS.toMillis(183)
//            ),
//            Vaccine(
//                id = 28,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.YEAR_5_6,
//                name = "Vitamin A – 6",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(1278),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(365 * 5),
//                dependantVaccineId = 27,
//                dependantCoolDuration = TimeUnit.DAYS.toMillis(183)
//            ),
//            Vaccine(
//                id = 29,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.YEAR_5_6,
//                name = "Vitamin A – 7",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(1460),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(365 * 5),
//                dependantVaccineId = 28,
//                dependantCoolDuration = TimeUnit.DAYS.toMillis(183)
//            ),
//            Vaccine(
//                id = 30,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.YEAR_5_6,
//                name = "Vitamin A – 8",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(1643),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(365 * 5),
//                dependantVaccineId = 29,
//                dependantCoolDuration = TimeUnit.DAYS.toMillis(183)
//            ),
//            Vaccine(
//                id = 31,
//                category = ImmunizationCategory.CHILD,
//                childCategory = ChildImmunizationCategory.YEAR_5_6,
//                name = "Vitamin A – 9",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(365 * 5),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(365 * 7),
//                dependantVaccineId = 30,
//                dependantCoolDuration = TimeUnit.DAYS.toMillis(183)
//            ),
//            ////------------------ImmunizationCategory.MOTHER-----------------///////////////
//            Vaccine(
//                id = 32,
//                category = ImmunizationCategory.MOTHER,
//                childCategory = ChildImmunizationCategory.YEAR_5_6,
//                name = "Td-1",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(0),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(36 * 7),
//            ),
//            Vaccine(
//                id = 33,
//                category = ImmunizationCategory.MOTHER,
//                childCategory = ChildImmunizationCategory.YEAR_5_6,
//                name = "Td-2",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(4 * 7),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(36 * 7),
//                dependantVaccineId = 32,
//                dependantCoolDuration = TimeUnit.DAYS.toMillis(4 * 7)
//            ),
//            Vaccine(
//                id = 34,
//                category = ImmunizationCategory.MOTHER,
//                childCategory = ChildImmunizationCategory.YEAR_5_6,
//                name = "Td-Booster",
//                minAllowedAgeInMillis = TimeUnit.DAYS.toMillis(0),
//                maxAllowedAgeInMillis = TimeUnit.DAYS.toMillis(36 * 7),
//            ),
//
//            )
//        vaccineDao.addVaccine(*vaccineList)
//    }


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