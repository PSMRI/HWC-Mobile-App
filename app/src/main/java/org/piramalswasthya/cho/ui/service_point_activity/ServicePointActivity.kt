package org.piramalswasthya.cho.ui.service_point_activity

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.dropdown_adapters.DropdownAdapter
import org.piramalswasthya.cho.adapter.model.DropdownList
import org.piramalswasthya.cho.database.room.dao.UserDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.ActivityServicePointBinding
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.model.UserVanSpDetails
import org.piramalswasthya.cho.repositories.LoginSettingsDataRepository
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import org.piramalswasthya.cho.ui.login_activity.cho_login.ChoLoginFragmentDirections
import javax.inject.Inject


@AndroidEntryPoint
class ServicePointActivity : AppCompatActivity() {

    @Inject
    lateinit var loginSettingsDataRepository: LoginSettingsDataRepository

    private val binding by lazy{
        ActivityServicePointBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var userDao: UserDao
    @Inject
    lateinit var userRepo: UserRepo
    private var user : UserCache? = null

    @Inject
    lateinit var preferenceDao: PreferenceDao

    private lateinit var viewModel: ServicePointViewModel

    private lateinit var vanList: List<UserVanSpDetails>
    private lateinit var servicePointList: List<UserVanSpDetails>

    private var serviceBool = false
    private var vanBool = false
    private var servicePointBool = false
    private var stateBool = false
    private var districtBool = false
    private var blockBool = false
    private var villageBool = false
    private var dialogClosed = false

    private var serviceType: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[ServicePointViewModel::class.java]
        onBackPressedDispatcher.addCallback(this@ServicePointActivity, onBackPressedCallback)
        dialogClosed = false
        setContentView(binding.root)
        fetchData()

        binding.btnContinue.setOnClickListener {
            submitData()
        }

        binding.rgService.setOnCheckedChangeListener { radioGroup, checkedId ->

            serviceBool = true
            // This will get the radiobutton that has changed in its check state
            val checkedRadioButton: RadioButton = findViewById(checkedId)

            // This puts the value (true/false) into the variable
            val isChecked = checkedRadioButton.isChecked

            // If the radiobutton that has changed in check state is now checked...
            if (isChecked) {
                when(checkedRadioButton.text) {
                    "Morning" -> serviceType = 0
                    "Evening" -> serviceType = 1
                    "Full Day" -> serviceType = 2
                }
            }
        }

        binding.ddVan.setOnItemClickListener { _, _, position, _ ->
            if (viewModel.selectedVan != vanList[position]) {
                resetFields(1)
            }
            viewModel.selectedVan = vanList[position]
            viewModel.updateUserVanId(viewModel.selectedVan?.vanID!!.toInt())
            preferenceDao.saveVanData(vanList[position])
            binding.ddVan.setText(viewModel.selectedVan?.vanNoAndType, false)
            vanBool = true
//            binding.ddServicePoint.isEnabled = true
//            binding.servicePoint.isEnabled = true
//            binding.servicePoint.isClickable = true
        }

        binding.ddServicePoint.setOnItemClickListener { _, _, position, _ ->
            if (viewModel.selectedServicePoint != servicePointList[position]) {
                resetFields(2)
            }
            viewModel.selectedServicePoint = servicePointList[position]
            viewModel.updateUserServicePointId(viewModel.selectedServicePoint?.servicePointID!!.toInt())
            preferenceDao.saveServicePointData(servicePointList[position])
            binding.ddServicePoint.setText(viewModel.selectedServicePoint?.servicePointName, false)
            servicePointBool = true
            lifecycleScope.launch {
                viewModel.fetchMmuStateDetails()
            }
            binding.ddState.isEnabled = true
            binding.state.isEnabled = true
            binding.state.isClickable = true
        }

        binding.ddState.setOnItemClickListener { _, _, position, _ ->
            if (viewModel.selectedState != viewModel.masterStateDetailsList[position]) {
                resetFields(3)
            }
            viewModel.selectedState = viewModel.masterStateDetailsList[position]
            viewModel.updateUserState(viewModel.selectedState?.stateID!!.toInt())
            preferenceDao.saveStateData(viewModel.masterStateDetailsList[position])
            binding.ddState.setText(viewModel.selectedState?.stateName, false)
            stateBool = true
            lifecycleScope.launch {
                viewModel.fetchMmuDistrictDetails(viewModel.selectedState?.stateID!!)
            }
            binding.ddDtc.isEnabled = true
            binding.dtc.isEnabled = true
            binding.dtc.isClickable = true
        }

        binding.ddDtc.setOnItemClickListener { _, _, position, _ ->
            if (viewModel.selectedDistrict != viewModel.masterDistrictDetailsList[position]) {
                resetFields(4)
            }
            viewModel.selectedDistrict = viewModel.masterDistrictDetailsList[position]
            viewModel.updateUserDistrict(viewModel.selectedDistrict?.districtID!!.toInt())
            preferenceDao.saveDistrictData(viewModel.masterDistrictDetailsList[position])
            binding.ddDtc.setText(viewModel.selectedDistrict?.districtName, false)
            districtBool = true
            lifecycleScope.launch {
                viewModel.fetchMmuBlockDetails(viewModel.selectedDistrict?.districtID!!)
            }
            binding.ddTaluk.isEnabled = true
            binding.taluk.isEnabled = true
            binding.taluk.isClickable = true
        }

        binding.ddTaluk.setOnItemClickListener { _, _, position, _ ->
            if (viewModel.selectedBlock != viewModel.masterBlockDetailsList[position]) {
                resetFields(5)
            }
            viewModel.selectedBlock = viewModel.masterBlockDetailsList[position]
            viewModel.updateUserBlock(viewModel.selectedBlock?.blockID!!.toInt())
            preferenceDao.saveBlockData(viewModel.masterBlockDetailsList[position])
            binding.ddTaluk.setText(viewModel.selectedBlock?.blockName, false)
            blockBool = true
            lifecycleScope.launch {
                viewModel.fetchMmuVillageDetails(viewModel.selectedBlock?.blockID!!)
            }
            binding.ddSpv.isEnabled = true
            binding.spv.isEnabled = true
            binding.spv.isClickable = true
        }

        binding.ddSpv.setOnItemClickListener { _, _, position, _ ->
            viewModel.selectedVillage = viewModel.masterVillageDetailsList[position]
            viewModel.updateUserVillage(viewModel.selectedVillage?.districtBranchID!!.toInt())
            preferenceDao.saveVillageData(viewModel.masterVillageDetailsList[position])
            binding.ddSpv.setText(viewModel.selectedVillage?.villageName, false)
            villageBool = true
        }

    }

