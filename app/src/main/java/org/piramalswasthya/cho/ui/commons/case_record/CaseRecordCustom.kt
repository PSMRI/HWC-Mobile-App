package org.piramalswasthya.cho.ui.commons.case_record


import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.util.TypedValue
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.method.LinkMovementMethod
import android.view.Gravity
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.CHOCaseRecordItemAdapter
import org.piramalswasthya.cho.adapter.ChiefComplaintMultiAdapter
import org.piramalswasthya.cho.adapter.DiagnosisAdapter
import org.piramalswasthya.cho.adapter.PrescriptionAdapter
import org.piramalswasthya.cho.adapter.RecyclerViewItemChangeListenerD
import org.piramalswasthya.cho.adapter.RecyclerViewItemChangeListenersP
import org.piramalswasthya.cho.adapter.TempDropdownAdapter
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.CaseRecordCustomLayoutBinding
import org.piramalswasthya.cho.model.BenFlow
import org.piramalswasthya.cho.model.ChiefComplaintDB
import org.piramalswasthya.cho.model.CounsellingProvided
import org.piramalswasthya.cho.model.DiagnosisCaseRecord
import org.piramalswasthya.cho.model.DiagnosisValue
import org.piramalswasthya.cho.model.InvestigationCaseRecord
import org.piramalswasthya.cho.model.ItemMasterList
import org.piramalswasthya.cho.model.MasterDb
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.model.PatientVitalsModel
import org.piramalswasthya.cho.model.PrescriptionCaseRecord
import org.piramalswasthya.cho.model.PrescriptionTemplateDB
import org.piramalswasthya.cho.model.PrescriptionValues
import org.piramalswasthya.cho.model.PrescriptionValuesForTemplate
import org.piramalswasthya.cho.model.ProceduresMasterData
import org.piramalswasthya.cho.model.ReferralFollowUpFields
import org.piramalswasthya.cho.model.UserDomain
import org.piramalswasthya.cho.model.VisitDB
import org.piramalswasthya.cho.model.VitalsMasterDb
import org.piramalswasthya.cho.repositories.PrescriptionTemplateRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.frequencyMap
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.instructionDropdownList
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.medicalReferDropdownVal
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.medicationFrequencyList
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.tabletDosageList
import org.piramalswasthya.cho.ui.commons.DropdownConst.Companion.unitVal
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.utils.Constants.pattern
import org.piramalswasthya.cho.utils.HelperUtil
import org.piramalswasthya.cho.utils.HelperUtil.disableTextInputLayout
import org.piramalswasthya.cho.utils.generateIntFromUuid
import org.piramalswasthya.cho.utils.generateUuid
import org.piramalswasthya.cho.utils.nullIfEmpty
import org.piramalswasthya.cho.work.WorkerUtils
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Date
import javax.inject.Inject


@AndroidEntryPoint
class CaseRecordCustom : Fragment(R.layout.case_record_custom_layout), NavigationAdapter {
    companion object {
        private const val DEFAULT_DURATION_UNIT = "Day(s)"
    }

    private var _binding: CaseRecordCustomLayoutBinding? = null
    private val binding: CaseRecordCustomLayoutBinding
        get() = _binding!!

    private val viewModel: CaseRecordViewModel by viewModels()

    private val viewModeltemplate: TemplateBottomSheetViewModel by viewModels<TemplateBottomSheetViewModel>()

    @Inject
    lateinit var preferenceDao: PreferenceDao

    @Inject
    lateinit var userRepo: UserRepo

    @Inject
    lateinit var prescriptionTemplateRepo: PrescriptionTemplateRepo

    private val initialItemD = DiagnosisValue()
    private val itemListD = mutableListOf(initialItemD)
    private val initialItemP = PrescriptionValues()
    private val itemListP = mutableListOf(initialItemP)
    private val initialItemTemp = PrescriptionValuesForTemplate()
    private val tempList = mutableListOf(initialItemTemp)
    private lateinit var dAdapter: DiagnosisAdapter
    private lateinit var chAdapter: ChiefComplaintMultiAdapter
    private lateinit var pAdapter: PrescriptionAdapter

