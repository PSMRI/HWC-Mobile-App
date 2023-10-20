package org.piramalswasthya.cho.database.room.dao

import androidx.room.Insert
import androidx.room.Query
import org.piramalswasthya.cho.model.PatientVitalsModel
import org.piramalswasthya.cho.model.PrescriptionTemplateDB

interface PrescriptionTemplateDao {
    @Insert
    suspend fun insertPrescriptionTemplates(prescriptionTemplateDB: PrescriptionTemplateDB)

    @Query("SELECT * FROM Prescription_Template_DB WHERE user_id = :userID")
    suspend fun getPatientVitalsByPatientIDAndBenVisitNo(userID: Int, benVisitNo: Int): List<PrescriptionTemplateDB>?

}