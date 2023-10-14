package org.piramalswasthya.cho.ui.login_activity.login_settings

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
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
import org.piramalswasthya.cho.adapter.dropdown_adapters.DropdownAdapter
import org.piramalswasthya.cho.adapter.dropdown_adapters.StatesAdapter
import org.piramalswasthya.cho.adapter.dropdown_adapters.VillageAdapter
import org.piramalswasthya.cho.adapter.model.DropdownList
import org.piramalswasthya.cho.database.room.dao.StateMasterDao
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.fhir_utils.extension_names.district
import org.piramalswasthya.cho.model.BlockMaster
import org.piramalswasthya.cho.model.DistrictMaster
import org.piramalswasthya.cho.model.LoginSettingsData
import org.piramalswasthya.cho.model.StateMaster
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.model.UserMasterLocation
import org.piramalswasthya.cho.model.VillageMaster
import org.piramalswasthya.cho.network.NetworkResult
import org.piramalswasthya.cho.network.StateList
import org.piramalswasthya.cho.repositories.BlockMasterRepo
import org.piramalswasthya.cho.repositories.DistrictMasterRepo
import org.piramalswasthya.cho.repositories.LoginSettingsDataRepository
import org.piramalswasthya.cho.repositories.StateMasterRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.repositories.VillageMasterRepo
import org.piramalswasthya.cho.ui.commons.fhir_add_patient.FhirAddPatientViewModel
import org.piramalswasthya.cho.ui.login_activity.cho_login.ChoLoginFragmentDirections
import org.piramalswasthya.cho.ui.login_activity.cho_login.outreach.OutreachViewModel
import java.lang.Math.sin
import kotlin.math.*


@AndroidEntryPoint
class LoginSettingsFragment : Fragment() {

    @Inject
    lateinit var loginSettingsDataRepository: LoginSettingsDataRepository

