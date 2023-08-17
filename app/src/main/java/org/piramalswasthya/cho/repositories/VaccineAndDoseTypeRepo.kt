package org.piramalswasthya.cho.repositories

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.cho.database.converters.MasterDataListConverter
import org.piramalswasthya.cho.database.room.dao.VaccinationTypeAndDoseDao
import org.piramalswasthya.cho.database.room.dao.VisitReasonsAndCategoriesDao
import org.piramalswasthya.cho.model.DoseType
import org.piramalswasthya.cho.model.GenderMaster
import org.piramalswasthya.cho.model.VaccineType
import org.piramalswasthya.cho.model.VisitReason
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.TmcLocationDetailsRequest
import timber.log.Timber
import javax.inject.Inject

class VaccineAndDoseTypeRepo @Inject constructor(
    private val vaccinationTypeAndDoseDao: VaccinationTypeAndDoseDao,
    private val apiService: AmritApiService
){


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

}