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
import android.util.Log
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.async
import org.piramalswasthya.cho.R
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
import org.piramalswasthya.cho.repositories.BlockMasterRepo
import org.piramalswasthya.cho.repositories.DistrictMasterRepo
import org.piramalswasthya.cho.repositories.LoginSettingsDataRepository
import org.piramalswasthya.cho.repositories.StateMasterRepo
import org.piramalswasthya.cho.repositories.VillageMasterRepo
import org.piramalswasthya.cho.ui.commons.fhir_add_patient.FhirAddPatientViewModel
import org.piramalswasthya.cho.ui.login_activity.cho_login.outreach.OutreachViewModel


@AndroidEntryPoint
class LoginSettingsFragment : Fragment() {

    @Inject
    lateinit var loginSettingsDataRepository: LoginSettingsDataRepository

    @Inject
    lateinit var stateMasterDao: StateMasterDao

    @Inject
    lateinit var stateMasterRepo: StateMasterRepo

    @Inject
    lateinit var districtMasterRepo: DistrictMasterRepo

    @Inject
    lateinit var blockMasterRepo: BlockMasterRepo

    @Inject
    lateinit var villageMasterRepo: VillageMasterRepo

    @Inject
    lateinit var userDao: UserDao

    private var userInfo: UserCache? = null

//    @Inject
//    lateinit var loginSettingsDataRepository: LoginSettingsDataRepository

    @Inject
    lateinit var apiService: AmritApiService
    private val binding by lazy{
        FragmentLoginSettingBinding.inflate(layoutInflater)
    }
    private var stateMap: Map<Int, String>? = null
    private var districtMap: Map<Int, String>? = null
    private var blockMap: Map<Int, String>? = null
    private var villageMap: Map<Int, String>? = null

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
        val userName = (arguments?.getString("userName", ""))!!;

        lifecycleScope.launch {
            async { userInfo = userDao.getLoggedInUser() }.await()
            Log.i("states fetching", "not done")
            fetchStates()
        }
        binding.continueButton.setOnClickListener{
            // Save Login Settings Data with location to preferences
//            val loginSettingsData = myLocation?.let { it1 ->
//                LoginSettingsData(selectedState,selectedDistrict,selectedBlock,selectedVillage, myLocation!!.longitude,
//                    myLocation!!.latitude,
//                    it1,userName
//                )
//            }
////            if (loginSettingsData != null) {
////                preferenceDao.saveLoginSettingsRecord(loginSettingsData)
////                Toast.makeText(context,"Settings Saved to preference",Toast.LENGTH_SHORT).show()
////                findNavController().navigate(
////                    LoginSettingsFragmentDirections.actionLoginSettingsToLoginActivity()
////                )
////            }
//            if (loginSettingsData != null) {
//                lifecycleScope.launch {
//                    loginSettingsDataRepository.saveLoginSettingsData(loginSettingsData)
//                }
//                Toast.makeText(context,"Settings Saved to Repository",Toast.LENGTH_SHORT).show()
//                findNavController().navigate(
////                    LoginSettingsFragmentDirections.actionLoginSettingsToChoLogin(userName)
//                    LoginSettingsFragmentDirections.actionLoginSettingsToUsername()
//                )
//            }
//            else{
//                Toast.makeText(context,"Error!! Check permissions",Toast.LENGTH_SHORT).show()
//                getCurrentLocation()
//            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel() // Cancel the coroutine scope when the fragment is destroyed
    }

    private suspend fun addStatesToDb(){
        try {
            stateMap?.let{
                for(states in stateMap!!.entries){
                    stateMasterRepo.insertStateMaster(StateMaster(states.key, states.value))
                }
            }
        }
        catch (e : Exception){
            Log.i("exception in district", e.toString())
        }
    }

