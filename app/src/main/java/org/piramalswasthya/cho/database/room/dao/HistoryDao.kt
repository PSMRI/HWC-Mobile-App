package org.piramalswasthya.cho.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.cho.model.AlcoholDropdown
import org.piramalswasthya.cho.model.AllergicReactionDropdown
import org.piramalswasthya.cho.model.AssociateAilmentsHistory
import org.piramalswasthya.cho.model.AssociateAilmentsDropdown
import org.piramalswasthya.cho.model.ComorbidConditionsDropdown
import org.piramalswasthya.cho.model.CovidVaccinationStatusHistory
import org.piramalswasthya.cho.model.FamilyMemberDiseaseTypeDropdown
import org.piramalswasthya.cho.model.FamilyMemberDropdown
import org.piramalswasthya.cho.model.IllnessDropdown
import org.piramalswasthya.cho.model.MedicationHistory
import org.piramalswasthya.cho.model.PastIllnessHistory
import org.piramalswasthya.cho.model.PastSurgeryHistory
import org.piramalswasthya.cho.model.SurgeryDropdown
import org.piramalswasthya.cho.model.TobaccoAlcoholHistory
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
    suspend fun insertComorbidConditionDropdown(comorbidConditionsDropdown: ComorbidConditionsDropdown)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyDiseaseDropdown(familyMemberDiseaseTypeDropdown: FamilyMemberDiseaseTypeDropdown)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTobaccoDropdown(tobaccoDropdown: TobaccoDropdown)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPastIllnessHistory(pastIllnessHistory: PastIllnessHistory)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPastSurgeryHistory(pastSurgeryHistory: PastSurgeryHistory)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCovidVaccinationStatusHistory(covidVaccinationStatusHistory: CovidVaccinationStatusHistory)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicationHistory(medicationHistory: MedicationHistory)

    @Query("SELECT * FROM Covid_Vaccination_Status_history WHERE covidVaccinationStatusHistoryId = :covidId")
    fun getCovidVaccinationStatusHistoryByMedicationId(covidId: String): LiveData<CovidVaccinationStatusHistory>

    @Query("SELECT * FROM Past_Illness_History WHERE pastIllnessHistoryId = :pastIllnessId")
    fun getPastIllnessHistoryByMedicationId(pastIllnessId: String): LiveData<PastIllnessHistory>
    @Query("SELECT * FROM Past_Surgery_History WHERE pastSurgeryHistoryId = :pastSurgeryId")
    fun getPastSurgeryHistoryByMedicationId(pastSurgeryId: String): LiveData<PastSurgeryHistory>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssociateAilmentsHistory(associateAilmentsHistory: AssociateAilmentsHistory)

    @Query("SELECT * FROM Medication_history WHERE medicationHistoryId = :medicationId")
    fun getMedicationHistoryByMedicationId(medicationId: String): LiveData<MedicationHistory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTobAndAlcHistory(tobaccoAlcoholHistory: TobaccoAlcoholHistory)
    @Insert
     suspend fun insertAssociateAilments(associateAilmentsDropdown: AssociateAilmentsDropdown)
    @Query("select * from Tobacco_Alcohol_history WHERE tobaccoAndAlcoholId = :tobAndAlcId")
    fun getTobAndAlcHistory(tobAndAlcId:String): TobaccoAlcoholHistory
    @Query("select * from Associate_Ailments_history WHERE associateAilmentsId = :aaId")
    fun getAssociateAilmentHistory(aaId:String): AssociateAilmentsHistory
    @Query("select * from Illness_Dropdown")
    fun getAllIllnessDropdown(): LiveData<List<IllnessDropdown>>

    @Query("select * from Alcohol_Dropdown")
    fun getAllAlcoholDropdown(): LiveData<List<AlcoholDropdown>>

    @Query("select * from Allergic_Reaction_Dropdown")
    fun getAllAllergicReactionDropdown(): LiveData<List<AllergicReactionDropdown>>

    @Query("select * from Family_member_Dropdown")
    fun getAllFamilyMemberDropdown(): LiveData<List<FamilyMemberDropdown>>

    @Query("select * from Associate_Ailments_Dropdown")
    fun getAllAssociateAilmentsDropdown(): LiveData<List<AssociateAilmentsDropdown>>

    @Query("select * from Surgery_Dropdown")
    fun getAllSurgeryDropdown(): LiveData<List<SurgeryDropdown>>
@Query("SELECT c.comorbidConditionID, c.comorbidCondition FROM Comorbid_Condition_Dropdown c " +
        "INNER JOIN Family_Member_Disease_Type_Dropdown f ON " +
        "REPLACE(c.comorbidCondition, ' ', '') = REPLACE(f.diseaseType, ' ', '') " +
        "GROUP BY c.comorbidConditionID, c.comorbidCondition")
  suspend fun getAllDistinctComorbidConditions(): List<ComorbidConditionsDropdown>

     suspend fun saveMatchingComorbidConditionsAsAssociateAilments() {
        val matchedConditions = getAllDistinctComorbidConditions()
        val associateAilmentsList = matchedConditions.map { comorbidCondition ->
            AssociateAilmentsDropdown(assocateAilmentsId = comorbidCondition.comorbidConditionID, assocateAilments = comorbidCondition.comorbidCondition)
        }
        associateAilmentsList.forEach { associateA ->
            insertAssociateAilments(associateA)
        }
    }

    @Query("select * from Tobacco_Dropdown")
    fun getAllTobaccoDropdown(): LiveData<List<TobaccoDropdown>>

    @Query("select * from Illness_Dropdown")
    suspend fun getIllnessMasterMap():List<IllnessDropdown>
    @Query("select * from Surgery_Dropdown")
    suspend fun getSurgeryMasterMap():List<SurgeryDropdown>
}