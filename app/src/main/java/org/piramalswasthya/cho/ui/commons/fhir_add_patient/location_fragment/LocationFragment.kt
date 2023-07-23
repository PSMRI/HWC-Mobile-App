package org.piramalswasthya.cho.ui.commons.fhir_add_patient.location_fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.hl7.fhir.r4.model.Patient
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentLocationBinding
import org.piramalswasthya.cho.network.District
import org.piramalswasthya.cho.network.DistrictBlock
import org.piramalswasthya.cho.network.State
import org.piramalswasthya.cho.network.Village
import org.piramalswasthya.cho.ui.commons.FhirFragmentService
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.commons.fhir_add_patient.FhirLocationViewModel

@AndroidEntryPoint
class LocationFragment: Fragment(),NavigationAdapter, FhirFragmentService {

    override val viewModel: LocationViewModel by viewModels()

    override var fragment: Fragment = this;

    override var fragmentContainerId = 0;

    override val jsonFile : String = "location_information.json"


    private var stateList: List<State>? = null
    private var districtList: List<District>? = null
    private var blockList: List<DistrictBlock>? = null
    private var villageList: List<Village>? = null
    private var selectedState: State? = null
    private var selectedDistrict: District? = null
    private var selectedBlock: DistrictBlock? = null
    private var selectedVillage: Village? = null
    lateinit var pat : Patient

    private val binding by lazy{
        FragmentLocationBinding.inflate(layoutInflater)
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        pat= LocationFragmentArgs.fromBundle(requireArguments()).patientDetails
//        fetchStates()
    }

//    private fun fetchStates(){
//        viewModel.getStateList()
//        viewModel.refreshState.observe(viewLifecycleOwner){
//            when(it!!){
//                LocationViewModel.RefreshState.REFRESH_SUCCESS ->{
//                    stateList = viewModel.stateList
//                    viewModel.resetRefreshState()
//                }
//                LocationViewModel.RefreshState.REFRESH_FAILED ->{
//                    viewModel.resetRefreshState()
//                }
//                else ->{
//                }
//            }
//        }
//        val stateNames = stateList!!.map { it.stateName }.toTypedArray()
//        binding.dropdownState.setAdapter(ArrayAdapter(requireContext(), R.layout.drop_down, stateNames))
//        binding.dropdownState.setOnItemClickListener { parent, _, position, _ ->
//            val selectedState = parent.getItemAtPosition(position) as String
//            fetchDistricts(selectedState)
//        }
//    }
//
//    private fun fetchDistricts(selectedStateName: String){
//        selectedState = stateList?.find { it.stateName == selectedStateName }
//        selectedState?.stateID?.let { viewModel.getDistrictList(it) }
//        viewModel.refreshState.observe(viewLifecycleOwner){
//            when(it!!){
//                LocationViewModel.RefreshState.REFRESH_SUCCESS ->{
//                    districtList = viewModel.districtList
//                    viewModel.resetRefreshState()
//                }
//                LocationViewModel.RefreshState.REFRESH_FAILED ->{
//                    viewModel.resetRefreshState()
//                }
//                else ->{
//                }
//            }
//        }
//        val districtName = districtList!!.map { it.districtName }.toTypedArray()
//        binding.dropdownDist.setAdapter(ArrayAdapter(requireContext(), R.layout.drop_down, districtName))
//        binding.dropdownDist.setOnItemClickListener { parent, _, position, _ ->
//            val selectedDistrict = parent.getItemAtPosition(position) as String
//            fetchTaluks(selectedDistrict)
//        }
//    }
//
//    private fun fetchTaluks(selectedDistrictName: String){
//        selectedDistrict = districtList?.find { it.districtName == selectedDistrictName }
//        selectedDistrict?.districtID?.let { viewModel.getTaluks(it) }
//        viewModel.refreshState.observe(viewLifecycleOwner){
//            when(it!!){
//                LocationViewModel.RefreshState.REFRESH_SUCCESS ->{
//                    blockList  = viewModel.blockList
//                    viewModel.resetRefreshState()
//                }
//                LocationViewModel.RefreshState.REFRESH_FAILED ->{
//                    viewModel.resetRefreshState()
//                }
//                else ->{
//                }
//            }
//        }
//        val blockName = blockList!!.map { it.blockName }.toTypedArray()
//        binding.dropdownTaluk.setAdapter(ArrayAdapter(requireContext(), R.layout.drop_down, blockName))
//        binding.dropdownTaluk.setOnItemClickListener { parent, _, position, _ ->
//            val selectedTaluk = parent.getItemAtPosition(position) as String
//            fetchPanchayat(selectedTaluk)
//        }
//    }
//
//    private fun fetchPanchayat(selectedTalukName : String){
//        selectedBlock = blockList?.find { it.blockName == selectedTalukName }
//        selectedBlock?.blockID?.let { viewModel.getVillages(it) }
//        viewModel.refreshState.observe(viewLifecycleOwner){
//            when(it!!){
//                LocationViewModel.RefreshState.REFRESH_SUCCESS ->{
//                    villageList  = viewModel.villageList
//                    viewModel.resetRefreshState()
//                }
//                LocationViewModel.RefreshState.REFRESH_FAILED ->{
//                    viewModel.resetRefreshState()
//                }
//                else ->{
//                }
//            }
//        }
//        val villageName = villageList!!.map { it.villageName }.toTypedArray()
//        binding.dropdownPanchayat.setAdapter(ArrayAdapter(requireContext(), R.layout.drop_down, villageName))
//        binding.dropdownPanchayat.setOnItemClickListener { parent, _, position, _ ->
//            val selectedVillageName = parent.getItemAtPosition(position) as String
//            selectedVillage = villageList?.find { it.villageName == selectedVillageName }
//        }
//    }

    override fun getFragmentId(): Int {
        return R.id.fragment_add_patient_location
    }

    override fun onSubmitAction() {
        navigateNext()
    }

    override fun onCancelAction() {
//        findNavController().navigate(
//            LocationFragmentDirections.actionFhirLocationFragmentToFhirAddPatientFragment(Patient())
//        )
    }

    override fun navigateNext() {
//        findNavController().navigate(
//            LocationFragmentDirections.actionFhirLocationFragmentToFhirOtherInformationFragment()
//        )
    }
}