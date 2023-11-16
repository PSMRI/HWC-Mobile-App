package org.piramalswasthya.cho.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.adapter.SyncStatusAdapter
import org.piramalswasthya.cho.databinding.BottomSheetSyncOverallBinding

class SyncBottomSheetOverallFragment  : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSyncOverallBinding? = null
    private val binding: BottomSheetSyncOverallBinding
        get() = _binding!!

    private val viewModel: SyncViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSyncOverallBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter =  SyncStatusAdapter()
        val divider = DividerItemDecoration(context, LinearLayout.VERTICAL)
        binding.rvSync.adapter = adapter
        binding.rvSync.addItemDecoration(divider)

        lifecycleScope.launch{
            viewModel.syncStatus.collect{
                adapter.submitList(it)
            }
        }
    }




    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}