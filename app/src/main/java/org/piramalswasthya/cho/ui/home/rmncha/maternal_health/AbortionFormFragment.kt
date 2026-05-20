package org.piramalswasthya.cho.ui.home.rmncha.maternal_health

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.FormInputAdapter
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentNewFormBinding
import com.bumptech.glide.Glide
import org.piramalswasthya.cho.repositories.EcrRepo
import org.piramalswasthya.cho.repositories.MaternalHealthRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.UserRepo
import org.piramalswasthya.cho.utils.ImgUtils
import org.piramalswasthya.cho.work.WorkerUtils
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class AbortionFormFragment : Fragment() {

    @Inject
    lateinit var preferenceDao: PreferenceDao

    @Inject
    lateinit var maternalHealthRepo: MaternalHealthRepo

    @Inject
    lateinit var patientRepo: PatientRepo

    @Inject
    lateinit var userRepo: UserRepo

    @Inject
    lateinit var ecrRepo: EcrRepo

    private var _binding: FragmentNewFormBinding? = null
    private val binding get() = _binding!!

    private val patientId by lazy { requireArguments().getString(ARG_PATIENT_ID).orEmpty() }
    private val actionType by lazy { requireArguments().getString(ARG_ACTION_TYPE).orEmpty() }

    private val viewModel: AbortionFormViewModel by viewModels {
        AbortionFormViewModel.Factory(
            patientId = patientId,
            actionType = actionType,
            preferenceDao = preferenceDao,
            context = requireContext().applicationContext,
            maternalHealthRepo = maternalHealthRepo,
            patientRepo = patientRepo,
            userRepo = userRepo,
            ecrRepo = ecrRepo
        )
    }
    private var formAdapter: FormInputAdapter? = null
    private var latestList = emptyList<org.piramalswasthya.cho.model.FormElement>()
    private var currentImageFormId: Int? = null
    private var latestTmpUri: Uri? = null
    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                launchCameraCapture()
            } else {
                Toast.makeText(requireContext(), getString(R.string.camera_permission_denied), Toast.LENGTH_SHORT).show()
            }
        }
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                latestTmpUri?.let { updateImageUriForCurrentForm(it) }
            }
        }
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                updateImageUriForCurrentForm(it)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rebuildAdapter(isEnabled = actionType.equals("Add", true))

        lifecycleScope.launchWhenStarted {
            viewModel.formList.collect { list ->
                if (list.isNotEmpty()) {
                    latestList = list
                    formAdapter?.submitList(list)
                }
            }
        }

        viewModel.benName.observe(viewLifecycleOwner) { binding.tvBenName.text = it }
        viewModel.benAgeGender.observe(viewLifecycleOwner) { binding.tvAgeGender.text = it }
        viewModel.formReady.observe(viewLifecycleOwner) { ready ->
            if (!ready) {
                binding.llContent.visibility = View.GONE
                binding.pbForm.visibility = View.VISIBLE
            } else {
                binding.llContent.visibility = View.VISIBLE
                binding.pbForm.visibility = View.GONE
            }
        }
        viewModel.isEditable.observe(viewLifecycleOwner) { editable ->
            binding.btnSubmit.visibility = if (editable) View.VISIBLE else View.GONE
            binding.fabEdit.visibility = if (!editable && actionType.equals("View", true)) View.VISIBLE else View.GONE
            rebuildAdapter(isEnabled = editable)
        }

        binding.btnSubmit.setOnClickListener {
            val result = (binding.form.rvInputForm.adapter as? FormInputAdapter)?.validateInput(resources)
            if (result == -1) {
                viewModel.saveForm()
            } else if (result != null) {
                binding.form.rvInputForm.scrollToPosition(result)
            }
        }
        binding.fabEdit.setOnClickListener { viewModel.enableEditMode() }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                AbortionFormViewModel.State.IDLE -> Unit
                AbortionFormViewModel.State.SAVING -> {
                    binding.llContent.visibility = View.GONE
                    binding.pbForm.visibility = View.VISIBLE
                }
                AbortionFormViewModel.State.SAVE_SUCCESS -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    Toast.makeText(requireContext(), getString(R.string.save_successful_toast), Toast.LENGTH_LONG).show()
                    WorkerUtils.triggerAncVisitSync(requireContext())
                    WorkerUtils.triggerEligibleCoupleTrackingSync(requireContext())
                    WorkerUtils.triggerBeneficiarySync(requireContext())
                    parentFragmentManager.popBackStack()
                }
                AbortionFormViewModel.State.SAVE_FAILED -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    Toast.makeText(requireContext(), getString(R.string.something_wend_wong_contact_testing), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        formAdapter = null
    }

    private fun rebuildAdapter(isEnabled: Boolean) {
        formAdapter = FormInputAdapter(
            selectImageClickListener = FormInputAdapter.SelectUploadImageClickListener { formId ->
                if (isEnabled && (formId == 21 || formId == 22)) {
                    currentImageFormId = formId
                    showMediaOptionsPopup()
                }
            },
            imageClickListener = FormInputAdapter.ImageClickListener { formId ->
                if (isEnabled && (formId == 21 || formId == 22)) {
                    currentImageFormId = formId
                    showMediaOptionsPopup()
                }
            },
            formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                viewModel.updateListOnValueChanged(formId, index)
                if (formId == 1) {
                    binding.form.rvInputForm.adapter?.notifyItemChanged(viewModel.getWeeksOfPregnancyIndex())
                }
            },
            isEnabled = isEnabled,
            viewDocumentListner = FormInputAdapter.ViewDocumentOnClick { formId ->
                if (formId == 21 || formId == 22) showAbortionImagePreview(formId)
            }
        )
        binding.form.rvInputForm.adapter = formAdapter
        if (latestList.isNotEmpty()) {
            formAdapter?.submitList(latestList)
        }
    }

    private fun showMediaOptionsPopup() {
        val alertView = layoutInflater.inflate(R.layout.layout_media_options, binding.root, false)
        val btnGallery = alertView.findViewById<Button>(R.id.btnGallery)
        val btnCamera = alertView.findViewById<Button>(R.id.btnCamera)
        val btnPdf = alertView.findViewById<Button>(R.id.btnPdf)
        val btnCancel = alertView.findViewById<Button>(R.id.btnCancel)
        btnPdf.visibility = View.GONE
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(alertView)
            .setCancelable(true)
            .create()

        btnCamera.setOnClickListener {
            dialog.dismiss()
            handleCameraSelection()
        }
        btnGallery.setOnClickListener {
            dialog.dismiss()
            pickImageLauncher.launch("image/*")
        }
        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun handleCameraSelection() {
        val permissionStatus = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        )
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            launchCameraCapture()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCameraCapture() {
        latestTmpUri = getTmpFileUri()
        takePictureLauncher.launch(latestTmpUri)
    }

    private fun getTmpFileUri(): Uri {
        val imagesDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val tmpFile = File.createTempFile("cac_discharge_", ".jpg", imagesDir)
        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            tmpFile
        )
    }

    private fun updateImageUriForCurrentForm(uri: Uri) {
        val formId = currentImageFormId ?: return
        viewModel.setImageUriToFormElement(formId, uri.toString())
        val index = viewModel.getImageFieldIndex(formId)
        if (index >= 0) {
            binding.form.rvInputForm.adapter?.notifyItemChanged(index)
        }
    }

    private fun showAbortionImagePreview(formId: Int) {
        val raw = viewModel.getAbortionImageFieldValue(formId) ?: return
        val imageView = ImageView(requireContext()).apply {
            adjustViewBounds = true
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        val scroll = ScrollView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            addView(imageView)
        }
        when {
            raw.startsWith("data:image", ignoreCase = true) -> {
                val comma = raw.indexOf(',')
                val payload = if (comma >= 0) raw.substring(comma + 1) else raw
                ImgUtils.decodeBase64ToBitmap(payload)?.let { imageView.setImageBitmap(it) }
            }
            raw.contains("://") -> {
                Glide.with(this).load(Uri.parse(raw)).fitCenter().into(imageView)
            }
            else -> {
                ImgUtils.decodeBase64ToBitmap(raw)?.let { imageView.setImageBitmap(it) }
            }
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(
                when (formId) {
                    21 -> getString(R.string.abortion_discharge_summary_1)
                    22 -> getString(R.string.abortion_discharge_summary_2)
                    else -> getString(R.string.view)
                }
            )
            .setView(scroll)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    companion object {
        private const val ARG_ACTION_TYPE = "arg_action_type"
        private const val ARG_PATIENT_ID = "arg_patient_id"

        fun newInstance(actionType: String, patientId: String): AbortionFormFragment {
            return AbortionFormFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ACTION_TYPE, actionType)
                    putString(ARG_PATIENT_ID, patientId)
                }
            }
        }
    }
}

