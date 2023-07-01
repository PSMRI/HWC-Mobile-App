package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.cho.moddel.OccupationMaster
import org.piramalswasthya.cho.model.AgeUnit
import org.piramalswasthya.cho.model.CommunityMaster
import org.piramalswasthya.cho.model.GenderMaster
import org.piramalswasthya.cho.model.GovIdEntityMaster
import org.piramalswasthya.cho.model.IncomeMaster
import org.piramalswasthya.cho.model.LiteracyStatus
import org.piramalswasthya.cho.model.MaritalStatusMaster
import org.piramalswasthya.cho.model.OtherGovIdEntityMaster
import org.piramalswasthya.cho.model.QualificationMaster
import org.piramalswasthya.cho.model.RelationshipMaster
import org.piramalswasthya.cho.model.ReligionMaster
import org.piramalswasthya.cho.model.VisitCategory
import org.piramalswasthya.cho.model.VisitReason


@Dao
interface RegistrarMasterDataDao {
    //GENDER
    @Query("SELECT * FROM GENDER_MASTER")
    suspend fun getGenders(): List<GenderMaster>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGender(genderMaster: GenderMaster)


    //AGE UNIT
    @Query("SELECT * FROM AGE_UNIT")
    suspend fun getAgeUnit(): List<AgeUnit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgeUnit(ageUnit: AgeUnit)


    //INCOME_MASTER
    @Query("SELECT * FROM INCOME_MASTER")
    suspend fun getIncomeStatus(): List<IncomeMaster>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncomeStatus(incomeMaster: IncomeMaster)



    //LITERACY_STATUS
    @Query("SELECT * FROM LITERACY_STATUS")
    suspend fun getLiteracyStatus(): List<LiteracyStatus>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLiteracyStatus(literacyStatus: LiteracyStatus)


    //COMMUNITY_MASTER
    @Query("SELECT * FROM COMMUNITY_MASTER")
    suspend fun getCommunityMaster(): List<CommunityMaster>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunity(communityMaster: CommunityMaster)

    //MARITAL_STATUS
    @Query("SELECT * FROM MARITAL_STATUS_MASTER")
    suspend fun getMaritalStatus(): List<MaritalStatusMaster>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaritalStatus(maritalStatusMaster: MaritalStatusMaster)

    //GOV_ID_ENTITY_MASTER
    @Query("SELECT * FROM GOV_ID_ENTITY_MASTER")
    suspend fun getGovIdMaster(): List<GovIdEntityMaster>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGovIdMaster(govIdEntityMaster: GovIdEntityMaster)


    //OTHER_GOV_ID_ENTITY_MASTER
    @Query("SELECT * FROM OTHER_GOV_ID_ENTITY_MASTER")
    suspend fun getOtherGovIdMaster(): List<OtherGovIdEntityMaster>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOtherGovIdEntityMaster(maritalStatusMaster: OtherGovIdEntityMaster)

    //RELATIONSHIP_MASTER
    @Query("SELECT * FROM RELATIONSHIP_MASTER")
    suspend fun getRelationshipMaster(): List<RelationshipMaster>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelationshipMaster(relationshipMaster: RelationshipMaster)

    //RELIGION_MASTER
    @Query("SELECT * FROM RELIGION_MASTER")
    suspend fun getReligionMaster(): List<ReligionMaster>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReligionMaster(religionMaster: ReligionMaster)

    //QUALIFICATION_MASTER
    @Query("SELECT * FROM QUALIFICATION_MASTER")
    suspend fun getQualificationMaster(): List<QualificationMaster>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQualificationMaster(qualificationMaster: QualificationMaster)


    //OCCUPATION_MASTER
    @Query("SELECT * FROM OCCUPATION_MASTER")
    suspend fun getOccupationMaster(): List<OccupationMaster>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOccupationMaster(occupationMaster: OccupationMaster)




}