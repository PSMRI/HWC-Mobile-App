package org.piramalswasthya.cho.database.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import org.piramalswasthya.cho.model.LocationEntity
import org.piramalswasthya.cho.model.UserAuth
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.model.fhir.SelectedOutreachProgram

@Dao
interface UserAuthDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(user: UserAuth)

    @Query("SELECT COUNT(*) FROM USER_AUTH")
    fun getRowCount(): Int

}

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: UserCache)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(user: UserCache)

    @Query("UPDATE USER SET logged_in = 0")
    suspend fun resetAllUsersLoggedInState()

    @Query("SELECT * FROM USER WHERE logged_in = 1 LIMIT 1")
    suspend fun getLoggedInUser(): UserCache?

    @Query("SELECT country_id as id, country_name as name, country_nameHindi as nameHindi, country_nameAssamese as nameAssamese FROM USER WHERE logged_in = 1 LIMIT 1")
    suspend fun getCountry(): LocationEntity?

    @Query("SELECT * FROM USER WHERE logged_in = 1 LIMIT 1")
    fun getLoggedInUserLiveData(): LiveData<UserCache>

    @Delete
    suspend fun logout(loggedInUser: UserCache)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutreachProgram(outreachProgram: SelectedOutreachProgram)

    @Query("UPDATE USER SET stateID = :stateId")
    suspend fun updateUserStateId(stateId : Int)

    @Query("UPDATE USER SET districtID = :districtId")
    suspend fun updateUserDistrictId(districtId : Int)

    @Query("UPDATE USER SET blockID = :blockId")
    suspend fun updateUserBlockId(blockId : Int)

    @Query("UPDATE USER SET districtBranchID = :districtBranchID")
    suspend fun updateUserVillageId(districtBranchID : Int)

}