    private fun fetchStates() {
        Log.i("states fetching", "done")
        coroutineScope.launch {
            try {
                val request = LocationRequest(vanID = 153, spPSMID = "64")
                val stateData = stateMasterRepo.getStateList(request)
                when (stateData){
                    is NetworkResult.Success -> {
                        stateMap = stateData.data
                        addStatesToDb()
                        val stateNames = stateMap!!.values.toTypedArray()
                        binding.dropdownState.setAdapter(ArrayAdapter(requireContext(), R.layout.drop_down, stateNames))//                    binding.dropdownState.text = (state.stateName)
                        if(stateNames.isNotEmpty()) {
                            if(userInfo != null && userInfo!!.stateID != null && stateMap!!.containsKey(userInfo!!.stateID)){
                                Log.i("state if", "")
                                binding.dropdownState.setText(stateMap!![userInfo!!.stateID], false)
                                fetchDistricts(userInfo!!.stateID!!)
                            }
                            else{
                                Log.i("state else", "")
                                val firstState = stateMap!!.entries.toList()[0]
                                binding.dropdownState.setText(firstState.value, false)
                                coroutineScope.launch {
                                    if(updateUserStateId(firstState.key) > 0 && userInfo != null){
                                        userInfo!!.stateID = firstState.key
                                    }
                                }
                                fetchDistricts(firstState.key)
                            }
                        }
                        binding.dropdownState.setOnItemClickListener { parent, _, position, _ ->
                            val selectedStateId = stateMap!!.keys.toList()[position]
                            coroutineScope.launch {
                                if(updateUserStateId(selectedStateId) > 0 && userInfo != null){
                                    userInfo!!.stateID = selectedStateId
                                }
                            }
                            fetchDistricts(selectedStateId)
                        }
                    }

                    else -> {

                    }
                }

            } catch (e: Exception) {
                Timber.d("Fetching states failed ${e.message}")
            }
        }
    }

    private suspend fun addDistrictsToDb(stateId : Int){
        try {
            districtMap?.let{
                for(district in districtMap!!.entries){
                    districtMasterRepo.insertDistrict(DistrictMaster(district.key, stateId, district.value))
                }
            }
        }
        catch (e : Exception){
            Log.i("exception in district", e.toString())
        }
    }

    private suspend fun updateUserStateId(stateId : Int) : Int{
        return userDao.updateUserStateId(stateId)
    }

    private fun fetchDistricts(selectedStateId: Int) {

        coroutineScope.launch {
            try {

                val districtData = districtMasterRepo.districtMasterService(selectedStateId)
                when (districtData){
                    is NetworkResult.Success -> {
                        districtMap = districtData.data
                        addDistrictsToDb(selectedStateId)
                        val districtNames = districtMap!!.values.toTypedArray()
                        binding.dropdownDist.setAdapter(ArrayAdapter(requireContext(), R.layout.drop_down, districtNames))//                    binding.dropdownState.text = (state.stateName)
                        if(districtNames.isNotEmpty()) {
                            if(userInfo != null && userInfo!!.districtID != null && districtMap!!.containsKey(userInfo!!.districtID)){
                                Log.i("district if", "")
                                binding.dropdownDist.setText(districtMap!![userInfo!!.districtID], false)
                                fetchTaluks(userInfo!!.districtID!!)
                            }
                            else{
                                Log.i("district else", "")
                                val firstDistrict = districtMap!!.entries.toList()[0]
                                binding.dropdownDist.setText(firstDistrict.value, false)
                                coroutineScope.launch {
                                    if(updateUserDistrictId(firstDistrict.key) > 0 && userInfo != null){
                                        userInfo!!.districtID = firstDistrict.key
                                    }
                                }
                                fetchTaluks(firstDistrict.key)
                            }
                        }
                        binding.dropdownDist.setOnItemClickListener { parent, _, position, _ ->
                            val selectedDistrictId = districtMap!!.keys.toList()[position]
                            coroutineScope.launch {
                                if(updateUserDistrictId(selectedDistrictId) > 0 && userInfo != null){
                                    userInfo!!.districtID = selectedDistrictId
                                }
                            }
                            fetchTaluks(selectedDistrictId)
                        }
                    }

                    else -> {

                    }
                }

            } catch (e: Exception) {
                Timber.d("Fetching Districts failed ${e.message}")
            }
        }
    }

    private suspend fun addBlocksToDb(districtId : Int){
        try {
            blockMap?.let{
                for(block in blockMap!!.entries){
                    Log.i("adding blocks", block.key.toString() + " " + districtId)
                    blockMasterRepo.insertBlock(BlockMaster(blockID = block.key, districtID = districtId, blockName = block.value))
                }
            }
        }
        catch (e : Exception){
            Log.i("exception in block", e.toString())
        }
    }

    private suspend fun updateUserDistrictId(districtId : Int) : Int{
        return userDao.updateUserDistrictId(districtId)
    }

