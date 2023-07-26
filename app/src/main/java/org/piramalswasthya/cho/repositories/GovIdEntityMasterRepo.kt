package org.piramalswasthya.cho.repositories

import org.piramalswasthya.cho.database.room.dao.GovIdEntityMasterDao
import org.piramalswasthya.cho.database.room.dao.OtherGovIdEntityMasterDao
import javax.inject.Inject


class GovIdEntityMasterRepo @Inject constructor(
    private val govIdEntityMasterDao: GovIdEntityMasterDao
) {

    suspend fun getGovIdtEntityAsMap(): Map<Int, String> {
        return govIdEntityMasterDao.getGovIdEntityMaster().associate { it -> it.govtIdentityTypeID to it.identityType }
    }

}
