package org.piramalswasthya.cho.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.cho.model.CounsellingProvided
import org.piramalswasthya.cho.model.DrugFormMaster
import org.piramalswasthya.cho.model.DrugFrequencyMaster
import org.piramalswasthya.cho.model.HigherHealthCenter
import org.piramalswasthya.cho.model.ItemMasterList
import org.piramalswasthya.cho.model.ProceduresMasterData


@Dao
interface HealthCenterDao {

    @Query("SELECT * FROM HEALTH_CENTER")
    fun getHealthCenter() : LiveData<List<HigherHealthCenter>>
    @Query("SELECT * FROM HEALTH_CENTER where institutionID = institutionID")
    fun getHealthCenterByID(institutionID:Int?) : HigherHealthCenter
    @Query("select * from HEALTH_CENTER")
    suspend fun getHigherHealthMap():List<HigherHealthCenter>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthCenter(higherHealthCenter: HigherHealthCenter)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItemMasterList(itemMasterList: ItemMasterList)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrugFrequencyMasterList(drugFrequencyMaster: DrugFrequencyMaster)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCounsellingTypeMasterList(counsellingProvided: CounsellingProvided)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrugFormMasterList(drugFormMaster: DrugFormMaster)

    @Query("select itemFormName from drug_form_master where itemFormID = :itemFormID")
    suspend fun getItemFormNameByID(itemFormID: Int): String?

    @Query("SELECT * from Item_Master_List")
     fun getAllItemMasterList():LiveData<List<ItemMasterList>>
    @Query("SELECT * from Item_Master_List where itemID = id")
    fun getItemMasterListById(id:Int):ItemMasterList

    @Query("SELECT * from Counselling_Provided")
    fun getAllCounsellingProvided():LiveData<List<CounsellingProvided>>
}