    private fun fetchTaluks(selectedDistrictId: Int) {

        coroutineScope.launch {
            try {
                // Find the selected state by name
                val blockData = blockMasterRepo.blockMasterService(selectedDistrictId)
                when (blockData){
                    is NetworkResult.Success -> {
                        blockMap = blockData.data
                        addBlocksToDb(selectedDistrictId)
                        val blockNames = blockMap!!.values.toTypedArray()
                        binding.dropdownTaluk.setAdapter(ArrayAdapter(requireContext(), R.layout.drop_down, blockNames))//                    binding.dropdownState.text = (state.stateName)
                        if(blockNames.isNotEmpty()) {
                            if(userInfo != null && userInfo!!.blockID != null && blockMap!!.containsKey(userInfo!!.blockID)){
                                Log.i("district if", "")
                                binding.dropdownTaluk.setText(blockMap!![userInfo!!.blockID], false)
                                fetchVillageMaster(userInfo!!.blockID!!)
                            }
                            else{
                                Log.i("district else", "")
                                val firstBlock = blockMap!!.entries.toList()[0]
                                binding.dropdownTaluk.setText(firstBlock.value, false)
                                coroutineScope.launch {
                                    if(updateUserBlockId(firstBlock.key) > 0 && userInfo != null){
                                        userInfo!!.blockID = firstBlock.key
                                    }
                                }
                                fetchVillageMaster(firstBlock.key)
                            }
                        }
                        binding.dropdownTaluk.setOnItemClickListener { parent, _, position, _ ->
                            val selectedBlockId = blockMap!!.keys.toList()[position]
                            coroutineScope.launch {
                                if(updateUserBlockId(selectedBlockId) > 0 && userInfo != null){
                                    userInfo!!.blockID = selectedBlockId
                                }
                            }
                            fetchVillageMaster(selectedBlockId)
                        }
                    }

                    else -> {

                    }
                }

            } catch (e: Exception) {
                Timber.d("Fetching Taluks failed ${e.message}")
            }
        }
    }

    private suspend fun getVillagesByBlockIdAndAddToDb(){
        try {
            blockMap?.let {
                for (block in blockMap!!.entries){
                    val villageData = villageMasterRepo.villageMasterService(block.key)
                    when (villageData){
                        is NetworkResult.Success -> {
                            addVillagesToDb(villageData.data, block.key)
                        }
                        else -> {

                        }
                    }
                }
            }
        }
        catch (e : Exception){
            Log.i("exception in village", e.toString())
        }
    }

    private suspend fun addVillagesToDb(currVillageMap : Map<Int, String>,  blockId : Int){
        try {
            currVillageMap?.let{
                for(village in currVillageMap){
                    villageMasterRepo.insertVillage(VillageMaster(districtBranchID = village.key, blockID = blockId, villageName = village.value))
                }
            }
        }
        catch (e : Exception){
            Log.i("exception in village", e.toString())
        }
    }

    private suspend fun updateUserBlockId(blockId : Int) : Int{
        return userDao.updateUserBlockId(blockId)
    }

    private fun fetchVillageMaster(selectedBlockId: Int, ) {

        coroutineScope.launch {
            try {
                // Find the selected state by name
                val villageData = villageMasterRepo.villageMasterService(selectedBlockId)
                when (villageData){
                    is NetworkResult.Success -> {
                        villageMap = villageData.data
                        getVillagesByBlockIdAndAddToDb()
                        val villageNames = villageMap!!.values.toTypedArray()
                        binding.dropdownPanchayat.setAdapter(ArrayAdapter(requireContext(), R.layout.drop_down, villageNames))//                    binding.dropdownState.text = (state.stateName)
                        if(villageNames.isNotEmpty()) {
                            if(userInfo != null && userInfo!!.districtBranchID != null && villageMap!!.containsKey(userInfo!!.districtBranchID)){
                                Log.i("village if", "")
                                binding.dropdownPanchayat.setText(villageMap!![userInfo!!.districtBranchID], false)
                            }
                            else{
                                Log.i("village else", "")
                                val firstVillage = villageMap!!.entries.toList()[0]
                                binding.dropdownPanchayat.setText(firstVillage.value, false)
                                coroutineScope.launch {
                                    if(updateUserVillageId(firstVillage.key) > 0 && userInfo != null){
                                        userInfo!!.districtBranchID = firstVillage.key
                                    }
                                }
                            }
                        }
                        binding.dropdownPanchayat.setOnItemClickListener { parent, _, position, _ ->
                            val selectedVillageId = villageMap!!.keys.toList()[position]
                            coroutineScope.launch {
                                if(updateUserVillageId(selectedVillageId) > 0 && userInfo != null){
                                    userInfo!!.districtBranchID = selectedVillageId
                                }
                            }
                        }
                    }

                    else -> {

                    }
                }

            } catch (e: Exception) {
                Timber.d("Fetching villages failed ${e.message}")
            }
        }
    }

    private suspend fun updateUserVillageId(districtBranchId : Int) : Int{
        return userDao.updateUserVillageId(districtBranchId)
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