package org.piramalswasthya.cho.utils

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.facenet.FaceNetModel
import org.piramalswasthya.cho.facenet.Models
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.ui.commons.SpeechToTextContract
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.pow

/**
 * Reusable helper for search-by-voice (speech-to-text) and search-by-camera (face detection)
 * functionality in RMNCH fragments.
 *
 * Usage:
 *   1. Create an instance in the fragment **before** `onViewCreated`:
 *      ```
 *      private val faceSearchHelper = FaceSearchHelper(this, patientDao) { matched ->
 *          // handle matched Patient
 *      }
 *      ```
 *   2. In `onViewCreated` / `setupSearch()`, wire the UI:
 *      ```
 *      binding.searchTil.setEndIconOnClickListener { faceSearchHelper.launchSpeechToText() }
 *      binding.cameraIcon.setOnClickListener { faceSearchHelper.launchCameraSearch() }
 *      ```
 *   3. Receive the match callback or handle the speech-to-text result via [onSpeechResult].
 */
class FaceSearchHelper(
    private val fragment: Fragment,
    private val patientDao: PatientDao,
    private val isCameraSearchEnabled: Boolean = true,
    private val onSpeechResult: ((String) -> Unit)? = null,
    private val onFaceMatchResult: ((Patient?) -> Unit)? = null
) {

    // FaceNet
    private val modelInfo = Models.FACENET
    private val useGpu = false
    private val useXNNPack = true
    private lateinit var faceNetModel: FaceNetModel

    // Camera
    private var photoURI: Uri? = null
    private var currentFileName: String? = null

    // ── Activity-result launchers (MUST be registered at fragment init time) ──

    val speechToTextLauncher: ActivityResultLauncher<Unit> =
        fragment.registerForActivityResult(SpeechToTextContract()) { result ->
            if (result.isNotBlank() && !result.any { it.isDigit() }) {
                onSpeechResult?.invoke(result)
            }
        }

    @RequiresApi(Build.VERSION_CODES.P)
    private val permissionLauncher: ActivityResultLauncher<Array<String>> =
        fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.values.all { it }) {
                takePicture()
            } else {
                Toast.makeText(
                    fragment.requireContext(),
                    fragment.getString(R.string.permission_to_access_the_camera_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val takePictureLauncher: ActivityResultLauncher<Uri> =
        fragment.registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) handleCapturedPhoto()
        }

    // ── Public API ──

    /** Launch the speech-to-text recogniser. */
    fun launchSpeechToText() {
        speechToTextLauncher.launch(Unit)
    }

    /** Show a loading dialog, initialise FaceNet, then open the camera. */
    fun launchCameraSearch() {
        if (!isCameraSearchEnabled) return
        val ctx = fragment.requireContext()
        val dialogView = fragment.layoutInflater.inflate(R.layout.dialog_progress, null)
        dialogView.findViewById<ImageView>(R.id.loading_gif)?.let {
            Glide.with(fragment).load(R.drawable.face).into(it)
        }
        val dialog = AlertDialog.Builder(ctx)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        dialog.show()

        fragment.lifecycleScope.launch(Dispatchers.IO) {
            faceNetModel = FaceNetModel(fragment.requireActivity(), modelInfo, useGpu, useXNNPack)
            withContext(Dispatchers.Main) {
                if (fragment.isAdded) {
                    dialog.dismiss()
                    checkAndRequestCameraPermission()
                }
            }
        }
    }

    // ── Internal camera flow ──

    @RequiresApi(Build.VERSION_CODES.P)
    private fun checkAndRequestCameraPermission() {
        val ctx = fragment.requireContext()
        if (checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        ) {
            takePicture()
        } else {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
        }
    }

    private fun takePicture() {
        val ctx = fragment.requireContext()
        val photoFile: File? = try {
            createImageFile(ctx)
        } catch (_: Exception) {
            null
        }
        photoFile?.also {
            val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".provider", it)
            photoURI = uri
            takePictureLauncher.launch(uri)
        }
    }

    private fun createImageFile(ctx: android.content.Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault()).format(Date())
        val storageDir = ctx.getExternalFilesDir("images")
        currentFileName = "JPEG_${timeStamp}_.jpeg"
        var file = File(storageDir, currentFileName)
        var counter = 1
        while (file.exists()) {
            currentFileName = "JPEG_${timeStamp}_$counter.jpeg"
            file = File(storageDir, currentFileName)
            counter++
        }
        return file
    }

    // ── Face detection & matching ──

    private fun handleCapturedPhoto() {
        val ctx = fragment.requireContext()
        val uri = photoURI
        if (uri == null) {
            Toast.makeText(ctx, "Photo capture failed. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val baseOpts = BaseOptions.builder().setModelAssetPath("blaze_face_short_range.tflite")
            val options = FaceDetector.FaceDetectorOptions.builder()
                .setBaseOptions(baseOpts.build())
                .setMinDetectionConfidence(0.75f)
                .setRunningMode(RunningMode.IMAGE)
                .build()
            val faceDetector = FaceDetector.createFromOptions(ctx, options)

            val maxDimension = 1024
            val imageBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(ctx.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    val size = info.size
                    val sampleSize = maxOf(size.width, size.height) / maxDimension
                    if (sampleSize > 1) decoder.setTargetSampleSize(sampleSize)
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(ctx.contentResolver, uri)
            }.copy(Bitmap.Config.ARGB_8888, true)

            val mpImage = BitmapImageBuilder(imageBitmap).build()
            val detectionResult = faceDetector.detect(mpImage)

            when {
                detectionResult.detections().isEmpty() -> {
                    Toast.makeText(ctx, "No face detected", Toast.LENGTH_SHORT).show()
                    faceDetector.close()
                    return
                }
                detectionResult.detections().size > 1 -> {
                    Toast.makeText(ctx, "Multiple faces detected", Toast.LENGTH_SHORT).show()
                    faceDetector.close()
                    return
                }
            }

            val detection = detectionResult.detections()[0]
            val box = detection.boundingBox()

            if (box.right <= box.left || box.bottom <= box.top) {
                Toast.makeText(ctx, "Invalid face detection", Toast.LENGTH_SHORT).show()
                faceDetector.close()
                return
            }

            val left = box.left.toInt().coerceAtLeast(0)
            val top = box.top.toInt().coerceAtLeast(0)
            val right = box.right.toInt().coerceAtMost(imageBitmap.width)
            val bottom = box.bottom.toInt().coerceAtMost(imageBitmap.height)
            val width = right - left
            val height = bottom - top

            if (width <= 0 || height <= 0 || left >= imageBitmap.width || top >= imageBitmap.height) {
                Toast.makeText(ctx, "Invalid face detection", Toast.LENGTH_SHORT).show()
                faceDetector.close()
                return
            }

            val faceBitmap = Bitmap.createBitmap(imageBitmap, left, top, width, height)
            faceDetector.close()

            val embeddings = faceNetModel.getFaceEmbedding(faceBitmap)
            if (embeddings == null) {
                Toast.makeText(ctx, "Failed to generate face embeddings", Toast.LENGTH_SHORT).show()
                return
            }

            fragment.lifecycleScope.launch {
                val matchedPatient = compareFacesL2Norm(embeddings)
                onFaceMatchResult?.invoke(matchedPatient)
            }

        } catch (e: Exception) {
            Log.e("FaceSearchHelper", "Face detection failed", e)
            Toast.makeText(ctx, "Face detection failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun compareFacesL2Norm(newEmbedding: FloatArray): Patient? {
        var bestMatch: Patient? = null
        var bestDistance = Float.MAX_VALUE

        val patients = withContext(Dispatchers.IO) { patientDao.getAllPatients() }
        withContext(Dispatchers.Default) {
            for (patient in patients) {
                val patientEmbedding = patient.faceEmbedding?.toFloatArray()
                if (patientEmbedding == null || patientEmbedding.isEmpty()) continue

                val distance = l2Norm(newEmbedding, patientEmbedding)
                if (distance < bestDistance) {
                    bestDistance = distance
                    bestMatch = patient
                }
            }
        }

        return if (bestDistance < Models.FACENET.l2Threshold) bestMatch else null
    }

    private fun l2Norm(x1: FloatArray, x2: FloatArray): Float {
        var sum = 0.0f
        for (i in x1.indices) {
            sum += (x1[i] - x2[i]).pow(2)
        }
        return kotlin.math.sqrt(sum)
    }
}
