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
import org.piramalswasthya.cho.database.room.dao.StateMasterDao
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.BlockMaster
import org.piramalswasthya.cho.model.DistrictMaster
import org.piramalswasthya.cho.model.LoginSettingsData
import org.piramalswasthya.cho.model.StateMaster
import org.piramalswasthya.cho.model.VillageMaster
import org.piramalswasthya.cho.repositories.BlockMasterRepo
import org.piramalswasthya.cho.repositories.DistrictMasterRepo
import org.piramalswasthya.cho.repositories.LoginSettingsDataRepository
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
    lateinit var districtMasterRepo: DistrictMasterRepo

    @Inject
    lateinit var blockMasterRepo: BlockMasterRepo

    @Inject
    lateinit var villageMasterRepo: VillageMasterRepo

    @Inject
    lateinit var userDao: UserDao

//    @Inject
//    lateinit var loginSettingsDataRepository: LoginSettingsDataRepository

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

    private lateinit var viewModel: LoginSettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(LoginSettingsViewModel::class.java)
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
//                    LoginSettingsFragmentDirections.actionLoginSettingsToChoLogin(userName)
                    LoginSettingsFragmentDirections.actionLoginSettingsToUsername()
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

    private suspend fun addStatesToDb(){
        try {
            stateList?.let{
                for(state in stateList!!){
                    stateMasterDao.insertStates(StateMaster(stateID = state.stateID, stateName = state.stateName))
                }
            }
        }
        catch (e : Exception){
            Log.i("exception in district", e.toString())
        }
    }

    private fun fetchStates() {
        coroutineScope.launch {
            try {
                val request = LocationRequest(vanID = 153, spPSMID = "64")
                val stateData = apiService.getStates(request)
                if (stateData != null){
                    stateList = stateData.data.stateMaster
//                    async { addStatesToDb() }.await()
                    addStatesToDb()
                    val stateNames = stateList!!.map { it.stateName }.toTypedArray()
                    binding.dropdownState.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, stateNames)
                    binding.dropdownState.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                            Log.i("state is selected", "")
                            val selectedStateName = binding.dropdownState.adapter.getItem(position) as String
                            selectedState = stateList?.find { it.stateName == selectedStateName }
                            coroutineScope.launch { updateUserStateId(selectedState!!.stateID) }
                            // Fetch districts based on the selected state ID
                            fetchDistricts(selectedStateName)
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

    private suspend fun addDistrictsToDb(stateId : Int){
        try {
            districtList?.let{
                for(district in districtList!!){
                    districtMasterRepo.insertDistrict(DistrictMaster(district.districtID, stateId, district.districtName))
                }
            }
        }
        catch (e : Exception){
            Log.i("exception in district", e.toString())
        }
    }

    private suspend fun updateUserStateId(stateId : Int){
        userDao.updateUserStateId(stateId)
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
//                        async { addDistrictsToDb(stateId) }.await()
                        addDistrictsToDb(stateId)
                        val districtNames = districtList!!.map { it.districtName }.toTypedArray()
                        binding.dropdownDistrict.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, districtNames)
                        binding.dropdownDistrict.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                                Log.i("district is selected", "")
                                val selectedDistrictName = binding.dropdownDistrict.adapter.getItem(position) as String

                                selectedDistrict = districtList?.find { it.districtName == selectedDistrictName }
                                coroutineScope.launch { updateUserDistrictId(selectedDistrict!!.districtID) }

//                                 Fetch Taluks based on the selected value
                                fetchTaluks(selectedDistrictName)
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

    private suspend fun addBlocksToDb(districtId : Int){
        try {
            blockList?.let{
                for(block in blockList!!){
                    Log.i("adding blocks", block.blockID.toString() + " " + districtId)
                    blockMasterRepo.insertBlock(BlockMaster(blockID = block.blockID, districtID = districtId, blockName = block.blockName))
                }
            }
        }
        catch (e : Exception){
            Log.i("exception in block", e.toString())
        }
    }

    private suspend fun updateUserDistrictId(districtId : Int){
        userDao.updateUserDistrictId(districtId)
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
//                        async { addBlocksToDb(districtId) }.await()
                        addBlocksToDb(districtId)
                        val blockNames = blockList!!.map { it.blockName }.toTypedArray()
                        binding.dropdownTaluk.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, blockNames)
                        binding.dropdownTaluk.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                                Log.i("block is selected", "")
                                val selectedBlockName = binding.dropdownTaluk.adapter.getItem(position) as String

                                selectedBlock = blockList?.find { it.blockName == selectedBlockName }
                                coroutineScope.launch { updateUserBlockId(selectedBlock!!.blockID) }
                                fetchVillageMaster(selectedBlockName)
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

    private suspend fun getVillagesByBlockIdAndAddToDb(){
        try {
            blockList?.let {
                for (block in blockList!!){
                    val response = apiService.getVillages(block.blockID)
                    if (response != null) {
                        addVillagesToDb(response.data, block.blockID)
                    }
                }
            }
        }
        catch (e : Exception){
            Log.i("exception in village", e.toString())
        }
    }

    private suspend fun addVillagesToDb(currVillageList : List<Village>,  blockId : Int){
        try {
            currVillageList?.let{
                for(village in currVillageList){
                    villageMasterRepo.insertVillage(VillageMaster(districtBranchID = village.districtBranchID, blockID = blockId, villageName = village.villageName))
                }
            }
        }
        catch (e : Exception){
            Log.i("exception in village", e.toString())
        }
    }

    private suspend fun updateUserBlockId(blockId : Int){
        userDao.updateUserBlockId(blockId)
    }

    private fun fetchVillageMaster(selectedBlockName: String, ) {

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
//                        async { getVillagesByBlockIdAndAddToDb() }.await()
                        getVillagesByBlockIdAndAddToDb()
                        val villageNames = villageList!!.map { it.villageName }.toTypedArray()
                        binding.dropdownStreet.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, villageNames)
                        binding.dropdownStreet.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                                Log.i("state is selected", "")
                                val selectedVillageName = binding.dropdownStreet.adapter.getItem(position) as String
                                selectedVillage = villageList?.find { it.villageName == selectedVillageName }
                                coroutineScope.launch { updateUserVillageId(selectedVillage!!.districtBranchID) }
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

    private suspend fun updateUserVillageId(districtBranchId : Int){
        userDao.updateUserVillageId(districtBranchId)
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