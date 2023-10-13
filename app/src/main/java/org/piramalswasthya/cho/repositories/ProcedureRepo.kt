package org.piramalswasthya.cho.repositories

import org.piramalswasthya.cho.database.room.dao.ProcedureDao
import org.piramalswasthya.cho.model.ProcedureDataWithComponent
import javax.inject.Inject


class ProcedureRepo  @Inject constructor(
    private val procedureDao: ProcedureDao,
) {

    suspend fun getProceduresWithComponent(patientID: String, benVisitNo: Int): List<ProcedureDataWithComponent>{
        return procedureDao.getProceduresWithComponent(patientID, benVisitNo)
    }

}