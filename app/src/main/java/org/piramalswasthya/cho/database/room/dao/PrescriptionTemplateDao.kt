package org.piramalswasthya.cho.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import org.piramalswasthya.cho.model.PatientVitalsModel
import org.piramalswasthya.cho.model.PrescriptionTemplateDB
@Dao
interface PrescriptionTemplateDao {
    @Insert
    suspend fun insertPrescriptionTemplates(vararg prescriptionTemplateDB: PrescriptionTemplateDB)

    @Query("SELECT * FROM Prescription_Template_DB WHERE user_id = :userID")
    suspend fun getTemplateForUser(userID: Int): List<PrescriptionTemplateDB?>

}