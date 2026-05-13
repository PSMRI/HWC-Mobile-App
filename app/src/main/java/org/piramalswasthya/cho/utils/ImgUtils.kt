package org.piramalswasthya.cho.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import java.util.Base64
import android.util.Base64 as base64
import androidx.annotation.RequiresApi
import timber.log.Timber
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
        if (fileName.isNullOrEmpty()) return null

        val file = File(context.getExternalFilesDir("images"), fileName)
        if (!file.exists()) return null

        return try {
            val options = BitmapFactory.Options()
            options.inSampleSize = 4
            var bm = BitmapFactory.decodeFile(file.path, options)

            // Correct EXIF rotation — camera photos are often saved sideways
            val exif = android.media.ExifInterface(file.path)
            val orientation = exif.getAttributeInt(
                android.media.ExifInterface.TAG_ORIENTATION,
                android.media.ExifInterface.ORIENTATION_NORMAL
            )
            val matrix = android.graphics.Matrix()
            when (orientation) {
                android.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                android.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                android.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }
            if (!matrix.isIdentity) {
                bm = Bitmap.createBitmap(bm, 0, 0, bm.width, bm.height, matrix, true)
            }

            val stream = ByteArrayOutputStream()
            bm.compress(Bitmap.CompressFormat.JPEG, 20, stream)
            val byteFormat = stream.toByteArray()
            val encoded = Base64.getEncoder().encodeToString(byteFormat)
            if (encoded.isNullOrEmpty()) null else "data:image/png;base64,$encoded"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun base64ConvertedString(path: String?) : String?{
        if(path == null) return null
        try {
            val imageBytes = File(path).readBytes()
            return encodeToBase64(imageBytes);
        } catch (e: Exception) {
            println("Error: ${e.message}")
            return null;
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun encodeToBase64(bytes: ByteArray): String {
        val encoder = Base64.getEncoder()
        return encoder.encodeToString(bytes)
    }

    fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        val decodedBytes = base64.decode(base64String, base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    /**
     * Turns a gallery/camera URI or local path into a data-URI base64 string for API upload.
     * Leaves values that already look like API/base64 payloads unchanged.
     */
    fun encodeLocalImageValueForUpload(context: Context, value: String?): String? {
        val v = value?.trim().orEmpty()
        if (v.isEmpty()) return null
        if (v.startsWith("data:image/", ignoreCase = true)) return v
        if (!v.contains("://") && v.length > 200) return v

        return try {
            val uri = Uri.parse(v)
            context.contentResolver.openInputStream(uri)?.use { input ->
                val bytes = input.readBytes()
                val encoded = base64.encodeToString(bytes, base64.NO_WRAP)
                "data:image/jpeg;base64,$encoded"
            }
        } catch (e: Exception) {
            Timber.e(e, "encodeLocalImageValueForUpload failed for value prefix=${v.take(48)}")
            null
        }
    }

}