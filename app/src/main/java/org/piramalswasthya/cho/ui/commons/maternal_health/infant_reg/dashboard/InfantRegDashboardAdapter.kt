package org.piramalswasthya.cho.ui.commons.maternal_health.infant_reg.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.databinding.ItemInfantRegDashboardBinding
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo

class InfantRegDashboardAdapter(
    private val clickListener: (String) -> Unit
) : ListAdapter<PatientDisplayWithVisitInfo, InfantRegDashboardAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<PatientDisplayWithVisitInfo>() {
        override fun areItemsTheSame(
            oldItem: PatientDisplayWithVisitInfo,
            newItem: PatientDisplayWithVisitInfo
        ) = oldItem.patient.patientID == newItem.patient.patientID

        override fun areContentsTheSame(
            oldItem: PatientDisplayWithVisitInfo,
            newItem: PatientDisplayWithVisitInfo
        ) = oldItem == newItem
    }

    class ViewHolder(
        private val binding: ItemInfantRegDashboardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(patient: PatientDisplayWithVisitInfo, clickListener: (String) -> Unit) {
            binding.apply {
                // Display mother's name
                val fullName = "${patient.patient.firstName ?: ""} ${patient.patient.lastName ?: ""}".trim()
                tvMotherName.text = fullName.ifEmpty { "Unknown" }
                
                // Display mother's ID
                tvMotherId.text = "ID: ${patient.patient.patientID}"
                
                // Display age
                tvAge.text = "${patient.patient.age ?: "N/A"} years"
                
                // Display baby info (placeholder - will need to query delivery outcome for actual count)
                tvBabyInfo.text = "Babies need registration"
                
                // Status badge (placeholder - pending by default for dashboard)
                tvStatus.text = "Pending"
                tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#FF9800")) // Orange
                tvStatus.setTextColor(android.graphics.Color.WHITE)
                
                // Click listener
                root.setOnClickListener {
                    clickListener(patient.patient.patientID)
                }
                
                // Button click
                btnView.setOnClickListener {
                    clickListener(patient.patient.patientID)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemInfantRegDashboardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }
}
