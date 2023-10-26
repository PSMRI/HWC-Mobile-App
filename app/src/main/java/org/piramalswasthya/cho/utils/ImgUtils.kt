package org.piramalswasthya.cho.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import java.util.Base64
import androidx.annotation.RequiresApi
import java.io.ByteArrayOutputStream
import java.io.File

object ImgUtils {
    var canFinishActivity = false
    @RequiresApi(Build.VERSION_CODES.O)
    fun bitmapToBase64(bitmap: Bitmap?): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.getEncoder().encodeToString(byteArray)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun getEncodedStringForBenImage(context: Context, fileName: String?): String? {
        if (!fileName.isNullOrEmpty()) {
            val file = File(context.getExternalFilesDir("images"), fileName)

            if (file.exists()) {
                try {
                    val bm = BitmapFactory.decodeFile(file.path)
                    val stream = ByteArrayOutputStream()
                    bm.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                    val byteFormat = stream.toByteArray()
                    if (!Base64.getEncoder().encodeToString(byteFormat).isNullOrEmpty()) {
                        return "data:image/png;base64," + Base64.getEncoder()
                            .encodeToString(byteFormat)
                    }
                    return null

                } catch (e: Exception) {
                    // Handle the IOException here, log or return null as needed
                    e.printStackTrace()
                    return null
                }
            } else {
                // Handle the case where the file doesn't exist, log or return null as needed
                return null
            }
        }
        else {
            return null
        }
    }
}