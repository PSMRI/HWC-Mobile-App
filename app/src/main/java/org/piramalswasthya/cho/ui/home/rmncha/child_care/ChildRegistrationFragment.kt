package org.piramalswasthya.cho.ui.home.rmncha.child_care

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.asLiveData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.piramalswasthya.cho.adapter.FormInputAdapter
import org.piramalswasthya.cho.configuration.ChildRegistrationDataset
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentNeonatalOutcomeBinding
import org.piramalswasthya.cho.model.InfantRegCache
import org.piramalswasthya.cho.repositories.DeliveryOutcomeRepo
import org.piramalswasthya.cho.repositories.InfantRegRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.work.WorkerUtils
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ChildRegistrationFragment : Fragment() {

    @Inject
    lateinit var infantRegRepo: InfantRegRepo
    
    @Inject
    lateinit var deliveryOutcomeRepo: DeliveryOutcomeRepo
    
    @Inject
    lateinit var patientRepo: PatientRepo
    
    @Inject
    lateinit var userRepo: UserRepo
    
    @Inject
    lateinit var preferenceDao: PreferenceDao

    private var _binding: FragmentNeonatalOutcomeBinding? = null
    private val binding get() = _binding!!

    // Using arguments directly instead of SafeArgs since we don't have nav graph entry yet
    private val patientID: String? by lazy { arguments?.getString("patientID") }
    private val babyIndex: Int by lazy { arguments?.getInt("babyIndex", 0) ?: 0 }

    private lateinit var dataset: ChildRegistrationDataset
    private lateinit var formAdapter: FormInputAdapter
    private var currentInfantReg: InfantRegCache? = null
    private var isViewOnlyMode: Boolean = false
    private var isFormReadOnly: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNeonatalOutcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Reusing the same layout as NeonatalOutcome since it's just a recycler view + buttons

        dataset = ChildRegistrationDataset(requireContext(), preferenceDao.getCurrentLanguage())

        formAdapter = createFormAdapter(readOnly = false)

        binding.rvForm.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        binding.rvForm.adapter = formAdapter

        dataset.listFlow.asLiveData().observe(viewLifecycleOwner) { elements ->
            formAdapter.submitList(elements)
        }

        setupClickListeners()
        observeAlerts()
        loadData()
    }

    private fun createFormAdapter(readOnly: Boolean): FormInputAdapter {
        return FormInputAdapter(
            formValueListener = if (readOnly) null else FormInputAdapter.FormValueListener { id, index ->
                lifecycleScope.launch {
                    dataset.updateList(id, index)
                }
            },
            isEnabled = !readOnly
        )
    }

    private fun applyScreenMode(isViewOnly: Boolean) {
        binding.btnSave.visibility = if (isViewOnly) View.GONE else View.VISIBLE
        binding.btnCancel.text = if (isViewOnly) getString(R.string.back) else getString(R.string.cancel)
        if (isFormReadOnly == isViewOnly) return
        isFormReadOnly = isViewOnly
        formAdapter = createFormAdapter(readOnly = isFormReadOnly)
        binding.rvForm.adapter = formAdapter
        formAdapter.submitList(dataset.listFlow.value)
    }

    private fun loadData() {
        val pid = patientID ?: return
        
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                
                val user = userRepo.getLoggedInUser()
                val userName = user?.userName ?: ""
                
                // Load mother patient
                val mother = patientRepo.getPatient(pid)
                
                // Load delivery outcome
                val deliveryOutcome = deliveryOutcomeRepo.getDeliveryOutcome(pid)
                
                if (deliveryOutcome == null) {
                    showError("Delivery outcome not found")
                    return@launch
                }

                // Load existing infant reg
                val existing = infantRegRepo.getInfantReg(pid, babyIndex)
                
                currentInfantReg = existing ?: InfantRegCache(
                    motherPatientID = pid,
                    babyIndex = babyIndex,
                    isActive = true,
                    createdBy = userName,
                    updatedBy = userName,
                    syncState = SyncState.UNSYNCED
                )

                isViewOnlyMode = existing?.processed == "C"
                applyScreenMode(isViewOnlyMode)
                
                dataset.setUpPage(mother, deliveryOutcome, babyIndex, currentInfantReg)
                binding.progressBar.visibility = View.GONE
                
            } catch (e: Exception) {
                Timber.e(e, "Error loading child registration data")
                showError("Error loading data: ${e.message}")
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            saveForm()
        }

        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun saveForm() {
        if (isViewOnlyMode) return
        val reg = currentInfantReg ?: return

        val invalidIndex = formAdapter.validateInput(resources, binding.rvForm)
        if (invalidIndex != -1) {
            binding.rvForm.scrollToPosition(invalidIndex)
            return
        }
        
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnSave.isEnabled = false
                binding.btnCancel.isEnabled = false
                
                dataset.mapValues(reg)
                
                val user = userRepo.getLoggedInUser()
                val userName = user?.userName ?: ""
                val currentTime = System.currentTimeMillis()

                val updated = reg.copy(
                    updatedBy = userName,
                    updatedDate = currentTime,
                    syncState = SyncState.UNSYNCED,
                    isActive = true,
                    processed = if (reg.processed == "N") "N" else "U"
                )

                infantRegRepo.saveInfantReg(updated)
                WorkerUtils.triggerInfantRegistrationSync(requireContext())
                
                if (isAdded) {
                    Toast.makeText(requireContext(), "Infant registration saved successfully", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to save child registration")
                if (isAdded) {
                    showError("Failed to save: ${e.message}")
                    binding.btnSave.isEnabled = true
                    binding.btnCancel.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun observeAlerts() {
        lifecycleScope.launch {
            dataset.alertErrorMessageFlow.collect { message ->
                message?.let {
                    if (isAdded) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert_title))
                            .setMessage(it)
                            .setPositiveButton(getString(R.string.continue_btn), null)
                            .show()
                    }
                    dataset.resetErrorMessageFlow()
                }
            }
        }
    }
    
    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        binding.progressBar.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
