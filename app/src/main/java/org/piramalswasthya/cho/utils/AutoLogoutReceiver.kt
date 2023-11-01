package org.piramalswasthya.cho.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AutoLogoutReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("timed11", "shouldLogout")


//            val pattern = "yyyy-MM-dd'T'HH:mm:ssZ"
//            val timeZone = TimeZone.getTimeZone("GMT+0530")
//            val formatter = SimpleDateFormat(pattern, Locale.getDefault())
//            formatter.timeZone = timeZone
//
//            val logoutTimestamp = formatter.format(Date())
//        var user:UserCache?
//        GlobalScope.launch(Dispatchers.IO) {
//            user = userDao.getLoggedInUser()
//
//            val selectedOutreachProgram = SelectedOutreachProgram(
//                0,
//                user?.userId,
//                user?.userName,
//                null,
//                null,
//                logoutTimestamp,
//                null,
//                null,
//                null,
//                "By System",
//                null
//            )
//            userDao.insertOutreachProgram(selectedOutreachProgram)
//            userDao.resetAllUsersLoggedInState()
//            if (user != null) {
//                userDao.updateLogoutTime(user!!.userId, Date())
//            }
//        }

    }
}