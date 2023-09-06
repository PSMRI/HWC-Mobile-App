package org.piramalswasthya.sakhi.work

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.repositories.PatientRepo
import timber.log.Timber
import java.lang.Integer.min
import java.util.concurrent.TimeUnit

@HiltWorker
class PullFromAmritWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val patientRepo: PatientRepo,
    private val preferenceDao: PreferenceDao,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "PullFromAmritWorker"
        const val Progress = "Progress"
        const val NumPages = "Total Pages"
        const val n = 4 // Number of threads!
    }

//    private val notificationManager =
//        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as
//                NotificationManager

    private var page1: Int = 0
    private var page2: Int = 0
    private var page3: Int = 0
    private var page4: Int = 0


    override suspend fun doWork(): Result {
        return try {
            try {
                // This ensures that you waiting for the Notification update to be done.
//                setForeground(createForegroundInfo("Downloading"))
            } catch (throwable: Throwable) {
                // Handle this exception gracefully
                Timber.d("FgLW", "Something bad happened", throwable)
            }
            withContext(Dispatchers.IO) {
//                val startTime = System.currentTimeMillis()
//                var numPages: Int
//                val startPage = if(preferenceDao.getLastSyncedTimeStamp()==Konstants.defaultTimeStamp)
//                    preferenceDao.getFirstSyncLastSyncedPage()
//                else 0

                try {
//                    do {
//                        numPages = benRepo.getBeneficiariesFromServerForWorker(startPage)
//                    } while (numPages == -2)
////                for (i in 1 until numPages)
////                    benRepo.getBeneficiariesFromServerForWorker(i)
//                    if (numPages == 0)
//                        return@withContext Result.success()
//                    val result1 =
//                        awaitAll(
//                            async { getBenForPage(numPages, 0, startPage) },
//                            async { getBenForPage(numPages, 1, startPage) },
//                            async { getBenForPage(numPages, 2, startPage) },
//                            async { getBenForPage(numPages, 3, startPage) },
//                        )
//                    val endTime = System.currentTimeMillis()
//                    val timeTaken = TimeUnit.MILLISECONDS.toSeconds(endTime - startTime)
//                    Timber.d("Full load took $timeTaken seconds for $numPages pages  $result1")
//
//                    if (result1.all { it }) {
//                        preferenceDao.setLastSyncedTimeStamp(System.currentTimeMillis())
//
//                        return@withContext Result.success()
//                    }
                    return@withContext Result.failure()
                }catch (e : SQLiteConstraintException){
                        Timber.d("exception $e raised ${e.message} with stacktrace : ${e.stackTrace}")
                    return@withContext Result.failure()
                    }


//                for (j in 0 until n) {
//                    if (j < numPages)
//                        getBenForPage(numPages, j)
//                }


            }

        } catch (e: java.lang.Exception) {
            Timber.d("Error occurred in PullFromAmritFullLoadWorker $e ${e.stackTrace}")

            Result.failure()
        }
    }

//    private fun createForegroundInfo(progress: String): ForegroundInfo {
//        // This PendingIntent can be used to cancel the worker
////        val intent = WorkManager.getInstance(applicationContext)
////            .createCancelPendingIntent(id)
//
//        val notification = NotificationCompat.Builder(
//            appContext,
//            appContext.getString(org.piramalswasthya.sakhi.R.string.notification_sync_channel_id)
//        )
//            .setContentTitle("Syncing Data")
//            .setContentText(progress)
//            .setSmallIcon(org.piramalswasthya.sakhi.R.drawable.ic_launcher_foreground)
//            .setProgress(100, 0, true)
//            .setOngoing(true)
//            .build()
//
//        return ForegroundInfo(0, notification)
//    }


//    private suspend fun getBenForPage(numPages: Int, rem: Int, startPage : Int): Boolean {
//        return withContext(Dispatchers.IO) {
//            var page: Int = startPage + rem
//
//            try{while (page < numPages) {
//                val ret = benRepo.getBeneficiariesFromServerForWorker(page)
//
//                if (ret == -1)
//                    throw IllegalStateException("benRepo.getBeneficiariesFromServerForWorker(page) returned -1 ")
//                if (ret != -2) {
//                    val finalPage = (page1+page2+page3+page4)/4
//                    val minPageSynced = min(min(page1,page2), min(page3,page4))
//                    preferenceDao.setFirstSyncLastSyncedPage(minPageSynced)
//                    setProgressAsync(workDataOf(Progress to finalPage, NumPages to numPages ))
//                    page += n
//                }
//                when(rem){
//                    0-> page1 = page
//                    1-> page2 = page
//                    2-> page3 = page
//                    3-> page4 = page
//                }
//
//            }}catch (e : SQLiteConstraintException){
//                Timber.d("exception $e raised ${e.message} with stacktrace : ${e.stackTrace}")
//            }
//            true
//        }
//    }

}