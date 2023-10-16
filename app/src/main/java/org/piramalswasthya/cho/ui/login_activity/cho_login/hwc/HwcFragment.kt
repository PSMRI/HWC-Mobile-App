package org.piramalswasthya.cho.ui.login_activity.cho_login.hwc

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentHwcBinding
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.ui.login_activity.cho_login.ChoLoginFragmentDirections
import org.piramalswasthya.cho.ui.login_activity.cho_login.outreach.OutreachViewModel
import org.piramalswasthya.cho.ui.master_location_settings.MasterLocationSettingsActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
@AndroidEntryPoint
class HwcFragment constructor(
    private val userName: String,
    private val rememberUsername: Boolean,
    private val isBiometric: Boolean,
    ): Fragment() {

    @Inject
    lateinit var prefDao: PreferenceDao
    @Inject
    lateinit var userDao: UserDao

    private var _binding: FragmentHwcBinding? = null
    private val binding: FragmentHwcBinding
        get() = _binding!!

    private lateinit var viewModel: HwcViewModel

    private var currentLocation: Location? = null
    private var myInitialLoc: Location? = null
    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var user : UserCache? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        getCurrentLocation()
        viewModel = ViewModelProvider(this).get(HwcViewModel::class.java)

        _binding = FragmentHwcBinding.inflate(layoutInflater, container, false)
        if (isBiometric) {
            binding.tilPasswordHwc.visibility = View.GONE
            binding.btnHwcLogin.text = "Proceed to Home"
        } else {
            binding.tilPasswordHwc.visibility = View.VISIBLE
            if (!viewModel.fetchRememberedPassword().isNullOrBlank()) {
            viewModel.fetchRememberedPassword()?.let {
                binding.etPasswordHwc.setText(it)
            }
        }
    }
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, onBackPressedCallback)

        val pattern = "yyyy-MM-dd'T'HH:mm:ssZ"
        val timeZone = TimeZone.getTimeZone("GMT+0530")
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        formatter.timeZone = timeZone
        val timestamp = formatter.format(Date())

            binding.btnHwcLogin.setOnClickListener {
                lifecycleScope.launch {
                    user = userDao.getLoggedInUser()
                }
                if (!isBiometric) {
                    viewModel.authUser(
                        userName,
                        binding.etPasswordHwc.text.toString(),
                        "HWC",
                        null,
                        timestamp,
                        null,
                        null,
                        null,
                        null,
                    )

                    viewModel.state.observe(viewLifecycleOwner) { state ->
                        when (state!!) {

                            OutreachViewModel.State.SUCCESS -> {
                                binding.patientListFragment.visibility = View.VISIBLE
                                binding.rlSaving.visibility = View.GONE

                                if (rememberUsername)
                                    viewModel.rememberUser(
                                        userName,
                                        binding.etPasswordHwc.text.toString()
                                    )
                                else {
                                    viewModel.forgetUser()
                                }
                                if(user?.districtBranchID != null && user?.masterLatitude != null && user?.masterLongitude != null){
                                    val distance = calculateDistance(user?.masterLatitude!!,user?.masterLongitude!!,
                                        currentLocation?.latitude!!, currentLocation?.longitude!!)
                                    if(distance > 500){
                                        showDialog()
                                    }
                                    else
                                        findNavController().navigate(
                                            ChoLoginFragmentDirections.actionSignInToHomeFromCho(true)
                                        )
                                    //TODO COMPARE WITH CURRENT LOCATION
                                }else{
                                    //TODO OPEN SETTINGS PAGE
                                    startActivity(Intent(context, MasterLocationSettingsActivity::class.java))

                                }
                                viewModel.resetState()
                                activity?.finish()
                            }

                            OutreachViewModel.State.SAVING -> {
                                binding.patientListFragment.visibility = View.GONE
                                binding.rlSaving.visibility = View.VISIBLE
                            }

                            OutreachViewModel.State.ERROR_SERVER,
                            OutreachViewModel.State.ERROR_NETWORK -> {
                                binding.patientListFragment.visibility = View.VISIBLE
                                binding.rlSaving.visibility = View.GONE
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.error_while_logging_in),
                                    Toast.LENGTH_LONG
                                ).show()
//                        viewModel.forgetUser()
                                viewModel.resetState()
                            }

                            else -> {}
                        }

                    }
                }
                else{
                lifecycleScope.launch {
                    viewModel.setOutreachDetails(
                        "HWC",
                        null,
                        timestamp,
                        null,
                        null,
                        null,
                        null,
                    )
                    if(user?.districtBranchID != null && user?.masterLatitude != null && user?.masterLongitude != null){
                        val distance = calculateDistance(user?.masterLatitude!!,user?.masterLongitude!!,
                            currentLocation?.latitude!!, currentLocation?.longitude!!)
                        if(distance > 500){
                            showDialog()
                        }
                        else
                            findNavController().navigate(
                                ChoLoginFragmentDirections.actionSignInToHomeFromCho(true)
                            )
                        //TODO COMPARE WITH CURRENT LOCATION
                    }
                    viewModel.resetState()
                    activity?.finish()
                }
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
                    currentLocation = location

//                        val distance = calculateDistance(
//                        17.0, //TODO GET MASTER VILLAGE LOCATION COORDINATES
//                        -122.0,
//                        location.latitude,
//                        location.longitude
//                    )

                    // Check if the user has moved more than 500 meters
//                    if (distance > 500) {
                        // Show the dialog to ask for an update
//                        showDialog()
//                        Toast.makeText(activity,"Value of distance $distance and location is ${location.longitude} and ${location.latitude}",Toast.LENGTH_LONG).show()
//                    }

                    // Stop listening for location updates once you have the current location
//                    locationManager?.removeUpdates(this)
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
    private fun showDialog() {
        val alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder.setMessage("Your current location is 400 meters away from your assigned village.")
            .setTitle("Alert!")
            .setCancelable(false)
            .setPositiveButton("OK") { d, _ ->
                d.dismiss()
                findNavController().navigate(
                    ChoLoginFragmentDirections.actionSignInToHomeFromCho(true)
                )
            }.create()
            .show()
    }
    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
        private const val MIN_TIME_BETWEEN_UPDATES: Long = 1000 // 1 second
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 10f // 10 meters
    }
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
////        binding.btnHwcLogin.setOnClickListener {
////            findNavController().navigate(
////                ChoLoginFragmentDirections.actionSignInToHomeFromCho()
////            )
////        }
//        // TODO: Use the ViewModel
//    }

}