    private fun resetFields(fieldPosition: Int) {
        if (fieldPosition == 1) {
            serviceBool =  false
            stateBool =  false
            districtBool =  false
            blockBool = false
            villageBool = false
            resetDropDown(binding.ddServicePoint, binding.servicePoint, true, R.string.service_point)
            resetDropDown(binding.ddState, binding.state, false, R.string.state)
            resetDropDown(binding.ddDtc, binding.dtc, false, R.string.district_town_city)
            resetDropDown(binding.ddTaluk, binding.taluk, false, R.string.taluk_tehsil)
            resetDropDown(binding.ddSpv, binding.spv, false, R.string.street_panchayat_village)
        } else if (fieldPosition == 2) {
            stateBool =  false
            districtBool =  false
            blockBool = false
            villageBool = false
            resetDropDown(binding.ddState, binding.state, true, R.string.state)
            resetDropDown(binding.ddDtc, binding.dtc, false, R.string.district_town_city)
            resetDropDown(binding.ddTaluk, binding.taluk, false, R.string.taluk_tehsil)
            resetDropDown(binding.ddSpv, binding.spv, false, R.string.street_panchayat_village)
        } else if (fieldPosition == 3) {
            districtBool =  false
            blockBool = false
            villageBool = false
            resetDropDown(binding.ddDtc, binding.dtc, true, R.string.district_town_city)
            resetDropDown(binding.ddTaluk, binding.taluk, false, R.string.taluk_tehsil)
            resetDropDown(binding.ddSpv, binding.spv, false, R.string.street_panchayat_village)
        } else if (fieldPosition == 4) {
            blockBool = false
            villageBool = false
            resetDropDown(binding.ddTaluk, binding.taluk, true, R.string.taluk_tehsil)
            resetDropDown(binding.ddSpv, binding.spv, false, R.string.street_panchayat_village)
        } else if (fieldPosition == 5) {
            villageBool = false
            resetDropDown(binding.ddSpv, binding.spv, true, R.string.street_panchayat_village)
        }
    }

    private fun resetDropDown(dd: AutoCompleteTextView, til: TextInputLayout, shouldEnable: Boolean, stringRes: Int) {
        dd.clearListSelection()
        dd.setText(resources.getString(stringRes), false)
        dd.isEnabled = shouldEnable
        til.isEnabled = shouldEnable
        til.isClickable = shouldEnable
    }

