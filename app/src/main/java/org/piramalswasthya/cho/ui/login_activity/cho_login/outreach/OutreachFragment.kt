package org.piramalswasthya.cho.ui.login_activity.cho_login.outreach

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.OutreachDropdownAdapter
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentOutreachBinding
import org.piramalswasthya.cho.model.OutreachDropdownList
import org.piramalswasthya.cho.model.SubVisitCategory
import org.piramalswasthya.cho.ui.login_activity.cho_login.ChoLoginFragmentDirections
import org.piramalswasthya.cho.utils.ImgUtils
import org.piramalswasthya.cho.utils.nullIfEmpty
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@AndroidEntryPoint
class OutreachFragment(
    private val userName: String,
    private val rememberUsername: Boolean,
    private val isBiometric: Boolean,
    ) : Fragment() {

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
    var imageString: String? = null
    private var outreachList = ArrayList<OutreachDropdownList>()
    private lateinit var faceDetector: FirebaseVisionFaceDetector
    private var outreachNameMap = emptyMap<Int,String>()
    private var myLocation: Location? = null
    private var myInitialLoc: Location? = null
    private var locationManager: LocationManager? = null
    private lateinit var outreachAdapter: OutreachDropdownAdapter
    private var locationListener: LocationListener? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var loginType: String = ""
    private var tryAuthUser = false
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

    }

    @RequiresApi(Build.VERSION_CODES.O)
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
//        getCurrentLocation()
        if (isBiometric) {
            binding.tilPassword.visibility = View.GONE
            binding.btnOutreachLogin.text = "Proceed to Home"
        }else {
            binding.tilPassword.visibility = View.VISIBLE
            if (!viewModel.fetchRememberedPassword().isNullOrBlank()) {
                viewModel.fetchRememberedPassword()?.let {
                    binding.etPassword.setText(it)
                }
            }
        }
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun processImage(image: Bitmap) {
        val firebaseImage = FirebaseVisionImage.fromBitmap(image)

        faceDetector.detectInImage(firebaseImage)
            .addOnSuccessListener { faces ->
                if (faces.size == 0) {
                    Timber.d("Invalid Image!")
                    validImage = false
                    Toast.makeText(requireContext(), "Invalid Image! Try Again", Toast.LENGTH_SHORT)
                        .show()
                }
                if (faces.size > 1) {
                    Timber.d("Invalid Image! Multiple faces detected")
                    validImage = false
                    binding.imageView.setImageResource(R.drawable.placeholder_image)
                    Toast.makeText(
                        requireContext(),
                        "Invalid Image! Multiple Faces Detected",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    processDetectedFaces(faces)
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors
                validImage = false
                binding.imageView.setImageResource(R.drawable.placeholder_image)
                Toast.makeText(context, "Exception! Image Processing Failed", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
                imageString = ImgUtils.bitmapToBase64(image)
                // Both eyes are open, liveness confirmed
                // Implement your logic for a live face
            } else {
                Timber.d("Eyes Are Closed")
                validImage = false
                binding.imageView.setImageResource(R.drawable.placeholder_image)
                Toast.makeText(requireContext(), "Eyes Closed! Try Again", Toast.LENGTH_SHORT)
                    .show()
                // At least one eye is closed, possibly not live face
                // Implement your logic for an inactive or spoofed face
            }
        }
    }
    private fun <K, V> findKeyByValue(map: Map<K, V>, value: V): K? {
        return map.entries.find { it.value == value }?.key
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            outreachNameMap = viewModel.getReferNameTypeMap()
        }
        outreachAdapter = OutreachDropdownAdapter(requireContext(), R.layout.dropdown_subcategory,R.id.tv_dropdown_item_text, outreachList.map { it.outreachType })
        binding.outreachText.setAdapter(outreachAdapter)
        binding.outreachText.setText("Home Visit",false)
        viewModel.outreachList.observe(viewLifecycleOwner){ c->
            outreachList.clear()
            outreachList.addAll(c)
            outreachAdapter.addAll(c.map{it.outreachType})
            outreachAdapter.notifyDataSetChanged()
        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, onBackPressedCallback)

        Timber.tag("Outreach username").i(userName);
//        getCurrentLocation()
        binding.imageView.setOnClickListener {
            requestCameraPermission()
        }
        binding.btnOutreachLogin.setOnClickListener {
            if (imageString!=null && validImage!!) {

            // call for lat long
            getCurrentLocation()

            val outreachVal = binding.outreachText.text.toString()
            val selectedOption = findKeyByValue(outreachNameMap, outreachVal)
            val pattern = "yyyy-MM-dd'T'HH:mm:ssZ"
            val timeZone = TimeZone.getTimeZone("GMT+0530")
            val formatter = SimpleDateFormat(pattern, Locale.getDefault())
            formatter.timeZone = timeZone

            val timestamp = formatter.format(Date())

            if (myLocation != null) {
                latitude = myLocation!!.latitude
                longitude = myLocation!!.longitude
            }
            if (!isBiometric) {
                tryAuthUser = true

                viewModel.state.observe(viewLifecycleOwner) { state ->
                    when (state!!) {
                        OutreachViewModel.State.SUCCESS -> {
                            binding.patientListFragment.visibility = View.VISIBLE
                            binding.rlSaving.visibility = View.GONE
                            if (rememberUsername)
                                viewModel.rememberUser(userName, binding.etPassword.text.toString())
                            else {
                                viewModel.forgetUser()
                            }
                            lifecycleScope.launch {
                                viewModel.setOutreachDetails(
                                    "OUTREACH",
                                    outreachVal,
                                    timestamp,
                                    null,
                                    latitude,
                                    longitude,
                                    null,
                                    imageString,
                                    null
                                )
                                findNavController().navigate(
                                    ChoLoginFragmentDirections.actionSignInToHomeFromCho(true)
                                )
                                viewModel.resetState()
                                activity?.finish()
                            }
                        }

                        OutreachViewModel.State.SAVING -> {
                            binding.patientListFragment.visibility = View.GONE
                            binding.rlSaving.visibility = View.VISIBLE
                        }

                        OutreachViewModel.State.ERROR_SERVER,
                        OutreachViewModel.State.ERROR_NETWORK -> {
                            binding.patientListFragment.visibility = View.VISIBLE
                            binding.rlSaving.visibility = View.GONE
//                        viewModel.forgetUser()
                            viewModel.resetState()
                        }

                        else -> {}
                    }

                }
            } else {
                lifecycleScope.launch {
                    viewModel.setOutreachDetails(
                        "OUTREACH",
                        outreachVal,
                        timestamp,
                        null,
                        latitude,
                        longitude,
                        null,
                        imageString,
                        null
                    )
                    findNavController().navigate(
                        ChoLoginFragmentDirections.actionSignInToHomeFromCho(true)
                    )
                    viewModel.resetState()
                    activity?.finish()
                }
            }
        }
            else{
                Toast.makeText(context, "Please capture the image to continue.", Toast.LENGTH_SHORT).show()
            }
        }


    }
    private val onBackPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!isBiometric)
                    findNavController().navigateUp()
                else {
                    MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.logout))
                        .setMessage("Please confirm to logout and exit.")
                        .setPositiveButton(getString(R.string.select_yes)) { dialog, _ ->
                            lifecycleScope.launch {
                                val user = userDao.getLoggedInUser()
                                userDao.resetAllUsersLoggedInState()
                                if (user != null) {
                                    userDao.updateLogoutTime(user.userId, Date())
                                }
                            }
                            requireActivity().finish()
                            dialog.dismiss()
                        }.setNegativeButton(getString(R.string.select_no)) { dialog, _ ->
                            dialog.dismiss()
                        }.create()
                        .show()
                }
            }
        }
    }
    private fun getCurrentLocation() {
        // Check if location permissions are granted
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

            //  Location listener
            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    myLocation = location
                    if (tryAuthUser) {
                        val outreachVal = binding.outreachText.text.toString()
                        val selectedOption = findKeyByValue(outreachNameMap, outreachVal)
                        val pattern = "yyyy-MM-dd'T'HH:mm:ssZ"
                        val timeZone = TimeZone.getTimeZone("GMT+0530")
                        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
                        formatter.timeZone = timeZone

                        val timestamp = formatter.format(Date())
                        viewModel.authUser(
                            userName,
                            binding.etPassword.text.toString(),
                            "OUTREACH",
                            outreachVal,
                            timestamp,
                            null,
                            latitude,
                            longitude,
                            null,
                            imageString,
                            requireContext()
                        )
                    }

                }

                override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

                override fun onProviderEnabled(provider: String) {}

                override fun onProviderDisabled(provider: String) {
                    Toast.makeText(
                        context, "Location Provider/GPS disabled",
                        Toast.LENGTH_SHORT
                    ).show()
                    val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(settingsIntent)
                }
            }

            // Request location updates
            locationManager?.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0,
                0f,
                locationListener!!
            )
        } else {
            // Request location permissions if not granted
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSION_REQUEST_CODE
            )
        }
    }


    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
        private const val MIN_TIME_BETWEEN_UPDATES: Long = 1000 // 1 second
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 10f // 10 meters
    }


}


