package org.piramalswasthya.cho.ui.commons.personal_details

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.PatientItemAdapter
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentPersonalDetailsBinding
import org.piramalswasthya.cho.network.ESanjeevaniApiService
import org.piramalswasthya.cho.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.cho.ui.edit_patient_details_activity.EditPatientDetailsActivity
import org.piramalswasthya.cho.ui.home.HomeViewModel
import javax.inject.Inject


@AndroidEntryPoint
class PersonalDetailsFragment : Fragment() {
    @Inject
    lateinit var apiService : ESanjeevaniApiService
    private lateinit var viewModel: PersonalDetailsViewModel
    private lateinit var homeviewModel: HomeViewModel
    private var itemAdapter : PatientItemAdapter? = null

    @Inject
    lateinit var preferenceDao: PreferenceDao

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
//    private val parentViewModel: HomeViewModel by lazy {
//        ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
//    }
//private val parentViewModel: HomeViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        HomeViewModel.resetSearchBool()
        _binding = FragmentPersonalDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        homeviewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
//        parentViewModel.searchBool.observe((viewLifecycleOwner)){
        HomeViewModel.searchBool.observe(viewLifecycleOwner){
            bool ->
            when(bool!!) {
                true ->{
//                    binding.search.post {
//                        lifecycleScope.launch {
//                            withContext(Dispatchers.IO){
//                                delay(5000)
//                            }
                            binding.search.requestFocus()
                            activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

                        }

//                    }
//                }
//                    Handler(Looper.getMainLooper()).postDelayed(
//                    {
//                    binding.search.requestFocus()
//            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
//            imm?.showSoftInput(binding.search, InputMethodManager.SHOW_FORCED);
//                }
//            , 100)
                else -> {}
            }

        }

        viewModel = ViewModelProvider(this).get(PersonalDetailsViewModel::class.java)
        viewModel.patientObserver.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                PersonalDetailsViewModel.NetworkState.SUCCESS -> {
                     itemAdapter = context?.let { it ->
                         PatientItemAdapter(
                            apiService,
                            it,
//                            onItemClicked = {
//                                val intent = Intent(context, EditPatientDetailsActivity::class.java)
//                                intent.putExtra("patientId", it.patient.patientID);
//                                startActivity(intent)
//                            },
                            clickListener = PatientItemAdapter.BenClickListener(
                            {
                                benVisitInfo ->
                                    if(benVisitInfo.nurseFlag == null){
                                        val intent = Intent(context, EditPatientDetailsActivity::class.java)
                                        intent.putExtra("benVisitInfo", benVisitInfo);
                                        startActivity(intent)
                                    }
                                    else if(benVisitInfo.nurseFlag == 9 && benVisitInfo.doctorFlag == 1){
                                        val intent = Intent(context, EditPatientDetailsActivity::class.java)
                                        intent.putExtra("benVisitInfo", benVisitInfo);
                                        startActivity(intent)
                                    }
                                    else if(benVisitInfo.nurseFlag == 9 && benVisitInfo.doctorFlag == 2){
                                         Toast.makeText(
                                            requireContext(),
                                            resources.getString(R.string.pendingForLabtech),
                                            Toast.LENGTH_SHORT
                                         ).show()
                                    }
                                    else if(benVisitInfo.nurseFlag == 9 && benVisitInfo.doctorFlag == 3){
                                        Toast.makeText(
                                            requireContext(),
                                            "Lab test pending",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    else if(benVisitInfo.nurseFlag == 9 && benVisitInfo.doctorFlag == 9){
                                        Toast.makeText(
                                            requireContext(),
                                            resources.getString(R.string.flowCompleted),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                            },
                            {
                                benId ->
                                    Log.d("ben click listener", "ben click listener")
                                    checkAndGenerateABHA(benId!!)
                            }
                         ),
                            showAbha = true
                        )
                    }
                    binding.patientListContainer.patientList.adapter = itemAdapter
                    if(preferenceDao.isUserOnlyDoctorOrMo()) {
                        lifecycleScope.launch {
                            viewModel.patientListForDoctor?.collect { it ->
                                itemAdapter?.submitList(it.sortedByDescending { it.patient.registrationDate})
                                binding.patientListContainer.patientCount.text =
                                    itemAdapter?.itemCount.toString() + getString(
                                        R.string.patients_cnt_display
                                    )
                                patientCount = it.size
                            }
                        }
                    }
                    else{
                        lifecycleScope.launch {
                            viewModel.patientListForNurse?.collect { it ->
                                itemAdapter?.submitList(it.sortedByDescending { it.patient.registrationDate})
                                binding.patientListContainer.patientCount.text =
                                    itemAdapter?.itemCount.toString() + getString(
                                        R.string.patients_cnt_display
                                    )
                                patientCount = it.size
                            }
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
