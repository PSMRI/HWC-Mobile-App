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
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import org.piramalswasthya.cho.ui.register_patient_activity.location_details.LocationFragmentDirections
import javax.inject.Inject

@AndroidEntryPoint
class OtherInformationsFragment : Fragment() , NavigationAdapter {

    @Inject
    lateinit var govIdEntityMasterDao: GovIdEntityMasterDao

    @Inject
    lateinit var otherGovIdEntityMasterDao: OtherGovIdEntityMasterDao

    companion object {
        fun newInstance() = OtherInformationsFragment()
    }

    private var _binding: FragmentOtherInformationsBinding? = null
    private val binding: FragmentOtherInformationsBinding
        get() = _binding!!

    private lateinit var viewModel: OtherInformationsViewModel

    val fhirEngine: FhirEngine
        get() = CHOApplication.fhirEngine(requireContext())

    var selectedAbhaGenType : AbhaGenType? = null

    var selectedGovtIdType : GovIdEntityMaster? = null

    var selectedGovtHealthProgType : OtherGovIdEntityMaster? = null

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
        fetchGovtIds()
        fetchOtherGovtIds()
    }

    private fun fetchAbhaGenMode(){
        selectedAbhaGenType = AbhaGenType(1, "Aadhar")
        val abhaIdList = listOf(selectedAbhaGenType)
        val abhaIdNames = abhaIdList.map { it?.identityType }.toTypedArray()
        binding.dropdownAbhaGenMode.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, abhaIdNames)

        binding.dropdownGovtIdType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedAbhaGenType = abhaIdList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun fetchGovtIds(){
        coroutineScope.launch {
            val govtIdLists = govIdEntityMasterDao.getGovIdEntityMaster()
            if(govtIdLists != null){
                val govtIdNames = govtIdLists.map { it.identityType }.toTypedArray()
                binding.dropdownGovtIdType.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, govtIdNames)

                binding.dropdownGovtIdType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectedGovtIdType = govtIdLists[position]
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Do nothing
                    }
                }
            }

        }
    }

    private fun fetchOtherGovtIds(){
        coroutineScope.launch {
            val otherGovtIdLists = otherGovIdEntityMasterDao.getOtherGovIdEntityMaster()
            if(otherGovtIdLists != null){
                val govtIdNames = otherGovtIdLists.map { it.identityType }.toTypedArray()
                binding.dropdownGovtHealthProgType.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, govtIdNames)

                binding.dropdownGovtIdType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectedGovtHealthProgType = otherGovtIdLists[position]
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Do nothing
                    }
                }
            }

        }
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
        var extensionStr = Extension()

        extensionStr.url = "http://hl7.org/fhir/StructureDefinition/Patient#Patient.main.abhaGenerationMode"
        val str = StringType("string Value");
        extensionStr.setValue(str)
        patient.addExtension(extensionStr)

        var extensionCode = Extension()

        extensionCode.url = "http://hl7.org/fhir/StructureDefinition/Patient#Patient.main.dropdown"
        val cdt = Coding();
        cdt.code = "2"
        cdt.display = "hgjhgjgh"
        extensionCode.setValue(cdt)

        patient.addExtension(extensionCode)
//        }
//        patient.extension.a
    }

    override fun onCancelAction() {
        findNavController().navigate(
            OtherInformationsFragmentDirections.actionOtherInformationsFragmentToFragmentLocation()
        )
    }

}