package org.piramalswasthya.cho.ui.commons.history_custom.FieldsFragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.adapter.IllnessAdapter
import org.piramalswasthya.cho.adapter.TobaccoAdapter
import org.piramalswasthya.cho.databinding.FragmentTobaccoBinding
import org.piramalswasthya.cho.model.IllnessDropdown
import org.piramalswasthya.cho.model.TobaccoDropdown
import org.piramalswasthya.cho.ui.HistoryFieldsInterface
@AndroidEntryPoint
class TobaccoFragment : Fragment() {

    private var _binding: FragmentTobaccoBinding? = null
    private val binding: FragmentTobaccoBinding
        get() = _binding!!

    private var historyListener: HistoryFieldsInterface? = null
    val viewModel: TobaccoFieldViewModel by viewModels()
    private var tobaccoOption = ArrayList<TobaccoDropdown>()
    private lateinit var tobaccoAdapter: TobaccoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTobaccoBinding.inflate(inflater, container,false)
        return binding.root
    }
    private var fragmentTag :String? = null
    fun setFragmentTag(tag:String){
        fragmentTag = tag
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


//        tobaccoAdapter = TobaccoAdapter(requireContext(), R.layout.drop_down,tobaccoOption)
//        binding.tobaccoText.setAdapter(tobaccoAdapter)
//
//        viewModel.tobaccoDropdown.observe( viewLifecycleOwner) { tob ->
//            tobaccoOption.clear()
//            tobaccoOption.addAll(tob)
//            tobaccoAdapter.notifyDataSetChanged()
//        }
        val tobaccoAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item)
        //AlcoholAdapter(requireContext(), R.layout.drop_down,alcoholOption)
        binding.tobaccoText.setAdapter(tobaccoAdapter)

        viewModel.tobaccoDropdown.observe( viewLifecycleOwner) { tob ->
//            alcoholOption.clear()
//            alcoholOption.addAll(alc)
            tobaccoAdapter.clear()
            tobaccoAdapter.addAll(tob.map { it.habitValue })
            tobaccoAdapter.notifyDataSetChanged()
        }

        binding.deleteButton.setOnClickListener {
            fragmentTag?.let {
                historyListener?.onDeleteButtonClickedTobacco(it)
            }
        }
        binding.plusButton.setOnClickListener {
            fragmentTag?.let {
                historyListener?.onAddButtonClickedTobacco(it)
            }
        }

        binding.plusButton.isEnabled = false
        binding.resetButton.isEnabled = false
        binding.tobaccoText.addTextChangedListener(inputTextWatcher)
        binding.resetButton.setOnClickListener {
            binding.tobaccoText.text?.clear()
        }
    }

    fun setListener(listener: HistoryFieldsInterface) {
        this.historyListener = listener
    }

    private val inputTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // No action needed
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            updateAddAndResetButtonState()
        }

        override fun afterTextChanged(s: Editable?) {
            // No action needed
        }
    }

    private fun updateAddAndResetButtonState() {
        val durationUnit = binding.tobaccoText.text.toString().trim()
        binding.plusButton.isEnabled = durationUnit.isNotEmpty()
        binding.resetButton.isEnabled = durationUnit.isNotEmpty()
    }

}