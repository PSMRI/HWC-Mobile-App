package org.piramalswasthya.cho.ui.login_activity.cho_login.outreach

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentOutreachBinding
import org.piramalswasthya.cho.ui.login_activity.cho_login.ChoLoginFragmentDirections
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class OutreachFragment(
    private val userName: String,
    private val rememberUsername: Boolean,
    ): Fragment() {

    @Inject
    lateinit var prefDao: PreferenceDao
    @Inject
    lateinit var userDao: UserDao

    private var _binding: FragmentOutreachBinding? = null
    private val binding: FragmentOutreachBinding
        get() = _binding!!

    private lateinit var viewModel: OutreachViewModel

    private val REQUEST_IMAGE_CAPTURE = 1

    private val CAMERA_PERMISSION_REQUEST = 101

    var validImage: Boolean? = false

    var image: Bitmap? = null

    private lateinit var faceDetector: FirebaseVisionFaceDetector
    private fun requestCameraPermission() {
        requireContext()
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        } else {
            dispatchTakePictureIntent()
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                Toast.makeText(
                    requireContext(), "Camera permission denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
//        //Ensure that there's a camera activity to handle the intent
//        if (takePictureIntent.resolveActivity(CustomPackageManager()) != null) {
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
//        }
    }

//    private fun dispatchTakePictureIntent() {
//        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//
//        if (takePictureIntent.resolveActivity(packageManager) != null) {
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
//        }
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            image = imageBitmap
            processImage(imageBitmap)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(OutreachViewModel::class.java)
        _binding = FragmentOutreachBinding.inflate(layoutInflater, container, false)
        val options = FirebaseVisionFaceDetectorOptions.Builder()
            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
            .build()

        faceDetector = FirebaseVision.getInstance().getVisionFaceDetector(options)

        return binding.root
    }

    private fun processImage(image: Bitmap) {
        val firebaseImage = FirebaseVisionImage.fromBitmap(image)

        faceDetector.detectInImage(firebaseImage)
            .addOnSuccessListener { faces ->
                if(faces.size ==0){
                    Timber.d("Invalid Image!")
                    validImage = false
                    Toast.makeText(requireContext(),"Invalid Image! Try Again",Toast.LENGTH_SHORT).show()
                }
                if(faces.size >1) {
                    Timber.d("Invalid Image! Multiple faces detected")
                    validImage = false
                    binding.imageView.setImageResource(R.drawable.placeholder_image)
                    Toast.makeText(requireContext(),"Invalid Image! Multiple Faces Detected",Toast.LENGTH_SHORT).show()
                }
                else{
                    processDetectedFaces(faces)
                }
//                for (face in faces) {
//                    Timber.d("face found is $face")
////                    Toast.makeText(context,"Valid Image",Toast.LENGTH_SHORT).show()
//
//                    // Access face information (e.g., bounding box, landmarks, etc.)
//                    val bounds = face.boundingBox
//                    // ...
//                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors
                validImage = false
                binding.imageView.setImageResource(R.drawable.placeholder_image)
                Toast.makeText(context,"Exception! Image Processing Failed",Toast.LENGTH_SHORT).show()
            }
    }

    private fun processDetectedFaces(faces: List<FirebaseVisionFace>) {
        for (face in faces) {
            val bounds = face.boundingBox

            // Check if both eyes are open
            val leftEyeOpen = face.leftEyeOpenProbability ?: 0f
            val rightEyeOpen = face.rightEyeOpenProbability ?: 0f

            if (leftEyeOpen > 0.5 && rightEyeOpen > 0.5) {
                Timber.d("Eyes Are Open")
                validImage = true
                binding.imageView.setImageBitmap(image)
                // Both eyes are open, liveness confirmed
                // Implement your logic for a live face
            } else {
                Timber.d("Eyes Are Closed")
                validImage = false
                binding.imageView.setImageResource(R.drawable.placeholder_image)
                Toast.makeText(requireContext(),"Eyes Closed! Try Again",Toast.LENGTH_SHORT).show()
                // At least one eye is closed, possibly not live face
                // Implement your logic for an inactive or spoofed face
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.tag("Outreach username").i(userName);
        binding.imageView.setOnClickListener {
            requestCameraPermission()
        }
        binding.btnOutreachLogin.setOnClickListener {

            val radioGroup = binding.selectProgram
            val selectedOptionId = radioGroup.checkedRadioButtonId
            val selectedOption = view.findViewById<MaterialRadioButton>(selectedOptionId).text.toString()
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            viewModel.authUser(userName, binding.etPassword.text.toString(),selectedOption,timestamp)

            viewModel.state.observe(viewLifecycleOwner) { state ->
                when (state!!) {
                    OutreachViewModel.State.SUCCESS -> {
                        if(rememberUsername)
                            viewModel.rememberUser(userName)
                        else {
                            viewModel.forgetUser()
                        }
                        findNavController().navigate(
                            ChoLoginFragmentDirections.actionSignInToHomeFromCho())
                        viewModel.resetState()
                        activity?.finish()
                    }
                    OutreachViewModel.State.ERROR_SERVER,
                    OutreachViewModel.State.ERROR_NETWORK-> {
                        Toast.makeText(requireContext(), "Error while logging in!!",Toast.LENGTH_LONG).show()
                        viewModel.resetState()
                    }
                    else -> {}
                }

                    }

            }



            }
//            viewModel.dummyAuthUser(userName, binding.etPassword.text.toString());
//            findNavController().navigate(
//                OutreachFragmentDirections.actionOutreachLoginFragmentToFhirVitalsFragment()
//            )
        }


