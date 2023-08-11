package org.piramalswasthya.cho.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.cho.model.AlcoholDropdown
import org.piramalswasthya.cho.model.AllergicReactionDropdown
import org.piramalswasthya.cho.model.FamilyMemberDropdown
import org.piramalswasthya.cho.model.IllnessDropdown
import org.piramalswasthya.cho.model.SurgeryDropdown
import org.piramalswasthya.cho.model.TobaccoDropdown

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIllnessDropdown(illnessDropdown: IllnessDropdown)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlcoholDropdown(alcoholDropdown: AlcoholDropdown)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllergicReactionDropdown(allergicReactionDropdown: AllergicReactionDropdown)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyMemberDropdown(familyMemberDropdown: FamilyMemberDropdown)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurgeryDropdown(surgeryDropdown: SurgeryDropdown)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTobaccoDropdown(tobaccoDropdown: TobaccoDropdown)

    @Query("select * from Illness_Dropdown")
    fun getAllIllnessDropdown(): LiveData<List<IllnessDropdown>>

    @Query("select * from Alcohol_Dropdown")
    fun getAllAlcoholDropdown(): LiveData<List<AlcoholDropdown>>

    @Query("select * from Allergic_Reaction_Dropdown")
    fun getAllAllergicReactionDropdown(): LiveData<List<AllergicReactionDropdown>>

    @Query("select * from Family_member_Dropdown")
    fun getAllFamilyMemberDropdown(): LiveData<List<FamilyMemberDropdown>>

    @Query("select * from Surgery_Dropdown")
    fun getAllSurgeryDropdown(): LiveData<List<SurgeryDropdown>>

    @Query("select * from Tobacco_Dropdown")
    fun getAllTobaccoDropdown(): LiveData<List<TobaccoDropdown>>

    @Query("select * from Illness_Dropdown")
    suspend fun getIllnessMasterMap():List<IllnessDropdown>
}