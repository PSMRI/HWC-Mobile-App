package org.piramalswasthya.cho.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.databinding.PatientListItemViewBinding
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.network.ESanjeevaniApiService


class PatientItemAdapter(
    private val apiService: ESanjeevaniApiService,
    private val context: Context,
    private val clickListener: BenClickListener,
    private val showAbha: Boolean = false,
) : ListAdapter<PatientDisplay, PatientItemAdapter.BenViewHolder>(BenDiffUtilCallBack) {

    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<PatientDisplay>() {
        override fun areItemsTheSame(
            oldItem: PatientDisplay, newItem: PatientDisplay
        ) = oldItem.patient.beneficiaryID == newItem.patient.beneficiaryID

        override fun areContentsTheSame(
            oldItem: PatientDisplay, newItem: PatientDisplay
        ) = oldItem == newItem

    }

    private var usernameEs: String = ""
    private var passwordEs: String = ""
    private var errorEs: String = ""
    private var patientId: String = ""
    private var network: Boolean = false

    class BenViewHolder private constructor(private val binding: PatientListItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): BenViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = PatientListItemViewBinding.inflate(layoutInflater, parent, false)
                return BenViewHolder(binding)
            }
        }

        fun bind(
            item: PatientDisplay,
            clickListener: BenClickListener?,
            showAbha: Boolean,
        ) {
            binding.ben = item.patient
            binding.clickListener = clickListener
            binding.showAbha = showAbha
            binding.hasAbha = !item.patient.healthIdDetails?.healthIdNumber.isNullOrEmpty()

            binding.patientName.text =
                (item.patient.firstName ?: "") + " " + (item.patient.lastName ?: "")
            binding.patientAbhaNumber.text = item.patient.healthIdDetails?.healthIdNumber ?: ""
            binding.patientAge.text = (item.patient.age?.toString() ?: "") + " " + item.ageUnit.name
            binding.patientPhoneNo.text = item.patient.phoneNo ?: ""
            binding.patientGender.text = item.gender.genderName
            if (item.patient.syncState == SyncState.SYNCED) {
                binding.ivSyncState.visibility = View.VISIBLE
                binding.patientBenId.text = item.patient.beneficiaryID.toString()
                binding.llBenId.visibility = View.VISIBLE
                binding.btnAbha.isEnabled = true
            } else {
                binding.btnAbha.isEnabled = false
                binding.llBenId.visibility = View.GONE
                binding.ivSyncState.visibility = View.GONE
            }

            binding.executePendingBindings()

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = BenViewHolder.from(parent)

    override fun onBindViewHolder(holder: BenViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener, showAbha)

    }

    class BenClickListener(
        private val clickedBen: (patientID: String) -> Unit,
        private val clickedABHA: (benId: Long?) -> Unit,
        private val clickedEsanjeevani: (patient: Patient) -> Unit,
    ) {
        fun onClickedBen(item: Patient) = clickedBen(
            item.patientID,
        )

        fun onClickABHA(item: Patient) {
            clickedABHA(item.beneficiaryID)
        }

        fun onClickEsanjeevani(item: Patient) {
            clickedEsanjeevani(item)
        }
    }


}