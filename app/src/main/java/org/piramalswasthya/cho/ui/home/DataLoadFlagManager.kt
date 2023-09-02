package org.piramalswasthya.cho.ui.home

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

class DataLoadFlagManager @Inject constructor(
    @ApplicationContext context: Context
) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "data_load_flag_prefs",
        Context.MODE_PRIVATE
    )

    private val LAST_LOGIN_DATE_KEY = "last_login_date"

    fun isDataLoaded(): Boolean {
        // Get the last login date from SharedPreferences
        val lastLoginDate = sharedPreferences.getLong(LAST_LOGIN_DATE_KEY, 0)

        // Get the current date
        val currentDate = Calendar.getInstance().timeInMillis

        // Check if the difference between the current date and last login date is greater than 10 days (in milliseconds)
        val shouldResetFlag = currentDate - lastLoginDate > 10 * 24 * 60 * 60 * 1000 // 10 days in milliseconds

        if (shouldResetFlag) {
            // If it's been more than 10 days since the last login, reset the flag
            setDataLoaded(false)
        }

        return sharedPreferences.getBoolean("data_loaded", false)
    }

    fun setDataLoaded(loaded: Boolean) {
        // Set the data_loaded flag to the provided value
        sharedPreferences.edit().putBoolean("data_loaded", loaded).apply()

        // Update the last login date when the flag is set to true
        if (loaded) {
            sharedPreferences.edit().putLong(LAST_LOGIN_DATE_KEY, Calendar.getInstance().timeInMillis).apply()
        }
    }
}
