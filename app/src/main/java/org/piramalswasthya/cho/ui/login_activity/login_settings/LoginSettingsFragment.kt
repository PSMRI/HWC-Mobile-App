package org.piramalswasthya.cho.ui.login_activity.login_settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.dropdown_adapters.DropdownAdapter
import org.piramalswasthya.cho.adapter.model.DropdownList
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentLoginSettingBinding
import org.piramalswasthya.cho.model.MasterLocationModel
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.model.UserMasterVillage
import org.piramalswasthya.cho.repositories.LoginSettingsDataRepository
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import javax.inject.Inject


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
    private val onBackPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        val userName = (arguments?.getString("userName", ""))!!
        viewModel = ViewModelProvider(this)[LoginSettingsViewModel::class.java]
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, onBackPressedCallback)



        binding.dropdownPanchayat.setOnItemClickListener { _, _, position, _ ->
//            viewModel.selectedVillage = viewModel.villageList[position]
            viewModel.userMasterVillage = viewModel.masterVillageList[position]
            viewModel.updateUserVillageId(viewModel.userMasterVillage?.districtBranchID!!.toInt())
            binding.dropdownPanchayat.setText(viewModel.userMasterVillage?.villageName,false)
            pancBool = true
        }

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


        if(myInitialLoc != null){
            Log.i("Current Location is","${myInitialLoc?.longitude}")
            binding.inputMasterLong.setText(myInitialLoc?.longitude.toString())
            binding.inputMasterLat.setText(myInitialLoc?.latitude.toString())
        }

        binding.getGPSLoc.setOnClickListener {
            getCurrentLocation()
        }

        binding.submit.setOnClickListener {
            if(!binding.inputMasterLat.text.isNullOrEmpty() && !binding.inputMasterLong.text.isNullOrEmpty()){
                submitLocationData()
                val intent = Intent(context, HomeActivity::class.java)
                intent.putExtra("showDashboard", true)
                startActivity(intent)
                requireActivity().finish()
            }
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
                try {
                    val user = userDao.getLoggedInUser()
                    userRepo.updateVillageCoordinates(
                        MasterLocationModel(
                            myLocation?.latitude!!, myLocation?.longitude!!,
                            "addressGeo", viewModel.userMasterVillage?.districtBranchID?.toInt()
                        )
                    )
                    userRepo.setUserMasterVillageIdAndName(
                        user!!,
                        viewModel.userMasterVillage?.districtBranchID?.toInt(),
                        viewModel.userMasterVillage?.villageName,
                    )
                    userRepo.setUserMasterVillage(
                        user!!, UserMasterVillage(
                            viewModel.userMasterVillage?.districtBranchID?.toInt(),
                            viewModel.userInfo?.userId
                        )
                    )
                    Toast.makeText(activity, "Data is Saved!", Toast.LENGTH_SHORT).show()
                }catch(e:Exception){
                    Toast.makeText(requireContext(), "Error while saving the master location", Toast.LENGTH_SHORT).show()
                }
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
                    if(myInitialLoc == null && myLocation != null) {
                        myInitialLoc = myLocation
                        binding.inputMasterLong.setText(myInitialLoc?.longitude.toString())
                        binding.inputMasterLat.setText(myInitialLoc?.latitude.toString())
                        Log.i("Initial Location","$myInitialLoc")
                    } else if(myInitialLoc != null) {
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
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 500f // 10 meters
    }
}