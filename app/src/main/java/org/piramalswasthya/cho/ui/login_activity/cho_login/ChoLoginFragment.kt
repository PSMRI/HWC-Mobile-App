package org.piramalswasthya.cho.ui.login_activity.cho_login
import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentChoLoginBinding
import org.piramalswasthya.cho.repositories.LoginSettingsDataRepository
import org.piramalswasthya.cho.ui.login_activity.cho_login.hwc.HwcFragment
import org.piramalswasthya.cho.ui.login_activity.cho_login.outreach.OutreachFragment
import javax.inject.Inject


@AndroidEntryPoint
class ChoLoginFragment : Fragment() {

    @Inject
    lateinit var prefDao: PreferenceDao
    @Inject
    lateinit var loginSettingsDataRepository: LoginSettingsDataRepository
    private var _binding: FragmentChoLoginBinding? = null
    private val viewModel: ChoLoginViewModel by viewModels()

    private val binding: FragmentChoLoginBinding
        get() = _binding!!


    private var myLocation: Location? = null
    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null

    private var userName: String = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChoLoginBinding.inflate(layoutInflater, container, false)
//        getCurrentLocation()
        return binding.root
    }

    private fun setActivityContainer(programId: Int){
        userName = (arguments?.getString("userName", ""))!!;
        val rememberUsername:Boolean = (arguments?.getBoolean("rememberUsername"))!!
        val isBiometric:Boolean = (arguments?.getBoolean("isBiometric"))!!
        val fragmentManager : FragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction : FragmentTransaction = fragmentManager.beginTransaction()
        val hwcFragment  = HwcFragment(userName,rememberUsername,isBiometric)
        val outreachFragment = OutreachFragment(userName,rememberUsername,isBiometric);
        when (programId){
            binding.btnHwc.id -> {
                fragmentTransaction.replace(binding.selectActivityContainer.id, hwcFragment)
                fragmentTransaction.commit()
            }
            binding.btnOutreach.id -> {
                fragmentTransaction.replace(binding.selectActivityContainer.id, outreachFragment)
                fragmentTransaction.commit()
            }
            else -> {
                fragmentTransaction.replace(binding.selectActivityContainer.id, hwcFragment)
                fragmentTransaction.commit()
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        getCurrentLocation()
        setActivityContainer(binding.selectProgram.id)
        binding.selectProgram.setOnCheckedChangeListener { _, programId ->
            setActivityContainer(programId)
        }


    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
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
    private fun showDialog() {
        val alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder.setMessage("You have moved more than 2 meters from the fixed point. Do you want to update your location?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                // Handle the user's choice to update location here
                // You can perform any necessary actions when the user selects "Yes"
            }
            .setNegativeButton("No") { _, _ ->
                // Handle the user's choice not to update location here
                // You can perform any necessary actions when the user selects "No"
            }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
        private const val MIN_TIME_BETWEEN_UPDATES: Long = 1000 // 1 second
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 10f // 10 meters
    }
}