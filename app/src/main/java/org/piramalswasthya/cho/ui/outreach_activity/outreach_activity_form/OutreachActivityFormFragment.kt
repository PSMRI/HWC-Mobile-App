package org.piramalswasthya.cho.ui.outreach_activity.outreach_activity_form

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentOutreachActivityFormBinding
import org.piramalswasthya.cho.databinding.FragmentPatientDetailsBinding
import org.piramalswasthya.cho.utils.DateTimeUtil


@AndroidEntryPoint
class OutreachActivityFormFragment : Fragment() {

    companion object {
        fun newInstance() = OutreachActivityFormFragment()
    }

    private val binding by lazy{
        FragmentOutreachActivityFormBinding.inflate(layoutInflater)
    }

    private lateinit var viewModel: OutreachActivityFormViewModel

    private lateinit var activityAdapter : ArrayAdapter<String>

    private var dateTimeUtil: DateTimeUtil = DateTimeUtil()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(OutreachActivityFormViewModel::class.java)
        activityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, viewModel.activityList)

        binding.activityDropdown.setAdapter(activityAdapter)

        dateTimeUtil.selectedDate.observe(viewLifecycleOwner){
            if(it != null){
                viewModel.dataOfActivity.value = it
                viewModel.dateOfActivityDisplay.value = DateTimeUtil.formatDate(it)
            }
        }

    }

    fun showDatePicker(view: View){
        Log.d("debuggable", "debuggable")
        dateTimeUtil.showDatePickerDialog(requireContext(), viewModel.dataOfActivity.value, null, null)
    }

}