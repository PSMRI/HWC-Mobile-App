package org.piramalswasthya.cho.ui.commons.fhir_examination_form

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentExaminationBinding
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.abdominalTextureList
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.abnormalityList
import org.piramalswasthya.cho.ui.commons.FhirFragmentService
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.commons.fhir_visit_details.VisitDetailViewModel
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.consciousnessList
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.dangerSignList
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.extentOfEdemaList
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.handednessList
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.jointsList
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.lateralityList
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.liverList
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.lymphNodeList
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.lymphTypeList
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.percussionSoundsList
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.spleenList
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.tracheaList

@AndroidEntryPoint
class ExaminationFragment: Fragment(),NavigationAdapter,FhirFragmentService {
    override var fragmentContainerId = 0
    override val fragment = this

    override val viewModel: VisitDetailViewModel by viewModels()


    override val jsonFile = "patient-visit-details-paginated.json"

    private var _binding: FragmentExaminationBinding? = null
    private val binding: FragmentExaminationBinding
        get() {
            return _binding!!
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentExaminationBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //setting the dropdowns values
        binding.consInput.setAdapter(ArrayAdapter(requireContext(), R.layout.drop_down, consciousnessList))
        binding.dangerDropVal.setAdapter(ArrayAdapter(requireContext(),R.layout.drop_down, dangerSignList))
        binding.lymphDropVal.setAdapter(ArrayAdapter(requireContext(),R.layout.drop_down, lymphNodeList))
        binding.lymphTypeDropVal.setAdapter(ArrayAdapter(requireContext(),R.layout.drop_down, lymphTypeList))
        binding.extentEdemaDropVal.setAdapter(ArrayAdapter(requireContext(),R.layout.drop_down, extentOfEdemaList))
        binding.symeticLayout.abdominalDropVal.setAdapter(ArrayAdapter(requireContext(),R.layout.drop_down, abdominalTextureList))
        binding.symeticLayout.leverDropVal.setAdapter(ArrayAdapter(requireContext(),R.layout.drop_down, liverList))
        binding.symeticLayout.spleenDropVal.setAdapter(ArrayAdapter(requireContext(),R.layout.drop_down, spleenList))
        binding.symeticLayout.tracheaDropVal.setAdapter(ArrayAdapter(requireContext(),R.layout.drop_down, tracheaList))
        binding.symeticLayout.percussionDropVal.setAdapter(ArrayAdapter(requireContext(),R.layout.drop_down, percussionSoundsList))
        binding.symeticLayout.handednessDropVal.setAdapter(ArrayAdapter(requireContext(),R.layout.drop_down, handednessList))
        binding.symeticLayout.jointDropVal.setAdapter(ArrayAdapter(requireContext(),R.layout.drop_down, jointsList))
        binding.symeticLayout.musLateralDropVal.setAdapter(ArrayAdapter(requireContext(),R.layout.drop_down, lateralityList))
        binding.symeticLayout.musAbnormalDropVal.setAdapter(ArrayAdapter(requireContext(),R.layout.drop_down, abnormalityList))
        binding.symeticLayout.uplLateralDropVal.setAdapter(ArrayAdapter(requireContext(),R.layout.drop_down, lateralityList))
        binding.symeticLayout.uplAbnormalDropVal.setAdapter(ArrayAdapter(requireContext(),R.layout.drop_down, abnormalityList))
        binding.symeticLayout.llLateralDropVal.setAdapter(ArrayAdapter(requireContext(),R.layout.drop_down, lateralityList))
        binding.symeticLayout.llAbnormalDropVal.setAdapter(ArrayAdapter(requireContext(),R.layout.drop_down, abnormalityList))

        binding.dangerRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            if(checkedId == binding.dangerYes.id) binding.dangerDropDown.visibility = View.VISIBLE
            else {
                binding.dangerDropDown.visibility = View.GONE
                binding.dangerDropVal.setText("")
            }
        }

        binding.edemaRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            if(checkedId == binding.edemaYes.id) binding.extentEdemaDropdown.visibility = View.VISIBLE
            else {
                binding.extentEdemaDropdown.visibility = View.GONE
                binding.extentEdemaDropVal.setText("")
            }
        }

        binding.lymphDropVal.setOnItemClickListener { _, _, _, _ ->
            if(binding.lymphTypeDropVal.text.isNotEmpty()){
                binding.headToeExamText.visibility = View.VISIBLE
                binding.headToeRadioGroup.visibility = View.VISIBLE
            } else{
                binding.headToeExamText.visibility = View.GONE
                binding.headToeRadioGroup.visibility = View.GONE
            }

        }
        binding.lymphTypeDropVal.setOnItemClickListener { _, _, _, _ ->
            if(binding.lymphDropVal.text.isNotEmpty()){
                binding.headToeExamText.visibility = View.VISIBLE
                binding.headToeRadioGroup.visibility = View.VISIBLE
            } else{
                binding.headToeExamText.visibility = View.GONE
                binding.headToeRadioGroup.visibility = View.GONE
            }
        }
    }

    override fun navigateNext() {
//        findNavController().navigate(
//            ExaminationFragmentDirections.actionExaminationFragmentToPrescription()
//        )
    }

    override fun getFragmentId(): Int {
        return R.id.fragment_examinations_info
    }

    override fun onSubmitAction() {
        navigateNext()
    }

    override fun onCancelAction() {
        findNavController().navigateUp()
    }
}