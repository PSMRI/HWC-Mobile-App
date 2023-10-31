package org.piramalswasthya.cho.work

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.fhir.SelectedOutreachProgram
import org.piramalswasthya.cho.repositories.UserRepo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AutoLogOutWorker@AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val userRepo: UserRepo,
    private val userDao: UserDao,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
//        val currentTime = Calendar.getInstance()
//        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
//        val min = currentTime.get(Calendar.MINUTE)
//
//        if (hour >= 19 && min >= 18) { // Check if it's 5 PM or later
//            // Perform the auto-logout action here, such as clearing the user's session.
////            userDao.insertOutreachProgram(selectedOutreachProgram)
//            Log.d("logoutTime", "shouldLogout")
//
//            val pattern = "yyyy-MM-dd'T'HH:mm:ssZ"
//            val timeZone = TimeZone.getTimeZone("GMT+0530")
//            val formatter = SimpleDateFormat(pattern, Locale.getDefault())
//            formatter.timeZone = timeZone
//
//            val logoutTimestamp = formatter.format(Date())
//            val user = userDao.getLoggedInUser()
//
//            val selectedOutreachProgram = SelectedOutreachProgram(0,
//                user?.userId,
//                user?.userName,
//                null,
//                null,
//                logoutTimestamp,
//                null,
//                null,
//                null,
//                "By System",
//                null)
//            userDao.insertOutreachProgram(selectedOutreachProgram)
//            userDao.resetAllUsersLoggedInState()
//            if (user != null) {
//                userDao.updateLogoutTime(user.userId, Date())
//            }
//        }

        return Result.success()
    }
}
