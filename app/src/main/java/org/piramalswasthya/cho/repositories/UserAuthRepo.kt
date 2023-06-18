package org.piramalswasthya.cho.repositories

import android.util.Log
import org.piramalswasthya.cho.database.room.dao.UserAuthDao
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.UserAuth
//import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.ui.login_activity.cho_login.outreach.OutreachViewModel
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject


class UserAuthRepo @Inject constructor(
    private val userAuthDao: UserAuthDao,
) {
    fun authenticateUser(userName: String, password: String,): String {
        try{
            val currRowCount = userAuthDao.getRowCount();
            Log.i("tag", currRowCount.toString())
            userAuthDao.insert(UserAuth(currRowCount, userName, password))
        } catch (e : Exception) {
            Timber.tag("tag").i(e.message)
//            print(e.message);
        }
        return "";
    }
}