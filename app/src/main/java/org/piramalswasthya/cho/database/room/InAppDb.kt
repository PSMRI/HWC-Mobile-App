package org.piramalswasthya.cho.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.converters.LocationEntityListConverter
import org.piramalswasthya.cho.database.converters.SyncStateConverter
import org.piramalswasthya.cho.database.room.dao.UserAuthDao
import org.piramalswasthya.cho.model.*
import timber.log.Timber

@Database(
    entities = [
        UserCache::class,
        UserAuth::class
    ],
//    views = [BenBasicCache::class],
    version = 3, exportSchema = false
)

@TypeConverters(LocationEntityListConverter::class, SyncStateConverter::class)

abstract class InAppDb : RoomDatabase() {

    abstract val userDao: UserDao

    abstract val userAuthDao: UserAuthDao

    companion object {
        @Volatile
        private var INSTANCE: InAppDb? = null

        fun getInstance(appContext: Context): InAppDb {

            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        appContext,
                        InAppDb::class.java,
                        "CHO-1.0-In-app-database"
                    )
//                        .allowMainThreadQueries()
                        .fallbackToDestructiveMigration()
                        .setQueryCallback(
                            object : QueryCallback {
                                override fun onQuery(sqlQuery: String, bindArgs: List<Any?>) {
                                    Timber.d("Query to Room : sqlQuery=$sqlQuery with arguments : $bindArgs")
                                }
                            },
                            Dispatchers.IO.asExecutor()
                        )
                        .build()

                    INSTANCE = instance
                }
                return instance

            }
        }
    }
}