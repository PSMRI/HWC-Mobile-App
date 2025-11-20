package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.model.LocationEntity
import org.piramalswasthya.cho.model.UserAuth
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.model.fhir.SelectedOutreachProgram
import java.util.Date

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
    @Query("UPDATE USER SET lastLogoutTime =:logoutTimestamp  WHERE user_id =:userId")
    suspend fun updateLogoutTime(userId:Int, logoutTimestamp: Date)
    @Query("SELECT * FROM USER ORDER BY lastLogoutTime DESC LIMIT 1")
    suspend fun getLastLoggedOutUser(): UserCache?
    @Query("SELECT * FROM USER WHERE logged_in = 1 LIMIT 1")
    suspend fun getLoggedInUser(): UserCache?

    @Query("SELECT * FROM USER U WHERE username like :username AND Password = :password LIMIT 1")
    suspend fun getUser(username:String, password:String): UserCache?

    @Query("SELECT country_id as id, country_name as name, country_nameHindi as nameHindi, country_nameAssamese as nameAssamese FROM USER WHERE logged_in = 1 LIMIT 1")
    suspend fun getCountry(): LocationEntity?

    @Query("SELECT * FROM USER WHERE logged_in = 1 LIMIT 1")
    fun getLoggedInUserAsFlow(): Flow<UserCache?>

    @Query("SELECT facilityID FROM USER WHERE logged_in = 1 LIMIT 1")
    suspend fun getLoggedInUserFacilityID(): Int

    @Query("SELECT van_id FROM USER WHERE logged_in = 1 LIMIT 1")
    suspend fun getLoggedInUserVanID(): Int

    @Query("SELECT service_map_id FROM USER WHERE logged_in = 1 LIMIT 1")
    suspend fun getLoggedInUserProviderServiceMapId(): Int
//    @Delete
//    suspend fun logout(loggedInUser: UserCache)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutreachProgram(outreachProgram: SelectedOutreachProgram)
    @Transaction
    @Query("SELECT * FROM SELECTED_OUTREACH_PROGRAM WHERE syncState =:syncState")
    suspend fun getLoginAuditDataListUnsynced(syncState: SyncState = SyncState.UNSYNCED) : List<SelectedOutreachProgram>
    @Query("UPDATE SELECTED_OUTREACH_PROGRAM SET syncState =:synced WHERE id = :selectedOutreachProgramId")
    suspend fun updateAuditDataFlag(selectedOutreachProgramId: Long,synced: SyncState = SyncState.SYNCED)
    @Query("UPDATE USER SET stateID = :stateId")
    suspend fun updateUserStateId(stateId : Int) : Int

    @Query("UPDATE USER SET districtID = :districtId")
    suspend fun updateUserDistrictId(districtId : Int) : Int

    @Query("UPDATE USER SET blockID = :blockId")
    suspend fun updateUserBlockId(blockId : Int) : Int

    @Query("UPDATE USER SET districtBranchID = :districtBranchID")
    suspend fun updateUserVillageId(districtBranchID : Int) : Int

    @Query("select * from user where username = :username")
    suspend fun getUserByName(username: String): UserCache?
    @Query("update user set logged_in = 1 where username = :username")
    suspend fun updateLoggedInStatus(username: String)

    @Query("select logged_in from user")
    suspend fun getLoggedInStatus():Int
}