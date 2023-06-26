package org.piramalswasthya.cho.repositories

import org.piramalswasthya.cho.database.room.dao.LoginSettingsDataDao
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.LoginSettingsData
import org.piramalswasthya.cho.network.AmritApiService
import javax.inject.Inject

class LoginSettingsDataRepository @Inject constructor(private val loginSettingsDataDao: LoginSettingsDataDao) {
    suspend fun saveLoginSettingsData(loginSettingsData: LoginSettingsData) {
        loginSettingsDataDao.insertOrUpdate(loginSettingsData)
    }

    suspend fun updateLoginSettingsData(loginSettingsData: LoginSettingsData) {
        loginSettingsDataDao.insertOrUpdate(loginSettingsData)
    }

    suspend fun deleteLoginSettingsData(loginSettingsData: LoginSettingsData) {
        loginSettingsDataDao.delete(loginSettingsData)
    }

    suspend fun getLoginSettingsDataByUsername(username: String): LoginSettingsData? {
        return loginSettingsDataDao.findByUsername(username)
    }
}