    private val binding by lazy{
        FragmentLoginSettingBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var userDao: UserDao
    @Inject
    lateinit var userRepo: UserRepo
    private var user : UserCache? = null
    private var myLocation: Location? = null
    private var myInitialLoc: Location? = null
    private var locationManager: LocationManager? = null

    private var locationListener: LocationListener? = null
    @Inject
    lateinit var preferenceDao: PreferenceDao


    private lateinit var viewModel: LoginSettingsViewModel

    private var stateBool = false
    private var distBool = false
    private var talukBool = false
    private var pancBool = false
    private var dialogClosed = false;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getCurrentLocation()
        dialogClosed = false
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        val userName = (arguments?.getString("userName", ""))!!
        viewModel = ViewModelProvider(this)[LoginSettingsViewModel::class.java]

//        binding.dropdownState.setOnItemClickListener { _, _, position, _ ->
//            viewModel.selectedState = viewModel.stateList[position]
//            viewModel.updateUserStateId(viewModel.selectedState!!.stateID)
//            viewModel.fetchDistricts(viewModel.selectedState!!.stateID)
//            binding.dropdownState.setText(viewModel.selectedState!!.stateName,false)
//        }
//
//        binding.dropdownDist.setOnItemClickListener { _, _, position, _ ->
//            viewModel.selectedDistrict = viewModel.districtList[position]
//            viewModel.updateUserDistrictId(viewModel.selectedDistrict!!.districtID)
//            viewModel.fetchTaluks(viewModel.selectedDistrict!!.districtID)
//            binding.dropdownDist.setText(viewModel.selectedDistrict!!.districtName,false)
//        }
//
//        binding.dropdownTaluk.setOnItemClickListener { _, _, position, _ ->
//            viewModel.selectedBlock = viewModel.blockList[position]
//            viewModel.updateUserBlockId(viewModel.selectedBlock!!.blockID)
//            viewModel.fetchVillageMaster(viewModel.selectedBlock!!.blockID)
//            binding.dropdownTaluk.setText(viewModel.selectedBlock!!.blockName,false)
//        }

        binding.dropdownPanchayat.setOnItemClickListener { _, _, position, _ ->
//            viewModel.selectedVillage = viewModel.villageList[position]
            viewModel.userMasterVillage = viewModel.masterVillageList[position]
            viewModel.updateUserVillageId(viewModel.userMasterVillage?.districtBranchID!!.toInt())
            binding.dropdownPanchayat.setText(viewModel.userMasterVillage?.villageName,false)
        }

//        viewModel.state.observe(viewLifecycleOwner) { state ->
//            when (state!!) {
//                LoginSettingsViewModel.NetworkState.SUCCESS -> {
//                    val statesAdapter = StatesAdapter(requireContext(), R.layout.drop_down, viewModel.stateList, binding.dropdownState)
//                    binding.dropdownState.setAdapter(statesAdapter)
//
//                    if(viewModel.selectedState != null){
//                        binding.dropdownState.setText(viewModel.selectedState!!.stateName, false)
//                    }
//                    else{
//                        binding.dropdownState.setText("", false)
//                    }
//                }
//                else -> {}
//            }
//        }
//
//        viewModel.district.observe(viewLifecycleOwner) { state ->
//            when (state!!) {
//                LoginSettingsViewModel.NetworkState.SUCCESS -> {
//                    val districtAdapter = DistrictAdapter(requireContext(), R.layout.drop_down, viewModel.districtList, binding.dropdownDist)
//                    binding.dropdownDist.setAdapter(districtAdapter)
//
//                    if(viewModel.selectedDistrict != null){
//                        binding.dropdownDist.setText(viewModel.selectedDistrict!!.districtName, false)
//                    }
//                    else{
//                        binding.dropdownDist.setText("", false)
//                    }
//                }
//                else -> {}
//            }
//        }
//
//        viewModel.block.observe(viewLifecycleOwner) { state ->
//            when (state!!) {
//                LoginSettingsViewModel.NetworkState.SUCCESS -> {
//                    val blockAdapter = BlockAdapter(requireContext(), R.layout.drop_down, viewModel.blockList, binding.dropdownTaluk)
//                    binding.dropdownTaluk.setAdapter(blockAdapter)
//
//                    if(viewModel.selectedBlock != null){
//                        binding.dropdownTaluk.setText(viewModel.selectedBlock!!.blockName, false)
//                    }
//                    else{
//                        binding.dropdownTaluk.setText("", false)
//                    }
//                }
//                else -> {}
//            }
//        }

        viewModel.locationMaster.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                LoginSettingsViewModel.NetworkState.SUCCESS -> {
                    binding.dropdownState.setText(viewModel.masterState, false)
                    binding.dropdownDist.setText(viewModel.masterDistrict, false)
                    binding.dropdownTaluk.setText(viewModel.masterBlock, false)
//                    val villageAdapter = VillageAdapter(requireContext(), R.layout.drop_down, viewModel.villageList, binding.dropdownPanchayat)
//                    binding.dropdownPanchayat.setAdapter(villageAdapter)
                    val dropdownList = viewModel.masterVillageList.map { it -> DropdownList(it.districtBranchID.toInt(), it.villageName) }
                    val dropdownAdapter = DropdownAdapter(requireContext(), R.layout.drop_down, dropdownList, binding.dropdownPanchayat)
                    binding.dropdownPanchayat.setAdapter(dropdownAdapter)
                    if(viewModel.masterVillageId != null){
                        binding.dropdownPanchayat.setText(viewModel.userMasterVillage?.villageName, false)
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

        if(myInitialLoc != null){
            Log.i("Current Location is","${myInitialLoc?.longitude}")
            binding.inputMasterLong.setText(myInitialLoc?.longitude.toString())
            binding.inputMasterLat.setText(myInitialLoc?.latitude.toString())
        }

        binding.getGPSLoc.setOnClickListener {
            getCurrentLocation()
        }
//        binding.enrollFpScreen.setOnClickListener {
//            findNavController().navigate(
//                LoginSettingsFragmentDirections.actionLoginSettingsFragmentToFingerPrintRegisterFragment(userName)
//            )
//        }

        binding.retry.setOnClickListener {
            resetData()
        }
        binding.submit.setOnClickListener {
            getCurrentLocation()
            if(!binding.inputMasterLat.text.isNullOrEmpty() && !binding.inputMasterLong.text.isNullOrEmpty())
                submitLocationData()
            else
                Toast.makeText(activity,"Please wait while master location coordinates are fetched.",Toast.LENGTH_LONG).show()

        }
    }

    private fun resetData(){
        binding.dropdownState.setText("",false)
        binding.dropdownDist.setText("",false)
        binding.dropdownTaluk.setText("",false)
        binding.dropdownPanchayat.setText("",false)
        binding.inputMasterLat.setText("")
        binding.inputMasterLong.setText("")
    }

    private fun submitLocationData() {
        if (binding.dropdownState.text.isNullOrEmpty()) {
            binding.dropdownState.requestFocus()
            binding.state.apply {
                boxStrokeColor = Color.RED
                hintTextColor = ColorStateList.valueOf(Color.RED)
            }
            stateBool = false
        } else {
            binding.state.apply {
                boxStrokeColor = resources.getColor(R.color.purple)
                hintTextColor = defaultHintTextColor
            }
            stateBool = true
        }

        if (binding.dropdownDist.text.isNullOrEmpty()) {
            binding.dropdownDist.requestFocus()
            binding.dist.apply {
                boxStrokeColor = Color.RED
                hintTextColor = ColorStateList.valueOf(Color.RED)
            }
            distBool = false
        } else {
            binding.dist.apply {
                boxStrokeColor = resources.getColor(R.color.purple)
                hintTextColor = defaultHintTextColor
            }
            distBool = true
        }

        if (binding.dropdownTaluk.text.isNullOrEmpty()) {
            binding.dropdownTaluk.requestFocus()
            binding.taluk.apply {
                boxStrokeColor = Color.RED
                hintTextColor = ColorStateList.valueOf(Color.RED)
            }
            talukBool = false
        } else {
            binding.taluk.apply {
                boxStrokeColor = resources.getColor(R.color.purple)
                hintTextColor = defaultHintTextColor
            }
            talukBool = true
        }

        if (binding.dropdownPanchayat.text.isNullOrEmpty()) {
            binding.dropdownPanchayat.requestFocus()
            binding.panchayat.apply {
                boxStrokeColor = Color.RED
                hintTextColor = ColorStateList.valueOf(Color.RED)
            }
            pancBool = false
        } else {
            binding.panchayat.apply {
                boxStrokeColor = resources.getColor(R.color.purple)
                hintTextColor = defaultHintTextColor
            }
            pancBool = true
        }

        if(stateBool && distBool && talukBool && pancBool){
            lifecycleScope.launch {

            val userMasterLocation = UserMasterLocation(
                userId = viewModel.userInfo?.userId,
                stateId = viewModel.masterStateId,
                stateName = viewModel.masterState,
                districtId =  viewModel.masterDistrictId,
                districtName=viewModel.masterDistrict,
                blockId= viewModel.masterBlockId,
                blockName=  viewModel.masterBlock,
                villageId = viewModel.userMasterVillage?.districtBranchID?.toInt(),
            villageName = viewModel.userMasterVillage?.villageName,
            masterLatitude = myLocation?.latitude,
            masterLongitude = myLocation?.longitude)
                userRepo.setUserMasterLoc(userMasterLocation)
                Toast.makeText(activity,"Data is Saved!",Toast.LENGTH_SHORT).show()
                requireActivity().finish()
            }
        } else {
            Toast.makeText(activity,"Please fill all the details",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager!!.removeUpdates(locationListener!!)
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
                    Log.i("Location","$myLocation")
                    if(myInitialLoc == null && myLocation != null) {
                        myInitialLoc = myLocation
                        binding.inputMasterLong.setText(myInitialLoc?.longitude.toString())
                        binding.inputMasterLat.setText(myInitialLoc?.latitude.toString())
                        Log.i("Initial Location","$myInitialLoc")
                    } else if(myInitialLoc != null) {
                        val distance = calculateDistance(
                            myInitialLoc!!.latitude,
                            myInitialLoc!!.longitude,
                            location.latitude,
                            location.longitude
                        )

                        Log.i("Calculated Distance is ", "$distance")
                        // Check if the user has moved more than 500 meters
                        if (distance > 500 && !dialogClosed) {
                            showDialog()
                            dialogClosed = true
                        }
                    }
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

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }
    private fun showDialog() {
        var alertDialog: AlertDialog? = null
        val alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder.setMessage("You have moved more than 500 meters from the fixed point. Do you want to update your location?")
//            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                // Handle the user's choice to update location here
                // You can perform any necessary actions when the user selects "Yes"
//                myInitialLoc = null
//                getCurrentLocation()
                alertDialog!!.dismiss()
            }
//            .setNegativeButton("No") { _, _ ->
//               alertDialog!!.dismiss()
//            }

        alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }


    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
        private const val MIN_TIME_BETWEEN_UPDATES: Long = 1000 // 1 second
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 500f // 10 meters
    }
}