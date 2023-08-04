package org.piramalswasthya.cho.ui.commons.history_custom

import org.piramalswasthya.cho.ui.commons.history_custom.dialog.IllnessDialogFragment
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentHistoryCustomBinding
import org.piramalswasthya.cho.ui.HistoryFieldsInterface
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.commons.history_custom.FieldsFragments.AAFragments
import org.piramalswasthya.cho.ui.commons.history_custom.FieldsFragments.IllnessFieldsFragment
import org.piramalswasthya.cho.ui.commons.history_custom.FieldsFragments.MedicationFragment
import org.piramalswasthya.cho.ui.commons.history_custom.FieldsFragments.PastSurgeryFragment
import org.piramalswasthya.cho.ui.commons.history_custom.dialog.AADialogFragment
import org.piramalswasthya.cho.ui.commons.history_custom.dialog.MedicationDialogFragment

@AndroidEntryPoint
class HistoryCustomFragment : Fragment(R.layout.fragment_history_custom), NavigationAdapter,HistoryFieldsInterface {

    private val AgeGroup = arrayOf(
        "12-19",
        "19-59",
        "More than 60"
    )
    private val vaccinationStatus = arrayOf(
        "Yes","No"
    )
    private val vaccType = arrayOf(
                "Covaxine",
                "Covishield",
                "Sputnik",
                "Corbevax"
    )

    private val doseTaken = arrayOf(
        "1st Dose",
        "2st Dose",
        "Precautionary/Booster Dose"
    )



    private var _binding: FragmentHistoryCustomBinding? = null
    private val binding: FragmentHistoryCustomBinding
        get() = _binding!!


    var addCountIllness: Int = 0
    var deleteCountIllness: Int = 0
    var addCountSurgery: Int = 0
    var deleteCountSurgery: Int = 0
    var addCountAA:Int =0
    var deleteCountAA:Int=0
    var addCountM:Int =0
    var deleteCountM:Int=0
    var illnessTag = mutableListOf<String>()
    var surgeryTag = mutableListOf<String>()
    var aaTag = mutableListOf<String>()
    var mTag = mutableListOf<String>()

