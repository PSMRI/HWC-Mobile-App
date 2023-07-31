package org.piramalswasthya.cho.repositories

import org.piramalswasthya.cho.database.room.dao.OtherGovIdEntityMasterDao
import org.piramalswasthya.cho.database.room.dao.StateMasterDao
import org.piramalswasthya.cho.network.AmritApiService
import javax.inject.Inject

class OtherGovIdEntityMasterRepo @Inject constructor(
    private val otherGovIdEntityMasterDao: OtherGovIdEntityMasterDao,
) {

    suspend fun getOtherGovtEntityAsMap(): Map<Int, String> {
        return otherGovIdEntityMasterDao.getOtherGovIdEntityMaster().associate { it -> it.govtIdentityTypeID to it.identityType }
    }

}
