package org.piramalswasthya.cho.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import org.piramalswasthya.cho.model.PatientVitalsModel
import org.piramalswasthya.cho.model.PrescriptionTemplateDB
@Dao
interface PrescriptionTemplateDao {
    @Insert
    suspend fun insertPrescriptionTemplates(vararg prescriptionTemplateDB: PrescriptionTemplateDB)

    @Query("SELECT * FROM Prescription_Template_DB WHERE user_id = :userID And deleteStatus==0")
    suspend fun getTemplateForUser(userID: Int): List<PrescriptionTemplateDB?>
    @Query("SELECT * FROM Prescription_Template_DB WHERE template_name = :selectedString And deleteStatus==0")
    suspend fun getTemplateForUserUsingTemplateName(selectedString: String): List<PrescriptionTemplateDB?>

    @Query("SELECT tempID FROM Prescription_Template_DB WHERE deleteStatus==0")
    suspend fun getTemplateIdWhichIsNotDeleted(): List<Int?>

    @Query("SELECT tempID FROM Prescription_Template_DB WHERE deleteStatus==1")
    suspend fun getTemplateIdWhichIsDeleted(): List<Int?>

    @Query("DELETE FROM Prescription_Template_DB where tempID = :tempID")
    suspend fun delete(tempID: Int)
    @Query("UPDATE Prescription_Template_DB SET deleteStatus = 1 where template_name = :selectedString")
    suspend fun markTemplateDelete(selectedString: String)

}