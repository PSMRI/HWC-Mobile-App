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
import org.piramalswasthya.cho.database.room.dao.GovIdEntityMasterDao
import org.piramalswasthya.cho.database.room.dao.OtherGovIdEntityMasterDao
import org.piramalswasthya.cho.database.room.dao.StateMasterDao
import org.piramalswasthya.cho.databinding.FragmentFhirOtherInformationBinding
import org.piramalswasthya.cho.databinding.FragmentHwcBinding
import org.piramalswasthya.cho.databinding.FragmentOtherInformationsBinding
import org.piramalswasthya.cho.model.AbhaGenType
import org.piramalswasthya.cho.model.GovIdEntityMaster
import org.piramalswasthya.cho.model.OtherGovIdEntityMaster
import org.piramalswasthya.cho.patient.patient
import org.piramalswasthya.cho.repositories.DistrictMasterRepo
import org.piramalswasthya.cho.repositories.GovIdEntityMasterRepo
import org.piramalswasthya.cho.repositories.OtherGovIdEntityMasterRepo
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.commons.govt_health_prog.GovtHealthProgFragment
import org.piramalswasthya.cho.ui.commons.govt_id.GovtIdFragment
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import org.piramalswasthya.cho.ui.register_patient_activity.location_details.LocationFragmentDirections
import javax.inject.Inject

@AndroidEntryPoint
class OtherInformationsFragment : Fragment() , NavigationAdapter {

//    @Inject
//    lateinit var govIdEntityMasterDao: GovIdEntityMasterDao
//
//    @Inject
//    lateinit var otherGovIdEntityMasterDao: OtherGovIdEntityMasterDao

    @Inject
    lateinit var govIdEntityMasterRepo: GovIdEntityMasterRepo

    @Inject
    lateinit var otherGovIdEntityMasterRepo: OtherGovIdEntityMasterRepo

    companion object {
        fun newInstance() = OtherInformationsFragment()
    }

    private var _binding: FragmentOtherInformationsBinding? = null
    private val binding: FragmentOtherInformationsBinding
        get() = _binding!!

    private lateinit var viewModel: OtherInformationsViewModel

    val fhirEngine: FhirEngine
        get() = CHOApplication.fhirEngine(requireContext())

    private var selectedAbhaGenType : AbhaGenType? = null


    private var selectedGovtIdType : GovIdEntityMaster? = null

    private var selectedGovtHealthProgType : OtherGovIdEntityMaster? = null

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOtherInformationsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fetchAbhaGenMode()
        addGovtIdForm()
        addGovtHealthProgForm()
//        fetchGovtIds()
//        fetchOtherGovtIds()
        binding.addIdBtn.setOnClickListener {
            Log.i("btn", "clicked")
            addGovtIdForm()
        }
        binding.addHealthProgBtn.setOnClickListener {
            Log.i("btn", "clicked")
            addGovtHealthProgForm()
        }
    }

    private fun addGovtIdForm(){
        val fragmentManager : FragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction : FragmentTransaction = fragmentManager.beginTransaction()

        val formFragment = GovtIdFragment()

        // Add the fragment to the dynamic fragment container
        fragmentTransaction.add(binding.govtIdContainer.id, formFragment, "FormFragment_0")
        fragmentTransaction.addToBackStack(null) // Optional: Add the transaction to the back stack
        fragmentTransaction.commit()
    }

    private fun addGovtHealthProgForm(){
        val fragmentManager : FragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction : FragmentTransaction = fragmentManager.beginTransaction()

        val formFragment = GovtHealthProgFragment()

        // Add the fragment to the dynamic fragment container
        fragmentTransaction.add(binding.govtHealthProgIdContainer.id, formFragment, "FormFragment_0")
        fragmentTransaction.addToBackStack(null) // Optional: Add the transaction to the back stack
        fragmentTransaction.commit()
    }

