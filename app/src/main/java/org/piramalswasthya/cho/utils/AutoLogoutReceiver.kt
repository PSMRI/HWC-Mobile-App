package org.piramalswasthya.cho.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.model.fhir.SelectedOutreachProgram
import org.piramalswasthya.cho.ui.login_activity.LoginActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject


@AndroidEntryPoint
class AutoLogoutReceiver : BroadcastReceiver() {
    @Inject
    lateinit var userDao: UserDao
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("timed11", "shouldLogout")


            val pattern = "yyyy-MM-dd'T'HH:mm:ssZ"
            val timeZone = TimeZone.getTimeZone("GMT+0530")
            val formatter = SimpleDateFormat(pattern, Locale.getDefault())
            formatter.timeZone = timeZone

            val logoutTimestamp = formatter.format(Date())
        var user: UserCache?
        GlobalScope.launch(Dispatchers.IO) {
            user = userDao.getLoggedInUser()
            if (user != null) {

            val selectedOutreachProgram = SelectedOutreachProgram(
                0,
                user?.userId,
                user?.userName,
                null,
                null,
                logoutTimestamp,
                null,
                null,
                null,
                "By System",
                null
            )
            userDao.insertOutreachProgram(selectedOutreachProgram)
            userDao.resetAllUsersLoggedInState()
//            if (user != null) {
                userDao.updateLogoutTime(user!!.userId, Date())
                val startIntent = Intent(context, LoginActivity::class.java)
                startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_NEW_TASK)
                context!!.startActivity(startIntent)

            }
        }

    }
}