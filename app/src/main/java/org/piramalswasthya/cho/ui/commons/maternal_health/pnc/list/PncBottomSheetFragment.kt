package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pnc.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.adapters.PncVisitAdapter
import org.piramalswasthya.sakhi.databinding.BottomSheetAncBinding

@AndroidEntryPoint
class PncBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAncBinding? = null
    private val binding: BottomSheetAncBinding
        get() = _binding!!

    private val viewModel: PncMotherListViewModel by viewModels({ requireParentFragment() })


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAncBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvAnc.adapter =
            PncVisitAdapter(PncVisitAdapter.PncVisitClickListener { benId, visitNumber ->
                findNavController().navigate(
                    PncMotherListFragmentDirections.actionPncMotherListFragmentToPncFormFragment(
                        benId, visitNumber
                    )
                )
                this.dismiss()
            })

        val divider = DividerItemDecoration(context, LinearLayout.VERTICAL)
        binding.rvAnc.addItemDecoration(divider)
        observeList()
    }


    private fun observeList() {
        lifecycleScope.launch {
            viewModel.bottomSheetList.collect {
                (_binding?.rvAnc?.adapter as PncVisitAdapter?)?.submitList(it)
            }
        }
    }


    override fun dismiss() {
//        submitListToAncRv(emptyList())
        super.dismiss()
    }

}