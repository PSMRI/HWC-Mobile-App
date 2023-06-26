package org.piramalswasthya.cho.database.shared_preferences

import android.content.Context
import android.content.SharedPreferences
import org.piramalswasthya.cho.R

class PreferenceManager private constructor() {

    companion object {

        @Volatile
        private var INSTANCE: SharedPreferences? = null
        internal fun getInstance(context: Context): SharedPreferences {

            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = context.getSharedPreferences(
                        context.resources.getString(R.string.PREF_NAME),
                        Context.MODE_PRIVATE
                    )
                    INSTANCE = instance
                }
                return instance!!
            }

        }
    }

}