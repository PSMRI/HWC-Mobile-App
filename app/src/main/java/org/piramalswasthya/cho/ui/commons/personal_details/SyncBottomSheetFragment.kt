package org.piramalswasthya.cho.ui.commons.personal_details

import org.piramalswasthya.cho.databinding.BottomSheetSyncBinding

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
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo


class SyncBottomSheetFragment(private val benVisit: PatientDisplayWithVisitInfo) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSyncBinding? = null
    private val binding: BottomSheetSyncBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSyncBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val divider = DividerItemDecoration(context, LinearLayout.VERTICAL)
        var doctorS = benVisit.doctorDataSynced
        var nurseS = benVisit.nurseDataSynced
        if(benVisit.patient.syncState == SyncState.SYNCED){
            binding.patS.setText(getString(R.string.Yes_state))
            binding.patS.setTextColor(requireContext().resources.getColor(R.color.green))
        }
        else{
            binding.patS.setText(getString(R.string.No_state))
            binding.patS.setTextColor(requireContext().resources.getColor(R.color.red))
        }
        if(nurseS == SyncState.SYNCED){
            binding.nurseS.setText(getString(R.string.Yes_state))
            binding.nurseS.setTextColor(requireContext().resources.getColor(R.color.green))
        }
        else{
            binding.nurseS.setText(getString(R.string.No_state))
            binding.nurseS.setTextColor(requireContext().resources.getColor(R.color.red))
        }
        if(doctorS == SyncState.SYNCED){
            binding.docS.setText(getString(R.string.Yes_state))
            binding.docS.setTextColor(requireContext().resources.getColor(R.color.green))
        }
        else{
            binding.docS.setText(getString(R.string.No_state))
            binding.docS.setTextColor(requireContext().resources.getColor(R.color.red))
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}