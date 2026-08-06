package org.piramalswasthya.cho.ui.home.rmncha.maternal_health

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.databinding.IncludeSearchBarWithCameraBinding
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.utils.FaceSearchHelper
import org.piramalswasthya.cho.utils.filterPatientsByQuery
import org.piramalswasthya.cho.utils.setupSearchTextWatcher
import org.piramalswasthya.cho.utils.updateListUI
import javax.inject.Inject

/**
 * Abstract base class for maternal health list fragments that share common patterns:
 * - Search bar with voice and camera search
 * - FaceSearchHelper for face-based patient lookup
 * - RecyclerView with empty state and count display
 * - Patient filtering by search query
 *
 * @param VB The ViewBinding type for the fragment's layout
 * @param T The domain model type for the patient list items
 */
abstract class BaseMaternalHealthListFragment<VB : ViewBinding, T : Any> : Fragment() {

    @Inject
    lateinit var patientDao: PatientDao

    private var _binding: VB? = null
    protected val binding: VB get() = _binding!!

    protected var allPatients: List<T> = emptyList()
    protected var filteredPatients: List<T> = emptyList()

    // ── Abstract contract for subclasses ──

    /** Inflate the fragment-specific ViewBinding. */
    abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    /** Return the included search bar binding. */
    abstract fun getSearchBarBinding(): IncludeSearchBarWithCameraBinding

    /** Return the RecyclerView for the patient list. */
    abstract fun getRecyclerView(): RecyclerView

    /** Return the empty state FrameLayout. */
    abstract fun getEmptyStateView(): FrameLayout

    /** Return the count TextView. */
    abstract fun getCountTextView(): TextView

    /** The string resource ID for the action bar title. */
    abstract val titleResId: Int

    /** Human-readable list name for the "not found" toast (e.g., "PNC Mother list"). */
    abstract val listDisplayName: String

    /** Log message prefix for updateUI (e.g., "Displaying PNC mothers"). */
    abstract val logMessage: String

    /** Set up the RecyclerView adapter. */
    abstract fun setupRecyclerView()

    /** Observe data from the repository and update [allPatients]. Call [onPatientsLoaded] when done. */
    abstract fun observePatients()

    /** Submit [list] to the adapter. */
    abstract fun submitListToAdapter(list: List<T>)

    /** Extract the [Patient] from a list item (used for filtering and face matching). */
    abstract fun extractPatient(item: T): Patient

    // ── FaceSearchHelper (shared) ──

    private val faceSearchHelper by lazy {
        FaceSearchHelper(
            fragment = this,
            patientDao = patientDao,
            onSpeechResult = { text -> getSearchBarBinding().search.setText(text) },
            onFaceMatchResult = { matchedPatient ->
                if (matchedPatient != null) {
                    filteredPatients = allPatients.filter {
                        extractPatient(it).patientID == matchedPatient.patientID
                    }
                    updateUI()
                    if (filteredPatients.isNotEmpty()) {
                        Toast.makeText(requireContext(), "1 matching record found", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Patient not found in this $listDisplayName", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "No matching patient found", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // ── Lifecycle ──

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = inflateBinding(inflater, container)
        faceSearchHelper // Eagerly initialise so activity-result launchers are registered
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        observePatients()
    }

    override fun onResume() {
        super.onResume()
        (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title =
            getString(titleResId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── Shared helpers ──

    private fun setupSearch() {
        val searchBar = getSearchBarBinding()
        searchBar.search.setupSearchTextWatcher { query -> filterPatients(query) }
        searchBar.searchTil.setEndIconOnClickListener { faceSearchHelper.launchSpeechToText() }
        searchBar.cameraIcon.setOnClickListener { faceSearchHelper.launchCameraSearch() }
    }

    protected fun filterPatients(query: String) {
        filteredPatients = allPatients.filterPatientsByQuery(query) { extractPatient(it) }
        updateUI()
    }

    protected fun updateUI() {
        if (_binding == null) return
        submitListToAdapter(filteredPatients)
        updateListUI(
            filteredList = filteredPatients,
            emptyStateView = getEmptyStateView(),
            recyclerView = getRecyclerView(),
            countTextView = getCountTextView(),
            resultString = getString(R.string.result),
            logMessage = logMessage
        )
    }

    /** Call after loading or refreshing [allPatients] to update the UI. */
    protected fun onPatientsLoaded() {
        filteredPatients = allPatients
        updateUI()
    }
}

