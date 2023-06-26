package org.piramalswasthya.cho.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.piramalswasthya.cho.database.converters.MasterDataListConverter
import org.piramalswasthya.cho.database.room.dao.RegistrarMasterDataDao
import org.piramalswasthya.cho.model.AgeUnit
import org.piramalswasthya.cho.model.CommunityMaster
import org.piramalswasthya.cho.model.GenderMaster
import org.piramalswasthya.cho.model.GovIdEntityMaster
import org.piramalswasthya.cho.model.IncomeMaster
import org.piramalswasthya.cho.model.LiteracyStatus
import org.piramalswasthya.cho.model.MaritalStatusMaster
import org.piramalswasthya.cho.model.OtherGovIdEntityMaster
import org.piramalswasthya.cho.model.RelationshipMaster
import org.piramalswasthya.cho.model.VisitReason
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.TmcLocationDetails
import org.piramalswasthya.cho.network.TmcLocationDetailsRequest
import timber.log.Timber
import javax.inject.Inject

class RegistrarMasterDataRepo @Inject constructor(
    private val registrarMasterDataDao: RegistrarMasterDataDao,
    private val apiService: AmritApiService

) {
    private suspend fun registrarMasterService(): JSONObject? {
        val response = apiService.getRegistrarMasterData(TmcLocationDetails(1))
//        val statusCode = response.code()
//        if (statusCode == 200) {
        val responseString = response.body()?.string()
        val responseJson = responseString?.let { JSONObject(it) }
        Timber.tag("dataJson").d(responseJson.toString())

        return responseJson?.getJSONObject("data")

    }


    //GENDER MASTER
    private suspend fun genderMasterService(): List<GenderMaster> {
        val genderList = registrarMasterService()?.getJSONArray("genderMaster")
        return MasterDataListConverter.toGenderList(genderList.toString())
    }

    suspend fun saveGenderMasterResponseToCache() {
        genderMasterService().forEach { genderMaster: GenderMaster ->
            withContext(Dispatchers.IO) {
                registrarMasterDataDao.insertGender(genderMaster)
            }
            Timber.tag("genderItem").d(genderMaster.toString())
        }
    }

    suspend fun getGenderMasterCachedResponse(): List<GenderMaster> {
        return registrarMasterDataDao.getGenders()
    }


    //AGE UNIT
    private suspend fun ageUnitMasterService(): List<AgeUnit> {
        val ageUnitList = registrarMasterService()?.getJSONArray("ageUnit")
        return MasterDataListConverter.toAgeUnitList(ageUnitList.toString())
    }

    suspend fun saveAgeUnitMasterResponseToCache() {
        ageUnitMasterService().forEach { ageUnit: AgeUnit ->
            withContext(Dispatchers.IO) {
                registrarMasterDataDao.insertAgeUnit(ageUnit)
            }
            Timber.tag("ageUnitItem").d(ageUnit.toString())
        }
    }

    suspend fun getAgeUnitMasterCachedResponse(): List<AgeUnit> {
        return registrarMasterDataDao.getAgeUnit()
    }


    //INCOME STATUS
    private suspend fun incomeStatusMasterService(): List<IncomeMaster> {
        val incomeStatusList = registrarMasterService()?.getJSONArray("incomeMaster")
        return MasterDataListConverter.toIncomeStatusList(incomeStatusList.toString())
    }

    suspend fun saveIncomeMasterResponseToCache() {
        incomeStatusMasterService().forEach { incomeMaster: IncomeMaster ->
            withContext(Dispatchers.IO) {
                registrarMasterDataDao.insertIncomeStatus(incomeMaster)
            }
            Timber.tag("incomeMasterItem").d(incomeMaster.toString())
        }
    }

    suspend fun getIncomeMasterCachedResponse(): List<IncomeMaster> {
        return registrarMasterDataDao.getIncomeStatus()
    }


    //LITERACY STATUS
    private suspend fun literacyStatusMasterService(): List<LiteracyStatus> {
        val literacyStatusList = registrarMasterService()?.getJSONArray("literacyStatus")
        return MasterDataListConverter.toLiteracyStatusList(literacyStatusList.toString())
    }

    suspend fun saveLiteracyStatusServiceResponseToCache() {
        literacyStatusMasterService().forEach { literacyStatus: LiteracyStatus ->
            withContext(Dispatchers.IO) {
                registrarMasterDataDao.insertLiteracyStatus(literacyStatus)
            }
            Timber.tag("literacyStatusItem").d(literacyStatus.toString())
        }
    }

    suspend fun getLiteracyStatusMasterCachedResponse(): List<LiteracyStatus> {
        return registrarMasterDataDao.getLiteracyStatus()
    }

    //MARITAL STATUS
    private suspend fun maritalStatusService(): List<MaritalStatusMaster> {
        val maritalStatusList = registrarMasterService()?.getJSONArray("maritalStatusMaster")
        return MasterDataListConverter.toMaritalStatusList(maritalStatusList.toString())
    }

    suspend fun saveMaritalStatusServiceResponseToCache() {
        maritalStatusService().forEach { maritalStatus: MaritalStatusMaster ->
            withContext(Dispatchers.IO) {
                registrarMasterDataDao.insertMaritalStatus(maritalStatus)
            }
            Timber.tag("maritalStatusItem").d(maritalStatus.toString())
        }
    }

    suspend fun getMaritalStatusMasterCachedResponse(): List<MaritalStatusMaster> {
        return registrarMasterDataDao.getMaritalStatus()
    }

    //COMMUNITY MASTER
    private suspend fun communityMasterService(): List<CommunityMaster> {
        val communityMasterList = registrarMasterService()?.getJSONArray("communityMaster")
        return MasterDataListConverter.toCommunityMasterList(communityMasterList.toString())
    }

    suspend fun saveCommunityMasterResponseToCache() {
        communityMasterService().forEach { communityMaster: CommunityMaster ->
            withContext(Dispatchers.IO) {
                registrarMasterDataDao.insertCommunity(communityMaster)
            }
            Timber.tag("communityMasterItem").d(communityMaster.toString())
        }
    }

    suspend fun getCommunityMasterCachedResponse(): List<CommunityMaster> {
        return registrarMasterDataDao.getCommunityMaster()
    }

    //GOV ID ENTITY MASTER
    private suspend fun govIdEntityMasterService(): List<GovIdEntityMaster> {
        val govIdEntityMasterList = registrarMasterService()?.getJSONArray("govIdEntityMaster")
        return MasterDataListConverter.toGovIdEntityList(govIdEntityMasterList.toString())
    }

    suspend fun saveGovIdEntityMasterResponseToCache() {
        govIdEntityMasterService().forEach { govIdEntityMaster: GovIdEntityMaster ->
            withContext(Dispatchers.IO) {
                registrarMasterDataDao.insertGovIdMaster(govIdEntityMaster)
            }
            Timber.tag("govIdEntityMasterItem").d(govIdEntityMaster.toString())
        }
    }

    suspend fun getGovIdEntityMasterCachedResponse(): List<GovIdEntityMaster> {
        return registrarMasterDataDao.getGovIdMaster()
    }


//OTHER GOV ID ENTITY MASTER
private suspend fun otherGovIdEntityMasterService(): List<OtherGovIdEntityMaster> {
    val otherGovIdEntityMasterList = registrarMasterService()?.getJSONArray("otherGovIdEntityMaster")
    return MasterDataListConverter.toOtherGovIdEntityList(otherGovIdEntityMasterList.toString())
}

    suspend fun saveOtherGovIdEntityMasterResponseToCache() {
        otherGovIdEntityMasterService().forEach { otherGovIdEntityMaster: OtherGovIdEntityMaster ->
            withContext(Dispatchers.IO) {
                registrarMasterDataDao.insertOtherGovIdEntityMaster(otherGovIdEntityMaster)
            }
            Timber.tag("otherGovIdEntityMasterItem").d(otherGovIdEntityMaster.toString())
        }
    }

    suspend fun getOtherGovIdEntityMasterCachedResponse(): List<OtherGovIdEntityMaster> {
        return registrarMasterDataDao.getOtherGovIdMaster()
    }

    //RELATIONSHIP MASTER
    private suspend fun relationshipMasterService(): List<RelationshipMaster> {
        val relationshipMasterList = registrarMasterService()?.getJSONArray("relationshipMaster")
        return MasterDataListConverter.toRelationshipMasterList(relationshipMasterList.toString())
    }

    suspend fun saveRelationshipMasterResponseToCache() {
        relationshipMasterService().forEach { relationshipMaster: RelationshipMaster ->
            withContext(Dispatchers.IO) {
                registrarMasterDataDao.insertRelationshipMaster(relationshipMaster)
            }
            Timber.tag("relationshipMasterItem").d(relationshipMaster.toString())
        }
    }

    suspend fun getRelationshipMasterCachedResponse(): List<RelationshipMaster> {
        return registrarMasterDataDao.getRelationshipMaster()
    }

    //OCCUPATION MASTER
//    private suspend fun relationshipMasterService(): List<RelationshipMaster> {
//        val relationshipMasterList = registrarMasterService()?.getJSONArray("relationshipMaster")
//        return MasterDataListConverter.toRelationshipMasterList(relationshipMasterList.toString())
//    }
//
//    suspend fun saveRelationshipMasterResponseToCache() {
//        relationshipMasterService().forEach { relationshipMaster: RelationshipMaster ->
//            withContext(Dispatchers.IO) {
//                registrarMasterDataDao.insertRelationshipMaster(relationshipMaster)
//            }
//            Timber.tag("relationshipMasterItem").d(relationshipMaster.toString())
//        }
//    }
//
//    suspend fun getRelationshipMasterCachedResponse(): List<RelationshipMaster> {
//        return registrarMasterDataDao.getRelationshipMaster()
//    }









}