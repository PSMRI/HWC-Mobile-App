package org.piramalswasthya.cho.ui.home.rmncha.child_care

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
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

        dataset = ChildBeneficiaryRegistrationDataset(
            requireContext(),
            preferenceDao.getCurrentLanguage()
        )

        formAdapter = createFormAdapter(readOnly = false)

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

                val mother = getPatientOrNull(motherId)
                if (mother == null) {
                    showError("Mother record not found")
                    return@launch
                }
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
                isViewOnlyMode = (existingChild != null) || infantReg.processed == "C"

                motherPatient = mother
                deliveryOutcome = delivery
                currentInfantReg = infantReg
                existingChildPatient = existingChild

                applyScreenMode(isViewOnlyMode)

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

    private fun createFormAdapter(readOnly: Boolean): FormInputAdapter {
        return FormInputAdapter(
            formValueListener = if (readOnly) null else FormInputAdapter.FormValueListener { id, index ->
                lifecycleScope.launch { dataset.updateList(id, index) }
            },
            isEnabled = !readOnly
        )
    }

    private fun applyScreenMode(isViewOnly: Boolean) {
        binding.btnSave.visibility = if (isViewOnly) View.GONE else View.VISIBLE

        (binding.btnCancel.layoutParams as? ConstraintLayout.LayoutParams)?.let { params ->
            if (isViewOnly) {
                params.endToStart = ConstraintLayout.LayoutParams.UNSET
                params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            } else {
                params.endToStart = R.id.btnSave
                params.endToEnd = ConstraintLayout.LayoutParams.UNSET
            }
            binding.btnCancel.layoutParams = params
        }

        if (isFormReadOnly == isViewOnly) return
        isFormReadOnly = isViewOnly
        formAdapter = createFormAdapter(readOnly = isFormReadOnly)
        binding.rvForm.adapter = formAdapter
        formAdapter.submitList(dataset.listFlow.value)
    }

    private fun saveForm() {
        if (isViewOnlyMode) return

        val validationResult = formAdapter.validateInput(resources, binding.rvForm)
        if (validationResult != -1) {
            binding.rvForm.scrollToPosition(validationResult)
            return
        }

        motherPatient ?: return
        deliveryOutcome ?: return
        val infantReg = currentInfantReg ?: return

        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnSave.isEnabled = false
                binding.btnCancel.isEnabled = false

                val now = System.currentTimeMillis()
                val userName = userRepo.getLoggedInUser()?.userName.orEmpty()
                val childGenderId = dataset.getGenderID() ?: infantReg.genderID

                val childPatient = buildOrUpdateChildPatient(
                    mother = motherPatient ?: return@launch,
                    delivery = deliveryOutcome ?: return@launch,
                    infantReg = infantReg,
                    childName = dataset.getChildFullName(),
                    childGenderId = childGenderId,
                    userName = userName
                )

                val updatedInfant = infantReg.copy(
                    childPatientID = childPatient.patientID,
                    babyName = dataset.getChildFullName().ifBlank { infantReg.babyName },
                    genderID = childGenderId,
                    updatedBy = userName,
                    updatedDate = now,
                    syncState = SyncState.UNSYNCED,
                    processed = if (infantReg.processed == "N") "N" else "U"
                )
                infantRegRepo.upsertInfantReg(updatedInfant)
                val childSyncDone = infantRegRepo.syncChildRegistration(updatedInfant)
                if (!childSyncDone) {
                    Timber.w("Child saveAll failed for motherPatientID=${updatedInfant.motherPatientID}, babyIndex=${updatedInfant.babyIndex}")
                }
                val beneficiarySyncDone = patientRepo.processPatientById(childPatient.patientID)
                if (!beneficiarySyncDone) {
                    Timber.w("Beneficiary sync failed for child patientID=${childPatient.patientID}")
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

    private suspend fun buildOrUpdateChildPatient(
        mother: Patient,
        delivery: DeliveryOutcomeCache,
        infantReg: InfantRegCache,
        childName: String,
        childGenderId: Int?,
        userName: String
    ): Patient {
        val now = Date()
        val childDob = delivery.dateOfDelivery?.let { Date(it) } ?: now
        val finalChildName = childName.trim().ifBlank {
            infantReg.babyName?.trim().orEmpty().ifBlank { "Baby" }
        }
        val firstName = finalChildName.substringBefore(" ").ifBlank { finalChildName }
        val lastName = finalChildName.substringAfter(" ", "").trim().ifBlank { "" }
        val fatherName = mother.spouseName?.takeIf { it.isNotBlank() } ?: mother.parentName
        val childMaritalStatusId = if (childGenderId == 2) 7 else null
        val childReproductiveStatusId = if (childGenderId == 2) 7 else null

        val persistedChildPatientID = infantRegRepo
            .getInfantReg(infantReg.motherPatientID, infantReg.babyIndex)
            ?.childPatientID
        val existingChild = (infantReg.childPatientID ?: persistedChildPatientID)
            ?.let { getPatientOrNull(it) }
        val childPatient = (existingChild ?: Patient(
            patientID = generateUuid(),
            firstName = firstName,
            lastName = lastName,
            dob = childDob,
            age = null,
            ageUnitID = null,
            maritalStatusID = childMaritalStatusId,
            spouseName = "",
            ageAtMarriage = null,
            phoneNo = mother.phoneNo,
            genderID = childGenderId,
            registrationDate = now,
            stateID = mother.stateID,
            districtID = mother.districtID,
            blockID = mother.blockID,
            districtBranchID = mother.districtBranchID,
            communityID = mother.communityID,
            religionID = mother.religionID,
            parentName = fatherName,
            syncState = SyncState.UNSYNCED,
            beneficiaryID = null,
            beneficiaryRegID = null,
            benImage = null,
            statusOfWomanID = childReproductiveStatusId,
            isNewAbha = false,
            healthIdDetails = null,
            labTechnicianFlag = 0,
            faceEmbedding = null
        )).copy(
            firstName = firstName,
            lastName = lastName,
            dob = childDob,
            genderID = childGenderId,
            phoneNo = mother.phoneNo,
            parentName = fatherName,
            stateID = mother.stateID,
            districtID = mother.districtID,
            blockID = mother.blockID,
            districtBranchID = mother.districtBranchID,
            communityID = mother.communityID,
            religionID = mother.religionID,
            maritalStatusID = childMaritalStatusId,
            spouseName = "",
            statusOfWomanID = childReproductiveStatusId,
            syncState = SyncState.UNSYNCED,
            registrationDate = existingChild?.registrationDate ?: now
        )

        if (existingChild == null) {
            patientRepo.insertPatient(childPatient)
        } else {
            patientRepo.updateRecord(childPatient)
        }
        return childPatient
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