    private var testNameMap = emptyMap<Int, String>()
    private var investigationBD: InvestigationCaseRecord? = null
    private var referNameMap = emptyMap<Int, String>()
    private var selectedTestName = mutableListOf<Int>()
    var familyM: MaterialCardView? = null
    var selectF: TextView? = null
    private val instructionDropdown = instructionDropdownList
    private val tempDBVal = ArrayList<PrescriptionTemplateDB?>()
    private val formMListVal = ArrayList<ItemMasterList>()
    private var formForFilter = ArrayList<ItemMasterList>()
    private val counsellingTypes = ArrayList<CounsellingProvided>()
    private val procedureDropdown = ArrayList<ProceduresMasterData>()
    private val frequencyListVal = medicationFrequencyList
    private lateinit var tempDropdownAdapter: TempDropdownAdapter
    private val referDropdownVal = medicalReferDropdownVal
    private val unitListVal = unitVal
    private val dosage = tabletDosageList
    private var masterDb: MasterDb? = null
    private lateinit var patientId: String
    private lateinit var benVisitInfo: PatientDisplayWithVisitInfo
    private var patId = ""
    private lateinit var referDropdown: AutoCompleteTextView
    private var doctorFlag = 2
    private var pharmacistFlag = 0
    private var viewRecordFragment: Boolean? = null
    private var isFlowComplete: Boolean? = null
    private var isFollowupVisit: Boolean? = null
    var isAddTemplateClicked = false
    private val benFlowMap = mutableMapOf<Int, BenFlow>()
    private var benFlowListCache: List<BenFlow> = emptyList()
    private var patientVisitNosCache: List<Int> = emptyList()
    private var effectivePharmacistFlagForVisibility: Int? = null
    private var isAlreadyFilledReadOnlyForVisibility: Boolean = false
    private var dispensedLockedPrescriptionCount: Int = 0
    private var isFreshCaseEntryFromVisitDetails: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = CaseRecordCustomLayoutBinding.inflate(inflater, container, false)
        val editText = binding.externalI
        editText.requestFocus()
        return binding.root
    }

    private fun setCaseEditorVisibility(isVisible: Boolean) {
        val visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.plusButtonD.visibility = visibility
        binding.plusButtonP.visibility = visibility
        binding.useTempForFields.visibility = visibility
        binding.tvAddTemplateTitle.visibility = visibility
        binding.tempName.visibility = visibility
        binding.saveTemplate.visibility = visibility
        binding.deleteTemp.visibility = visibility
        binding.externalI.visibility = visibility
        binding.testName.visibility = visibility
        binding.referReason.visibility = visibility
        binding.referDropdown.visibility = visibility
        binding.textReferHeading.visibility = visibility
    }

    private fun hideReferSummaryLabels() {
        binding.referDateLabel.visibility = View.GONE
        binding.referToLabel.visibility = View.GONE
        binding.referalReasonLabel.visibility = View.GONE
    }

    private fun applyEditableCaseUi(btnSubmit: Button?, btnCancel: Button?, submitTextRes: Int) {
        btnSubmit?.visibility = View.VISIBLE
        btnSubmit?.text = getString(submitTextRes)
        btnCancel?.visibility = View.VISIBLE
        btnCancel?.text = getString(R.string.close)
        setCaseEditorVisibility(true)
    }

    private fun applyReadOnlyCaseUi(btnSubmit: Button?, btnCancel: Button?) {
        btnSubmit?.visibility = View.GONE
        btnCancel?.visibility = View.VISIBLE
        btnCancel?.text = getString(R.string.close)
        setCaseEditorVisibility(false)
        hideReferSummaryLabels()
    }

    private fun isDoctorWorkflowRole(): Boolean {
        return preferenceDao.isDoctorSelected() ||
                (preferenceDao.isUserCHO() && preferenceDao.isNurseSelected()) ||
                preferenceDao.isRegistrarSelected()
    }

    private fun isDoctorExistingVisitFlow(): Boolean {
        return (isDoctorWorkflowRole() && !isFreshCaseEntryFromVisitDetails) || viewRecordFragment == true
    }

    private fun isOtherCphcCategory(category: String?): Boolean {
        val c = category?.trim().orEmpty()
        return c.equals(getString(R.string.other_cphc_services), ignoreCase = true)
    }

    private fun updateShowCphcDetailsButtonVisibility(visitInfo: PatientDisplayWithVisitInfo?) {
        val visitCategory = visitInfo?.visitCategory
        val masterDbCategory =
            (arguments?.getSerializable("MasterDb") as? MasterDb)?.visitMasterDb?.category

        // Fast local check first
        if (isOtherCphcCategory(visitCategory) || isOtherCphcCategory(masterDbCategory)) {
            binding.btnShowCphcDetails.visibility = View.VISIBLE
            return
        }

        // Doctor-tab fallback: fetch visit category from Visit_DB for this patient+visit.
        val patientID = visitInfo?.patient?.patientID
        val benVisitNo = visitInfo?.benVisitNo
        if (patientID.isNullOrBlank() || benVisitNo == null || benVisitNo <= 0) {
            binding.btnShowCphcDetails.visibility = View.GONE
            return
        }

        lifecycleScope.launch {
            val dbCategory = viewModel.getVisitCategoryByPatientAndVisit(patientID, benVisitNo)
            binding.btnShowCphcDetails.visibility =
                if (isOtherCphcCategory(dbCategory)) View.VISIBLE else View.GONE
        }
    }

    private val onBackPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onCancelAction()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, onBackPressedCallback)
        super.onViewCreated(view, savedInstanceState)
        familyM = binding.testName
        selectF = binding.selectF
        referDropdown = binding.referDropdownText
        binding.btnShowCphcDetails.setOnClickListener { showPreviousCphcDetailsPopup() }


        binding.tvAddTemplateTitle.setOnClickListener {

            if(!isAddTemplateClicked){
                val drawable = resources.getDrawable(R.drawable.ic_down_angle)
                binding.tvAddTemplateTitle.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)

                isAddTemplateClicked = true
                binding.buttonLayout.visibility = View.VISIBLE
            }else{
                val drawable = resources.getDrawable(R.drawable.ic_up_angle)
                binding.tvAddTemplateTitle.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)

                isAddTemplateClicked = false
                binding.buttonLayout.visibility = View.GONE
            }
        }

        val tableLayout = binding.tableLayout

        viewRecordFragment = arguments?.getBoolean("viewRecord")
        isFlowComplete = arguments?.getBoolean("isFlowComplete")
        isFollowupVisit = arguments?.getBoolean("isFollowupVisit")
        var isClosedViewOnly = (viewRecordFragment == true && isFlowComplete == true)
        isFreshCaseEntryFromVisitDetails =
            // CHO-role and Register-role enter the fresh-case-entry flow.
            viewRecordFragment != true &&
                    (arguments?.getSerializable("MasterDb") as? MasterDb) != null

        // Use DB value for pharmacist_flag in edit path so completed (lab+pharmacist) cases show correct UI even if intent was stale
        var effectivePharmacistFlag: Int? = null

        if (viewRecordFragment == true) {
            benVisitInfo = arguments?.getSerializable("benVisitInfo") as PatientDisplayWithVisitInfo
            updateShowCphcDetailsButtonVisibility(benVisitInfo)
            // Pending pharmacist cycle is not a closed case, even if stale extras say flowComplete=true.
            if ((benVisitInfo.pharmacist_flag ?: 0) == 1) {
                isClosedViewOnly = false
            }
            // Lab reviewed + medicine dispensed (doctorFlag=3, pharmacist_flag=9) is editable review cycle.
            if (benVisitInfo.nurseFlag == 9 &&
                benVisitInfo.doctorFlag == 3 &&
                (benVisitInfo.pharmacist_flag ?: 0) == 9 &&
                isDoctorWorkflowRole()
            ) {
                isClosedViewOnly = false
            }
            effectivePharmacistFlag = benVisitInfo.pharmacist_flag
            effectivePharmacistFlagForVisibility = effectivePharmacistFlag
            viewModel.getFormMaster()
            val btnSubmit = activity?.findViewById<Button>(R.id.btnSubmit)
            val btnCancel = activity?.findViewById<Button>(R.id.btnCancel)
            btnSubmit?.visibility = View.GONE
            btnCancel?.text = getString(R.string.close)
            btnCancel?.visibility = View.VISIBLE

            if (isFlowComplete == true){
                binding.patientList.visibility = View.VISIBLE
            }else{
                binding.patientList.visibility = View.GONE
            }

            setCaseEditorVisibility(false)

            getVisitResObserver(benVisitInfo)
            if (isClosedViewOnly) {
                applyReadOnlyCaseUi(btnSubmit, btnCancel)
            } else if( benVisitInfo.nurseFlag == 9 && benVisitInfo.doctorFlag == 3 && isDoctorWorkflowRole() && benVisitInfo.pharmacist_flag != 9 ){
                applyEditableCaseUi(btnSubmit, btnCancel, R.string.submit)

            } else if ( benVisitInfo.nurseFlag == 9 && benVisitInfo.doctorFlag == 3 && isDoctorWorkflowRole() && benVisitInfo.pharmacist_flag == 9 ) {
                // Lab done + medicine dispensed: doctor reviews results.
                // Doctor can add new tests/medicines (starts new cycle) OR submit without changes (closes case with confirmation).
                applyEditableCaseUi(btnSubmit, btnCancel, R.string.close_case_btn)

            } else if ( benVisitInfo.nurseFlag == 9 && benVisitInfo.doctorFlag == 1 && isDoctorWorkflowRole() && benVisitInfo.pharmacist_flag != 9 )
            {
                applyEditableCaseUi(btnSubmit, btnCancel, R.string.submit)

            } else if ( benVisitInfo.nurseFlag == 9 && benVisitInfo.doctorFlag == 9 && isDoctorWorkflowRole() && benVisitInfo.pharmacist_flag == 1 ) {
                // Medicine pending at pharmacist (not dispensed yet): allow doctor correction/update.
                applyEditableCaseUi(btnSubmit, btnCancel, R.string.submit)

            } else {
                // Already filled (lab + pharmacist submitted): only Close button; no Submit/Cancel; no plus icons; no refer section
                applyReadOnlyCaseUi(btnSubmit, btnCancel)
            }


            lifecycleScope.launch {
                val isDoctorCanEditInView = benVisitInfo.nurseFlag == 9 &&
                        ((benVisitInfo.doctorFlag == 1 || benVisitInfo.doctorFlag == 3) ||
                                (benVisitInfo.doctorFlag == 9 && benVisitInfo.pharmacist_flag == 1)) &&
                        benVisitInfo.pharmacist_flag != 9 &&
                        isDoctorWorkflowRole() &&
                        !isClosedViewOnly
                if (isFollowupVisit == true && isDoctorCanEditInView){
                    btnSubmit?.visibility = View.VISIBLE
                    // Always use Submit for new data, Close Case for review only
                    btnSubmit?.text = getString(R.string.submit)
                    // Remove existing observer to prevent duplicates
                    viewModel.benFlows.removeObservers(viewLifecycleOwner)
                    viewModel.benFlows.observe(viewLifecycleOwner) { benFlowList ->
                        if (benFlowList.isNullOrEmpty()) return@observe

                        val distinctList =
                            benFlowList.distinctBy { it.benVisitNo }

                        benFlowMap.clear()
                        distinctList.forEach { benFlow ->
                            benFlow.benVisitNo?.let { visitNo ->
                                benFlowMap[visitNo] = benFlow
                            }
                        }

                        benFlowListCache = benFlowMap.values.toList()

                        val lastNonFollowUp = benFlowMap.values
                            .sortedBy { it.benVisitNo }
                            .lastOrNull { it.VisitReason != "Follow Up" }

                        lastNonFollowUp?.let {

                            val benVisitInfo = PatientDisplayWithVisitInfo(
                                benVisitInfo.patient,
                                genderName = null,
                                villageName = null,
                                ageUnit = null,
                                maritalStatus = null,
                                nurseDataSynced =null,
                                doctorDataSynced = null,
                                createNewBenFlow = null,
                                prescriptionID = null,
                                benVisitNo = it.benVisitNo,
                                visitCategory = null,
                                benFlowID = null,
                                nurseFlag = null,
                                doctorFlag = null,
                                labtechFlag = null,
                                pharmacist_flag = null,
                                visitDate = null,
                                referDate = null,
                                referTo = null,
                                referralReason = null
                            )

                            lifecycleScope.launch {
                                loadPrescriptionRowsForVisit(benVisitInfo)
                                convertToDiagnosisValues(viewModel.getProvisionalDiagnosisForVisitNumAndPatientId(benVisitInfo))
                            }
                        }
                    }
                } else {
                    lifecycleScope.launch {
                        loadPrescriptionRowsForVisit(benVisitInfo)
                        convertToDiagnosisValues(viewModel.getProvisionalDiagnosisForVisitNumAndPatientId(benVisitInfo))
                    }
                    testNameMap = viewModel.getTestNameTypeMap()
                    if (benVisitInfo.benVisitNo != null) {
                        viewModel.getPreviousTest(benVisitInfo)
                    }
                }
            }
            investigationBD = viewModel.previousTests.value
            val resp = investigationBD?.previousTestIds?.split(",")
                ?.mapNotNull { it.trim().takeIf { s -> s.isNotEmpty() }?.toIntOrNull() }
            val selectedRelationTypes = mapProcedureIdsToNames(procedureDropdown,resp)
            val selectedRelationTypesString = selectedRelationTypes.joinToString(", ")
            binding.selectF.text = selectedRelationTypesString
            updateRouteExternalTestVisibility(investigationBD)

        } else {
            binding.patientList.visibility = View.VISIBLE
            benVisitInfo =
                requireActivity().intent?.getSerializableExtra("benVisitInfo") as PatientDisplayWithVisitInfo
            updateShowCphcDetailsButtonVisibility(benVisitInfo)

            getVisitResObserver(benVisitInfo)

            // Use latest pharmacist_flag from DB so completed (lab+pharmacist) cases get correct UI even if intent was stale
            val benVisitNo = benVisitInfo.benVisitNo
            val sync = if (benVisitNo != null) {
                runBlocking(Dispatchers.IO) {
                    try {
                        viewModel.getPatientVisitInfoSyncByPatientIdAndBenVisitNo(
                            benVisitInfo.patient.patientID,
                            benVisitNo
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            } else null
            effectivePharmacistFlag = sync?.pharmacist_flag ?: benVisitInfo.pharmacist_flag ?: 0
            effectivePharmacistFlagForVisibility = effectivePharmacistFlag

            // CHO fresh-case entry (new chief complaint from visit details): always start editable.
            if (isFreshCaseEntryFromVisitDetails) {
                val btnSubmit = activity?.findViewById<Button>(R.id.btnSubmit)
                val btnCancel = activity?.findViewById<Button>(R.id.btnCancel)
                effectivePharmacistFlag = 0
                effectivePharmacistFlagForVisibility = 0
                applyEditableCaseUi(btnSubmit, btnCancel, R.string.submit)
                binding.prescriptionExtra.visibility = View.VISIBLE
                binding.diagnosisExtra.visibility = View.VISIBLE
                binding.vitalsExtra.visibility = View.VISIBLE
                binding.vitalsLayout.visibility = View.VISIBLE
            }
            // Existing-visit flow: show editable sections only when pharmacist has NOT yet dispensed.
            else if (effectivePharmacistFlag == 0) {
                val btnSubmit = activity?.findViewById<Button>(R.id.btnSubmit)
                val btnCancel = activity?.findViewById<Button>(R.id.btnCancel)
                applyEditableCaseUi(btnSubmit, btnCancel, R.string.submit)
                binding.prescriptionExtra.visibility = View.VISIBLE
                binding.diagnosisExtra.visibility = View.VISIBLE
                binding.vitalsExtra.visibility = View.VISIBLE
                binding.vitalsLayout.visibility = View.VISIBLE
            } else {
                if (effectivePharmacistFlag == 1) {
                    val btnSubmit = activity?.findViewById<Button>(R.id.btnSubmit)
                    val btnCancel = activity?.findViewById<Button>(R.id.btnCancel)
                    val isDoctorEditBeforeDispense = benVisitInfo.nurseFlag == 9 &&
                            (benVisitInfo.doctorFlag == 3 || benVisitInfo.doctorFlag == 9)
                    if (isDoctorEditBeforeDispense) {
                        applyEditableCaseUi(btnSubmit, btnCancel, R.string.submit)
                    } else {
                        applyReadOnlyCaseUi(btnSubmit, btnCancel)
                    }
                } else if (effectivePharmacistFlag == 9) {
                    val btnSubmit = activity?.findViewById<Button>(R.id.btnSubmit)
                    val btnCancel = activity?.findViewById<Button>(R.id.btnCancel)
                    val isLabReviewWithDispensedMedicine = benVisitInfo.nurseFlag == 9 &&
                            benVisitInfo.doctorFlag == 3

                    if (isLabReviewWithDispensedMedicine) {
                        applyEditableCaseUi(btnSubmit, btnCancel, R.string.close_case_btn)
                    } else {
                        applyReadOnlyCaseUi(btnSubmit, btnCancel)
                    }
                }
            }
        }

        // When already filled (lab + pharmacist submitted): only Close button; no Submit/Cancel; no plus icons; no refer section
        // Doctor can edit when: nurseFlag=9 AND (doctorFlag=1 OR doctorFlag=3) AND pharmacist_flag != 9
        // Special case: doctorFlag=3 AND pharmacist_flag=9 → doctor reviews lab+dispensed medicine and can add new or close
        val isDoctorReviewingAfterLabAndDispense =
            benVisitInfo.nurseFlag == 9 &&
                    benVisitInfo.doctorFlag == 3 &&
                    effectivePharmacistFlag == 9 &&
                    !isClosedViewOnly
        if (isDoctorReviewingAfterLabAndDispense) {
            activity?.findViewById<Button>(R.id.btnSubmit)?.visibility = View.VISIBLE
            activity?.findViewById<Button>(R.id.btnSubmit)?.text = getString(R.string.close_case_btn)
        }
        val isDoctorEditingPendingDispense = benVisitInfo.nurseFlag == 9 &&
                benVisitInfo.doctorFlag == 9 && effectivePharmacistFlag == 1
        val isDoctorCanEdit = (isDoctorWorkflowRole() ||
                isDoctorReviewingAfterLabAndDispense ||
                isDoctorEditingPendingDispense) &&
                benVisitInfo.nurseFlag == 9 &&
                (benVisitInfo.doctorFlag == 1 || benVisitInfo.doctorFlag == 3 || benVisitInfo.doctorFlag == 9) &&
                (effectivePharmacistFlag != 9 || isDoctorReviewingAfterLabAndDispense)
        val isAlreadyFilledReadOnly = isClosedViewOnly ||
                (viewRecordFragment == true && !isDoctorCanEdit) ||
                (!isFreshCaseEntryFromVisitDetails && effectivePharmacistFlag == 9 && !isDoctorReviewingAfterLabAndDispense)
        isAlreadyFilledReadOnlyForVisibility = isAlreadyFilledReadOnly
        if (isAlreadyFilledReadOnly) {
            applyReadOnlyCaseUi(
                activity?.findViewById(R.id.btnSubmit),
                activity?.findViewById(R.id.btnCancel)
            )
        }

        // Provisional/Final Diagnosis and Medicine fields: always show; filled = disabled, fresh = editable (handled in adapters)
        if (isDoctorExistingVisitFlow()) {
            binding.tvProvisionalDignosisTitle.visibility = View.VISIBLE
            binding.diagnosisExtra.visibility = View.VISIBLE
            binding.prescriptionExtra.visibility = View.VISIBLE
        }

        // Show refer section (labels) only when case is editable, not when already filled read-only
        if (!isAlreadyFilledReadOnly) {
            if (benVisitInfo.referDate != null) {
                binding.referDateLabel.visibility = View.VISIBLE
                binding.referDate.setText(benVisitInfo.referDate)
            }
            if (benVisitInfo.referTo != null) {
                binding.referToLabel.visibility = View.VISIBLE
                binding.referTo.setText(benVisitInfo.referTo)
                disableTextInputLayout(binding.referToLabel)
            }
            if (benVisitInfo.referralReason != null) {
                binding.referalReasonLabel.visibility = View.VISIBLE
                binding.referalReason.setText(benVisitInfo.referralReason!!.split(pattern)[0])
                disableTextInputLayout(binding.referalReasonLabel)
            }
        }

        if (isDoctorExistingVisitFlow()) {
            patientId = benVisitInfo.patient.patientID
            patId = benVisitInfo.patient.patientID
            viewModel.getVitalsDB(patId)

            benVisitInfo.benVisitNo?.let { visitNo ->
                viewModel.getChiefComplaintDB(benVisitInfo.patient.patientID, visitNo)
                viewModel.getLabList(benVisitInfo.patient.patientID, visitNo)
            }
            // Load medicine/prescription and diagnosis so they show in case record (same as lab data)
            lifecycleScope.launch {
                loadPrescriptionRowsForVisit(benVisitInfo)
                convertToDiagnosisValues(viewModel.getProvisionalDiagnosisForVisitNumAndPatientId(benVisitInfo))
            }
            // Remove existing observer to prevent duplicates
            viewModel.labReportList.removeObservers(viewLifecycleOwner)
            viewModel.labReportList.observe(viewLifecycleOwner) { labReports ->
                while (tableLayout.childCount > 1) {
                    tableLayout.removeViewAt(1)
                }
                viewModel.labReportProcedureTypes.clear()

                if (labReports.isEmpty()) return@observe

                val uniqueLatestReports = labReports
                    .filter { it.procedure.procedureName != null }
                    .groupBy { it.procedure.procedureName!! }
                    .mapValues { (_, reports) ->
                        reports.maxByOrNull { report ->
                            report.procedure.createdDate?.let { dateStr ->
                                try {
                                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).parse(dateStr)?.time ?: 0L
                                } catch (_: Exception) {
                                    try {
                                        SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(dateStr)?.time ?: 0L
                                    } catch (_: Exception) {
                                        dateStr.hashCode().toLong()
                                    }
                                }
                            } ?: 0L
                        } ?: reports.first()
                    }
                    .values
                    .toList()

                uniqueLatestReports.map { it.procedure.procedureName }.filterNotNull().distinct()
                    .forEach { viewModel.labReportProcedureTypes.add(it) }

                binding.scrollview.visibility = View.VISIBLE
                binding.resultHeading.visibility = View.VISIBLE
                binding.dateOption.visibility = View.VISIBLE

                uniqueLatestReports.maxByOrNull { it.procedure.createdDate ?: "" }?.procedure?.createdDate?.let {
                    binding.inputDate.setText(it)
                }
                binding.inputDate.inputType = InputType.TYPE_NULL
                binding.inputDate.isFocusable = false
                binding.inputDate.isClickable = false

                for (labReport in uniqueLatestReports) {
                    val procedureName = labReport.procedure.procedureName ?: continue
                    val components = labReport.components
                    if (components.isEmpty()) {
                        val tableRowVal =
                            layoutInflater.inflate(R.layout.report_custom_layout, null) as TableRow
                        tableRowVal.findViewById<TextView>(R.id.nameTextView).text = procedureName
                        tableRowVal.findViewById<TextView>(R.id.numberTextView).text = ""
                        tableLayout.addView(tableRowVal)
                    } else {
                        for ((index, component) in components.withIndex()) {
                            val resultVal = buildString {
                                component.componentName?.let { append("<b>${TextUtils.htmlEncode(it)}:</b> ") }
                                append(TextUtils.htmlEncode(component.testResultValue.orEmpty()))
                                component.testResultUnit?.let { append(" ${TextUtils.htmlEncode(it)}") }
                                component.remarks?.let { append(" <br> <b>Remarks: </b> ${TextUtils.htmlEncode(it)}") }
                            }
                            val tableRowVal =
                                layoutInflater.inflate(R.layout.report_custom_layout, null) as TableRow
                            tableRowVal.findViewById<TextView>(R.id.nameTextView).text =
                                if (index == 0) procedureName else ""
                            tableRowVal.findViewById<TextView>(R.id.numberTextView).text =
                                HtmlCompat.fromHtml(resultVal, HtmlCompat.FROM_HTML_MODE_LEGACY)
                            tableLayout.addView(tableRowVal)
                        }
                    }
                }
            }

        }

        val adapter = CHOCaseRecordItemAdapter(CHOCaseRecordItemAdapter.BenClickListener { benVisitInfo ->
            if (benVisitInfo.nurseFlag == 9 && benVisitInfo.doctorFlag == 3 && isDoctorWorkflowRole()) {
                navigatetoCaseCustomRecordSelf(false, benVisitInfo)
            } else if (benVisitInfo.nurseFlag == 9 && benVisitInfo.doctorFlag == 1 && isDoctorWorkflowRole()) {
                navigatetoCaseCustomRecordSelf(false, benVisitInfo)
            } else {
                navigatetoCaseCustomRecordSelf(true, benVisitInfo)
            }
        })
        binding.patientList.adapter = adapter

        // Remove existing observer to prevent duplicates
        viewModel.benFlows.removeObservers(viewLifecycleOwner)
        viewModel.benFlows.observe(viewLifecycleOwner) { benFlowList ->
            if (!benFlowList.isNullOrEmpty()) {
                adapter.updateBenFlows(benFlowList.distinctBy { it.benVisitNo })
            }
        }

        binding.inputTestName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed, but must be implemented
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed, but must be implemented
            }

            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrBlank()) {
                    val text = s.toString()
                    val capitalizedText = text.substring(0, 1).toUpperCase() + text.substring(1)
                    if (text != capitalizedText) {
                        binding.inputTestName.removeTextChangedListener(this) // Avoid infinite loop
                        binding.inputTestName.setText(capitalizedText)
                        binding.inputTestName.setSelection(capitalizedText.length) // Set cursor position to the end
                        binding.inputTestName.addTextChangedListener(this) // Add the listener back
                    }
                }
            }
        })
        lifecycleScope.launch {
            viewModel.getPatientDisplayListForDoctorByPatient(benVisitInfo.patient.patientID).collect {
                if (it.isNotEmpty()) {
                    adapter.submitList(it)
                    patientVisitNosCache = it.mapNotNull { visit -> visit.benVisitNo }.distinct().sortedDescending()
                } else {
                    binding.patientList.visibility = View.GONE
                    patientVisitNosCache = emptyList()
                }
            }
        }
        lifecycleScope.launch {
            testNameMap = viewModel.getTestNameTypeMap()
            if (benVisitInfo.benVisitNo != null) {
                viewModel.getPreviousTest(benVisitInfo)
            }
        }

        lifecycleScope.launch {
            referNameMap = viewModel.getReferNameTypeMap()
        }

        val chiefComplaintDB = mutableListOf<ChiefComplaintDB>()

        if (isDoctorExistingVisitFlow()) {
            // Remove existing observer to prevent duplicates
            viewModel.chiefComplaintDB.removeObservers(viewLifecycleOwner)
            viewModel.chiefComplaintDB.observe(viewLifecycleOwner) { chiefComplaintList ->
                // Clear the existing data in chiefComplaintDB
                chiefComplaintDB.clear()

                // Loop through the chiefComplaintList and add data to chiefComplaintDB
                for (chiefComplaintItem in chiefComplaintList) {
                    val chiefC = ChiefComplaintDB(
                        id = "33+${chiefComplaintItem.chiefComplaintId}",
                        chiefComplaintId = chiefComplaintItem.chiefComplaintId,
                        chiefComplaint = chiefComplaintItem.chiefComplaint,
                        duration = chiefComplaintItem.duration,
                        durationUnit = chiefComplaintItem.durationUnit,
                        description = chiefComplaintItem.description,
                        patientID = "",
                        benFlowID = 0
                    )
                    chiefComplaintDB.add(chiefC) // Add the item to the list
                }
                if (::chAdapter.isInitialized) {
                    chAdapter.notifyDataSetChanged()
                }
            }
            // In doctor edit mode show section even when empty (new registration / no complaint)
            val showChiefComplaintSection = chiefComplaintDB.isNotEmpty() ||
                    (isDoctorWorkflowRole() && viewRecordFragment != true)
            binding.chiefComplaintHeading.visibility = if (showChiefComplaintSection) View.VISIBLE else View.GONE
            // Remove existing observer to prevent duplicates
            viewModel.previousTests.removeObservers(viewLifecycleOwner)
            viewModel.previousTests.observe(viewLifecycleOwner) { record ->
                updateRouteExternalTestVisibility(record)
            }
        } else {
            masterDb = arguments?.getSerializable("MasterDb") as? MasterDb

            for (i in 0 until (masterDb?.visitMasterDb?.chiefComplaint?.size ?: 0)) {
                val chiefComplaintItem = masterDb!!.visitMasterDb!!.chiefComplaint!![i]
                val chiefC = ChiefComplaintDB(
                    id = "33+${i}",
                    chiefComplaintId = chiefComplaintItem.id,
                    chiefComplaint = chiefComplaintItem.chiefComplaint,
                    duration = chiefComplaintItem.duration,
                    durationUnit = chiefComplaintItem.durationUnit,
                    description = chiefComplaintItem.description,
                    patientID = "",
                    benFlowID = 0
                )
                chiefComplaintDB.add(chiefC) // Add the item to the list
            }
        }
        chAdapter = ChiefComplaintMultiAdapter(chiefComplaintDB,"")
        binding.chiefComplaintExtra.adapter = chAdapter
        val layoutManagerC = LinearLayoutManager(requireContext())
        binding.chiefComplaintExtra.layoutManager = layoutManagerC

        if (!isDoctorExistingVisitFlow()) {
            val hasComplaints = chiefComplaintDB.isNotEmpty()
            // Hide Chief Complaint section when no complaints exist (e.g., specialized-module visits without a CC).
            binding.chiefComplaintHeading.visibility = if (hasComplaints) View.VISIBLE else View.GONE
            binding.chiefComplaintExtra.visibility = if (hasComplaints) View.VISIBLE else View.GONE
        }

        // Remove existing observer to prevent duplicates
        viewModel.formMedicineDosage.removeObservers(viewLifecycleOwner)
        viewModel.formMedicineDosage.observe(viewLifecycleOwner) { f ->
            formMListVal.clear()
            formMListVal.addAll(f)

            formForFilter.clear()
            formForFilter.addAll(f)
            if (::pAdapter.isInitialized) {
                pAdapter.notifyDataSetChanged()
            }
        }


        val tempAdapter =
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line)
        val uniqueTemplateNames = LinkedHashSet<String>()
        binding.inputUseTempForFields.setAdapter(tempAdapter)

        // Remove existing observer to prevent duplicates
        viewModel.tempDB.removeObservers(viewLifecycleOwner)
        viewModel.tempDB.observe(viewLifecycleOwner) { vc ->
            uniqueTemplateNames.clear()
            vc.mapNotNullTo(uniqueTemplateNames) { it?.templateName }

            // Add "None" to the HashSet
            uniqueTemplateNames.add("None")

            tempAdapter.clear()
            tempAdapter.addAll(uniqueTemplateNames)
            tempAdapter.notifyDataSetChanged()
        }

        binding.inputUseTempForFields.setOnItemClickListener { parent, _, position, _ ->
            itemListP.clear()
            if (::pAdapter.isInitialized) {
                pAdapter.notifyDataSetChanged()
            }
            val selectedString = parent.getItemAtPosition(position) as String
            if (selectedString == "None") {
                itemListP.clear()
                itemListP.add(PrescriptionValues())
                if (::pAdapter.isInitialized) {
                    pAdapter.notifyDataSetChanged()
                }
                val inputMethodManager =
                    requireContext().getSystemService(InputMethodManager::class.java)
                inputMethodManager.hideSoftInputFromWindow(
                    binding.inputUseTempForFields.windowToken,
                    0
                )
            } else {
                itemListP.clear()
                if (::pAdapter.isInitialized) {
                    pAdapter.notifyDataSetChanged()
                }
                lifecycleScope.launch {
                    val selectedTemplates = viewModel.getTemplatesByTemplateName(selectedString)
                    convertToPrescriptionValues(selectedTemplates)
                }
            }
        }


        // Declare counsellingTypesAdapter before the observer
        val counsellingTypesAdapter =
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line)
        binding.routeDropDownVal.setAdapter(counsellingTypesAdapter)

        // Remove existing observer to prevent duplicates
        viewModel.counsellingProvided.removeObservers(viewLifecycleOwner)
        viewModel.counsellingProvided.observe(viewLifecycleOwner) { f ->
            counsellingTypes.clear()
            counsellingTypes.addAll(f)
            counsellingTypesAdapter.clear()
            counsellingTypesAdapter.addAll(f.map { it.name })
            counsellingTypesAdapter.notifyDataSetChanged()
            if (::pAdapter.isInitialized) {
                pAdapter.notifyDataSetChanged()
            }
        }
        // Remove existing observer to prevent duplicates
        viewModel.procedureDropdown.removeObservers(viewLifecycleOwner)
        viewModel.procedureDropdown.observe(viewLifecycleOwner) { f ->
            procedureDropdown.clear()
            procedureDropdown.addAll(f)
            familyM!!.setOnClickListener {
                showDialogWithFamilyMembers(procedureDropdown, viewModel.labReportProcedureTypes)
            }
        }
        binding.saveTemplate.setOnClickListener {
            saveTemp(uniqueTemplateNames)
        }
        binding.deleteTemp.setOnClickListener {

            tempAdapter.notifyDataSetChanged()
            openBottomSheet(uniqueTemplateNames,tempAdapter)
        }

        val referAdapter =
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line)
        binding.referDropdownText.setAdapter(referAdapter)
        // Remove existing observer to prevent duplicates
        viewModel.higherHealthCare.removeObservers(viewLifecycleOwner)
        viewModel.higherHealthCare.observe(viewLifecycleOwner) { vc ->
            referAdapter.clear()
            referAdapter.addAll(vc.map { it.institutionName })
            referAdapter.notifyDataSetChanged()
        }

        // counsellingTypesAdapter is already declared and set up above with the counsellingProvided observer

        // When doctor can edit (fresh card or post-lab, pharmacist not dispensed), show all fields in diagnosis/prescription
        val isCaseRecordReadOnly = viewRecordFragment == true && !isDoctorCanEdit

        dAdapter = DiagnosisAdapter(
            requireContext(),
            isCaseRecordReadOnly,
            isFollowupVisit,
            itemListD,
            object : RecyclerViewItemChangeListenerD {
                override fun onItemChanged() {
                    binding.plusButtonD.isEnabled = !isAnyItemEmptyD()
                }
            }
        )
        binding.diagnosisExtra.adapter = dAdapter
        val layoutManager = LinearLayoutManager(requireContext())
        binding.diagnosisExtra.layoutManager = layoutManager
        dAdapter.notifyItemInserted(itemListD.size - 1)
        binding.plusButtonD.isEnabled = !isAnyItemEmptyD()
        binding.plusButtonD.setOnClickListener {
            val newItem = DiagnosisValue()
            itemListD.add(newItem)
//            dAdapter.notifyItemInserted(itemListD.size - 1)
            view.clearFocus()
            dAdapter.notifyDataSetChanged()
            binding.plusButtonD.isEnabled = !isAnyItemEmptyD()
            binding.plusButtonD.isEnabled = false
        }
        val isMedicineDispensedByPharmacist = effectivePharmacistFlag == 9
        pAdapter = PrescriptionAdapter(
//            tempDBVal,
//            tempList,
            isCaseRecordReadOnly,
            isFollowupVisit,
            isMedicineDispensedByPharmacist,
            dispensedLockedPrescriptionCount,
            itemListP,
            formMListVal,
            frequencyListVal,
            unitListVal,
            instructionDropdown,
            formForFilter,
            object : RecyclerViewItemChangeListenersP {
                override fun onItemChanged() {
                    binding.plusButtonP.isEnabled = !isAnyItemEmptyP()
                    // Update button text when medicine changes
                    updateSubmitButtonText()
                }
            },
            fetchStockListener = { itemId, callback ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val stock = viewModel.getAvailableStockForRule(itemId)
                    callback(stock)
                }
            }
        )
        binding.prescriptionExtra.adapter = pAdapter
        pAdapter.setDispensedLockedItemCount(dispensedLockedPrescriptionCount)
        val layoutManager2 = LinearLayoutManager(requireContext())
        binding.prescriptionExtra.layoutManager = layoutManager2
        pAdapter.notifyItemInserted(itemListP.size - 1)
        binding.plusButtonP.isEnabled = !isAnyItemEmptyP()
        // Set initial button text
        updateSubmitButtonText()
        binding.plusButtonP.setOnClickListener {
            val newItem = PrescriptionValues()
            newItem.title = "Medicine - ${itemListP.size + 1}"
            Timber.d("Plus button clicked: Creating new item with title='${newItem.title}', form='${newItem.form}', id=${newItem.id}, isDispensed=${newItem.isDispensed}")
            itemListP.add(newItem)
            Timber.d("Item added to list. Total items: ${itemListP.size}")
//            pAdapter.notifyItemInserted(itemListP.size -   1)
            view.clearFocus()
            pAdapter.notifyItemInserted(itemListP.size - 1)
            binding.plusButtonP.isEnabled = !isAnyItemEmptyP()
            binding.plusButtonP.isEnabled = false
            // Update button text after adding medicine
            updateSubmitButtonText()
        }
        if (isDoctorExistingVisitFlow()) {
            var bool = true
            // Remove existing observer to prevent duplicates
            viewModel.vitalsDB.removeObservers(viewLifecycleOwner)
            viewModel.vitalsDB.observe(viewLifecycleOwner) { vitalsDB ->
                var vitalDb2 = VitalsMasterDb(
                    height = vitalsDB.height,
                    weight = vitalsDB.weight,
                    bmi = vitalsDB.bmi,
                    waistCircumference = vitalsDB.waistCircumference,
                    temperature = vitalsDB.temperature,
                    pulseRate = vitalsDB.pulseRate,
                    spo2 = vitalsDB.spo2,
                    bpSystolic = vitalsDB.bpSystolic,
                    bpDiastolic = vitalsDB.bpDiastolic,
                    respiratoryRate = vitalsDB.respiratoryRate,
                    rbs = vitalsDB.rbs
                )
                bool = false
                populateVitalsFieldsW(vitalDb2)
            }
            if (bool) {
                populateVitalsFields()
            }
        } else {
            populateVitalsFields()
        }
    }

    private fun getVisitResObserver(benVisitInfo: PatientDisplayWithVisitInfo){
        benVisitInfo.patient.beneficiaryID?.let { beneficiaryID ->
            viewModel.getVisitReasonByBenFlowID(beneficiaryID)
        } ?: Timber.d("benFlowID is null, cannot get VisitReason")
    }

    private fun navigatetoCaseCustomRecordSelf(isVisible: Boolean, it: PatientDisplayWithVisitInfo) {
        // Refresh ben flow data in background, but do not block click navigation on observer timing.
        getVisitResObserver(it)

        val visitNo = it.benVisitNo
        val followupVisitFromCache = visitNo?.let { no ->
            benFlowMap[no]?.VisitReason == "Follow Up"
        } ?: false
        val followupVisit = followupVisitFromCache || (it.visitCategory == "Follow Up")

        findNavController().navigate(
            R.id.action_caseRecordCustom_self, Bundle().apply {
                putBoolean("viewRecord", isVisible)
                putBoolean("isFlowComplete", false)
                putBoolean("isFollowupVisit", followupVisit)
                putSerializable("benVisitInfo", it)
            }
        )
    }

    private suspend fun loadPrescriptionRowsForVisit(visitInfo: PatientDisplayWithVisitInfo) {
        val visitNo = visitInfo.benVisitNo
        if (visitNo == null || visitNo <= 0) {
            dispensedLockedPrescriptionCount = 0
            convertToPrescriptionValuesFromPC(emptyList(), isDispensed = false, append = false)
            return
        }
        val patientID = visitInfo.patient.patientID
        val editableRows = viewModel.getPrescriptionForVisitNumAndPatientId(visitInfo)
        val latestVisitInfo = viewModel.getPatientVisitInfoSyncByPatientIdAndBenVisitNo(patientID, visitNo)
        val pharmacistFlag = latestVisitInfo?.pharmacist_flag ?: visitInfo.pharmacist_flag ?: 0

        if (pharmacistFlag == 9) {
            val dispensedRows = viewModel.getDispensedPrescriptionsForVisitNumAndPatientId(patientID, visitNo)
            if (dispensedRows.isNotEmpty()) {
                convertToPrescriptionValuesFromPC(dispensedRows, isDispensed = true, append = false)
                dispensedLockedPrescriptionCount = itemListP.size
            } else {
                // Fallback for older/local records where pharmacist table rows are not present yet:
                // still render existing medicines as dispensed (view-only) and allow new rows via +.
                convertToPrescriptionValuesFromPC(editableRows, isDispensed = true, append = false)
                dispensedLockedPrescriptionCount = itemListP.size
            }
        } else {
            val dispensedRows = viewModel.getDispensedPrescriptionsForVisitNumAndPatientId(patientID, visitNo)
            if (dispensedRows.isNotEmpty()) {
                // Pending pharmacist cycle: keep old dispensed history visible + current editable rows.
                convertToPrescriptionValuesFromPC(dispensedRows, isDispensed = true, append = false)
                dispensedLockedPrescriptionCount = itemListP.size
                convertToPrescriptionValuesFromPC(editableRows, isDispensed = false, append = true)
            } else {
                dispensedLockedPrescriptionCount = 0
                convertToPrescriptionValuesFromPC(editableRows, isDispensed = false, append = false)
            }
        }

        if (::pAdapter.isInitialized) {
            pAdapter.setDispensedLockedItemCount(dispensedLockedPrescriptionCount)
        }
    }

    private fun showPreviousCphcDetailsPopup() {
        fun boolLabel(value: Boolean?): String = when (value) {
            true -> getString(R.string.yes)
            false -> getString(R.string.no)
            null -> getString(R.string.no_data)
        }
        fun valueLabel(value: Any?): String = value?.toString()?.takeIf { it.isNotBlank() } ?: getString(R.string.no_data)

        val currentVisitNo = benVisitInfo.benVisitNo

        lifecycleScope.launch {
            val popupPatientId = if (patId.isNotBlank()) patId else benVisitInfo.patient.patientID
            val details = if (currentVisitNo == null || currentVisitNo <= 0) {
                ""
            } else {
                buildString {
                    val chiefComplaints =
                        viewModel.getChiefComplaintByPatientAndVisit(popupPatientId, currentVisitNo)
                    val earDiagnosis =
                        viewModel.getEarDiagnosisByPatientAndVisit(popupPatientId, currentVisitNo)
                    val noseDiagnosis =
                        viewModel.getNoseDiagnosisByPatientAndVisit(popupPatientId, currentVisitNo)
                    val throatDiagnosis =
                        viewModel.getThroatDiagnosisByPatientAndVisit(popupPatientId, currentVisitNo)
                    val oralHealth =
                        viewModel.getOralHealthByPatientAndVisit(popupPatientId, currentVisitNo)
                    val ophthalmicVisit =
                        viewModel.getOphthalmicByPatientAndVisit(popupPatientId, currentVisitNo)
                    val elderlyAssessment =
                        viewModel.getElderlyByPatientAndVisit(popupPatientId, currentVisitNo)
                    val mentalScreening =
                        viewModel.getMentalByPatientAndVisit(popupPatientId, currentVisitNo)
                    val painAssessment =
                        viewModel.getPainAssessmentByPatientAndVisit(popupPatientId, currentVisitNo)
                    val psychosocialSupport =
                        viewModel.getPsychosocialByPatientAndVisit(popupPatientId, currentVisitNo)
                    val hasAnyCphcData =
                        chiefComplaints.isNotEmpty() ||
                                earDiagnosis != null ||
                                noseDiagnosis != null ||
                                throatDiagnosis != null ||
                                oralHealth != null ||
                                ophthalmicVisit != null ||
                                elderlyAssessment != null ||
                                mentalScreening != null ||
                                painAssessment != null ||
                                psychosocialSupport != null
                    if (!hasAnyCphcData) return@buildString

                    append(getString(R.string.cphc_visit_current_format, currentVisitNo)).append("\n")

                    if (chiefComplaints.isNotEmpty()) {
                        append(getString(R.string.select_chief_complaint_without_astrict)).append(":\n")
                        chiefComplaints.forEach { cc ->
                            append("- ").append(cc.chiefComplaint ?: "")
                            if (!cc.duration.isNullOrBlank()) {
                                append(" (").append(cc.duration)
                                if (!cc.durationUnit.isNullOrBlank()) {
                                    append(" ").append(cc.durationUnit)
                                }
                                append(")")
                            }
                            append("\n")
                        }
                    }

                    if (earDiagnosis != null) {
                        append(getString(R.string.cphc_ear_diagnosis)).append(":\n")
                        append("- ").append(getString(R.string.ear_difficulty_hearing)).append(": ").append(boolLabel(earDiagnosis.difficultyHearing)).append("\n")
                        append("- ").append(getString(R.string.ear_whisper_test_response)).append(": ").append(earDiagnosis.whisperTestResponse ?: getString(R.string.no_data)).append("\n")
                        append("- ").append(getString(R.string.ear_hearing_test_outcome)).append(": ").append(earDiagnosis.hearingTestOutcome ?: getString(R.string.no_data)).append("\n")
                        append("- ").append(getString(R.string.ear_pain)).append(": ").append(boolLabel(earDiagnosis.earPain)).append("\n")
                        append("- ").append(getString(R.string.ear_discharge_present)).append(": ").append(boolLabel(earDiagnosis.earDischargePresent)).append("\n")
                        append("- ").append(getString(R.string.cphc_ear_foreign_body)).append(": ").append(earDiagnosis.foreignBodyInEar ?: getString(R.string.no_data)).append("\n")
                        append("- ").append(getString(R.string.cphc_ear_condition_type)).append(": ").append(earDiagnosis.earConditionType ?: getString(R.string.no_data)).append("\n")
                        append("- ").append(getString(R.string.ear_congenital_malformation)).append(": ").append(boolLabel(earDiagnosis.congenitalEarMalformation)).append("\n")
                    }

                    if (noseDiagnosis != null) {
                        append(getString(R.string.cphc_nose_diagnosis))
                            .append(":\n")
                        append("- ").append(getString(R.string.cphc_nose_difficulty_breathing)).append(": ").append(boolLabel(noseDiagnosis.difficultyBreathing)).append("\n")
                        append("- ").append(getString(R.string.cphc_nose_open_mouth_breathing)).append(": ").append(boolLabel(noseDiagnosis.openMouthBreathing)).append("\n")
                        append("- ").append(getString(R.string.cphc_nose_bleed)).append(": ").append(boolLabel(noseDiagnosis.noseBleed)).append("\n")
                        append("- ").append(getString(R.string.cphc_nose_systolic_bp)).append(": ").append(valueLabel(noseDiagnosis.systolicBp)).append("\n")
                        append("- ").append(getString(R.string.cphc_nose_diastolic_bp)).append(": ").append(valueLabel(noseDiagnosis.diastolicBp)).append("\n")
                        append("- ").append(getString(R.string.cphc_nose_foreign_body)).append(": ").append(valueLabel(noseDiagnosis.foreignBodyNose)).append("\n")
                        append("- ").append(getString(R.string.cphc_nose_sinusitis)).append(": ").append(boolLabel(noseDiagnosis.sinusitis)).append("\n")
                    }

                    if (throatDiagnosis != null) {
                        append(getString(R.string.cphc_throat_diagnosis))
                            .append(":\n")
                        append("- ").append(getString(R.string.cphc_throat_symptoms)).append(": ").append(valueLabel(throatDiagnosis.symptoms?.joinToString(", "))).append("\n")
                        append("- ").append(getString(R.string.cphc_throat_neck_swelling)).append(": ").append(boolLabel(throatDiagnosis.neckSwelling)).append("\n")
                        append("- ").append(getString(R.string.cphc_throat_difficulty_swallowing)).append(": ").append(boolLabel(throatDiagnosis.difficultySwallowing)).append("\n")
                        append("- ").append(getString(R.string.cphc_throat_tonsillitis)).append(": ").append(boolLabel(throatDiagnosis.tonsillitis)).append("\n")
                        append("- ").append(getString(R.string.cphc_throat_pharyngitis)).append(": ").append(boolLabel(throatDiagnosis.pharyngitis)).append("\n")
                        append("- ").append(getString(R.string.cphc_throat_laryngitis)).append(": ").append(boolLabel(throatDiagnosis.laryngitis)).append("\n")
                        append("- ").append(getString(R.string.cphc_throat_sinusitis)).append(": ").append(boolLabel(throatDiagnosis.sinusitis)).append("\n")
                        append("- ").append(getString(R.string.cphc_throat_cleft_lip)).append(": ").append(boolLabel(throatDiagnosis.cleftLip)).append("\n")
                        append("- ").append(getString(R.string.cphc_throat_cleft_palate)).append(": ").append(boolLabel(throatDiagnosis.cleftPalate)).append("\n")
                    }

                    if (oralHealth != null) {
                        append(getString(R.string.cphc_oral_health))
                            .append(":\n")
                        append("- ").append(getString(R.string.cphc_oral_tooth_decay_present)).append(": ").append(boolLabel(oralHealth.toothDecayPresent)).append("\n")
                        append("- ").append(getString(R.string.cphc_oral_tooth_decay_symptoms)).append(": ").append(valueLabel(oralHealth.toothDecaySymptoms)).append("\n")
                        append("- ").append(getString(R.string.cphc_oral_gum_disease_present)).append(": ").append(boolLabel(oralHealth.gumDiseasePresent)).append("\n")
                        append("- ").append(getString(R.string.cphc_oral_gum_disease_symptoms)).append(": ").append(valueLabel(oralHealth.gumDiseaseSymptoms)).append("\n")
                        append("- ").append(getString(R.string.cphc_oral_irregular_teeth_jaws)).append(": ").append(boolLabel(oralHealth.irregularTeethJaws)).append("\n")
                        append("- ").append(getString(R.string.cphc_oral_abnormal_growth_ulcer)).append(": ").append(boolLabel(oralHealth.abnormalGrowthUlcer)).append("\n")
                        append("- ").append(getString(R.string.cphc_oral_cleft_lip_palate)).append(": ").append(boolLabel(oralHealth.cleftLipPalate)).append("\n")
                        append("- ").append(getString(R.string.cphc_oral_dental_fluorosis)).append(": ").append(boolLabel(oralHealth.dentalFluorosis)).append("\n")
                        append("- ").append(getString(R.string.cphc_oral_dental_emergency)).append(": ").append(valueLabel(oralHealth.dentalEmergency)).append("\n")
                    }

                    if (ophthalmicVisit != null) {
                        append(getString(R.string.cphc_ophthalmic))
                            .append(":\n")
                        append("- ").append(getString(R.string.cphc_oph_is_diabetic)).append(": ").append(boolLabel(ophthalmicVisit.isDiabetic)).append("\n")
                        append("- ").append(getString(R.string.cphc_oph_screening_performed)).append(": ").append(boolLabel(ophthalmicVisit.screeningPerformed)).append("\n")
                        append("- ").append(getString(R.string.cphc_oph_visual_acuity_chart_used)).append(": ").append(valueLabel(ophthalmicVisit.visualAcuityChartUsed)).append("\n")
                        append("- ").append(getString(R.string.cphc_oph_dist_va_right)).append(": ").append(valueLabel(ophthalmicVisit.distVARight)).append("\n")
                        append("- ").append(getString(R.string.cphc_oph_dist_va_left)).append(": ").append(valueLabel(ophthalmicVisit.distVALeft)).append("\n")
                        append("- ").append(getString(R.string.cphc_oph_near_va)).append(": ").append(valueLabel(ophthalmicVisit.nearVA)).append("\n")
                        append("- ").append(getString(R.string.cphc_oph_case_id_conditions)).append(": ").append(valueLabel(ophthalmicVisit.caseIdConditions)).append("\n")
                        append("- ").append(getString(R.string.cphc_oph_cataract_symptoms)).append(": ").append(boolLabel(ophthalmicVisit.cataractSymptoms)).append("\n")
                        append("- ").append(getString(R.string.cphc_oph_glaucoma_symptoms)).append(": ").append(boolLabel(ophthalmicVisit.glaucomaSymptoms)).append("\n")
                        append("- ").append(getString(R.string.cphc_oph_diabetic_retinopathy_symptoms)).append(": ").append(boolLabel(ophthalmicVisit.diabeticRetinopathySymptoms)).append("\n")
                        append("- ").append(getString(R.string.cphc_oph_presbyopia_symptoms)).append(": ").append(boolLabel(ophthalmicVisit.presbyopiaSymptoms)).append("\n")
                        append("- ").append(getString(R.string.cphc_oph_trachoma_status)).append(": ").append(valueLabel(ophthalmicVisit.trachomaStatus)).append("\n")
                        append("- ").append(getString(R.string.cphc_oph_corneal_disease_type)).append(": ").append(valueLabel(ophthalmicVisit.cornealDiseaseType)).append("\n")
                        append("- ").append(getString(R.string.cphc_oph_vitamin_a_deficiency)).append(": ").append(boolLabel(ophthalmicVisit.vitaminADeficiency)).append("\n")
                        append("- ").append(getString(R.string.cphc_oph_injury_type)).append(": ").append(valueLabel(ophthalmicVisit.injuryType)).append("\n")
                        append("- ").append(getString(R.string.cphc_oph_foreign_body_removal)).append(": ").append(valueLabel(ophthalmicVisit.foreignBodyRemoval)).append("\n")
                        append("- ").append(getString(R.string.cphc_oph_chemical_exposure)).append(": ").append(boolLabel(ophthalmicVisit.chemicalExposure)).append("\n")
                    }

                    if (elderlyAssessment != null) {
                        append(getString(R.string.cphc_elderly))
                            .append(":\n")
                        append("- ").append(getString(R.string.cphc_elderly_geriatric_complaints)).append(": ").append(boolLabel(elderlyAssessment.geriatricComplaints)).append("\n")
                        append("- ").append(getString(R.string.cphc_elderly_multiple_chronic_conditions)).append(": ").append(boolLabel(elderlyAssessment.multipleChronicConditions)).append("\n")
                        append("- ").append(getString(R.string.cphc_elderly_recent_falls)).append(": ").append(boolLabel(elderlyAssessment.recentFalls)).append("\n")
                        append("- ").append(getString(R.string.cphc_elderly_difficulty_walking_balance)).append(": ").append(boolLabel(elderlyAssessment.difficultyWalkingBalance)).append("\n")
                        append("- ").append(getString(R.string.cphc_elderly_visual_hearing_difficulty)).append(": ").append(boolLabel(elderlyAssessment.visualHearingDifficulty)).append("\n")
                        append("- ").append(getString(R.string.cphc_elderly_functional_decline)).append(": ").append(boolLabel(elderlyAssessment.functionalDecline)).append("\n")
                        append("- ").append(getString(R.string.cphc_elderly_bathing)).append(": ").append(valueLabel(elderlyAssessment.bathing)).append("\n")
                        append("- ").append(getString(R.string.cphc_elderly_dressing)).append(": ").append(valueLabel(elderlyAssessment.dressing)).append("\n")
                        append("- ").append(getString(R.string.cphc_elderly_toileting)).append(": ").append(valueLabel(elderlyAssessment.toileting)).append("\n")
                        append("- ").append(getString(R.string.cphc_elderly_transferring)).append(": ").append(valueLabel(elderlyAssessment.transferring)).append("\n")
                        append("- ").append(getString(R.string.cphc_elderly_continence)).append(": ").append(valueLabel(elderlyAssessment.continence)).append("\n")
                        append("- ").append(getString(R.string.cphc_elderly_feeding)).append(": ").append(valueLabel(elderlyAssessment.feeding)).append("\n")
                        append("- ").append(getString(R.string.cphc_elderly_total_score)).append(": ").append(valueLabel(elderlyAssessment.totalScore)).append("\n")
                        append("- ").append(getString(R.string.cphc_elderly_functional_status)).append(": ").append(valueLabel(elderlyAssessment.functionalStatus)).append("\n")
                        append("- ").append(getString(R.string.cphc_elderly_functional_decline_flag)).append(": ").append(boolLabel(elderlyAssessment.functionalDeclineFlag)).append("\n")
                        append("- ").append(getString(R.string.cphc_elderly_memory_loss)).append(": ").append(boolLabel(elderlyAssessment.memoryLoss)).append("\n")
                        append("- ").append(getString(R.string.cphc_elderly_dementia_memory_loss)).append(": ").append(boolLabel(elderlyAssessment.dementiaMemoryLoss)).append("\n")
                        append("- ").append(getString(R.string.cphc_elderly_dementia_disorientation)).append(": ").append(boolLabel(elderlyAssessment.dementiaDisorientation)).append("\n")
                        append("- ").append(getString(R.string.cphc_elderly_dementia_behavioural_changes)).append(": ").append(boolLabel(elderlyAssessment.dementiaBehaviouralChanges)).append("\n")
                        append("- ").append(getString(R.string.cphc_elderly_dementia_self_care_decline)).append(": ").append(boolLabel(elderlyAssessment.dementiaSelfCareDecline)).append("\n")
                        append("- ").append(getString(R.string.cphc_elderly_dementia_outcome)).append(": ").append(valueLabel(elderlyAssessment.dementiaScreeningOutcome)).append("\n")
                        append("- ").append(getString(R.string.cphc_elderly_dementia_referral_required)).append(": ").append(boolLabel(elderlyAssessment.dementiaReferralRequired)).append("\n")
                    }

                    if (mentalScreening != null) {
                        append(getString(R.string.cphc_mental))
                            .append(":\n")
                        append("- ").append(getString(R.string.cphc_mental_emotional_behavioural_concerns)).append(": ").append(boolLabel(mentalScreening.emotionalBehaviouralConcerns)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_substance_use_concerns)).append(": ").append(boolLabel(mentalScreening.substanceUseConcerns)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_self_harm_suicide_thoughts)).append(": ").append(boolLabel(mentalScreening.selfHarmSuicideThoughts)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_memory_loss_confusion)).append(": ").append(boolLabel(mentalScreening.memoryLossConfusion)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_seizures_fits_loc)).append(": ").append(boolLabel(mentalScreening.seizuresFitsLoc)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_is_postpartum)).append(": ").append(boolLabel(mentalScreening.isPostpartum)).append("\n")
                        append("- ").append(getString(R.string.phq9_little_interest)).append(": ").append(valueLabel(mentalScreening.phq9LittleInterest)).append("\n")
                        append("- ").append(getString(R.string.phq9_feeling_down)).append(": ").append(valueLabel(mentalScreening.phq9FeelingDown)).append("\n")
                        append("- ").append(getString(R.string.phq9_sleep_trouble)).append(": ").append(valueLabel(mentalScreening.phq9SleepTrouble)).append("\n")
                        append("- ").append(getString(R.string.phq9_feeling_tired)).append(": ").append(valueLabel(mentalScreening.phq9FeelingTired)).append("\n")
                        append("- ").append(getString(R.string.phq9_appetite)).append(": ").append(valueLabel(mentalScreening.phq9Appetite)).append("\n")
                        append("- ").append(getString(R.string.phq9_feeling_bad)).append(": ").append(valueLabel(mentalScreening.phq9FeelingBad)).append("\n")
                        append("- ").append(getString(R.string.phq9_concentration)).append(": ").append(valueLabel(mentalScreening.phq9Concentration)).append("\n")
                        append("- ").append(getString(R.string.phq9_moving_slowly)).append(": ").append(valueLabel(mentalScreening.phq9MovingSlowly)).append("\n")
                        append("- ").append(getString(R.string.phq9_self_harm_thoughts)).append(": ").append(valueLabel(mentalScreening.phq9SelfHarmThoughts)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_phq9_total_score)).append(": ").append(valueLabel(mentalScreening.phq9TotalScore)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_phq9_depression_severity)).append(": ").append(valueLabel(mentalScreening.phq9DepressionSeverity)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_phq9_system_action)).append(": ").append(valueLabel(mentalScreening.phq9SystemAction)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_substance_current_tobacco_use)).append(": ").append(boolLabel(mentalScreening.substanceCurrentTobaccoUse)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_substance_tobacco_type)).append(": ").append(valueLabel(mentalScreening.substanceTobaccoType)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_substance_tobacco_frequency)).append(": ").append(valueLabel(mentalScreening.substanceTobaccoFrequency)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_substance_tobacco_outcome)).append(": ").append(valueLabel(mentalScreening.substanceTobaccoOutcome)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_substance_alcohol_use)).append(": ").append(boolLabel(mentalScreening.substanceAlcoholUse)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_substance_tobacco_use)).append(": ").append(boolLabel(mentalScreening.substanceTobaccoUse)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_substance_other_use)).append(": ").append(boolLabel(mentalScreening.substanceOtherUse)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_substance_other_specify)).append(": ").append(valueLabel(mentalScreening.substanceOtherSpecify)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_substance_frequency)).append(": ").append(valueLabel(mentalScreening.substanceFrequency)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_substance_system_action)).append(": ").append(valueLabel(mentalScreening.substanceSystemAction)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_brief_intervention_given)).append(": ").append(boolLabel(mentalScreening.briefInterventionGiven)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_suicide_current_thoughts)).append(": ").append(boolLabel(mentalScreening.suicideCurrentThoughts)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_suicide_plan)).append(": ").append(boolLabel(mentalScreening.suicidePlan)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_suicide_previous_attempt)).append(": ").append(boolLabel(mentalScreening.suicidePreviousAttempt)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_suicide_hopelessness)).append(": ").append(boolLabel(mentalScreening.suicideHopelessness)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_suicide_immediate_assess)).append(": ").append(boolLabel(mentalScreening.suicideImmediateAssess)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_suicide_risk_level)).append(": ").append(valueLabel(mentalScreening.suicideRiskLevel)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_dementia_progressive_memory_loss)).append(": ").append(boolLabel(mentalScreening.dementiaProgressiveMemoryLoss)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_dementia_forgetting_recent)).append(": ").append(boolLabel(mentalScreening.dementiaForgettingRecent)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_dementia_disorientation)).append(": ").append(boolLabel(mentalScreening.dementiaDisorientation)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_dementia_daily_activities)).append(": ").append(boolLabel(mentalScreening.dementiaDailyActivities)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_dementia_behavioural_changes)).append(": ").append(boolLabel(mentalScreening.dementiaBehaviouralChanges)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_epilepsy_recurrent_seizures)).append(": ").append(boolLabel(mentalScreening.epilepsyRecurrentSeizures)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_epilepsy_jerky_movements)).append(": ").append(boolLabel(mentalScreening.epilepsyJerkyMovements)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_epilepsy_tongue_bite)).append(": ").append(boolLabel(mentalScreening.epilepsyTongueBite)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_epilepsy_confusion_after)).append(": ").append(boolLabel(mentalScreening.epilepsyConfusionAfter)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_epilepsy_loc_duration)).append(": ").append(valueLabel(mentalScreening.epilepsyLocDuration)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_substance_alcohol_loss)).append(": ").append(boolLabel(mentalScreening.substance_alcohol_loss)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_substance_alcohol_impact)).append(": ").append(boolLabel(mentalScreening.substanceAlcoholImpact)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_substance_alcohol_withdrawal)).append(": ").append(boolLabel(mentalScreening.substanceAlcoholWithdrawal)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_substance_alcohol_problematic)).append(": ").append(boolLabel(mentalScreening.substanceAlcoholProblematic)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_substance_alcohol_classification)).append(": ").append(valueLabel(mentalScreening.substanceAlcoholClassification)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_substance_alcohol_system_action)).append(": ").append(valueLabel(mentalScreening.substanceAlcoholSystemAction)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_substance_alcohol_frequency)).append(": ").append(valueLabel(mentalScreening.substance_alcohol_frequency)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_ed_recurrent_episode_loss)).append(": ").append(boolLabel(mentalScreening.edRecurrentEpisodeloss)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_ed_recurrent_jerky_movements)).append(": ").append(boolLabel(mentalScreening.edRecurrentJerkyMovements)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_ed_confusion_drowsiness)).append(": ").append(boolLabel(mentalScreening.edConfusionordrowsiness)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_ed_progressive_memory_loss)).append(": ").append(boolLabel(mentalScreening.edProgressiveMemoryLoss)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_ed_confusion_disorientation)).append(": ").append(boolLabel(mentalScreening.edConfusionDisorientation)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_ed_functional_decline)).append(": ").append(boolLabel(mentalScreening.edFunctionalDecline)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_ed_screening_outcome)).append(": ").append(valueLabel(mentalScreening.edScreeningOutcome)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_ed_psychosocial_intervention_provided)).append(": ").append(boolLabel(mentalScreening.edPsychosocialInterventionProvided)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_ed_intervention_type)).append(": ").append(valueLabel(mentalScreening.edInterventionType)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_ed_session_date)).append(": ").append(valueLabel(mentalScreening.edSessionDate)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_ed_duration_minutes)).append(": ").append(valueLabel(mentalScreening.edDurationMinutes)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_ed_remarks)).append(": ").append(valueLabel(mentalScreening.edRemarks)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_ed_referral_required)).append(": ").append(valueLabel(mentalScreening.edReferralRequired)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_ed_reason)).append(": ").append(valueLabel(mentalScreening.edReason)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_referral_required)).append(": ").append(boolLabel(mentalScreening.referralRequired)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_referral_level)).append(": ").append(valueLabel(mentalScreening.referralLevel)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_reason_for_referral)).append(": ").append(valueLabel(mentalScreening.reasonForReferral)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_referral_date)).append(": ").append(valueLabel(mentalScreening.referralDate)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_follow_up_required)).append(": ").append(boolLabel(mentalScreening.followUpRequired)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_follow_up_date)).append(": ").append(valueLabel(mentalScreening.followUpDate)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_improvement_noted)).append(": ").append(valueLabel(mentalScreening.improvementNoted)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_adherence_to_advice)).append(": ").append(valueLabel(mentalScreening.adherenceToAdvice)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_referral_escalation_required)).append(": ").append(boolLabel(mentalScreening.referralEscalationRequired)).append("\n")
                        append("- ").append(getString(R.string.cphc_mental_case_closure_reason)).append(": ").append(valueLabel(mentalScreening.caseClosureReason)).append("\n")
                    }

                    if (painAssessment != null) {
                        append(getString(R.string.pain_symptom_assessment_palliative)).append(":\n")
                        append("- ").append(getString(R.string.pain_severity)).append(": ").append(valueLabel(painAssessment.painSeverity)).append("\n")
                        append("- ").append(getString(R.string.pain_duration)).append(": ").append(valueLabel(painAssessment.painDuration)).append("\n")
                        append("- ").append(getString(R.string.other_symptoms_present)).append(": ").append(boolLabel(painAssessment.symptomsPresent)).append("\n")
                        append("- ").append(getString(R.string.other_symptoms_severity)).append(": ").append(valueLabel(painAssessment.otherSymptomsSeverity)).append("\n")
                        append("- ").append(getString(R.string.immediate_relief_provided)).append(": ").append(boolLabel(painAssessment.immediateReliefProvided)).append("\n")
                        append("- ").append(getString(R.string.persistent_pain_present)).append(": ").append(boolLabel(painAssessment.persistentPainPresent)).append("\n")
                        append("- ").append(getString(R.string.distressing_symptoms_present)).append(": ").append(valueLabel(painAssessment.distressingSymptoms)).append("\n")
                        append("- ").append(getString(R.string.bedridden_or_severely_dependent)).append(": ").append(boolLabel(painAssessment.bedriddenOrSeverelyDependent)).append("\n")
                        append("- ").append(getString(R.string.life_limiting_illness_known)).append(": ").append(boolLabel(painAssessment.lifeLimitingIllnessKnown)).append("\n")
                        append("- ").append(getString(R.string.caregiver_support_required)).append(": ").append(boolLabel(painAssessment.caregiverSupportRequired)).append("\n")
                        append("- ").append(getString(R.string.palliative_care_eligible)).append(": ").append(boolLabel(painAssessment.palliativeCareEligible)).append("\n")
                        append("- ").append(getString(R.string.symptom_assessment_basic_field_title)).append(": ").append(valueLabel(painAssessment.basicSymptomsSelected)).append("\n")
                        append("- ").append(getString(R.string.basic_symptom_relief_provided)).append(": ").append(boolLabel(painAssessment.basicSymptomReliefProvided)).append("\n")
                        append("- ").append(getString(R.string.basic_psychosocial_support_provided)).append(": ").append(boolLabel(painAssessment.basicPsychosocialSupportProvided)).append("\n")
                        append("- ").append(getString(R.string.basic_caregiver_counselling_provided)).append(": ").append(boolLabel(painAssessment.basicCaregiverCounsellingProvided)).append("\n")
                        append("- ").append(getString(R.string.basic_management_remarks)).append(": ").append(valueLabel(painAssessment.basicManagementRemarks)).append("\n")
                        appendNestedObjectFields(
                            target = this,
                            header = getString(R.string.cphc_referral),
                            referral = painAssessment.referralFollowUp,
                            boolFormatter = ::boolLabel,
                            valueFormatter = ::valueLabel
                        )
                    }

                    if (psychosocialSupport != null) {
                        append(getString(R.string.title_psychosocial_caregiver_support)).append(":\n")
                        append("- ").append(getString(R.string.psychosocial_counselling_provided)).append(": ").append(boolLabel(psychosocialSupport.psychosocialCounsellingProvided)).append("\n")
                        append("- ").append(getString(R.string.psychosocial_caregiver_counselling)).append(": ").append(boolLabel(psychosocialSupport.caregiverCounsellingProvided)).append("\n")
                        append("- ").append(getString(R.string.psychosocial_caregiver_distress)).append(": ").append(boolLabel(psychosocialSupport.caregiverDistressIdentified)).append("\n")
                        append("- ").append(getString(R.string.psychosocial_counselling_remarks)).append(": ").append(valueLabel(psychosocialSupport.counsellingRemarks)).append("\n")
                        appendNestedObjectFields(
                            target = this,
                            header = getString(R.string.cphc_referral),
                            referral = psychosocialSupport.referralFollowUp,
                            boolFormatter = ::boolLabel,
                            valueFormatter = ::valueLabel
                        )
                    }
                }.trim()
            }

            val contentView = buildPreviousCphcDialogContent(details.ifBlank { getString(R.string.no_previous_cphc_details) })

            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.cphc_details_title))
                .setView(contentView)
                .setPositiveButton(getString(R.string.ok), null)
                .show()
        }
    }

    private fun buildPreviousCphcDialogContent(detailsText: String): View {
        val context = requireContext()
        var showUnavailable = false
        val content = TextView(context).apply {
            text = buildStyledCphcDetails(detailsText, includeUnavailable = showUnavailable)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setLineSpacing(dpToPx(4f).toFloat(), 1f)
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
            setPadding(dpToPx(20f), dpToPx(8f), dpToPx(20f), dpToPx(4f))
        }

        val scrollView = ScrollView(context).apply {
            addView(content)
        }

        val unavailableCount = countUnavailableRows(detailsText)
        if (unavailableCount <= 0) return scrollView

        val toggleLink = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.END
            }

            gravity = Gravity.END

            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark))
            setPadding(dpToPx(20f), 0, dpToPx(20f), dpToPx(10f))
            movementMethod = LinkMovementMethod.getInstance()
            highlightColor = ContextCompat.getColor(context, android.R.color.transparent)

            fun renderToggleText() {
                val label = if (showUnavailable) {
                    getString(R.string.cphc_hide_unavailable_details)
                } else {
                    getString(R.string.cphc_show_unavailable_details, unavailableCount)
                }
                val clickable = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        showUnavailable = !showUnavailable
                        content.text = buildStyledCphcDetails(detailsText, includeUnavailable = showUnavailable)
                        renderToggleText()
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.isUnderlineText = true
                        ds.color = ContextCompat.getColor(context, android.R.color.holo_blue_dark)
                    }
                }
                text = SpannableStringBuilder(label).apply {
                    setSpan(clickable, 0, label.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }

            renderToggleText()
        }

        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL

            addView(toggleLink)
            addView(scrollView)
        }
    }

    private fun buildStyledCphcDetails(detailsText: String, includeUnavailable: Boolean = false): CharSequence {
        val styled = SpannableStringBuilder()
        val lines = detailsText.lines()
        val chiefComplaintsHeader = "${getString(R.string.select_chief_complaint_without_astrict)}:"
        val reportedConcernsHeader = getString(R.string.cphc_reported_concerns)
        val visitLinePrefix = getString(R.string.cphc_visit_current_format, 0).substringBefore("0")
        val noDataLabel = getString(R.string.no_data)

        lines.forEachIndexed { index, rawLine ->
            val line = rawLine.trimEnd()
            val isVisitLine = line.startsWith(visitLinePrefix)
            val isSectionHeader = line.isNotBlank() && !line.startsWith("- ") && line.endsWith(":")
            val isItemLine = line.startsWith("- ")
            val itemValue = if (isItemLine && line.contains(":")) line.substringAfter(":").trim() else ""
            val shouldHideItem = isItemLine && (itemValue.equals("N/A", ignoreCase = true) || itemValue.equals(noDataLabel, ignoreCase = true))

            if (shouldHideItem && !includeUnavailable) {
                return@forEachIndexed
            }

            val displaySectionHeader = if (line == chiefComplaintsHeader) {
                reportedConcernsHeader
            } else {
                line
            }

            val displayLine = when {
                isVisitLine -> line
                isSectionHeader -> "\u2022 $displaySectionHeader"
                isItemLine -> "   \u25E6 ${line.removePrefix("- ").trimStart()}"
                else -> line
            }
            val start = styled.length
            styled.append(displayLine)

            val isHeader = isVisitLine || isSectionHeader
            if (isHeader) {
                styled.setSpan(StyleSpan(Typeface.BOLD), start, styled.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            if (isItemLine) {
                val colonIndex = displayLine.indexOf(':')
                if (colonIndex >= 0 && colonIndex + 1 < displayLine.length) {
                    val valueStartInLine = (colonIndex + 1).let { idx ->
                        if (idx < displayLine.length && displayLine[idx] == ' ') idx + 1 else idx
                    }
                    if (valueStartInLine < displayLine.length) {
                        styled.setSpan(
                            StyleSpan(Typeface.BOLD),
                            start + valueStartInLine,
                            start + displayLine.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }

            if (index < lines.lastIndex) {
                styled.append('\n')
            }
        }
        while (styled.isNotEmpty() && styled.last() == '\n') {
            styled.delete(styled.length - 1, styled.length)
        }
        return styled
    }

    private fun countUnavailableRows(detailsText: String): Int {
        val noDataLabel = getString(R.string.no_data)
        return detailsText.lines().count { line ->
            val trimmed = line.trim()
            if (!trimmed.startsWith("- ") || !trimmed.contains(":")) return@count false
            val itemValue = trimmed.substringAfter(":").trim()
            itemValue.equals("N/A", ignoreCase = true) || itemValue.equals(noDataLabel, ignoreCase = true)
        }
    }

    private fun dpToPx(dp: Float): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun appendNestedObjectFields(
        target: StringBuilder,
        header: String,
        referral: ReferralFollowUpFields?,
        boolFormatter: (Boolean?) -> String,
        valueFormatter: (Any?) -> String
    ) {
        target.append("- ").append(header).append(":\n")
        if (referral == null) {
            target.append("- ").append(getString(R.string.no_data)).append("\n")
            return
        }

        target.append("- ").append(getString(R.string.cphc_case_status)).append(": ").append(valueFormatter(referral.caseStatus)).append("\n")
        target.append("- ").append(getString(R.string.cphc_date_of_death)).append(": ").append(valueFormatter(referral.dateOfDeath)).append("\n")
        target.append("- ").append(getString(R.string.cphc_follow_up_required)).append(": ").append(boolFormatter(referral.followUpRequired)).append("\n")
        target.append("- ").append(getString(R.string.follow_up_date_title)).append(": ").append(valueFormatter(referral.followUpDate)).append("\n")
        target.append("- ").append(getString(R.string.cphc_referral_required)).append(": ").append(boolFormatter(referral.referralRequired)).append("\n")
        target.append("- ").append(getString(R.string.cphc_referral_level)).append(": ").append(valueFormatter(referral.referralLevel)).append("\n")
        target.append("- ").append(getString(R.string.cphc_reason_for_referral)).append(": ").append(valueFormatter(referral.reasonForReferral)).append("\n")
        target.append("- ").append(getString(R.string.cphc_remarks)).append(": ").append(valueFormatter(referral.remarks)).append("\n")
    }

    fun convertToPrescriptionValuesFromPC(
        prescriptionCaseRecords: List<PrescriptionCaseRecord?>,
        isDispensed: Boolean = false,
        append: Boolean = false
    ) {
        if (!append) {
            itemListP.clear()
        }
        var index = itemListP.size + 1
        for (prescriptionCaseRecord in prescriptionCaseRecords) {
            val prescriptionValue = prescriptionCaseRecord?.let {
                PrescriptionValues(
                    id = it.itemId,
                    form = "",
                    frequency = it.frequency ?: "",
                    duration = it.duration ?: "",
                    instructions = it.instructions ?: "",
                    unit = it.unit ?: DEFAULT_DURATION_UNIT,
                    isDispensed = isDispensed,
                    title = "Medicine - $index"
                )
            }
            if (prescriptionValue != null) {
                itemListP.add(prescriptionValue)
                index++
            }
        }
        if (!append && itemListP.isEmpty()) {
            itemListP.add(PrescriptionValues())
        }
        if (::pAdapter.isInitialized) {
            pAdapter.notifyDataSetChanged()
            updateSubmitButtonText()
            binding.plusButtonP.isEnabled = !isAnyItemEmptyP()
        }
    }

    override fun onDestroyView() {
        // Clean up all observers to prevent memory leaks
        viewModel.benFlows.removeObservers(viewLifecycleOwner)
        viewModel.labReportList.removeObservers(viewLifecycleOwner)
        viewModel.chiefComplaintDB.removeObservers(viewLifecycleOwner)
        viewModel.previousTests.removeObservers(viewLifecycleOwner)
        viewModel.formMedicineDosage.removeObservers(viewLifecycleOwner)
        viewModel.tempDB.removeObservers(viewLifecycleOwner)
        viewModel.counsellingProvided.removeObservers(viewLifecycleOwner)
        viewModel.procedureDropdown.removeObservers(viewLifecycleOwner)
        viewModel.higherHealthCare.removeObservers(viewLifecycleOwner)
        viewModel.vitalsDB.removeObservers(viewLifecycleOwner)
        viewModel.isDataSaved.removeObservers(viewLifecycleOwner)
        viewModel.isDataDeleted.removeObservers(viewLifecycleOwner)

        Timber.d("onDestroyView: All observers cleaned up")
        super.onDestroyView()
    }

    fun convertToDiagnosisValues(diagnosisCaseRecords: List<DiagnosisCaseRecord?>): List<DiagnosisValue> {
        itemListD.clear()
        val diagnosisValuesList = mutableListOf<DiagnosisValue>()

        for (diagnosisCaseRecord in diagnosisCaseRecords) {
            val diagnosisValue = diagnosisCaseRecord?.let {
                DiagnosisValue(
                    id = -1,
                    diagnosis = diagnosisCaseRecord.diagnosis,
                    isPreFilled = true
                )
            }
            if (diagnosisValue != null) {
                itemListD.add(diagnosisValue)
            }
        }
        if (itemListD.isEmpty()) {
            itemListD.add(DiagnosisValue())
        }
        if (itemListD.isNotEmpty() && ::dAdapter.isInitialized) {
            dAdapter.notifyDataSetChanged()
        }

        return diagnosisValuesList
    }


    fun convertToPrescriptionValues(prescriptionTemplateDB: List<PrescriptionTemplateDB?>) {
        for (templateDB in prescriptionTemplateDB) {
            val prescriptionValue = templateDB?.let {
                it?.drugName?.let { it1 ->
                    PrescriptionValues(
                        id = templateDB.drugId,
                        form = it1,
                        frequency = templateDB.frequency ?: "",
                        duration = templateDB.duration ?: "",
                        instructions = templateDB.instructions ?: "",
                        unit = templateDB.unit ?: DEFAULT_DURATION_UNIT,
                        title = "Medicine - ${itemListP.size + 1}"
                    )
                }
            }
            if (prescriptionValue != null) {
                itemListP.add(prescriptionValue)
            }
        }
        Timber.tag("Arrr").d("ItemSize: itemListP size: ${itemListP.size}")

        if (itemListP.isNotEmpty()) {
            pAdapter.notifyDataSetChanged()
        }
        val inputMethodManager = requireContext().getSystemService(InputMethodManager::class.java)
        inputMethodManager.hideSoftInputFromWindow(binding.inputUseTempForFields.windowToken, 0)
    }

    private lateinit var syncBottomSheet: TemplateListBottomSheetFragment
    private fun openBottomSheet(str: HashSet<String?>, tempAdapter: ArrayAdapter<String>) {
        syncBottomSheet = TemplateListBottomSheetFragment(str, prescriptionTemplateRepo,
            object : TemplateListBottomSheetFragment.OnTemplateDeletedListener {
                override fun onTemplateDeleted(updatedList: List<String>, string: String?) {
                    tempAdapter.clear()
                    tempAdapter.addAll(updatedList)
                    tempAdapter.notifyDataSetChanged()
                    string?.let {
                        viewModeltemplate.callMarkDel(it)
                    }
                    viewModeltemplate.callDel()
                    Toast.makeText(requireContext(), getString(R.string.template_deleted), Toast.LENGTH_SHORT).show()

                }
            }
        )


        if (!syncBottomSheet.isVisible)
            syncBottomSheet.show(childFragmentManager, resources.getString(R.string.sync))
    }

    private fun populateVitalsFieldsW(vitals: VitalsMasterDb) {
        //   hideNullFieldsW(vitals)
        binding.inputHeight.setText(vitals?.height?:"")
        binding.inputWeight.setText(vitals?.weight?:"")
        binding.inputBmi.setText(vitals.bmi?:"")
//        binding.inputWaistCircum.setText(vitals.waistCircumference.toString())
        binding.inputTemperature.setText(vitals.temperature?:"")
        binding.inputPulseRate.setText(vitals.pulseRate?:"")
        binding.inputSpo2.setText(vitals.spo2?:"")
        binding.bpCustomLayout.inputBpDiastolic.setText(vitals.bpDiastolic?:"0")
        binding.bpCustomLayout.inputBpSystolic.setText(vitals.bpSystolic?:"0")
        binding.inputRespiratoryPerMin.setText(vitals.respiratoryRate?:"")
        binding.inputRBS.setText(vitals.rbs?:"")
    }

    private fun populateVitalsFields() {
        //   hideNullFields()
        // Check if the masterDb and vitalsMasterDb are not null
        if (masterDb != null && masterDb?.vitalsMasterDb != null) {
            val vitals = masterDb?.vitalsMasterDb
            binding.inputHeight.setText(vitals?.height?:"")
            binding.inputWeight.setText(vitals?.weight?:"")
            binding.inputBmi.setText(vitals?.bmi?:"")
//            binding.inputWaistCircum.setText(vitals?.waistCircumference?:")
            binding.inputTemperature.setText(vitals?.temperature?:"")
            binding.inputPulseRate.setText(vitals?.pulseRate?:"")
            binding.inputSpo2.setText(vitals?.spo2?:"")
            binding.bpCustomLayout.inputBpDiastolic.setText(vitals?.bpDiastolic?:"0")
            binding.bpCustomLayout.inputBpSystolic.setText(vitals?.bpSystolic?:"0")
            binding.inputRespiratoryPerMin.setText(vitals?.respiratoryRate?:"")
            binding.inputRBS.setText(vitals?.rbs?:"")
        }
    }

    private fun hideNullFieldsW(vitalsDB: VitalsMasterDb) {
        val itemH = vitalsDB.height
        val itemW = vitalsDB.weight
        val itemB = vitalsDB.bmi
        val itemC = vitalsDB.waistCircumference
        val itemT = vitalsDB.temperature
        val itemP = vitalsDB.pulseRate
        val itemS = vitalsDB.spo2
        val itemBs = vitalsDB.bpSystolic
        val itemBd = vitalsDB.bpDiastolic
        val itemRs = vitalsDB.respiratoryRate
        val itemRb = vitalsDB.rbs
        if (itemH.isNullOrEmpty() || itemH.equals("null")) {
            binding.heightEditTxt.visibility = View.GONE
        } else {
            binding.heightEditTxt.visibility = View.VISIBLE
        }

        if (itemW.isNullOrEmpty() || itemW.equals("null")) {
            binding.weightEditTxt.visibility = View.GONE
        } else {
            binding.weightEditTxt.visibility = View.VISIBLE
        }

        if (itemB.isNullOrEmpty() || itemB.equals("null")) {
            binding.bmill.visibility = View.GONE
        } else {
            binding.bmill.visibility = View.VISIBLE
        }

//        if (itemC.isNullOrEmpty() || itemC.equals("null")) {
//            binding.waistCircumEditTxt.visibility = View.GONE
//        } else {
//            binding.waistCircumEditTxt.visibility = View.VISIBLE
//        }

        if (itemT.isNullOrEmpty() || itemT.equals("null")) {
            binding.temperatureEditTxt.visibility = View.GONE
        } else {
            binding.temperatureEditTxt.visibility = View.VISIBLE
        }

        if (itemP.isNullOrEmpty() || itemP.equals("null")) {
            binding.pulseRateEditTxt.visibility = View.GONE
        } else {
            binding.pulseRateEditTxt.visibility = View.VISIBLE
        }

        if (itemS.isNullOrEmpty() || itemS.equals("null")) {
            binding.spo2EditTxt.visibility = View.GONE
        } else {
            binding.spo2EditTxt.visibility = View.VISIBLE
        }
        //Custtomr layout validation not required start
        /*
                if (itemBs.isNullOrEmpty() || itemBs.equals("null")) {
                    binding.bpSystolicEditTxt.visibility = View.GONE
                } else {
                    binding.bpSystolicEditTxt.visibility = View.VISIBLE
                }*/

        /* if (itemBd.isNullOrEmpty() || itemBd.equals("null")) {
             binding.bpDiastolicEditTxt.visibility = View.GONE
         } else {
             binding.bpDiastolicEditTxt.visibility = View.VISIBLE
         }*/
        //Custtomr layout validation not required end

        if (itemRs.isNullOrEmpty() || itemRs.equals("null")) {
            binding.respiratoryEditTxt.visibility = View.GONE
        } else {
            binding.respiratoryEditTxt.visibility = View.VISIBLE
        }

        if (itemRb.isNullOrEmpty() || itemRb.equals("null")) {
            binding.rbsEditTxt.visibility = View.GONE
        } else {
            binding.rbsEditTxt.visibility = View.VISIBLE
        }

        if ((itemH.isNullOrEmpty() && itemW.isNullOrEmpty() && itemB.isNullOrEmpty() && itemC.isNullOrEmpty() && itemT.isNullOrEmpty() && itemP.isNullOrEmpty() && itemS.isNullOrEmpty() && itemBs.isNullOrEmpty() && itemBd.isNullOrEmpty() && itemRs.isNullOrEmpty() && itemRb.isNullOrEmpty()) ||
            (itemH.equals("null") && itemW.equals("null") && itemB.equals("null") && itemC.equals("null") && itemT.equals(
                "null"
            ) && itemP.equals("null") && itemS.equals("null") && itemBs.equals("null") && itemBd.equals(
                "null"
            ) && itemRs.equals("null") && itemRb.equals("null"))
        ) {
            binding.vitalsExtra.visibility = View.GONE
            binding.vitalsLayout.visibility = View.GONE
        } else {
            binding.vitalsExtra.visibility = View.VISIBLE
            binding.vitalsLayout.visibility = View.VISIBLE
        }
    }

    private fun hideNullFields() {
        var itemH = masterDb?.vitalsMasterDb?.height.toString()
        var itemW = masterDb?.vitalsMasterDb?.weight.toString()
        var itemB = masterDb?.vitalsMasterDb?.bmi.toString()
//        var  itemC = masterDb?.vitalsMasterDb?.waistCircumference.toString()
        var itemT = masterDb?.vitalsMasterDb?.temperature.toString()
        var itemP = masterDb?.vitalsMasterDb?.pulseRate.toString()
        var itemS = masterDb?.vitalsMasterDb?.spo2.toString()
        var itemBs = masterDb?.vitalsMasterDb?.bpSystolic.toString()
        var itemBd = masterDb?.vitalsMasterDb?.bpDiastolic.toString()
        var itemRs = masterDb?.vitalsMasterDb?.respiratoryRate.toString()
        var itemRb = masterDb?.vitalsMasterDb?.rbs.toString()
        if (itemH.isNullOrEmpty() || itemH.equals("null")) {
            binding.heightEditTxt.visibility = View.GONE
        }
        if (itemW.isNullOrEmpty() || itemW.equals("null")) {
            binding.weightEditTxt.visibility = View.GONE
        }
        if (itemB.isNullOrEmpty() || itemB.equals("null")) {
            binding.bmill.visibility = View.GONE
        }
//        if(itemC.isNullOrEmpty()||itemC.equals("null")){
//            binding.waistCircumEditTxt.visibility = View.GONE
//        }
        if (itemT.isNullOrEmpty() || itemT.equals("null")) {
            binding.temperatureEditTxt.visibility = View.GONE
        }
        if (itemP.isNullOrEmpty() || itemP.equals("null")) {
            binding.pulseRateEditTxt.visibility = View.GONE
        }
        if (itemS.isNullOrEmpty() || itemS.equals("null")) {
            binding.spo2EditTxt.visibility = View.GONE
        }
        //Custtomr layout validation not required start
        /*  if (itemBs.isNullOrEmpty() || itemBs.equals("null")) {
              binding.bpSystolicEditTxt.visibility = View.GONE
          }
          if (itemBd.isNullOrEmpty() || itemBd.equals("null")) {
              binding.bpDiastolicEditTxt.visibility = View.GONE
          }*/
        //Custtomr layout validation not required end

        if (itemRs.isNullOrEmpty() || itemRs.equals("null")) {
            binding.respiratoryEditTxt.visibility = View.GONE
        }
        if (itemRb.isNullOrEmpty() || itemRb.equals("null")) {
            binding.rbsEditTxt.visibility = View.GONE
        }
        if ((itemH.isNullOrEmpty() && itemW.isNullOrEmpty() && itemB.isNullOrEmpty() && itemT.isNullOrEmpty() && itemP.isNullOrEmpty() && itemS.isNullOrEmpty() && itemBs.isNullOrEmpty() && itemBd.isNullOrEmpty() && itemRs.isNullOrEmpty() && itemRb.isNullOrEmpty())
            ||
            (itemH.equals("null") && itemW.equals("null") && itemB.equals("null") && itemT.equals("null") && itemP.equals(
                "null"
            ) && itemS.equals("null") && itemBs.equals("null") && itemBd.equals("null") && itemRs.equals(
                "null"
            ) && itemRb.equals("null"))
        ) {
            binding.vitalsExtra.visibility = View.INVISIBLE
        }
    }

    /**
     * When Close button is shown (already filled): hide route/external/test if value is null;
     * hide testName also when medicine is already filled (pharmacist submitted).
     */
    private fun updateRouteExternalTestVisibility(record: InvestigationCaseRecord?) {
        if (!isAlreadyFilledReadOnlyForVisibility) return
        val counsellingList = record?.counsellingProvidedList?.filter { it.isNotBlank() }
        binding.routeDropDown.visibility = if (counsellingList.isNullOrEmpty()) View.GONE else {
            binding.routeDropDownVal.setText(counsellingList.joinToString(", "), false)
            binding.routeDropDownVal.isFocusable = false
            binding.routeDropDownVal.isClickable = false
            View.VISIBLE
        }
        val externalVal = record?.externalInvestigations?.trim()?.takeIf { it.isNotEmpty() }
        binding.externalI.visibility = if (externalVal == null) View.GONE else {
            binding.inputExternalI.setText(externalVal)
            binding.inputExternalI.isFocusable = false
            binding.inputExternalI.isClickable = false
            View.VISIBLE
        }
        val medicineAlreadyFilled = effectivePharmacistFlagForVisibility == 9
        val testIds = record?.previousTestIds?.split(",")
            ?.mapNotNull { it.trim().takeIf { s -> s.isNotEmpty() }?.toIntOrNull() }
        val hasTestValue = !testIds.isNullOrEmpty()
        binding.testName.visibility = when {
            medicineAlreadyFilled -> View.GONE
            !hasTestValue -> View.GONE
            else -> {
                val names = mapProcedureIdsToNames(procedureDropdown, testIds)
                binding.selectF.text = names.joinToString(", ")
                binding.selectF.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black))
                View.VISIBLE
            }
        }
    }

    fun mapProcedureIdsToNames(proceduresMasterData: List<ProceduresMasterData>,procedureIds: List<Int>?): List<String> {
        if (procedureIds != null) {
            return procedureIds.mapNotNull { id ->
                proceduresMasterData.find { it.procedureID == id }?.procedureName
            }
        }
        return emptyList()
    }
    private fun showDialogWithFamilyMembers(
        proceduresMasterData: List<ProceduresMasterData>,
        labReportProcedureTypes: List<String>
    ) {
        // Remove existing observer to prevent duplicates
        viewModel.previousTests.removeObservers(viewLifecycleOwner)
        viewModel.previousTests.observe(viewLifecycleOwner) {
            val selectedItems =
//                BooleanArray(procedureDropdown.size) { false }
                BooleanArray(procedureDropdown.size) { selectedTestName.contains(it) }
            investigationBD = viewModel.previousTests.value
            val resp = investigationBD?.previousTestIds?.split(",")
                ?.mapNotNull { it.trim().takeIf { s -> s.isNotEmpty() }?.toIntOrNull() }
//            if (resp != null) {
//                val previousTestList = resp.toMutableList()
//                for (index in selectedItems.indices) {
//                    if (previousTestList.contains(procedureDropdown!!.get(index).procedureID)) {
//                        selectedItems[index] = true
//                    }
//                }
//            }

//            val disabledItems = labReportProcedureTypes.map { type ->
//                proceduresMasterData.indexOfFirst { it.procedureName == type }
//            }.toSet().toTypedArray()

            val builder = AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.select_test_name))
                .setCancelable(false)
                .setMultiChoiceItems(
                    procedureDropdown.map { it.procedureName }.toTypedArray(),
                    selectedItems
                ) { _, which, isChecked ->
                    if (isChecked) {
//                        if (!disabledItems.contains(which)) {
                        selectedTestName.add(which)
//                        } else {
//                            Toast.makeText(
//                                requireContext(),
//                                "Test with result cannot be selected",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
                    } else {
                        selectedTestName.remove(which)
                    }

                }
                .setPositiveButton("Ok") { dialog, which ->
                    val selectedRelationTypes =
                        selectedTestName.map { proceduresMasterData[it].procedureName }
                    val selectedRelationTypesString = selectedRelationTypes.joinToString(", ")
                    binding.selectF.text = selectedRelationTypesString
                    binding.selectF.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.black
                        )
                    )
                    // Update button text based on selection
                    updateSubmitButtonText()
                }
                .setNeutralButton("Clear all") { dialog, which ->
                    selectedTestName.clear()
                    Arrays.fill(selectedItems, false)
                    val listView = (dialog as? AlertDialog)?.listView
                    listView?.clearChoices()
                    listView?.requestLayout()
                    binding.selectF.text = resources.getString(R.string.select_test_name)
                    binding.selectF.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.defaultInput
                        )
                    )
                    // Update button text after clearing
                    updateSubmitButtonText()
                }

            val alertDialog = builder.create()
            alertDialog.setOnShowListener {
                if (resp != null) {
                    val previousTestList = resp.toMutableList()
                    for (index in selectedItems.indices) {
                        if (previousTestList.contains(procedureDropdown!!.get(index).procedureID)) {
                            alertDialog.listView.get(index).isEnabled = true
//                            alertDialog.listView.get(index).setOnClickListener() {
//                                alertDialog.listView.get(index).isEnabled = false
//                            }
                        }
                    }
                }
            }
            alertDialog.show()

        }
    }
    fun isAnyItemEmptyD(): Boolean {
        for (item in itemListD) {
            if (item.diagnosis.isEmpty()) {
                return true
            }
        }
        return false
    }

    fun isAnyItemEmptyP(): Boolean {
        for (item in itemListP) {
            // Skip validation for dispensed medicines (they are read-only)
            if (item.isDispensed) {
                continue
            }
            val hasForm = item.form.isNotEmpty() || item.id != null
            if (!hasForm || item.frequency.isEmpty() || item.duration.isEmpty()) {
                return true
            }
        }
        return false
    }

    private fun parseTestIds(csv: String?): Set<Int> {
        if (csv.isNullOrBlank()) return emptySet()
        return csv.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()
    }

    private fun getSelectedTestIds(): Set<Int> {
        val selectedText = binding.selectF.text?.toString().orEmpty()
        if (selectedText.isBlank()) return emptySet()
        return selectedText.split(",")
            .mapNotNull { testName ->
                val trimmedName = testName.trim()
                if (trimmedName.isBlank()) null else findKeyByValue(testNameMap, trimmedName)
            }
            .toSet()
    }

    private fun hasTestSelectionChanged(): Boolean {
        val selectedIds = getSelectedTestIds()
        val existingIds = parseTestIds(investigationBD?.previousTestIds) + parseTestIds(investigationBD?.newTestIds)
        if (investigationBD == null) {
            return selectedIds.isNotEmpty()
        }
        return selectedIds != existingIds
    }

    /**
     * Updates the submit button text based on whether new tests or medicines are selected.
     * - "Close Case" when no new test/medicine (just reviewing)
     * - "Submit" when new test/medicine is selected (making changes)
     */
    private fun updateSubmitButtonText() {
        val btnSubmit = activity?.findViewById<Button>(R.id.btnSubmit) ?: return

        // Only update for lab review state (doctorFlag=3 and pharmacist_flag=9)
        val isLabReviewState = benVisitInfo.nurseFlag == 9 &&
                benVisitInfo.doctorFlag == 3 &&
                isDoctorWorkflowRole()

        if (!isLabReviewState) return

        val currentPharmacistFlag = effectivePharmacistFlagForVisibility ?: benVisitInfo.pharmacist_flag ?: 0
        if (currentPharmacistFlag != 9) return

        val hasNewTest = hasTestSelectionChanged()

        // Check if new medicine is selected
        val newRowsStart = dispensedLockedPrescriptionCount.coerceAtMost(itemListP.size)
        val hasNewMedicine = itemListP.drop(newRowsStart).any { it.id != null }

        // Update button text
        btnSubmit.text = if (hasNewTest || hasNewMedicine) {
            getString(R.string.submit)
        } else {
            getString(R.string.close_case_btn)
        }
    }

    private fun <K, V> findKeyByValue(map: Map<K, V>, value: V): K? {
        return map.entries.find { it.value == value }?.key
    }

    private fun saveNurseAndDoctorData(
        benVisitNo: Int,
        createNewBenflow: Boolean,
        user: UserDomain?
    ) {

        val visitDB = VisitDB(
            visitId = generateUuid(),
            category = masterDb?.visitMasterDb?.category.nullIfEmpty(),
            reasonForVisit = masterDb?.visitMasterDb?.reason.nullIfEmpty(),
            subCategory = masterDb?.visitMasterDb?.subCategory.nullIfEmpty(),
            patientID = patId,
            benVisitNo = benVisitNo,
            benVisitDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()),
            createdBy = user?.userName
        )


        var chiefComplaints = mutableListOf<ChiefComplaintDB>()
        for (i in 0 until (masterDb?.visitMasterDb?.chiefComplaint?.size ?: 0)) {
            val chiefComplaintItem = masterDb!!.visitMasterDb!!.chiefComplaint!![i]
            val chiefC = ChiefComplaintDB(
                id = generateUuid(),
                chiefComplaintId = chiefComplaintItem.id,
                chiefComplaint = chiefComplaintItem.chiefComplaint.nullIfEmpty(),
                duration = chiefComplaintItem.duration.nullIfEmpty(),
                durationUnit = chiefComplaintItem.durationUnit.nullIfEmpty(),
                description = chiefComplaintItem.description.nullIfEmpty(),
                patientID = patId,
                benFlowID = null,
                benVisitNo = benVisitNo
            )
            chiefComplaints.add(chiefC)
        }


        val patientVitals = PatientVitalsModel(
            vitalsId = generateUuid(),
            height = masterDb?.vitalsMasterDb?.height.nullIfEmpty(),
            weight = masterDb?.vitalsMasterDb?.weight.nullIfEmpty(),
            bmi = masterDb?.vitalsMasterDb?.bmi.nullIfEmpty(),
            waistCircumference = masterDb?.vitalsMasterDb?.waistCircumference.nullIfEmpty(),
            temperature = masterDb?.vitalsMasterDb?.temperature.nullIfEmpty(),
            pulseRate = masterDb?.vitalsMasterDb?.pulseRate.nullIfEmpty(),
            spo2 = masterDb?.vitalsMasterDb?.spo2.nullIfEmpty(),
            bpDiastolic = masterDb?.vitalsMasterDb?.bpDiastolic.nullIfEmpty(),
            bpSystolic = masterDb?.vitalsMasterDb?.bpSystolic.nullIfEmpty(),
            respiratoryRate = masterDb?.vitalsMasterDb?.respiratoryRate.nullIfEmpty(),
            rbs = masterDb?.vitalsMasterDb?.rbs.nullIfEmpty(),
            patientID = patId,
            benVisitNo = benVisitNo
        )


        var diagnosisList = mutableListOf<DiagnosisCaseRecord>()
        for (i in 0 until itemListD.size) {
            val diagnosisData = itemListD[i]
            if (diagnosisData.diagnosis.isNullOrEmpty()) {
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.diagnosisCannotBeEmpty),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return;
            } else {
                var diagnosis = DiagnosisCaseRecord(
                    diagnosisCaseRecordId = generateUuid(),
                    diagnosis = diagnosisData.diagnosis,
                    patientID = patId,
                    benVisitNo = benVisitNo
                )
                diagnosisList.add(diagnosis)
            }
        }


        val testName = binding.selectF.text.toString()
        val testNamesList = testName.split(",").map { it.trim() }
        val idString = testNamesList.joinToString(",") { testNameS ->
            val id =
                findKeyByValue(testNameMap, testNameS) // Replace with your function to get the ID
            id?.toString() ?: ""
        }
        val selectedTestIds = parseTestIds(idString)
        val existingTestIds = parseTestIds(investigationBD?.previousTestIds) + parseTestIds(investigationBD?.newTestIds)
        val hasNewOrChangedTests = if (investigationBD == null) {
            selectedTestIds.isNotEmpty()
        } else {
            selectedTestIds.isNotEmpty() && selectedTestIds != existingTestIds
        }
        val mergedPreviousTestIds = if (investigationBD != null) {
            (existingTestIds + selectedTestIds).takeIf { it.isNotEmpty() }?.joinToString(",")
        } else {
            selectedTestIds.takeIf { it.isNotEmpty() }?.joinToString(",")
        }
        val newTestIds = if (hasNewOrChangedTests) {
            selectedTestIds.joinToString(",")
        } else {
            null
        }

        val externalInvestigations = binding.inputExternalI.text.toString().nullIfEmpty()
        val referR = binding.inputReferReason.text.toString().nullIfEmpty()
        val counsellingTypesVal = binding.routeDropDownVal.text.toString().nullIfEmpty()
        val referVal = binding.referDropdownText.text.toString().nullIfEmpty()
        val referId = findKeyByValue(referNameMap, referVal)
        val counsellingList = counsellingTypesVal?.let { arrayListOf(it) } ?: arrayListOf()

        val investigation = InvestigationCaseRecord(
            investigationCaseRecordId = generateUuid(),
            previousTestIds = mergedPreviousTestIds,
            newTestIds = newTestIds,
            externalInvestigations = externalInvestigations,
            counsellingProvidedList = counsellingList,
            counsellingTypes = counsellingTypesVal,
            patientID = patId,
            institutionId = referId,
            referReson = referR,
            benVisitNo = benVisitNo
        )

        if(user?.masterVillageName != null && investigation.referReson != null){
            investigation.referReson += (pattern + user.masterVillageName)
        }

        if(user?.masterVillageName != null && (investigation.referReson != null || investigation.institutionId != null)){
            if(investigation.referReson == null){
                investigation.referReson = (pattern + user.masterVillageName);
            }
            else{
                investigation.referReson += (pattern + user.masterVillageName)
            }
        }

        val prescriptionList = mutableListOf<PrescriptionCaseRecord>();
        for (i in 0 until itemListP.size) {
            val prescriptionData = itemListP[i]
            var formVal = prescriptionData.id
            var freqVal = prescriptionData.frequency.nullIfEmpty()
            var unitVal = prescriptionData.unit.nullIfEmpty()
            var durVal = prescriptionData.duration.nullIfEmpty()
            var instructions = prescriptionData.instructions.nullIfEmpty()


            if (formVal != null) {
                val frequencyDescription = frequencyMap[freqVal]
                var mappedFrequency: String? = null
                if (frequencyDescription != null) {
                    mappedFrequency = frequencyDescription
                }
                var pres = PrescriptionCaseRecord(
                    prescriptionCaseRecordId = generateUuid(),
                    itemId = formVal,
                    frequency = mappedFrequency,
                    duration = durVal,
                    instructions = instructions,
                    unit = unitVal,
                    patientID = patId,
                    benVisitNo = benVisitNo
                )
                prescriptionList.add(pres);
            }
        }

        val hasLabInCase = selectedTestIds.isNotEmpty() || existingTestIds.isNotEmpty()
        doctorFlag = when {
            hasNewOrChangedTests -> 2
            hasLabInCase -> 3
            else -> 9
        }
        if (prescriptionList.size == 0) {
            pharmacistFlag = 0
        } else {
            pharmacistFlag = 1
        }
        // Auto-close: nothing prescribed (no test + no medicine) → mark case as complete immediately
        val effectivePharmacistFlag = if (doctorFlag == 9 && pharmacistFlag == 0) 9 else pharmacistFlag
        val patientVisitInfoSync = PatientVisitInfoSync(
            patientID = patId,
            benVisitNo = benVisitNo,
            createNewBenFlow = createNewBenflow,
            nurseFlag = 9,
            doctorFlag = doctorFlag,
            pharmacist_flag = effectivePharmacistFlag,
            visitDate = Date(),
        )

        viewModel.saveNurseAndDoctorData(
            visitDB,
            chiefComplaints,
            patientVitals,
            diagnosisList,
            investigation,
            prescriptionList,
            patientVisitInfoSync
        )

    }

    private fun saveDoctorData(benVisitNo: Int) {

        var diagnosisList = mutableListOf<DiagnosisCaseRecord>()
        for (i in 0 until itemListD.size) {
            val diagnosisData = itemListD[i]
            if (diagnosisData.diagnosis.isNullOrEmpty()) {
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.diagnosisCannotBeEmpty),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return;
            } else {
                var diagnosis = DiagnosisCaseRecord(
                    diagnosisCaseRecordId = generateUuid(),
                    diagnosis = diagnosisData.diagnosis,
                    patientID = patId,
                    benVisitNo = benVisitNo
                )
                diagnosisList.add(diagnosis)
            }
        }


        val testName = binding.selectF.text.toString()
        val testNamesList = testName.split(",").map { it.trim() }
        val idString = testNamesList.joinToString(",") { testNameS ->
            val id =
                findKeyByValue(testNameMap, testNameS) // Replace with your function to get the ID
            id?.toString() ?: ""
        }
        val selectedTestIds = parseTestIds(idString)
        val existingTestIds = parseTestIds(investigationBD?.previousTestIds) + parseTestIds(investigationBD?.newTestIds)
        val hasNewOrChangedTests = if (investigationBD == null) {
            selectedTestIds.isNotEmpty()
        } else {
            selectedTestIds.isNotEmpty() && selectedTestIds != existingTestIds
        }
        val mergedPreviousTestIds = if (investigationBD != null) {
            (existingTestIds + selectedTestIds).takeIf { it.isNotEmpty() }?.joinToString(",")
        } else {
            selectedTestIds.takeIf { it.isNotEmpty() }?.joinToString(",")
        }
        val newTestIds = if (hasNewOrChangedTests) {
            selectedTestIds.joinToString(",")
        } else {
            null
        }

        val externalInvestigations = binding.inputExternalI.text.toString().nullIfEmpty()
        val referR = binding.inputReferReason.text.toString().nullIfEmpty()
        val counsellingTypesVal = binding.routeDropDownVal.text.toString().nullIfEmpty()
        val referVal = binding.referDropdownText.text.toString().nullIfEmpty()
        val referId = findKeyByValue(referNameMap, referVal)


        val counsellingList = counsellingTypesVal?.let { arrayListOf(it) } ?: arrayListOf()


        val investigation = InvestigationCaseRecord(
            investigationCaseRecordId = generateUuid(),
            previousTestIds = mergedPreviousTestIds,
            newTestIds = newTestIds,
            externalInvestigations = externalInvestigations,
            counsellingProvidedList = counsellingList,
            counsellingTypes = counsellingTypesVal,
            patientID = patId,
            institutionId = referId,
            referReson = referR,
            benVisitNo = benVisitNo
        )


        val prescriptionList = mutableListOf<PrescriptionCaseRecord>();
        val prescriptionStartIndex = dispensedLockedPrescriptionCount.coerceAtMost(itemListP.size)
        for (i in prescriptionStartIndex until itemListP.size) {
            val prescriptionData = itemListP[i]
            var formVal = prescriptionData.id
            var freqVal = prescriptionData.frequency.nullIfEmpty()
            var unitVal = prescriptionData.unit.nullIfEmpty()
            var durVal = prescriptionData.duration.nullIfEmpty()
            var instructions = prescriptionData.instructions.nullIfEmpty()
            if (formVal != null) {
                val frequencyDescription = frequencyMap[freqVal]
                var mappedFrequency: String? = null
                if (frequencyDescription != null) {
                    mappedFrequency = frequencyDescription
                }
                var pres = PrescriptionCaseRecord(
                    prescriptionCaseRecordId = generateUuid(),
                    itemId = formVal,
                    frequency = mappedFrequency,
                    duration = durVal,
                    instructions = instructions,
                    unit = unitVal ?: DEFAULT_DURATION_UNIT,
                    patientID = patId,
                    benVisitNo = benVisitNo
                )
                prescriptionList.add(pres);
            }
        }
        val hasLabInCase = selectedTestIds.isNotEmpty() || existingTestIds.isNotEmpty()
        doctorFlag = when {
            hasNewOrChangedTests -> 2
            hasLabInCase -> 3
            else -> 9
        }

        viewModel.saveDoctorData(
            diagnosisList,
            investigation,
            prescriptionList,
            benVisitInfo,
            doctorFlag
        )

    }

    fun saveTemp(uniqueTemplateNames: HashSet<String?>) {
        var tempNameVal = binding.inputTestName.text.toString()
        if (tempNameVal == null || tempNameVal.equals("null") || tempNameVal.equals("")) {
            requireActivity().runOnUiThread {
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.template_null),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            if (uniqueTemplateNames.contains(binding.inputTestName.text.toString())) {
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.templte_exists),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                val isNameExists = tempDBVal.any { it?.templateName == tempNameVal }
                if (isNameExists) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            resources.getString(R.string.templte_exists),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val tempId = generateIntFromUuid()
                    val prescriptionTempList = mutableListOf<PrescriptionTemplateDB>();
                    for (i in 0 until itemListP.size) {
                        val prescriptionTemp = itemListP[i]
                        var formName = prescriptionTemp.form
                        var formVal = prescriptionTemp.id
                        var freqVal = prescriptionTemp.frequency.nullIfEmpty()
                        var unitVal = prescriptionTemp.unit.nullIfEmpty()
                        var durVal = prescriptionTemp.duration.nullIfEmpty()
                        var instructions = prescriptionTemp.instructions.nullIfEmpty()

                        if (formVal != null) {
                            var pres = viewModel.userIDVAl?.let {
                                PrescriptionTemplateDB(
                                    id = generateUuid(),
                                    tempID = tempId,
                                    templateName = tempNameVal,
                                    userID = it,
                                    drugName = formName,
                                    drugId = formVal,
                                    frequency = freqVal,
                                    duration = durVal,
                                    unit = unitVal ?: DEFAULT_DURATION_UNIT,
                                    instructions = instructions,
                                    deleteStatus = 0
                                )
                            }
                            if (pres != null) {
                                prescriptionTempList.add(pres)
                            }
                            Timber.tag("arr").i("${pres}")
                            if (pres != null) {
                                viewModel.savePrescriptionTemp(pres)
                            }
                        }
                    }
                    if (prescriptionTempList != null) {
                        viewModel.savePrescriptionTempToServer(prescriptionTempList)
                    }
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            resources.getString(R.string.template_save),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    binding.saveTemplate.isEnabled = false
                    binding.saveTemplate.alpha = 0.5f
                }
            }
        }
    }

    override fun getFragmentId(): Int {
        return R.id.case_record_custome_layout
    }

    override fun onSubmitAction() {
        navigateNext()
    }

    override fun onCancelAction() {
        if (isDoctorWorkflowRole()) {
            requireActivity().finish()
        } else {
            findNavController().navigateUp()
        }
    }

    fun navigateNext() {
        val isDoctorLabReviewCase = ::benVisitInfo.isInitialized &&
                benVisitInfo.nurseFlag == 9 && benVisitInfo.doctorFlag == 3
        if (isDoctorExistingVisitFlow() || isDoctorLabReviewCase) {
            val visitNo = benVisitInfo.benVisitNo
            if (visitNo == null || visitNo <= 0) {
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.something_wend_wong),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            // Manual close scenario: lab was involved (doctorFlag=3) AND doctor is NOT adding a new test
            // If doctor picks a new test → let the normal save flow handle it (new lab cycle)
            // If doctor submits with no test → close case with confirmation dialog
            val isLabReviewState = benVisitInfo.nurseFlag == 9 &&
                    benVisitInfo.doctorFlag == 3 &&
                    isDoctorWorkflowRole()

            if (isLabReviewState) {
                val currentPharmacistFlag = effectivePharmacistFlagForVisibility ?: benVisitInfo.pharmacist_flag ?: 0
                val hasNewTest = hasTestSelectionChanged()

                val hasNewMedicine = if (currentPharmacistFlag == 9) {
                    val newRowsStart = dispensedLockedPrescriptionCount.coerceAtMost(itemListP.size)
                    itemListP.drop(newRowsStart).any { it.id != null }
                } else {
                    itemListP.any { it.id != null }
                }

                if (currentPharmacistFlag != 1 && !hasNewTest && !hasNewMedicine) {
                    // No new test selected → confirm closure
                    // Set up observer BEFORE showing dialog to avoid race condition
                    viewModel.isDataSaved.removeObservers(viewLifecycleOwner)
                    viewModel.isDataSaved.observe(viewLifecycleOwner) { saved ->
                        if (saved == true) {
                            WorkerUtils.triggerAmritSyncWorker(requireContext())
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.case_closed_successfully),
                                Toast.LENGTH_SHORT
                            ).show()
                            requireActivity().finish()
                        }
                    }

                    // Also observe error messages from closure validation
                    viewModel.errorMessage.removeObservers(viewLifecycleOwner)
                    viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
                        if (!errorMsg.isNullOrBlank()) {
                            Toast.makeText(
                                requireContext(),
                                errorMsg,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    AlertDialog.Builder(requireContext())
                        .setTitle(getString(R.string.info))
                        .setMessage(getString(R.string.case_close_confirmation))
                        .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                            dialog.dismiss()
                            // Fetch latest visit info from DB before closing to ensure flags are current
                            lifecycleScope.launch {
                                val latestVisitInfo = viewModel.getPatientVisitInfoSyncByPatientIdAndBenVisitNo(
                                    benVisitInfo.patient.patientID,
                                    visitNo
                                )
                                if (latestVisitInfo != null) {
                                    // Update benVisitInfo with latest flags
                                    val updatedBenVisitInfo = benVisitInfo.copy(
                                        labtechFlag = latestVisitInfo.labtechFlag,
                                        pharmacist_flag = latestVisitInfo.pharmacist_flag,
                                        doctorFlag = latestVisitInfo.doctorFlag,
                                        nurseFlag = latestVisitInfo.nurseFlag
                                    )
                                    viewModel.closeCaseManually(updatedBenVisitInfo)
                                } else {
                                    viewModel.closeCaseManually(benVisitInfo)
                                }
                            }
                        }
                        .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setCancelable(false)
                        .show()
                    return
                }
                // Has new test → fall through to normal save flow (starts new lab cycle)
            }

            val validate = dAdapter.setError()
            if (validate == -1) {
                viewModel.isDataDeleted.removeObservers(viewLifecycleOwner)
                viewModel.deleteOldDoctorData(
                    benVisitInfo.patient.patientID,
                    visitNo
                )
                // Remove existing observer to prevent duplicates
                viewModel.isDataDeleted.removeObservers(viewLifecycleOwner)
                viewModel.isDataDeleted.observe(viewLifecycleOwner) { state ->
                    when (state!!) {
                        true -> {
                            saveDoctorData(visitNo)
                            viewModel.isDataSaved.removeObservers(viewLifecycleOwner)
                            viewModel.isDataSaved.observe(viewLifecycleOwner) {
                                when (it!!) {
                                    true -> {
                                        WorkerUtils.clinicalPushWorker(requireContext())
                                        requireActivity().runOnUiThread {
                                            Toast.makeText(
                                                requireContext(),
                                                resources.getString(R.string.dataSavedCaseRecord),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        requireActivity().finish()
                                    }

                                    else -> {}
                                }
                            }
                        }

                        else -> {}
                    }
                }
            } else {
                binding.diagnosisExtra.scrollToPosition(validate)
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.diagnosisCannotBeEmpty),
                    Toast.LENGTH_SHORT
                ).show()
            }

        } else {
            CoroutineScope(Dispatchers.Main).launch {
                if (masterDb!!.patientId.toString() != null) {
                    patId = masterDb!!.patientId.toString()
                }

                val validate = dAdapter.setError()
                if (validate == -1) {
                    var benVisitNo = 0;
                    var createNewBenflow = false;
                    viewModel.getLastVisitInfoSync(patId).let {
                        if (it == null) {
                            benVisitNo = 1;
                        } else if (it.nurseFlag == 1) {
                            benVisitNo = it.benVisitNo
                        } else {
                            benVisitNo = it.benVisitNo + 1
                            createNewBenflow = true;
                        }
                    }

                    val user = userRepo.getLoggedInUser()

                    saveNurseAndDoctorData(benVisitNo, createNewBenflow, user)

                    // Remove existing observer to prevent duplicates
                    viewModel.isDataSaved.removeObservers(viewLifecycleOwner)
                    viewModel.isDataSaved.observe(viewLifecycleOwner) { state ->
                        when (state!!) {
                            true -> {
                                WorkerUtils.clinicalPushWorker(requireContext())
                                requireActivity().runOnUiThread {
                                    Toast.makeText(
                                        requireContext(),
                                        resources.getString(R.string.dataSavedCaseRecord),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                requireActivity().finish()
                            }

                            else -> {}
                        }

                    }

                } else {
                    showToast()
                }
            }
        }
    }

    fun showToast() {
        requireActivity().runOnUiThread {
            Toast.makeText(
                requireContext(),
                resources.getString(R.string.diagnosisCannotBeEmpty),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