    private val viewModel:HistoryCustomViewModel by viewModels()
    private lateinit var dropdownAgeG: AutoCompleteTextView
    private lateinit var dropdownVS: AutoCompleteTextView
    private lateinit var dropdownVT: AutoCompleteTextView
    private lateinit var dropdownDT: AutoCompleteTextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        addCountIllness=0
        deleteCountIllness=0
        addCountSurgery=0
        deleteCountSurgery=0
        addCountAA=0
        deleteCountAA=0
        addCountM=0
        deleteCountM=0
        _binding = FragmentHistoryCustomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dropdownAgeG = binding.ageGrText
        dropdownVS = binding.vStatusText
        dropdownVT = binding.vTypeText
        dropdownDT = binding.doseTakenText
        val ageAAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, AgeGroup)
        dropdownAgeG.setAdapter(ageAAdapter)
        val vacAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, vaccinationStatus)
        dropdownVS.setAdapter(vacAdapter)
        val vacTAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, vaccType)
        dropdownVT.setAdapter(vacTAdapter)
        val doseAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, doseTaken)
        dropdownDT.setAdapter(doseAdapter)
        binding.btnPreviousHistory.setOnClickListener {
            openIllnessDialogBox()
        }
        binding.btnPreviousHistoryAA.setOnClickListener {
            openIllnessDialogBox()
        }
        binding.btnPreviousHistoryMedical.setOnClickListener {
            openIllnessDialogBox()
        }
        addIllnessFields(addCountIllness)
        addSurgeryFields(addCountSurgery)
        addAAFields(addCountAA)
        addMFields(addCountM)
    }
    private fun addMFields(count:Int){
        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction :FragmentTransaction = fragmentManager.beginTransaction()
        val mFields = MedicationFragment()
        val tag = "Extra m$count"
        mFields.setFragmentTag(tag)
        mFields.setListener(this)
        fragmentTransaction.add(binding.medicationExtra.id,mFields,tag)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
        mTag.add(tag)
        addCountM+=1
    }
    private fun addAAFields(count:Int){
        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction :FragmentTransaction = fragmentManager.beginTransaction()
        val aaFields = AAFragments()
        val tag = "Extra AA$count"
        aaFields.setFragmentTag(tag)
        aaFields.setListener(this)
        fragmentTransaction.add(binding.associatedAilmentsExtra.id,aaFields,tag)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
        aaTag.add(tag)
        addCountAA+=1
    }

    private fun addSurgeryFields(count:Int){
        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction :FragmentTransaction = fragmentManager.beginTransaction()
        val surgeryFields = PastSurgeryFragment()
        val tag = "Extra Surgery$count"
        surgeryFields.setFragmentTag(tag)
        surgeryFields.setListener(this)
        fragmentTransaction.add(binding.pastSurgeryExtra.id,surgeryFields,tag)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
        surgeryTag.add(tag)
        addCountSurgery+=1
    }

    private fun addIllnessFields(count:Int){
        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction :FragmentTransaction = fragmentManager.beginTransaction()
        val illnessFields = IllnessFieldsFragment()
        val tag = "Extra Illness_$count"
        illnessFields.setFragmentTag(tag)
        illnessFields.setListener(this)
        fragmentTransaction.add(binding.pastIllnessExtra.id,illnessFields,tag)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
        illnessTag.add(tag)
        addCountIllness+=1
    }
    private fun deleteAAFields(tag: String){
        val fragmentManager : FragmentManager = requireActivity().supportFragmentManager
        val fragmentToDelete = fragmentManager.findFragmentByTag(tag)
        if (fragmentToDelete != null) {
            fragmentManager.beginTransaction().remove(fragmentToDelete).commit()
            aaTag.remove(tag)
            deleteCountAA += 1
        }
    }
    private fun deleteMFields(tag: String){
        val fragmentManager : FragmentManager = requireActivity().supportFragmentManager
        val fragmentToDelete = fragmentManager.findFragmentByTag(tag)
        if (fragmentToDelete != null) {
            fragmentManager.beginTransaction().remove(fragmentToDelete).commit()
            mTag.remove(tag)
            deleteCountM += 1
        }
    }
    private fun deleteIllnessFields(tag: String){
        val fragmentManager : FragmentManager = requireActivity().supportFragmentManager
        val fragmentToDelete = fragmentManager.findFragmentByTag(tag)
        if (fragmentToDelete != null) {
            fragmentManager.beginTransaction().remove(fragmentToDelete).commit()
            illnessTag.remove(tag)
            deleteCountIllness += 1
        }
    }
    private fun deleteSurgeryFields(tag: String){
        val fragmentManager : FragmentManager = requireActivity().supportFragmentManager
        val fragmentToDelete = fragmentManager.findFragmentByTag(tag)
        if (fragmentToDelete != null) {
            fragmentManager.beginTransaction().remove(fragmentToDelete).commit()
            surgeryTag.remove(tag)
            deleteCountSurgery += 1
        }
    }
    override fun onDeleteButtonClickedAA(fragmentTag: String) {
        if(addCountAA - 1 > deleteCountAA) deleteAAFields(fragmentTag)
    }

    override fun onAddButtonClickedAA(fragmentTag: String) {
        addAAFields(addCountAA)
    }
    override fun onDeleteButtonClickedM(fragmentTag: String) {
        if(addCountM - 1 > deleteCountM) deleteMFields(fragmentTag)
    }

    override fun onAddButtonClickedM(fragmentTag: String) {
        addMFields(addCountM)
    }
    override fun onDeleteButtonClickedSurgery(fragmentTag: String) {
        if(addCountSurgery - 1 > deleteCountSurgery) deleteSurgeryFields(fragmentTag)
    }

    override fun onAddButtonClickedSurgery(fragmentTag: String) {
        addSurgeryFields(addCountSurgery)
    }
    override fun onDeleteButtonClickedIllness(fragmentTag: String) {
        if(addCountIllness - 1 > deleteCountIllness) deleteIllnessFields(fragmentTag)
    }

    override fun onAddButtonClickedIllness(fragmentTag: String) {
        addIllnessFields(addCountIllness)
    }
    private fun openIllnessDialogBox() {
        // Create an instance of the custom dialog fragment and show it
        val dialogFragment = IllnessDialogFragment()
        dialogFragment.show(parentFragmentManager, "illness_dialog_box")
    }
    private fun openAADialogBox() {
        // Create an instance of the custom dialog fragment and show it
        val dialogFragment = AADialogFragment()
        dialogFragment.show(parentFragmentManager, "aa_dialog_box")
    }
    private fun openMDialogBox() {
        // Create an instance of the custom dialog fragment and show it
        val dialogFragment = MedicationDialogFragment()
        dialogFragment.show(parentFragmentManager, "medication_dialog_box")
    }
    override fun getFragmentId(): Int {
      return R.id.fragment_history_custom
    }

    override fun onSubmitAction() {
      navigateNext()
    }

    override fun onCancelAction() {
        findNavController().navigate(
            HistoryCustomFragmentDirections.actionHistoryCustomFragmentToFhirVisitDetailsFragment()
        )
    }
    fun navigateNext(){
        findNavController().navigate(
            HistoryCustomFragmentDirections.actionHistoryCustomFragmentToFhirVitalsFragment()
        )
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}