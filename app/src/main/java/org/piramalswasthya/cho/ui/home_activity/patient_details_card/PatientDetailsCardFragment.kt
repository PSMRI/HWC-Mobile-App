package org.piramalswasthya.cho.ui.home_activity.patient_details_card

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.piramalswasthya.cho.R

class PatientDetailsCardFragment : Fragment() {

    companion object {
        fun newInstance() = PatientDetailsCardFragment()
    }

    private lateinit var viewModel: PatientDetailsCardViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.patient_details_card, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PatientDetailsCardViewModel::class.java)
        // TODO: Use the ViewModel
    }

}