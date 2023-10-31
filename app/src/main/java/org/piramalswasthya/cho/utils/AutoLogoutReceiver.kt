package org.piramalswasthya.cho.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AutoLogoutReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
//        Log.d("timed", "shouldLogout")

        // Perform the logout action here, such as clearing user session and redirecting to the login screen.
        // You can also use a shared preference or some other mechanism to track the logout state.
    }
}