package org.piramalswasthya.cho.ui.commons.pharmacist



import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.PatientItemAdapter
import org.piramalswasthya.cho.adapter.PharmacistItemAdapter
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentLabTechnicianFormBinding
import org.piramalswasthya.cho.databinding.FragmentPharmacistFormBinding
import org.piramalswasthya.cho.databinding.FragmentUsernameBinding
import org.piramalswasthya.cho.model.ComponentDetailDTO
import org.piramalswasthya.cho.model.ProcedureDTO
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.cho.ui.commons.FhirFragmentService
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.commons.personal_details.PersonalDetailsViewModel
import org.piramalswasthya.cho.ui.edit_patient_details_activity.EditPatientDetailsActivity
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class PharmacistFormFragment : Fragment(R.layout.fragment_pharmacist_form), FhirFragmentService, NavigationAdapter {

    private var _binding: FragmentPharmacistFormBinding? = null

    private val binding: FragmentPharmacistFormBinding
        get() {
            return _binding!!
        }

    override var fragment: Fragment = this;
    @Inject
    lateinit var preferenceDao: PreferenceDao
    override var fragmentContainerId = 0;
    private var userInfo: UserCache? = null

    override val jsonFile : String = "vitals-page.json"

    override lateinit var viewModel: PharmacistFormViewModel

    private var dtos: List<ProcedureDTO>? = null

    private var itemAdapter : PharmacistItemAdapter? = null

    private val args: PharmacistFormFragmentArgs by lazy {
        PharmacistFormFragmentArgs.fromBundle(requireArguments())
    }

    private val bundle = Bundle()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Create the ComposeView
        _binding = FragmentPharmacistFormBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(PharmacistFormViewModel::class.java)
        viewModel.prescriptionObserver.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                PharmacistFormViewModel.NetworkState.SUCCESS -> {
                    var result = ""
                    if(itemAdapter?.itemCount==0||itemAdapter?.itemCount==1) {
                        result = getString(R.string.patient_cnt_display)
                    }
                    else {
                        result = getString(R.string.patients_cnt_display)
                    }
                    itemAdapter = context?.let { it ->
                        PharmacistItemAdapter(
                            it
                        )
                    }
                    binding.pharmacistList.adapter = itemAdapter
                    lifecycleScope.launch {
                        viewModel.getPrescription(patientId = args.patientId)
                    }

                    viewModel.prescriptions.observe(viewLifecycleOwner) {
                        viewModel.prescriptions?.value.asFlow().collect { it ->
                            itemAdapter?.submitList(it.sortedByDescending { it.patient.registrationDate})
                            binding.patientListContainer.patientCount.text =
                                itemAdapter?.itemCount.toString() + getResultStr(itemAdapter?.itemCount)
                            patientCount = it.size
                        }
                    }

                }

                else -> {

                }
            }
//        }
        }
    }

    override fun getFragmentId(): Int {
        return R.id.fragment_pharmacist_form;
    }

    override fun onSubmitAction() {
        var isValidData = true
        dtos?.forEach { procedureDTO ->
            procedureDTO.compListDetails.forEach { componentDetailDTO ->
                if (!componentDetailDTO.testResultValue.isNullOrEmpty() &&
                    componentDetailDTO.range_max != null &&
                    componentDetailDTO.range_min != null) {
                    isValidData = (componentDetailDTO.testResultValue!!.toDouble() > componentDetailDTO.range_min && componentDetailDTO.testResultValue!!.toDouble() < componentDetailDTO.range_max)
                }
            }
        }
        if (isValidData) {
//            viewModel.saveLabData(dtos, args.patientId)
            navigateNext()
        } else {
            Toast.makeText(requireContext(), "in valid data entered", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCancelAction() {
        requireActivity().finish()
    }

    override fun navigateNext() {
//        findNavController().navigate(
//            R.id.action_labTechnicianFormFragment_to_patientHomeFragment, bundle
//        )
        requireActivity().finish()
    }

}