package org.piramalswasthya.cho.repositories

import org.piramalswasthya.cho.database.converters.MasterDataListConverter
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.database.room.dao.RegistrarMasterDataDao
import org.piramalswasthya.cho.model.GenderMaster
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.network.AmritApiService
import javax.inject.Inject

class PatientRepo  @Inject constructor(
    private val patientDao: PatientDao
) {

    suspend fun insertPatient(patient: Patient) {
        patientDao.insertPatient(patient)
    }

    suspend fun getPatientList() : List<PatientDisplay>{
        return patientDao.getPatientList()
    }

}