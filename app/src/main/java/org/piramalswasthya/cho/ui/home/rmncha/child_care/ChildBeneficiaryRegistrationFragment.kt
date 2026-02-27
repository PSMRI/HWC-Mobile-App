package org.piramalswasthya.cho.ui.home.rmncha.child_care

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.FormInputAdapter
import org.piramalswasthya.cho.configuration.ChildBeneficiaryRegistrationDataset
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentNeonatalOutcomeBinding
import org.piramalswasthya.cho.model.DeliveryOutcomeCache
import org.piramalswasthya.cho.model.InfantRegCache
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.repositories.DeliveryOutcomeRepo
import org.piramalswasthya.cho.repositories.InfantRegRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.utils.generateUuid
import org.piramalswasthya.cho.work.WorkerUtils
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class ChildBeneficiaryRegistrationFragment : Fragment() {

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

    private val motherPatientID: String? by lazy {
        arguments?.getString(ChildRegistrationListFragment.ARG_MOTHER_PATIENT_ID)
            ?: arguments?.getString("patientID")
    }
    private val babyIndex: Int by lazy {
        arguments?.getInt(ChildRegistrationListFragment.ARG_BABY_INDEX, 0)
            ?: arguments?.getInt("babyIndex", 0)
            ?: 0
    }
    private val childPatientIDArg: String? by lazy {
        arguments?.getString(ChildRegistrationListFragment.ARG_CHILD_PATIENT_ID)
    }

    private lateinit var dataset: ChildBeneficiaryRegistrationDataset
    private lateinit var formAdapter: FormInputAdapter

    private var currentInfantReg: InfantRegCache? = null
    private var motherPatient: Patient? = null
    private var deliveryOutcome: DeliveryOutcomeCache? = null
    private var existingChildPatient: Patient? = null

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

        dataset = ChildBeneficiaryRegistrationDataset(
            requireContext(),
            preferenceDao.getCurrentLanguage()
        )

        formAdapter = FormInputAdapter(
            formValueListener = FormInputAdapter.FormValueListener { id, index ->
                lifecycleScope.launch { dataset.updateList(id, index) }
            }
        )

        binding.rvForm.layoutManager = LinearLayoutManager(requireContext())
        binding.rvForm.adapter = formAdapter

        dataset.listFlow.asLiveData().observe(viewLifecycleOwner) { elements ->
            formAdapter.submitList(elements)
        }

        binding.btnSave.setOnClickListener { saveForm() }
        binding.btnCancel.setOnClickListener { parentFragmentManager.popBackStack() }

        loadData()
    }

    private fun loadData() {
        val motherId = motherPatientID
        if (motherId.isNullOrBlank()) {
            showError("Missing mother patient ID")
            return
        }

        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE

                val mother = patientRepo.getPatient(motherId)
                val delivery = deliveryOutcomeRepo.getDeliveryOutcome(motherId)
                val infantReg = infantRegRepo.getInfantReg(motherId, babyIndex)

                if (delivery == null) {
                    showError("Delivery outcome not found")
                    return@launch
                }
                if (infantReg == null) {
                    showError("Infant registration not found")
                    return@launch
                }

                val childPatientID = childPatientIDArg ?: infantReg.childPatientID
                val existingChild = childPatientID?.let { getPatientOrNull(it) }

                motherPatient = mother
                deliveryOutcome = delivery
                currentInfantReg = infantReg
                existingChildPatient = existingChild

                dataset.setUpPage(
                    motherPatient = mother,
                    deliveryOutcomeCache = delivery,
                    infantRegCache = infantReg,
                    existingChild = existingChild
                )

                binding.progressBar.visibility = View.GONE
            } catch (e: Exception) {
                Timber.e(e, "Error loading child beneficiary registration")
                showError("Error loading data: ${e.message}")
            }
        }
    }

    private fun saveForm() {
        val validationResult = formAdapter.validateInput(resources, binding.rvForm)
        if (validationResult != -1) {
            binding.rvForm.scrollToPosition(validationResult)
            return
        }

        val mother = motherPatient ?: return
        val delivery = deliveryOutcome ?: return
        val infantReg = currentInfantReg ?: return

        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnSave.isEnabled = false
                binding.btnCancel.isEnabled = false

                val now = System.currentTimeMillis()
                val userName = userRepo.getLoggedInUser()?.userName.orEmpty()

                val childDob = dataset.getDobMillis()?.let { Date(it) }
                    ?: delivery.dateOfDelivery?.let { Date(it) }
                    ?: Date(now)
                val childGenderId = dataset.getGenderID() ?: infantReg.genderID ?: existingChildPatient?.genderID
                val childVillageId = mother.districtBranchID ?: existingChildPatient?.districtBranchID

                if (childGenderId == null || childVillageId == null) {
                    showError("Child details are incomplete (sex/village). Please complete and save again.")
                    return@launch
                }

                val childRecord = (existingChildPatient ?: Patient(
                    patientID = generateUuid(),
                    registrationDate = Date(now),
                    syncState = SyncState.UNSYNCED
                )).copy(
                    firstName = dataset.getChildFirstName(),
                    lastName = dataset.getChildLastName(),
                    dob = childDob,
                    age = null,
                    ageUnitID = null,
                    maritalStatusID = existingChildPatient?.maritalStatusID,
                    spouseName = null,
                    ageAtMarriage = null,
                    phoneNo = mother.phoneNo ?: existingChildPatient?.phoneNo,
                    genderID = childGenderId,
                    registrationDate = existingChildPatient?.registrationDate ?: Date(now),
                    stateID = mother.stateID ?: existingChildPatient?.stateID,
                    districtID = mother.districtID ?: existingChildPatient?.districtID,
                    blockID = mother.blockID ?: existingChildPatient?.blockID,
                    districtBranchID = childVillageId,
                    communityID = mother.communityID ?: existingChildPatient?.communityID,
                    religionID = mother.religionID ?: existingChildPatient?.religionID,
                    parentName = dataset.getFatherName() ?: mother.spouseName ?: mother.parentName,
                    syncState = SyncState.UNSYNCED,
                    beneficiaryID = existingChildPatient?.beneficiaryID,
                    beneficiaryRegID = existingChildPatient?.beneficiaryRegID,
                    benImage = existingChildPatient?.benImage,
                    statusOfWomanID = existingChildPatient?.statusOfWomanID,
                    isNewAbha = existingChildPatient?.isNewAbha ?: false,
                    healthIdDetails = existingChildPatient?.healthIdDetails,
                    faceEmbedding = existingChildPatient?.faceEmbedding
                )

                if (existingChildPatient == null) {
                    patientRepo.insertPatient(childRecord)
                } else {
                    patientRepo.updateRecord(childRecord)
                }

                val updatedInfant = infantReg.copy(
                    childPatientID = childRecord.patientID,
                    babyName = dataset.getChildFullName(),
                    updatedBy = userName,
                    updatedDate = now,
                    syncState = SyncState.UNSYNCED,
                    processed = if (infantReg.processed == "N") "N" else "U"
                )
                infantRegRepo.upsertInfantReg(updatedInfant)
                if (isAdded) {
                    WorkerUtils.triggerAmritSyncWorker(requireContext())
                }

                if (isAdded) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.child_registration_saved),
                        Toast.LENGTH_SHORT
                    ).show()
                    parentFragmentManager.popBackStack()
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to save child beneficiary record")
                if (isAdded) {
                    showError("Failed to save: ${e.message}")
                }
            } finally {
                if (isAdded) {
                    binding.btnSave.isEnabled = true
                    binding.btnCancel.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private suspend fun getPatientOrNull(patientID: String): Patient? {
        return try {
            patientRepo.getPatient(patientID)
        } catch (_: Exception) {
            null
        }
    }

    private fun showError(message: String) {
        if (isAdded) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title =
            getString(R.string.child_registration)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
