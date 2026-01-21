package org.piramalswasthya.cho.ui.commons.maternal_health.delivery_outcome.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.databinding.ItemDeliveryOutcomeListBinding
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo

class DeliveryOutcomeListAdapter(
    private val clickListener: (String) -> Unit
) : ListAdapter<PatientDisplayWithVisitInfo, DeliveryOutcomeListAdapter.ViewHolder>(DiffCallback) {

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
        private val binding: ItemDeliveryOutcomeListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(patient: PatientDisplayWithVisitInfo, clickListener: (String) -> Unit) {
            binding.apply {
                // Display patient name
                val fullName = "${patient.patient.firstName ?: ""} ${patient.patient.lastName ?: ""}".trim()
                tvPatientName.text = fullName.ifEmpty { "Unknown" }
                
                // Display patient ID
                tvPatientId.text = "ID: ${patient.patient.patientID}"
                
                // Display age and gender
                val age = patient.patient.age ?: "N/A"
                val gender = patient.patient.genderID ?: "N/A"
                tvAgeGender.text = "$age | $gender"
                
                // Display village/address
                tvVillage.text =  "N/A"
                
                // Click listener
                root.setOnClickListener {
                    clickListener(patient.patient.patientID)
                }
                
                // Button click
                btnRegister.setOnClickListener {
                    clickListener(patient.patient.patientID)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemDeliveryOutcomeListBinding.inflate(
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