//    private fun addGovtIdForm(view: View){
//        val fragmentManager : FragmentManager = requireActivity().supportFragmentManager
//        val fragmentTransaction : FragmentTransaction = fragmentManager.beginTransaction()
//
//        val formFragment = GovtIdFragment()
//
//        // Add the fragment to the dynamic fragment container
//        fragmentTransaction.add(binding.govtIdContainer.id, formFragment, "FormFragment_0")
//        fragmentTransaction.addToBackStack(null) // Optional: Add the transaction to the back stack
//        fragmentTransaction.commit()
//    }

    private fun fetchAbhaGenMode(){
        coroutineScope.launch {
            val abhaGenTypeMap = mutableMapOf(1 to "Aadhar")
            if(abhaGenTypeMap != null){
                val abhaIdNames = abhaGenTypeMap.values.toTypedArray()
                binding.dropdownAbhaGenMode.setAdapter(ArrayAdapter(requireContext(), R.layout.drop_down, abhaIdNames))
                if(abhaIdNames.isNotEmpty()) {
                    selectedAbhaGenType = AbhaGenType(abhaGenTypeMap!!.entries.toList()[0].key, abhaGenTypeMap!!.entries.toList()[0].value)
                    binding.dropdownAbhaGenMode.setText(selectedAbhaGenType!!.identityType, false)
                }
                binding.dropdownAbhaGenMode.setOnItemClickListener { parent, _, position, _ ->
                    selectedAbhaGenType = AbhaGenType(abhaGenTypeMap!!.entries.toList()[position].key, abhaGenTypeMap!!.entries.toList()[position].value)
                    binding.dropdownAbhaGenMode.setText(selectedAbhaGenType!!.identityType, false)
                }
            }

//            binding.dropdownGovtIdType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                    selectedAbhaGenType = abhaIdList[position]
//                }
//
//                override fun onNothingSelected(parent: AdapterView<*>?) {
//                    // Do nothing
//                }
//            }
        }
    }

    private fun fetchGovtIds(){
//        coroutineScope.launch {
//            val govtIdMap = govIdEntityMasterRepo.getGovIdtEntityAsMap()
//            if(govtIdMap != null){
//                val govtIdNames = govtIdMap.values.toTypedArray()
//                binding.dropdownGovtIdType.setAdapter(ArrayAdapter(requireContext(), R.layout.drop_down, govtIdNames))
//                if(govtIdNames.isNotEmpty()) {
//                    selectedGovtIdType = GovIdEntityMaster( govtIdMap!!.entries.toList()[0].key, govtIdMap!!.entries.toList()[0].value)
//                    binding.dropdownGovtIdType.setText(selectedGovtIdType!!.identityType, false)
//                }
//                binding.dropdownGovtIdType.setOnItemClickListener { parent, _, position, _ ->
//                    selectedGovtIdType = GovIdEntityMaster( govtIdMap!!.entries.toList()[position].key, govtIdMap!!.entries.toList()[position].value)
//                    binding.dropdownGovtIdType.setText(selectedGovtIdType!!.identityType, false)
//                }
//            }
//        }
    }

    private fun fetchOtherGovtIds(){
//        coroutineScope.launch {
//            val otherGovtIdMap = otherGovIdEntityMasterRepo.getOtherGovtEntityAsMap()
//            if(otherGovtIdMap != null){
//                val otherGovtIdNames = otherGovtIdMap.values.toTypedArray()
//                binding.dropdownGovtHealthProgType.setAdapter(ArrayAdapter(requireContext(), R.layout.drop_down, otherGovtIdNames))
//                if(otherGovtIdNames.isNotEmpty()) {
//                    selectedGovtHealthProgType = OtherGovIdEntityMaster(otherGovtIdMap!!.entries.toList()[0].key, otherGovtIdMap!!.entries.toList()[0].value)
//                    binding.dropdownGovtHealthProgType.setText(selectedGovtHealthProgType!!.identityType, false)
//                }
//                binding.dropdownGovtIdType.setOnItemClickListener { parent, _, position, _ ->
//                    selectedGovtHealthProgType = OtherGovIdEntityMaster(otherGovtIdMap!!.entries.toList()[position].key, otherGovtIdMap!!.entries.toList()[position].value)
//                    binding.dropdownGovtHealthProgType.setText(selectedGovtHealthProgType!!.identityType, false)
//                }
//            }
//
//        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(OtherInformationsViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun getFragmentId(): Int {
        return R.id.fragment_other_informations;
    }

    override fun onSubmitAction() {
        CoroutineScope(Dispatchers.Main).launch{
            addPatientExtensions()
            var demo = patient
            Log.i("patient id", demo.id)
            fhirEngine.create(patient)
        }
        val intent = Intent(context, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun addPatientExtensions(){

//        if(selectedAbhaGenType != null){
//            var extensionAbhaGen = Extension()
//            extensionAbhaGen.url = "http://hl7.org/fhir/StructureDefinition/Patient#Patient.main.abhaGenerationMode"
//            var cdt = Coding();
//            cdt.code = selectedAbhaGenType!!.identityTypeID.toString()
//            cdt.display = selectedAbhaGenType!!.identityType
//            extensionAbhaGen.setValue(cdt)
//            patient.addExtension(extensionAbhaGen)
//        }
//
//        if(selectedGovtIdType != null){
//            var extensionGovtIdType = Extension()
//            extensionGovtIdType.url = "http://hl7.org/fhir/StructureDefinition/Patient#Patient.identity.govtIdentityType"
//            var cdt = Coding();
//            cdt.code = selectedGovtIdType!!.govtIdentityTypeID.toString()
//            cdt.display = selectedGovtIdType!!.identityType
//            extensionGovtIdType.setValue(cdt)
//            patient.addExtension(extensionGovtIdType)
//        }
//
//        var extensionGovtIdNumber = Extension()
//        extensionGovtIdNumber.url = "http://hl7.org/fhir/StructureDefinition/Patient#Patient.identity.govtIdentityNo"
//        var str = StringType(binding.govtIdNumberText.text.toString());
//        extensionGovtIdNumber.setValue(str)
//        patient.addExtension(extensionGovtIdNumber)
//
//        if(selectedGovtHealthProgType != null){
//            var extensionGovtHealthProgramType = Extension()
//            extensionGovtHealthProgramType.url = "http://hl7.org/fhir/StructureDefinition/Patient#Patient.identity.govtHealthProgramType"
//            var cdt = Coding();
//            cdt.code = selectedGovtHealthProgType!!.govtIdentityTypeID.toString()
//            cdt.display = selectedGovtHealthProgType!!.identityType
//            extensionGovtHealthProgramType.setValue(cdt)
//            patient.addExtension(extensionGovtHealthProgramType)
//        }
//
//        var extensionGovtHealthProgramId = Extension()
//        extensionGovtHealthProgramId.url = "http://hl7.org/fhir/StructureDefinition/Patient#Patient.identity.govtHealthProgramId"
//        str = StringType(binding.govtHealthProgIdText.text.toString());
//        extensionGovtHealthProgramId.setValue(str)
//        patient.addExtension(extensionGovtHealthProgramId)

//        }
//        patient.extension.a
    }

    override fun onCancelAction() {
        findNavController().navigate(
            OtherInformationsFragmentDirections.actionOtherInformationsFragmentToFragmentLocation()
        )
    }

}