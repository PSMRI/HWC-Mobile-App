package org.piramalswasthya.cho.ui.commons.case_record

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.piramalswasthya.cho.adapter.RecyclerViewItemClickedListener
import org.piramalswasthya.cho.adapter.TempListAdapter
import org.piramalswasthya.cho.databinding.TempBottomSheetBinding
import timber.log.Timber


class TemplateListBottomSheetFragment(private val str: HashSet<String?>) : BottomSheetDialogFragment() {

    private var _binding: TempBottomSheetBinding? = null
    private val binding: TempBottomSheetBinding
        get() = _binding!!

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
                    Timber.tag("aryan").i("${string}")
                }
            },
        )

        val divider = DividerItemDecoration(context, LinearLayout.VERTICAL)
        binding.tempExtra.adapter = adapter
        binding.tempExtra.addItemDecoration(divider)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}