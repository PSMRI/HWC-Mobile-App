package org.piramalswasthya.cho.repositories

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.piramalswasthya.cho.database.room.dao.ProcedureDao
import org.piramalswasthya.cho.model.ProcedureDataWithComponent
import javax.inject.Inject


class ProcedureRepo  @Inject constructor(
    private val procedureDao: ProcedureDao,
) {
    suspend fun getProceduresWithComponent(patientID: String, benVisitNo: Int): List<ProcedureDataWithComponent>{
        return withContext(Dispatchers.IO) {
            procedureDao.getProceduresWithComponent(patientID, benVisitNo)
        }
    }
}