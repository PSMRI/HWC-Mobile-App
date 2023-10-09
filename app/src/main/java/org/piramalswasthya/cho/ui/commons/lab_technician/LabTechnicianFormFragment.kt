package org.piramalswasthya.cho.ui.commons.lab_technician



import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.databinding.FragmentLabTechnicianFormBinding
import org.piramalswasthya.cho.model.ComponentDetailDTO
import org.piramalswasthya.cho.model.ProcedureDTO
import org.piramalswasthya.cho.model.UserCache
import org.piramalswasthya.cho.ui.commons.FhirFragmentService
import org.piramalswasthya.cho.ui.commons.NavigationAdapter
import org.piramalswasthya.cho.ui.home_activity.HomeActivity
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class LabTechnicianFormFragment : Fragment(R.layout.fragment_lab_technician_form), FhirFragmentService, NavigationAdapter {

    private var _binding: FragmentLabTechnicianFormBinding? = null

    private val binding: FragmentLabTechnicianFormBinding
        get() {
            return _binding!!
        }

    override var fragment: Fragment = this;
    @Inject
    lateinit var preferenceDao: PreferenceDao
    override var fragmentContainerId = 0;
    private var userInfo: UserCache? = null

    override val jsonFile : String = "vitals-page.json"

    override val viewModel: LabTechnicianFormViewModel by viewModels()

    private lateinit var composeView: ComposeView

    private var dtos: List<ProcedureDTO>? = null

    private val args: LabTechnicianFormFragmentArgs by lazy {
        LabTechnicianFormFragmentArgs.fromBundle(requireArguments())
    }

    private val bundle = Bundle()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Create the ComposeView
        composeView = ComposeView(requireContext())

        return composeView
    }
    private val onBackPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onCancelAction()
            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

        viewModel.getLoggedInUserDetails()
        viewModel.boolCall.observe(viewLifecycleOwner){
            if(it){
                userInfo = viewModel.loggedInUser
                viewModel.resetBool()
            }
        }
        composeView.setContent {
            AddLoading()
        }

        lifecycleScope.launch {
            viewModel.downloadProcedure(patientId = args.patientId)
            viewModel.getPrescribedProcedures(patientId = args.patientId)
        }

        viewModel.procedures.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                composeView.setContent {
                    AddNoData()
                }
            } else {
                dtos = viewModel.procedures.value
                composeView.setContent {
                    AddProcedures(dtos)
                }
            }
        }
    }

    @Composable
    fun AddLoading() {
        LinearProgressIndicator()
    }

    @Composable
    fun AddNoData() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "No procedures to add",
                style = TextStyle(fontSize = 24.sp)
            )
        }
    }
    @Composable
    fun AddProcedures(dtos: List<ProcedureDTO>?) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .weight(1f, false)
                    .padding(bottom = 50.dp)
            ) {
                dtos?.forEach { procedure ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Add your Compose elements here
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = procedure.procedureName,
                                style = TextStyle(fontSize = 24.sp)
                            )
                        }
                        procedure.compListDetails.forEach {
                            when (it.inputType) {
                                "TextBox" -> {
                                    var textState by remember { mutableStateOf("") }
                                    it.testResultValue?.let {res ->
                                        textState = res
                                    }
                                    var remarksTextState by remember { mutableStateOf("") }
                                    it.remarks?.let { rem ->
                                        remarksTextState = rem
                                    }
                                    Column(
                                        modifier = Modifier
                                            .border(1.dp, Color.Gray)
                                            .fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(color = Color.LightGray)
                                                .padding(16.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = it.testComponentName,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(color = Color.LightGray)
                                                )
                                            }
                                        }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 16.dp, top = 16.dp)
                                        ) {
                                            BasicTextField(
                                                value = textState,
                                                onValueChange = { newText ->
                                                    if (newText.isNotEmpty() && newText.toDouble() < 10000){
                                                        textState = newText
                                                        it.testResultValue = newText
                                                    } else if (newText.isEmpty()) {
                                                        textState = newText
                                                        it.testResultValue = newText
                                                    }
                                                },
                                                modifier = Modifier
                                                    .padding(horizontal = 16.dp)
                                                    .fillMaxWidth(0.6f),
                                                textStyle = TextStyle(fontSize = 20.sp),
                                                cursorBrush = SolidColor(Color.Black),
                                                keyboardOptions = KeyboardOptions(
                                                    keyboardType = KeyboardType.Number,
                                                    imeAction = ImeAction.Done
                                                ),
                                                singleLine = true, // Use singleLine = true for a single-line input
                                                decorationBox = { innerTextField ->
                                                    Box(
                                                        modifier = Modifier
                                                            .border(1.dp, Color.Gray)
                                                            .padding(5.dp)
                                                            .background(Color.White)
                                                    ) {
                                                        innerTextField()
                                                    }
                                                }

                                            )
                                            it.measurementUnit?.let { it1 ->
                                                Text(
                                                    text = it1,
                                                    modifier = Modifier
                                                        .padding(start = 5.dp, top = 5.dp)
                                                        .fillMaxWidth(0.6f),
                                                )
                                            }
                                        }
                                        RangeText(dto = it)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 16.dp, top = 5.dp, bottom = 5.dp)
                                        ) {
                                            Text(
                                                text = "Remarks:",
                                                modifier = Modifier
                                                    .padding(start = 16.dp, top = 5.dp)
                                                    .fillMaxWidth(0.3f),
                                            )
                                            BasicTextField(
                                                value = remarksTextState,
                                                onValueChange = { newText ->
                                                    remarksTextState = newText
                                                    it.remarks = newText
                                                },
                                                modifier = Modifier
                                                    .padding(5.dp)
                                                    .fillMaxWidth(0.6f),
                                                textStyle = TextStyle(fontSize = 16.sp),
                                                cursorBrush = SolidColor(Color.Black),
                                                singleLine = true, // Use singleLine = true for a single-line input
                                                decorationBox = { innerTextField ->
                                                    Box(
                                                        modifier = Modifier
                                                            .border(1.dp, Color.Gray)
                                                            .padding(5.dp)
                                                            .background(Color.White)
                                                    ) {
                                                        innerTextField()
                                                    }
                                                }
                                            )

                                        }
                                    }


                                }

                                "RadioButton" -> {
                                    var selectedOption by remember { mutableStateOf("") }
                                    var options = mutableListOf<String>()
                                    Timber.d("comp otpipns ${it.testComponentName}" + it.compOpt.size)
                                    it.compOpt.forEach { opt ->
                                        options.add(opt.name.toString())
                                    }
                                    var remarksTextState by remember { mutableStateOf("") }
                                    Column(
                                        modifier = Modifier
                                            .border(1.dp, Color.Gray)
                                            .fillMaxWidth()
                                    ) {

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(color = Color.LightGray)
                                                .padding(16.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = it.testComponentName,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(color = Color.LightGray)
                                                )
                                            }
                                        }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 16.dp, top = 16.dp)
                                        ) {
                                            Column {
                                                RadioGroup(
                                                    options = options,
                                                    selectedOption = selectedOption,
                                                    onOptionSelected = { option ->
                                                        selectedOption = option
                                                        it.testResultValue = option
                                                    }
                                                )
                                            }

                                        }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        ) {
                                            Text(
                                                text = "Remarks:",
                                                modifier = Modifier
                                                    .padding(5.dp)
                                                    .fillMaxWidth(0.3f),
                                            )
                                            BasicTextField(
                                                value = remarksTextState,
                                                onValueChange = { newText ->
                                                    remarksTextState = newText
                                                    it.remarks = newText
                                                },
                                                modifier = Modifier
                                                    .padding(5.dp)
                                                    .fillMaxWidth(0.6f),
                                                textStyle = TextStyle(fontSize = 16.sp),
                                                cursorBrush = SolidColor(Color.Black),
                                                singleLine = true, // Use singleLine = true for a single-line input
                                                decorationBox = { innerTextField ->
                                                    Box(
                                                        modifier = Modifier
                                                            .border(1.dp, Color.Gray)
                                                            .padding(5.dp)
                                                            .background(Color.White)
                                                    ) {
                                                        innerTextField()
                                                    }
                                                }
                                            )

                                        }


                                    }


                                }

                                "DropDown" -> {
                                    var selectedOption by remember { mutableStateOf("") }
                                    var options = mutableListOf<String>()
                                    it.compOpt.forEach { opt ->
                                        options.add(opt.name.toString())
                                    }
                                    var remarksTextState by remember { mutableStateOf("") }
                                    Column(
                                        modifier = Modifier
                                            .border(1.dp, Color.Gray)
                                            .fillMaxWidth()
                                    ) {

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                        ) {
                                            Text(
                                                text = it.testComponentName,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(color = Color.LightGray)
                                            )
                                        }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 16.dp, top = 16.dp)
                                        ) {
                                            Column {
                                                Row() {
                                                    DropDown(it.compOpt.map { c -> c.name })
                                                }
                                            }

                                        }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        ) {
                                            Text(
                                                text = "Remarks:",
                                                modifier = Modifier
                                                    .padding(5.dp)
                                                    .fillMaxWidth(0.3f),
                                            )
                                            BasicTextField(
                                                value = remarksTextState,
                                                onValueChange = { newText ->
                                                    remarksTextState = newText
                                                    it.remarks = newText
                                                },
                                                modifier = Modifier
                                                    .padding(5.dp)
                                                    .fillMaxWidth(0.6f),
                                                textStyle = TextStyle(fontSize = 16.sp),
                                                cursorBrush = SolidColor(Color.Black),
                                                singleLine = true, // Use singleLine = true for a single-line input
                                                decorationBox = { innerTextField ->
                                                    Box(
                                                        modifier = Modifier
                                                            .border(1.dp, Color.LightGray)
                                                            .padding(5.dp)
                                                            .background(Color.White)
                                                    ) {
                                                        innerTextField()
                                                    }
                                                }
                                            )

                                        }


                                    }

                                }

                                else -> null
                            }
                        }
                    }
                }
            }
        }

        }


    @Composable
    fun RadioGroup(
        options: List<String>,
        selectedOption: String,
        onOptionSelected: (String) -> Unit
    ) {
        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (option == selectedOption),
                        onClick = { onOptionSelected(option) }
                    )
                    .padding(horizontal = 16.dp)
            ) {
                RadioButton(
                    selected = (option == selectedOption),
                    onClick = { onOptionSelected(option) }
                )
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyMedium.merge(),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DropDown(map: List<String?>) {
        var expanded by remember { mutableStateOf(false) }
        var selectedOptionText by remember { mutableStateOf("") }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            TextField(
                modifier = Modifier.menuAnchor(),
                readOnly = true,
                value = selectedOptionText,
                onValueChange = {},
//                label = { Text("Label") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                map.forEach { selectionOption ->
                    if (selectionOption != null) {
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                selectedOptionText = selectionOption
                                expanded = false
                            },
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun RangeText(dto: ComponentDetailDTO) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            var value = dto.testResultValue
            var rangeMin = dto.range_min
            var rangeMax = dto.range_max
            if (!value.isNullOrEmpty() && rangeMax != null && rangeMin != null) {
                var valueDouble = value.toDouble()
                if (valueDouble > rangeMax || valueDouble < rangeMin) {
                    Text(
                        text = "Range " + dto.range_min + " to " + dto.range_max,
                        color = Color.Red,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(0.6f),
                    )
                } else {
                    Text(
                        text = "Range " + dto.range_min + " to " + dto.range_max,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(0.6f),
                    )
                }
            } else {
                Text(
                    text = "Range " + dto.range_min + " to " + dto.range_max,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(0.6f),
                )
            }
        }
    }

    override fun getFragmentId(): Int {
        return R.id.fragment_lab_technician_form;
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
            viewModel.saveLabData(dtos, args.patientId)
            navigateNext()
        } else {
            Toast.makeText(requireContext(), "in valid data entered", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCancelAction() {
        val intent = Intent(context, HomeActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    override fun navigateNext() {
//        findNavController().navigate(
//            R.id.action_labTechnicianFormFragment_to_patientHomeFragment, bundle
//        )
        val intent = Intent(context, HomeActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

}