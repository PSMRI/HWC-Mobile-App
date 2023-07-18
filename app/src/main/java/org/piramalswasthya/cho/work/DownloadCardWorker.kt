package org.piramalswasthya.cho.work


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.internal.notify
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.repositories.AbhaIdRepo
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

@HiltWorker
class DownloadCardWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val abhaIdRepo: AbhaIdRepo,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val name = "DownloadCardWorker"
        const val file_name = "file_name"
    }

    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "download abha card"

    override suspend fun doWork(): Result {
        val fileName = inputData.getString(file_name)
        return try {
            try {
                // This ensures that you waiting for the Notification update to be done.
                setForeground(createForegroundInfo("Downloading"))
            } catch (throwable: Throwable) {
                // Handle this exception gracefully
                Timber.d(throwable.message, "Something bad happened", throwable)
            }
            withContext(Dispatchers.IO) {
                val response = abhaIdRepo.downloadPdfCard()
                val directory =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = fileName?.let { File(directory, it) }
                val responseBody = response.body()!!
                val inputStream = responseBody.byteStream()
                val outputStream = FileOutputStream(file)

                val buffer = ByteArray(1024)
                var bytesRead = inputStream.read(buffer)
                var totalBytesRead = bytesRead.toLong()
                while (bytesRead != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    bytesRead = inputStream.read(buffer)
                }
                outputStream.close()
                inputStream.close()

                MediaScannerConnection.scanFile(
                    appContext,
                    arrayOf(file.toString()),
                    null,
                ) { _, uri ->
                    run {
                        if (fileName != null) {
                            showDownload(fileName, uri)
                        }
                    }
                }
                return@withContext Result.success()
            }
        } catch (e : java.lang.Exception) {
            return Result.failure()
        }
    }

    private fun createForegroundInfo(progress: String): ForegroundInfo {

        val notification = NotificationCompat.Builder(appContext,channelId)
            .setContentTitle("Downloading abha card")
            .setContentText(progress)
            .setSmallIcon(R.drawable.ic_download)
            .setProgress(100, 0, true)
            .build()

        return ForegroundInfo(0, notification)
    }

    private fun showDownload(fileName: String, uri: Uri) {
        val notificationBuilder = NotificationCompat.Builder(appContext,fileName)
            .setSmallIcon(R.drawable.ic_download)
            .setChannelId(channelId)
            .setContentTitle(fileName)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationBuilder.setContentTitle(fileName)
            .setContentText(fileName)
            .setAutoCancel(true)
            .setOngoing(false)
            .setContentIntent(
                PendingIntent.getActivity(
                    appContext,
                    0,
                    Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        notificationManager.notify(1, notificationBuilder.build())

    }
}