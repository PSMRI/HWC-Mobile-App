package org.piramalswasthya.cho.ui.commons.personal_details

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.PatientItemAdapter
import org.piramalswasthya.cho.databinding.FragmentPersonalDetailsBinding
import org.piramalswasthya.cho.network.ESanjeevaniApiService
import org.piramalswasthya.cho.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.cho.ui.edit_patient_details_activity.EditPatientDetailsActivity
import javax.inject.Inject


@AndroidEntryPoint
class PersonalDetailsFragment : Fragment() {
    @Inject
    lateinit var apiService : ESanjeevaniApiService
    private lateinit var viewModel: PersonalDetailsViewModel
    private var itemAdapter : PatientItemAdapter? = null

    private var _binding: FragmentPersonalDetailsBinding? = null
    private var patientCount : Int = 0

    private val binding
        get() = _binding!!

    private val abhaDisclaimer by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.beneficiary_abha_number))
            .setMessage("it")
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
            .create()
    }
    private val searchPrompt by lazy {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.note_ben_reg))
            .setMessage(getString(R.string.please_search_for_beneficiary))
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
                binding.search.requestFocus()
                dialog.dismiss() }
            .create()
    }
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        searchPrompt.show()
        _binding = FragmentPersonalDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(PersonalDetailsViewModel::class.java)

        viewModel.patientObserver.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                PersonalDetailsViewModel.NetworkState.SUCCESS -> {
                     itemAdapter = context?.let {
                        PatientItemAdapter(
                            apiService,
                            it,
                            onItemClicked = {
                                val intent = Intent(context, EditPatientDetailsActivity::class.java)
                                intent.putExtra("patientId", it.patient.patientID);
                                startActivity(intent)
                            },
                            clickListener = PatientItemAdapter.BenClickListener { benId ->
                                Log.d("ben click listener", "ben click listener")
                                checkAndGenerateABHA(benId!!)
                            },
                            showAbha = true
                        )
                    }
                    binding.patientListContainer.patientList.adapter = itemAdapter
                    lifecycleScope.launch {
                        viewModel.patientList?.collect {
                            itemAdapter?.submitList(it)
                            binding.patientListContainer.patientCount.text =
                        itemAdapter?.itemCount.toString() + getString(
                            R.string.patients_cnt_display)
                            patientCount = it.size
                        }
                    }

                }

                else -> {

                }
            }

            viewModel.abha.observe(viewLifecycleOwner) {
                it.let {
                    if (it != null) {
                        abhaDisclaimer.setMessage(it)
                        abhaDisclaimer.show()
                    }
                }
            }

            viewModel.benRegId.observe(viewLifecycleOwner) {
                if (it != null) {
                    val intent = Intent(requireActivity(), AbhaIdActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    intent.putExtra("benId", viewModel.benId.value)
                    intent.putExtra("benRegId", it)
                    requireActivity().startActivity(intent)
                    viewModel.resetBenRegId()
                }
            }


//        }

            val searchTextWatcher = object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun afterTextChanged(p0: Editable?) {
                    viewModel.filterText(p0?.toString() ?: "")
                    binding.patientListContainer.patientCount.text =
                        patientCount.toString()+ getString(
                            R.string.patients_cnt_display)

                }

            }
            binding.search.setOnFocusChangeListener { searchView, b ->
                if (b)
                    (searchView as EditText).addTextChangedListener(searchTextWatcher)
                else
                    (searchView as EditText).removeTextChangedListener(searchTextWatcher)

            }
        }
    }
    private fun checkAndGenerateABHA(benId: Long) {
        Log.d("checkAndGenerateABHA click listener","checkAndGenerateABHA click listener")
        viewModel.fetchAbha(benId)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            android.R.id.home -> {
//                // hide the soft keyboard when the navigation drawer is shown on the screen.
//                binding.search.clearFocus()
//                true
//            }
//
//            else -> false
//        }
//    }

}
