package org.piramalswasthya.cho.ui.abha_id_activity.aadhaar_id.aadhaar_num_gov


import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.FragmentAadhaarNumberGovBinding
import org.piramalswasthya.cho.ui.abha_id_activity.aadhaar_id.AadhaarIdViewModel
import java.util.*

@AndroidEntryPoint
class AadhaarNumberGovFragment : Fragment() {
    private var _binding: FragmentAadhaarNumberGovBinding? = null
    private val binding: FragmentAadhaarNumberGovBinding
        get() = _binding!!

    private val parentViewModel: AadhaarIdViewModel by viewModels({ requireActivity() })

    private val viewModel: AadhaarNumberGovViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAadhaarNumberGovBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tietAadhaarNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                checkValidity()
            }
        })

        binding.tietFullName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                checkValidity()
            }

        })

        binding.rgGender.setOnCheckedChangeListener { _, _ -> checkValidity() }

        // observing and setting values for state code dropdown
        viewModel.stateCodes.observe(viewLifecycleOwner) { stateCodes ->
            stateCodes?.map { res -> res.name }?.toTypedArray()?.let {
                val adapterStateCodes = ArrayAdapter(
                    requireContext(), android.R.layout.simple_spinner_dropdown_item, it
                )
                binding.actvStateDn.setAdapter(adapterStateCodes)
            }
        }

        // setting district code dropdown values
        binding.actvStateDn.setOnItemClickListener { _, _, index, _ ->
            viewModel.activeState = viewModel.stateCodes.value?.get(index)
            viewModel.activeState?.districts?.map { dt -> dt.name }?.toTypedArray()?.let {
                val adapterDistrictCodes = ArrayAdapter(
                    requireContext(), android.R.layout.simple_spinner_dropdown_item, it
                )
                binding.actvDistrictDn.setAdapter(adapterDistrictCodes)
            }
            checkValidity()
        }

        // observing district dropdown
        binding.actvDistrictDn.setOnItemClickListener { _, _, index, _ ->
            viewModel.activeDistrict = viewModel.activeState?.districts?.get(index)
            checkValidity()
        }

        // setting date picker values
        val today = Calendar.getInstance()

        binding.dateEt.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                it.context,
                { _, year, month, day ->
                    binding.dateEt.setText(
                        "${if (day > 9) day else "0$day"}-${if (month > 8) month + 1 else "0${month + 1}"}-$year"
                    )
                },
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
            )
            binding.tilEditText.error = null
            datePickerDialog.show()
            checkValidity()
        }

        // generating abha
        binding.btnGenerateAbha.setOnClickListener {
            generateAbhaCard()
        }

        viewModel.state.observe(viewLifecycleOwner) {
            if (it == AadhaarIdViewModel.State.ABHA_GENERATED_SUCCESS) {
                parentViewModel.setAbha(Gson().toJson(viewModel.abha.value))
            }
            parentViewModel.setState(it)
        }
    }

    private fun generateAbhaCard() {
        viewModel.generateAbhaCard(
            aadhaarNumber = binding.tietAadhaarNumber.text.toString(),
            fullName = binding.tietFullName.text.toString(),
            dateOfBirth = binding.dateEt.text.toString(),
            gender = when (binding.rgGender.checkedRadioButtonId) {
                binding.rbFemale.id -> "F"
                binding.rbMale.id -> "M"
                else -> throw java.lang.Exception("no gender selected")
            }
        )
    }

    fun checkValidity() {
        binding.apply {
            btnGenerateAbha.isEnabled =
                rgGender.checkedRadioButtonId != -1 && !dateEt.text.isNullOrEmpty() && tietAadhaarNumber.text?.takeIf { it.isNotEmpty() }
                    ?.let { it.length == 12 } ?: false && viewModel.activeState != null && viewModel.activeDistrict != null &&
                        !tietFullName.text.isNullOrEmpty()

        }
    }
}