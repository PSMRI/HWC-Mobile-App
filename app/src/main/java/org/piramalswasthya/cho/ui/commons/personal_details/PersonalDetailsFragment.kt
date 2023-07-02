package org.piramalswasthya.cho.ui.commons.personal_details

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import org.piramalswasthya.cho.CHOApplication
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.PatientItemRecyclerViewAdapter
import org.piramalswasthya.cho.databinding.FragmentPersonalDetailsBinding
import org.piramalswasthya.cho.ui.edit_patient_details_activity.EditPatientDetailsActivity
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import org.piramalswasthya.cho.ui.home_activity.HomeActivityDirections
import timber.log.Timber

class PersonalDetailsFragment : Fragment() {
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientListViewModel: PersonalDetailsViewModel
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
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = resources.getString(R.string.title_patient_list)
            setDisplayHomeAsUpEnabled(true)
        }

        binding.patientListContainer.patientList.adapter = PatientItemRecyclerViewAdapter(){
            val intent = Intent(context, EditPatientDetailsActivity::class.java)
            startActivity(intent)
            Log.d("Rv click", "$it")
        }
        fhirEngine = CHOApplication.fhirEngine(requireContext())
        patientListViewModel =
                ViewModelProvider(
                        this,
                        PersonalDetailsViewModel.PatientListViewModelFactory(
                                requireActivity().application,
                                fhirEngine
                        )
                )
                        .get(PersonalDetailsViewModel::class.java)
//        val recyclerView: RecyclerView = binding.patientListContainer.patientList
//        val adapter = PatientItemRecyclerViewAdapter(this::onPatientItemClicked)
//        recyclerView.adapter = adapter
//        recyclerView.addItemDecoration(
//                DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL).apply {
//                    setDrawable(ColorDrawable(Color.LTGRAY))
//                }
//        )
//
//        patientListViewModel.liveSearchedPatients.observe(viewLifecycleOwner) {
//            Timber.d("Submitting ${it.count()} patient records")
//            adapter.submitList(it)
//        }


        patientListViewModel.patientCount.observe(viewLifecycleOwner) {
            binding.patientListContainer.patientCount.text = "$it Patient(s)"
        }
        patientListViewModel.liveSearchedPatients.observe(viewLifecycleOwner) {
            (binding.patientListContainer.patientList.adapter as PatientItemRecyclerViewAdapter)
                    .submitList(it)
        }

//        binding.patientListContainer.patientList.setOnClickListener {
//            Timber.tag("Outreach username").i("view clicked!!!!!");
//            val intent = Intent(context, EditPatientDetailsActivity::class.java)
//            startActivity(intent)
//        }

        searchView = binding.search
        searchView.setOnQueryTextListener(
                object : SearchView.OnQueryTextListener {
                    override fun onQueryTextChange(newText: String): Boolean {
                        patientListViewModel.searchPatientsByName(newText)
                        return true
                    }

                    override fun onQueryTextSubmit(query: String): Boolean {
                        patientListViewModel.searchPatientsByName(query)
                        return true
                    }
                }
        )
        searchView.setOnQueryTextFocusChangeListener { view, focused ->
            if (!focused) {
                // hide soft keyboard
                (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                        .hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
        requireActivity()
                .onBackPressedDispatcher.addCallback(
                        viewLifecycleOwner,
                        object : OnBackPressedCallback(true) {
                            override fun handleOnBackPressed() {
                                if (searchView.query.isNotEmpty()) {
                                    searchView.setQuery("", true)
                                } else {
                                    isEnabled = false
                                    activity?.onBackPressed()
                                }
                            }
                        }
                )
        setHasOptionsMenu(true)

//        lifecycleScope.launch {
//            mainActivityViewModel.pollState.collect {
//                Timber.d("onViewCreated: pollState Got status $it")
//                // After the sync is successful, update the patients list on the page.
//                if (it is State.Finished) {
//                    patientListViewModel.searchPatientsByName(searchView.query.toString().trim())
//                }
//            }
//        }
    }

    //    private fun onPatientItemClicked(patientItem: PersonalDetailsViewModel.PatientItem) {
//        findNavController()
//            .navigate(PatientListFragmentDirections.navigateToProductDetail(patientItem.resourceId))
//    }
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

//    private fun onPatientItemClicked(patientItem: PersonalDetailsViewModel.PatientItem) {
//        findNavController()
//                .navigate(HomeActivityDirections.navigate(patientItem.resourceId))
//    }

}
