package org.piramalswasthya.cho.ui.register_patient_activity.other_informations

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.get
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.findNavController
import com.google.android.fhir.FhirEngine
import com.google.firebase.ml.common.FirebaseMLException.Code
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.CodeType
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.StringType
import org.piramalswasthya.cho.CHOApplication
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.dropdown_adapters.DropdownAdapter
import org.piramalswasthya.cho.adapter.model.DropdownList
import org.piramalswasthya.cho.database.room.dao.GovIdEntityMasterDao
import org.piramalswasthya.cho.database.room.dao.OtherGovIdEntityMasterDao
import org.piramalswasthya.cho.database.room.dao.StateMasterDao
import org.piramalswasthya.cho.databinding.FragmentFhirOtherInformationBinding
import org.piramalswasthya.cho.databinding.FragmentHwcBinding
import org.piramalswasthya.cho.databinding.FragmentOtherInformationsBinding
import org.piramalswasthya.cho.model.AbhaGenType
import org.piramalswasthya.cho.model.GovIdEntityMaster
import org.piramalswasthya.cho.model.OtherGovIdEntityMaster
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.patient.patient
import org.piramalswasthya.cho.repositories.DistrictMasterRepo
import org.piramalswasthya.cho.repositories.GovIdEntityMasterRepo
import org.piramalswasthya.cho.repositories.OtherGovIdEntityMasterRepo
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.commons.govt_health_prog.GovtHealthProgFragment
import org.piramalswasthya.cho.ui.commons.govt_id.GovtIdFragment
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import org.piramalswasthya.cho.ui.register_patient_activity.location_details.LocationFragmentDirections
import org.piramalswasthya.cho.ui.register_patient_activity.location_details.LocationViewModel
import org.piramalswasthya.cho.ui.register_patient_activity.patient_details.PatientDetailsViewModel
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class OtherInformationsFragment : Fragment() , NavigationAdapter {


    companion object {
        fun newInstance() = OtherInformationsFragment()
    }

    private var _binding: FragmentOtherInformationsBinding? = null
    private val binding: FragmentOtherInformationsBinding
        get() = _binding!!

    private lateinit var viewModel: OtherInformationsViewModel

    private var patient : Patient? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOtherInformationsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this).get(OtherInformationsViewModel::class.java)
        patient = arguments?.getSerializable("patient") as? Patient
        setChangeListeners()
        setAdapters()
    }

    private fun setChangeListeners(){
        binding.communityDropdown.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectedCommunity = viewModel.communityList[position];
            binding.communityDropdown.setText(viewModel.selectedCommunity!!.communityType, false)
        }

        binding.religionDropdown.setOnItemClickListener { parent, _, position, _ ->
            viewModel.selectedReligion = viewModel.religionList[position];
            binding.religionDropdown.setText(viewModel.selectedReligion!!.religionType, false)
        }
    }

    private fun setAdapters(){
        viewModel.community.observe(viewLifecycleOwner) { state ->
            when (state!!){
                OtherInformationsViewModel.NetworkState.SUCCESS -> {
                    val dropdownList = viewModel.communityList.map { it -> DropdownList(it.communityID, it.communityType) }
                    val dropdownAdapter = DropdownAdapter(requireContext(), R.layout.drop_down, dropdownList, binding.communityDropdown)
                    binding.communityDropdown.setAdapter(dropdownAdapter)
                }
                else -> {

                }
            }
        }

        viewModel.religion.observe(viewLifecycleOwner) { state ->
            when (state!!){
                OtherInformationsViewModel.NetworkState.SUCCESS -> {
                    val dropdownList = viewModel.religionList.map { it -> DropdownList(it.religionID, it.religionType) }
                    val dropdownAdapter = DropdownAdapter(requireContext(), R.layout.drop_down, dropdownList, binding.religionDropdown)
                    binding.religionDropdown.setAdapter(dropdownAdapter)
                }
                else -> {

                }
            }
        }
    }

//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        viewModel = ViewModelProvider(this).get(OtherInformationsViewModel::class.java)
//        // TODO: Use the ViewModel
//    }

    override fun getFragmentId(): Int {
        return R.id.fragment_other_informations;
    }

    override fun onSubmitAction() {
        updatePatientDetails()
        if(patient != null){
            patient!!.patientID = generateUuid()
            viewModel.insertPatient(patient!!)
        }
        val intent = Intent(context, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    fun updatePatientDetails(){

        if(patient != null){
            patient!!.parentName = binding.parentGurdianName.text.toString()
        }

        if(viewModel.selectedCommunity != null && patient != null){
            patient!!.communityID = viewModel.selectedCommunity!!.communityID;
        }

        if(viewModel.selectedReligion != null && patient != null){
            patient!!.religionID = viewModel.selectedReligion!!.religionID;
        }

    }

    fun generateUuid(): String {
        return UUID.randomUUID().toString()
    }

    override fun onCancelAction() {
        findNavController().navigateUp()
    }

}