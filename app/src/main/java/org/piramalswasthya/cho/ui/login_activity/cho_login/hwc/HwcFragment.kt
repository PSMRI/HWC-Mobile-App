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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentHwcBinding
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.repositories.UserRepo
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
    @Inject
    lateinit var userRepo: UserRepo

    private var _binding: FragmentHwcBinding? = null
    private val binding: FragmentHwcBinding
        get() = _binding!!

    private lateinit var viewModel: HwcViewModel

    var userLatitude :Double? = null
    var userLongitude :Double? = null
    var userLoginDistance :Int? = null
    var currentLatitude :Double? = null
    var currentLongitude :Double? = null

    private var currentLocation: Location? = null
    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        getCurrentLocation()
        viewModel = ViewModelProvider(this).get(HwcViewModel::class.java)

        _binding = FragmentHwcBinding.inflate(layoutInflater, container, false)
        if (isBiometric) {
            binding.tilPasswordHwc.visibility = View.GONE
            binding.btnHwcLogin.text = "Proceed to Home"
//            getCurrentLocation()
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
//        getCurrentLocation()

        val pattern = "yyyy-MM-dd'T'HH:mm:ssZ"
        val timeZone = TimeZone.getTimeZone("GMT+0530")
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        formatter.timeZone = timeZone
        val timestamp = formatter.format(Date())

            binding.btnHwcLogin.setOnClickListener {
                getCurrentLocation()

                if (!isBiometric) {
                    lifecycleScope.launch {
                        viewModel.authUser(
                            userName,
                            binding.etPasswordHwc.text.toString(),
                            "HWC",
                            null,
                            timestamp,
                            null,
                            currentLocation?.latitude,
                            currentLocation?.longitude,
                            null,
                        )
                    }

                    viewModel.state.observe(viewLifecycleOwner) { state ->
                        when (state!!) {

                            OutreachViewModel.State.SUCCESS -> {

                                lifecycleScope.launch {
                                   val user = userDao.getLoggedInUser()
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

                                userLatitude = user?.masterLatitude
                                userLongitude = user?.masterLongitude
                                userLoginDistance = user?.loginDistance
                                currentLatitude = currentLocation?.latitude
                                currentLongitude = currentLocation?.longitude

                                if (user?.masterVillageID != null && userLatitude != null && userLongitude != null) {
                                    if (currentLatitude != null && currentLongitude != null) {
                                        val distance = calculateDistance(
                                            userLatitude!!, userLongitude!!,
                                            currentLatitude!!, currentLongitude!!
                                        )
                                        if (distance > userLoginDistance!!) {
                                            showDialog(distance)
                                        } else {
//                                            Toast.makeText(
//                                                context,
//                                                "distance $distance",
//                                                Toast.LENGTH_LONG
//                                            ).show()
                                            findNavController().navigate(
                                                ChoLoginFragmentDirections.actionSignInToHomeFromCho(
                                                    true
                                                )
                                            )
                                            viewModel.resetState()
                                            activity?.finish()
                                        }

                                    } else {
                                        //TODO
                                        Toast.makeText(
                                            context,
                                            "Unable to verify location",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        findNavController().navigate(
                                            ChoLoginFragmentDirections.actionSignInToHomeFromCho(
                                                true
                                            )
                                        )
                                        viewModel.resetState()
                                        activity?.finish()
                                    }

                                } else {
                                    // OPEN SETTINGS PAGE
                                    startActivity(
                                        Intent(
                                            context,
                                            MasterLocationSettingsActivity::class.java
                                        )
                                    )
                                    viewModel.resetState()
                                    activity?.finish()
                                }
//                                viewModel.resetState()
//                                activity?.finish()
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
                        currentLocation?.latitude,
                        currentLocation?.longitude,
                        null,
                    )
                    val user = userDao.getLoggedInUser()
                    userLatitude = user?.masterLatitude
                     userLongitude = user?.masterLongitude
                     userLoginDistance = user?.loginDistance
                     currentLatitude = currentLocation?.latitude
                     currentLongitude = currentLocation?.longitude

                    if(user?.masterVillageID != null && userLatitude != null && userLongitude != null){

                        if(currentLatitude!=null && currentLongitude!=null) {
                            val distance = calculateDistance(
                                userLatitude!!, userLongitude!!,
                                currentLatitude!!, currentLongitude!!
                            )
                            if (distance > userLoginDistance!!) {
                                showDialog(distance)
                            }
                            else {
                                Toast.makeText(
                                    context,
                                    "distance $distance",
                                    Toast.LENGTH_LONG
                                ).show() //TODO REMOVE THIS TOAST
                                findNavController().navigate(
                                    ChoLoginFragmentDirections.actionSignInToHomeFromCho(
                                        true
                                    )
                                )
                                viewModel.resetState()
                                activity?.finish()
                            }
                        }
                        else {
                            Toast.makeText(
                                context,
                                "Unable to verify location",
                                Toast.LENGTH_LONG
                            ).show()
                            findNavController().navigate(
                                ChoLoginFragmentDirections.actionSignInToHomeFromCho(
                                    true
                                )
                            )
                            viewModel.resetState()
                            activity?.finish()
                        }
                        //TODO COMPARE WITH CURRENT LOCATION
                    }
//                    viewModel.resetState()
//                    activity?.finish()
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
    private fun showDialog(distance:Float) {
        val alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder.setMessage("Your current location is $distance meters away from your assigned village.")
            .setTitle("Alert!")
            .setCancelable(false)
            .setPositiveButton("OK") { d, _ ->
                d.dismiss()
                findNavController().navigate(
                    ChoLoginFragmentDirections.actionSignInToHomeFromCho(true)
                )
                viewModel.resetState()
                activity?.finish()
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

    override fun onResume() {
        super.onResume()
//        getCurrentLocation()
    }

}