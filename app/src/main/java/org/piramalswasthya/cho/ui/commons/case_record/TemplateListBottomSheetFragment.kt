package org.piramalswasthya.cho.ui.commons.case_record

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.adapter.RecyclerViewItemClickedListener
import org.piramalswasthya.cho.adapter.TempListAdapter
import org.piramalswasthya.cho.databinding.TempBottomSheetBinding
import org.piramalswasthya.cho.repositories.PrescriptionTemplateRepo
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class TemplateListBottomSheetFragment(private val str: HashSet<String?>,
  private val prescriptionTemplateRepo: PrescriptionTemplateRepo
) : BottomSheetDialogFragment() {
    private var _binding: TempBottomSheetBinding? = null
    private val binding: TempBottomSheetBinding
        get() = _binding!!
    private val viewModel: TemplateBottomSheetViewModel by viewModels<TemplateBottomSheetViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = TempBottomSheetBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = TempListAdapter(str,
            object : RecyclerViewItemClickedListener {
                override fun onItemClicked(string: String?) {
                    string?.let {
                        viewModel.callMarkDel(it)
                    }
                    viewModel.callDel()
                    str.remove(string)
                    showToastAndRefreshList("Template deleted")
                }
            },
        )
        val divider = DividerItemDecoration(context, LinearLayout.VERTICAL)
        binding.tempExtra.adapter = adapter
        binding.tempExtra.addItemDecoration(divider)
    }
    private fun showToastAndRefreshList(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}