    private val onBackPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        }
    }

    private fun submitData() {
        if (!serviceBool) {
            binding.rgService.requestFocus()
            Toast.makeText(this@ServicePointActivity, "Please select a service type", Toast.LENGTH_SHORT).show()
        }

        if (binding.ddVan.text.isNullOrEmpty()) {
            binding.ddVan.requestFocus()
            binding.van.apply {
                boxStrokeColor = Color.RED
                hintTextColor = ColorStateList.valueOf(Color.RED)
            }
            vanBool = false
        } else {
            binding.van.apply {
                boxStrokeColor = resources.getColor(R.color.purple)
                hintTextColor = defaultHintTextColor
            }
            vanBool = true
        }

        if (binding.ddServicePoint.text.isNullOrEmpty()) {
            binding.ddServicePoint.requestFocus()
            binding.servicePoint.apply {
                boxStrokeColor = Color.RED
                hintTextColor = ColorStateList.valueOf(Color.RED)
            }
            servicePointBool = false
        } else {
            binding.servicePoint.apply {
                boxStrokeColor = resources.getColor(R.color.purple)
                hintTextColor = defaultHintTextColor
            }
            servicePointBool = true
        }

        if (binding.ddState.text.isNullOrEmpty()) {
            binding.ddState.requestFocus()
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

        if (binding.ddDtc.text.isNullOrEmpty()) {
            binding.ddDtc.requestFocus()
            binding.dtc.apply {
                boxStrokeColor = Color.RED
                hintTextColor = ColorStateList.valueOf(Color.RED)
            }
            districtBool = false
        } else {
            binding.dtc.apply {
                boxStrokeColor = resources.getColor(R.color.purple)
                hintTextColor = defaultHintTextColor
            }
            districtBool = true
        }

        if (binding.ddTaluk.text.isNullOrEmpty()) {
            binding.ddTaluk.requestFocus()
            binding.taluk.apply {
                boxStrokeColor = Color.RED
                hintTextColor = ColorStateList.valueOf(Color.RED)
            }
            blockBool = false
        } else {
            binding.taluk.apply {
                boxStrokeColor = resources.getColor(R.color.purple)
                hintTextColor = defaultHintTextColor
            }
            blockBool = true
        }

        if (binding.ddSpv.text.isNullOrEmpty()) {
            binding.ddSpv.requestFocus()
            binding.spv.apply {
                boxStrokeColor = Color.RED
                hintTextColor = ColorStateList.valueOf(Color.RED)
            }
            villageBool = false
        } else {
            binding.spv.apply {
                boxStrokeColor = resources.getColor(R.color.purple)
                hintTextColor = defaultHintTextColor
            }
            villageBool = true
        }

        if(serviceBool && vanBool && servicePointBool && stateBool && districtBool && blockBool && villageBool) {
            startActivity(
                Intent(
                    this@ServicePointActivity,
                    HomeActivity::class.java
                )
            )
            viewModel.resetState()
            finish()
        }
    }

    private fun fetchData() {

        viewModel.vanSpDetails.observe(this) { state ->
            when (state!!) {
                ServicePointViewModel.NetworkState.SUCCESS -> {
                    var listVan = viewModel.masterVanSpDetailsList
                    var listServicePoint = viewModel.masterVanSpDetailsList
                    vanList = listVan.distinctBy { it.vanID }
                    servicePointList = listServicePoint.distinctBy { it.servicePointID }

                    val ddVanList = vanList.map { it -> DropdownList(it.vanID, it.vanNoAndType) }
                    val ddVanAdapter = DropdownAdapter(this@ServicePointActivity, R.layout.drop_down, ddVanList, binding.ddVan)
                    binding.ddVan.setAdapter(ddVanAdapter)

                    val ddServicePointList = servicePointList.map { it -> DropdownList(it.servicePointID, it.servicePointName) }
                    val ddServicePointAdapter = DropdownAdapter(this@ServicePointActivity, R.layout.drop_down, ddServicePointList, binding.ddServicePoint)
                    binding.ddServicePoint.setAdapter(ddServicePointAdapter)
                }
                else -> {}
            }
        }

        viewModel.stateDetails.observe(this) { state ->
            when (state!!) {
                ServicePointViewModel.NetworkState.SUCCESS -> {

                    val ddStateList = viewModel.masterStateDetailsList.map { it -> DropdownList(it.stateID, it.stateName) }
                    val ddStateAdapter = DropdownAdapter(this@ServicePointActivity, R.layout.drop_down, ddStateList, binding.ddState)
                    binding.ddState.setAdapter(ddStateAdapter)

                }
                else -> {}
            }
        }

        viewModel.districtDetails.observe(this) { state ->
            when (state!!) {
                ServicePointViewModel.NetworkState.SUCCESS -> {

                    val ddDistrictList = viewModel.masterDistrictDetailsList.map { it -> DropdownList(it.districtID, it.districtName) }
                    val ddDistrictAdapter = DropdownAdapter(this@ServicePointActivity, R.layout.drop_down, ddDistrictList, binding.ddDtc)
                    binding.ddDtc.setAdapter(ddDistrictAdapter)

                }
                else -> {}
            }
        }

        viewModel.blockDetails.observe(this) { state ->
            when (state!!) {
                ServicePointViewModel.NetworkState.SUCCESS -> {

                    val ddBlockList = viewModel.masterBlockDetailsList.map { it -> DropdownList(it.blockID, it.blockName) }
                    val ddBlockAdapter = DropdownAdapter(this@ServicePointActivity, R.layout.drop_down, ddBlockList, binding.ddTaluk)
                    binding.ddTaluk.setAdapter(ddBlockAdapter)

                }
                else -> {}
            }
        }

        viewModel.villageDetails.observe(this) { state ->
            when (state!!) {
                ServicePointViewModel.NetworkState.SUCCESS -> {

                    val ddVillageList = viewModel.masterVillageDetailsList.map { it -> DropdownList(it.districtBranchID, it.villageName) }
                    val ddVillageAdapter = DropdownAdapter(this@ServicePointActivity, R.layout.drop_down, ddVillageList, binding.ddSpv)
                    binding.ddSpv.setAdapter(ddVillageAdapter)

                }
                else -> {}
            }
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}
