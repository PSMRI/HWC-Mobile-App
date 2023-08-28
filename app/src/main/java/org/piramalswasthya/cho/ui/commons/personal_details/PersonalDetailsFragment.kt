package org.piramalswasthya.cho.ui.commons.personal_details

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.adapter.PatientItemAdapter
import org.piramalswasthya.cho.databinding.FragmentPersonalDetailsBinding
import org.piramalswasthya.cho.ui.edit_patient_details_activity.EditPatientDetailsActivity


@AndroidEntryPoint
class PersonalDetailsFragment : Fragment() {

    private lateinit var viewModel: PersonalDetailsViewModel

    private lateinit var searchView: SearchView

    private var _binding: FragmentPersonalDetailsBinding? = null


    private val binding
        get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(PersonalDetailsViewModel::class.java)

        viewModel.patientObserver.observe(viewLifecycleOwner) { state ->
            when (state!!){
                PersonalDetailsViewModel.NetworkState.SUCCESS -> {
                    binding.patientListContainer.patientCount.text = viewModel.patientList.size.toString() + " Patients"
                    val itemAdapter = PatientItemAdapter(viewModel.patientList
                    ) {
                        val intent = Intent(context, EditPatientDetailsActivity::class.java)
                        intent.putExtra("patientId", it.patient.patientID);
                        startActivity(intent)
                    }
                    binding.patientListContainer.patientList.adapter = itemAdapter
                }
                else -> {

                }
            }
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // hide the soft keyboard when the navigation drawer is shown on the screen.
                searchView.clearFocus()
                true
            }

            else -> false
        }
    }

}
