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
     * Turns a gallery/camera URI or local path into a compressed data-URI base64
     * string for API upload. Downsamples 4× and re-encodes JPEG at quality 20 so
     * the resulting payload fits comfortably inside server-side column limits —
     * a raw multi-MB photo would otherwise blow up the ancVisit/saveAll endpoint
     * with a `statusCode:5000 "Saving anc data to db failed"`. EXIF rotation is
     * preserved when the source has a file path we can read it from.
     * Leaves values that already look like API/base64 payloads unchanged.
     */
    fun encodeLocalImageValueForUpload(context: Context, value: String?): String? {
        val v = value?.trim().orEmpty()
        if (v.isEmpty()) return null
        if (v.startsWith("data:image/", ignoreCase = true)) return v
        if (!v.contains("://") && v.length > 200) return v

        return try {
            val options = BitmapFactory.Options().apply { inSampleSize = 4 }
            val exifPath: String? = if (!v.contains("://") && v.startsWith("/")) v else null

            // Decode straight from the file / InputStream so inSampleSize=4 caps
            // peak memory at ~(width/4 × height/4 × 4 bytes). readBytes() would
            // first materialise the full multi-MB JPEG on the heap, defeating
            // the downsample and OOM-ing on low-RAM devices.
            var bitmap: Bitmap = if (exifPath != null) {
                if (!File(exifPath).exists()) return null
                BitmapFactory.decodeFile(exifPath, options) ?: return null
            } else {
                val uri = Uri.parse(v)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    BitmapFactory.decodeStream(input, null, options)
                } ?: return null
            }

            // EXIF rotation is only retrievable from a file on disk.
            if (exifPath != null) {
                val exif = android.media.ExifInterface(exifPath)
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
                    val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                    if (rotated !== bitmap) bitmap.recycle()
                    bitmap = rotated
                }
            }

            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, out)
            bitmap.recycle()
            val encoded = base64.encodeToString(out.toByteArray(), base64.NO_WRAP)
            "data:image/jpeg;base64,$encoded"
        } catch (e: Exception) {
            Timber.e(e, "encodeLocalImageValueForUpload failed for value prefix=${v.take(48)}")
            null
        }
    }

    /**
     * Persists a picked image (content://, file://, or absolute path) into
     * app-private internal storage and returns its absolute path. Returning
     * a path keeps Room rows tiny — base64 encoding only happens at upload.
     *
     * Idempotent: an existing absolute path is returned as-is. Legacy data-URI
     * base64 values return null (their payload can't be safely materialised
     * here; the v146→147 migration scrubs them anyway).
     */
    fun saveImageToInternalStorage(context: Context, source: String?): String? {
        val raw = source?.trim().orEmpty()
        if (raw.isEmpty()) return null
        if (raw.startsWith("data:image", ignoreCase = true)) return null
        if (!raw.contains("://") && raw.startsWith("/")) {
            return if (File(raw).exists()) raw else null
        }
        return try {
            val uri = Uri.parse(raw)
            val dir = File(context.filesDir, "abortion_images").apply { mkdirs() }
            val outFile = File(
                dir,
                "img_${System.currentTimeMillis()}_${java.util.UUID.randomUUID()}.jpg"
            )
            context.contentResolver.openInputStream(uri)?.use { input ->
                outFile.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            outFile.absolutePath
        } catch (e: Exception) {
            Timber.e(e, "saveImageToInternalStorage failed for prefix=${raw.take(48)}")
            null
        }
    }

}