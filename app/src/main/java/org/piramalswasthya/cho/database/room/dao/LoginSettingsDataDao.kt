package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.cho.model.LoginSettingsData

@Dao
interface LoginSettingsDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(loginSettingsData: LoginSettingsData)

    @Query("SELECT * FROM login_settings_data WHERE username = :username")
    suspend fun findByUsername(username: String): LoginSettingsData?

    @Delete
    suspend fun delete(loginSettingsData: LoginSettingsData)

    @Query("SELECT * FROM login_settings_data")
    suspend fun getAll(): List<LoginSettingsData>
}
