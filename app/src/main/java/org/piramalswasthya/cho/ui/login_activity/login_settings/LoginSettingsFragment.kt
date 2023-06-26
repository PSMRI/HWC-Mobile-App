package org.piramalswasthya.cho.ui.login_activity.login_settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.databinding.FragmentLoginSettingBinding
import org.piramalswasthya.cho.model.LocationRequest
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.District
import org.piramalswasthya.cho.network.DistrictBlock
import org.piramalswasthya.cho.network.State
import org.piramalswasthya.cho.network.Village
import timber.log.Timber
import javax.inject.Inject
import android.provider.Settings
import androidx.lifecycle.lifecycleScope
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.LoginSettingsData
import org.piramalswasthya.cho.repositories.LoginSettingsDataRepository


@AndroidEntryPoint
class LoginSettingsFragment : Fragment() {

    @Inject
    lateinit var loginSettingsDataRepository: LoginSettingsDataRepository

    @Inject
    lateinit var apiService: AmritApiService
    private val binding by lazy{
        FragmentLoginSettingBinding.inflate(layoutInflater)
    }
    private var stateList: List<State>? = null
    private var selectedState: State? = null
    private var districtList: List<District>? = null
    private var selectedDistrict: District? = null
    private var blockList: List<DistrictBlock>? = null
    private var selectedBlock: DistrictBlock? = null
    private var villageList: List<Village>? = null
    private var selectedVillage: Village? = null
    private var myLocation: Location? = null
    private var locationManager: LocationManager? = null

    private var locationListener: LocationListener? = null
    @Inject
    lateinit var preferenceDao: PreferenceDao

    private val coroutineScope = CoroutineScope(Dispatchers.Main)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getCurrentLocation()
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fetchStates()
        val userName = (arguments?.getString("userName", ""))!!;
        binding.continueButton.setOnClickListener{
            // Save Login Settings Data with location to preferences
            val loginSettingsData = myLocation?.let { it1 ->
                LoginSettingsData(selectedState,selectedDistrict,selectedBlock,selectedVillage, myLocation!!.longitude,
                    myLocation!!.latitude,
                    it1,userName
                )
            }
//            if (loginSettingsData != null) {
//                preferenceDao.saveLoginSettingsRecord(loginSettingsData)
//                Toast.makeText(context,"Settings Saved to preference",Toast.LENGTH_SHORT).show()
//                findNavController().navigate(
//                    LoginSettingsFragmentDirections.actionLoginSettingsToLoginActivity()
//                )
//            }
            if (loginSettingsData != null) {
                lifecycleScope.launch {
                    loginSettingsDataRepository.saveLoginSettingsData(loginSettingsData)
                }
                Toast.makeText(context,"Settings Saved to Repository",Toast.LENGTH_SHORT).show()
                findNavController().navigate(
                    LoginSettingsFragmentDirections.actionLoginSettingsToChoLogin(userName)
                )
            }
            else{
                Toast.makeText(context,"Error!! Check permissions",Toast.LENGTH_SHORT).show()
                getCurrentLocation()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel() // Cancel the coroutine scope when the fragment is destroyed
    }
    private fun fetchStates() {
        coroutineScope.launch {
            try {
                val request = LocationRequest(vanID = 153, spPSMID = "64")
                val stateData = apiService.getStates(request)
                if (stateData != null){
                    stateList = stateData.data.stateMaster
                    val stateNames = stateList!!.map { it.stateName }.toTypedArray()
                    binding.dropdownState.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, stateNames)
                    binding.dropdownState.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            val selectedState = binding.dropdownState.adapter.getItem(position) as String

                            // Fetch districts based on the selected state ID
                            fetchDistricts(selectedState)
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            // Do nothing
                        }
                    }
                }

            } catch (e: Exception) {
                Timber.d("Fetching states failed ${e.message}")
            }
        }
    }

    private fun fetchDistricts(selectedStateName: String) {

        coroutineScope.launch {
            try {
                // Find the selected state by name
                selectedState = stateList?.find { it.stateName == selectedStateName }

                // If the selected state is found, make the API call with its stateID
                selectedState?.let {
                    val stateId = it.stateID
                    val response = apiService.getDistricts(stateId)

                    if (response!=null) {
                        districtList = response?.data
                        val districtNames = districtList!!.map { it.districtName }.toTypedArray()
                        binding.dropdownDistrict.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, districtNames)
                        binding.dropdownDistrict.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                val selectedDistrict = binding.dropdownDistrict.adapter.getItem(position) as String
//                                 Fetch Taluks based on the selected value
                                fetchTaluks(selectedDistrict)
                            }
                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                // Do nothing
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                Timber.d("Fetching Districts failed ${e.message}")
            }
        }
    }

    private fun fetchTaluks(selectedDistrictName: String) {

        coroutineScope.launch {
            try {
                // Find the selected state by name
                selectedDistrict = districtList?.find { it.districtName == selectedDistrictName }

                // If the selected state is found, make the API call with its stateID
                selectedDistrict?.let {
                    val districtId = it.districtID
                    val response = apiService.getDistrictBlocks(districtId)

                    if (response!=null) {
                        blockList = response?.data
                        val blockNames = blockList!!.map { it.blockName }.toTypedArray()
                        binding.dropdownTaluk.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, blockNames)
                        binding.dropdownTaluk.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                val selectedBlock = binding.dropdownTaluk.adapter.getItem(position) as String
//                                 Fetch Village/Area/Street based on the selected value
                                fetchVillageMaster(selectedBlock)
                            }
                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                // Do nothing
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                Timber.d("Fetching Taluks failed ${e.message}")
            }
        }
    }

    private fun fetchVillageMaster(selectedBlockName: String) {

        coroutineScope.launch {
            try {
                // Find the selected state by name
                selectedBlock = blockList?.find { it.blockName == selectedBlockName }

                // If the selected state is found, make the API call with its stateID
                selectedBlock?.let {
                    val blockId = it.blockID
                    val response = apiService.getVillages(blockId)

                    if (response!=null) {
                        villageList = response?.data
                        val villageNames = villageList!!.map { it.villageName }.toTypedArray()
                        binding.dropdownStreet.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, villageNames)
                        binding.dropdownStreet.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                val selectedVillageName = binding.dropdownStreet.adapter.getItem(position) as String
//                                 Fetch Village/Area/Street based on the selected value

                                selectedVillage = villageList?.find { it.villageName == selectedVillageName }

                            }
                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                // Do nothing
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                Timber.d("Fetching villages failed ${e.message}")
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
//                    val formattedLocation = "Latitude: ${location.latitude}\nLongitude: ${location.latitude}"
//                    Toast.makeText(
//                        context, formattedLocation,
//                        Toast.LENGTH_SHORT
//                    ).show()
                    // Stop listening for location updates once you have the current location
                    locationManager?.removeUpdates(this)
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
                MIN_TIME_BETWEEN_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES,
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

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
        private const val MIN_TIME_BETWEEN_UPDATES: Long = 1000 // 1 second
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 10f // 10 meters
    }
}