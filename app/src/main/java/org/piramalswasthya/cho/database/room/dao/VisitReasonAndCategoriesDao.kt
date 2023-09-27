package org.piramalswasthya.cho.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import org.piramalswasthya.cho.model.ChiefComplaintDB
import org.piramalswasthya.cho.model.VisitCategory
import org.piramalswasthya.cho.model.VisitDB
import org.piramalswasthya.cho.model.VisitReason

@Dao
interface VisitReasonsAndCategoriesDao {
    @Query("SELECT * FROM VISIT_REASON")
    suspend fun getVisitReasons(): List<VisitReason>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisitReason(visitReason: VisitReason)

    @Query("SELECT * FROM VISIT_CATEGORY")
    suspend fun getVisitCategories(): List<VisitCategory>

//    @Query("SELECT * FROM VISIT_DB WHERE visitId = :id")
//     fun getVisitDb(id:String):LiveData<VisitDB>
//
//    @Query("SELECT * FROM Chielf_Complaint_DB WHERE id = :id")
//     fun getChiefComplaintDb(id:String):LiveData<ChiefComplaintDB>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisitCategory(visitReason: VisitCategory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisitDB(visitDB: VisitDB)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChiefComplaintDb(chiefComplaintDB: ChiefComplaintDB)

    @Insert
    suspend fun insertAll(chiefComplaints: List<ChiefComplaintDB>)

    @Query("SELECT * FROM Visit_DB WHERE beneficiaryRegID = :beneficiaryRegID")
    suspend fun getVisitDb(beneficiaryRegID: Long) : VisitDB?

    @Query("SELECT * FROM Visit_DB WHERE beneficiaryRegID = :beneficiaryRegID AND benVisitNo = :benVisitNo")
    suspend fun getVisitDbByBenRegIdAndBenVisitNo(beneficiaryRegID: Long, benVisitNo: Int) : VisitDB?

    @Query("SELECT * FROM Visit_DB WHERE patientID = :patientID")
    suspend fun getVisitDbByPatientId(patientID: String) : VisitDB?

    @Query("SELECT * FROM Chielf_Complaint_DB WHERE patientID = :patientID")
    suspend fun getChiefComplaintsByPatientId(patientID: String) : List<ChiefComplaintDB>?

    @Query("SELECT * FROM Chielf_Complaint_DB WHERE beneficiaryRegID = :beneficiaryRegID")
    suspend fun getChiefComplaints(beneficiaryRegID: Long) : List<ChiefComplaintDB>?

    @Query("SELECT * FROM Chielf_Complaint_DB WHERE beneficiaryRegID = :beneficiaryRegID AND benVisitNo = :benVisitNo")
    suspend fun getChiefComplaintsByBenRegIdAndBenVisitNo(beneficiaryRegID: Long, benVisitNo: Int) : List<ChiefComplaintDB>?

    @Query("UPDATE Visit_DB SET benFlowID = :benflowId WHERE beneficiaryRegID = :beneficiaryRegID AND benVisitNo = :benVisitNo")
    suspend fun updateVisitDbBenflow(benflowId: Long, beneficiaryRegID: Long, benVisitNo: Int) : Int

    @Query("UPDATE Chielf_Complaint_DB SET benFlowID = :benflowId WHERE beneficiaryRegID = :beneficiaryRegID AND benVisitNo = :benVisitNo")
    suspend fun updateChiefComplaintsBenflow(benflowId: Long, beneficiaryRegID: Long, benVisitNo: Int) : Int

    @Transaction
    @Query("UPDATE Visit_DB SET beneficiaryID = :beneficiaryID, beneficiaryRegID = :beneficiaryRegID WHERE patientID = :patientID")
    suspend fun updateBenIdBenRegIdVisitDb(beneficiaryID: Long, beneficiaryRegID: Long, patientID: String): Int

    @Transaction
    @Query("UPDATE Chielf_Complaint_DB SET beneficiaryID = :beneficiaryID, beneficiaryRegID = :beneficiaryRegID WHERE patientID = :patientID")
    suspend fun updateBenIdBenRegIdChiefComplaint(beneficiaryID: Long, beneficiaryRegID: Long, patientID: String): Int

    @Transaction
    @Query("DELETE FROM Visit_DB WHERE patientID = :patientID")
    suspend fun deleteVisitDbByPatientId(patientID: String): Int

    @Transaction
    @Query("DELETE FROM Visit_DB WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun deleteVisitDbByPatientIdAndBenVisitNo(patientID: String, benVisitNo: Int)

    @Transaction
    @Query("DELETE FROM Chielf_Complaint_DB WHERE patientID = :patientID")
    suspend fun deleteChiefComplaintsByPatientId(patientID: String): Int

    @Transaction
    @Query("DELETE FROM Chielf_Complaint_DB WHERE patientID = :patientID AND benVisitNo = :benVisitNo")
    suspend fun deleteChiefComplaintsByPatientIdAndBenVisitNo(patientID: String, benVisitNo: Int)

}