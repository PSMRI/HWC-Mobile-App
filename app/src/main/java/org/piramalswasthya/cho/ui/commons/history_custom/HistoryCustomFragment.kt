package org.piramalswasthya.cho.ui.commons.history_custom

import IllnessDialogFragment
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentHistoryCustomBinding
import org.piramalswasthya.cho.ui.HistoryFieldsInterface
import org.piramalswasthya.cho.ui.commons.NavigationAdapter

@AndroidEntryPoint
class HistoryCustomFragment : Fragment(R.layout.fragment_history_custom), NavigationAdapter,HistoryFieldsInterface {


    private var _binding: FragmentHistoryCustomBinding? = null
    private val binding: FragmentHistoryCustomBinding
        get() = _binding!!


    var addCount: Int = 0
    var deleteCount: Int = 0
    var illnessTag = mutableListOf<String>()

    private val viewModel:HistoryCustomViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        addCount=0
        deleteCount=0
        _binding = FragmentHistoryCustomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.btnPreviousHistory.setOnClickListener {
            openIllnessDialogBox()
        }
        addIllnessFields(addCount)
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
        addCount+=1
    }
    private fun deleteIllnessFields(tag: String){
        val fragmentManager : FragmentManager = requireActivity().supportFragmentManager
        val fragmentToDelete = fragmentManager.findFragmentByTag(tag)
        if (fragmentToDelete != null) {
            fragmentManager.beginTransaction().remove(fragmentToDelete).commit()
            illnessTag.remove(tag)
            deleteCount += 1
        }
    }
    override fun onDeleteButtonClicked(fragmentTag: String) {
        if(addCount - 1 > deleteCount) deleteIllnessFields(fragmentTag)
    }

    override fun onAddButtonClicked(fragmentTag: String) {
        addIllnessFields(addCount)
    }
    private fun openIllnessDialogBox() {
        // Create an instance of the custom dialog fragment and show it
        val dialogFragment = IllnessDialogFragment()
        dialogFragment.show(parentFragmentManager, "illness_dialog_box")
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