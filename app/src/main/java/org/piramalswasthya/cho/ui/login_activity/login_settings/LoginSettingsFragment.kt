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
import android.util.Log
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.async
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.dropdown_adapters.BlockAdapter
import org.piramalswasthya.cho.adapter.dropdown_adapters.DistrictAdapter
import org.piramalswasthya.cho.adapter.dropdown_adapters.StatesAdapter
import org.piramalswasthya.cho.adapter.dropdown_adapters.VillageAdapter
import org.piramalswasthya.cho.database.room.dao.StateMasterDao
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.BlockMaster
import org.piramalswasthya.cho.model.DistrictMaster
import org.piramalswasthya.cho.model.LoginSettingsData
import org.piramalswasthya.cho.model.StateMaster
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.model.VillageMaster
import org.piramalswasthya.cho.network.NetworkResult
import org.piramalswasthya.cho.network.StateList
import org.piramalswasthya.cho.repositories.BlockMasterRepo
import org.piramalswasthya.cho.repositories.DistrictMasterRepo
import org.piramalswasthya.cho.repositories.LoginSettingsDataRepository
import org.piramalswasthya.cho.repositories.StateMasterRepo
import org.piramalswasthya.cho.repositories.VillageMasterRepo
import org.piramalswasthya.cho.ui.commons.fhir_add_patient.FhirAddPatientViewModel
import org.piramalswasthya.cho.ui.login_activity.cho_login.ChoLoginFragmentDirections
import org.piramalswasthya.cho.ui.login_activity.cho_login.outreach.OutreachViewModel


@AndroidEntryPoint
class LoginSettingsFragment : Fragment() {

    @Inject
    lateinit var loginSettingsDataRepository: LoginSettingsDataRepository

    private val binding by lazy{
        FragmentLoginSettingBinding.inflate(layoutInflater)
    }


    private var myLocation: Location? = null
    private var locationManager: LocationManager? = null

    private var locationListener: LocationListener? = null
    @Inject
    lateinit var preferenceDao: PreferenceDao


    private lateinit var viewModel: LoginSettingsViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getCurrentLocation()
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val userName = (arguments?.getString("userName", ""))!!;
        viewModel = ViewModelProvider(this).get(LoginSettingsViewModel::class.java)

        binding.dropdownState.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectedState = viewModel.stateList[position]
            viewModel.updateUserStateId(viewModel.selectedState!!.stateID)
            viewModel.fetchDistricts(viewModel.selectedState!!.stateID)
            binding.dropdownState.setText(viewModel.selectedState!!.stateName,false)
        }

        binding.dropdownDist.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectedDistrict = viewModel.districtList[position]
            viewModel.updateUserDistrictId(viewModel.selectedDistrict!!.districtID)
            viewModel.fetchTaluks(viewModel.selectedDistrict!!.districtID)
            binding.dropdownDist.setText(viewModel.selectedDistrict!!.districtName,false)
        }

        binding.dropdownTaluk.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectedBlock = viewModel.blockList[position]
            viewModel.updateUserBlockId(viewModel.selectedBlock!!.blockID)
            viewModel.fetchVillageMaster(viewModel.selectedBlock!!.blockID)
            binding.dropdownTaluk.setText(viewModel.selectedBlock!!.blockName,false)
        }

        binding.dropdownPanchayat.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectedVillage = viewModel.villageList[position]
            viewModel.updateUserVillageId(viewModel.selectedVillage!!.districtBranchID)
            binding.dropdownPanchayat.setText(viewModel.selectedVillage!!.villageName,false)
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                LoginSettingsViewModel.NetworkState.SUCCESS -> {
                    val statesAdapter = StatesAdapter(requireContext(), R.layout.drop_down, viewModel.stateList, binding.dropdownState)
                    binding.dropdownState.setAdapter(statesAdapter)

                    if(viewModel.selectedState != null){
                        binding.dropdownState.setText(viewModel.selectedState!!.stateName, false)
                    }
                    else{
                        binding.dropdownState.setText("", false)
                    }
                }
                else -> {}
            }
        }

        viewModel.district.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                LoginSettingsViewModel.NetworkState.SUCCESS -> {
                    val districtAdapter = DistrictAdapter(requireContext(), R.layout.drop_down, viewModel.districtList, binding.dropdownDist)
                    binding.dropdownDist.setAdapter(districtAdapter)

                    if(viewModel.selectedDistrict != null){
                        binding.dropdownDist.setText(viewModel.selectedDistrict!!.districtName, false)
                    }
                    else{
                        binding.dropdownDist.setText("", false)
                    }
                }
                else -> {}
            }
        }

        viewModel.block.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                LoginSettingsViewModel.NetworkState.SUCCESS -> {
                    val blockAdapter = BlockAdapter(requireContext(), R.layout.drop_down, viewModel.blockList, binding.dropdownTaluk)
                    binding.dropdownTaluk.setAdapter(blockAdapter)

                    if(viewModel.selectedBlock != null){
                        binding.dropdownTaluk.setText(viewModel.selectedBlock!!.blockName, false)
                    }
                    else{
                        binding.dropdownTaluk.setText("", false)
                    }
                }
                else -> {}
            }
        }

        viewModel.village.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                LoginSettingsViewModel.NetworkState.SUCCESS -> {
                    val villageAdapter = VillageAdapter(requireContext(), R.layout.drop_down, viewModel.villageList, binding.dropdownPanchayat)
                    binding.dropdownPanchayat.setAdapter(villageAdapter)

                    if(viewModel.selectedVillage != null){
                        binding.dropdownPanchayat.setText(viewModel.selectedVillage!!.villageName, false)
                    }
                    else{
                        binding.dropdownPanchayat.setText("", false)
                    }
                }
                else -> {}
            }
        }

//        binding.continueButton.setOnClickListener{
//            // Save Login Settings Data with location to preferences
////            val loginSettingsData = myLocation?.let { it1 ->
////                LoginSettingsData(selectedState,selectedDistrict,selectedBlock,selectedVillage, myLocation!!.longitude,
////                    myLocation!!.latitude,
////                    it1,userName
////                )
////            }
//////            if (loginSettingsData != null) {
//////                preferenceDao.saveLoginSettingsRecord(loginSettingsData)
//////                Toast.makeText(context,"Settings Saved to preference",Toast.LENGTH_SHORT).show()
//////                findNavController().navigate(
//////                    LoginSettingsFragmentDirections.actionLoginSettingsToLoginActivity()
//////                )
//////            }
////            if (loginSettingsData != null) {
////                lifecycleScope.launch {
////                    loginSettingsDataRepository.saveLoginSettingsData(loginSettingsData)
////                }
////                Toast.makeText(context,"Settings Saved to Repository",Toast.LENGTH_SHORT).show()
////                findNavController().navigate(
//////                    LoginSettingsFragmentDirections.actionLoginSettingsToChoLogin(userName)
////                    LoginSettingsFragmentDirections.actionLoginSettingsToUsername()
////                )
////            }
////            else{
////                Toast.makeText(context,"Error!! Check permissions",Toast.LENGTH_SHORT).show()
////                getCurrentLocation()
////            }
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
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