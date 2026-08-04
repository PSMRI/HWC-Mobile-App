package org.piramalswasthya.cho.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.databinding.RvItemNeonatalOutcomeBinding
import org.piramalswasthya.cho.model.PatientWithPwrDomain

class NeonatalOutcomeAdapter(private val clickListener: ClickListener) :
    ListAdapter<PatientWithPwrDomain, NeonatalOutcomeAdapter.ViewHolder>(
        NeonatalOutcomeDiffCallback()
    ) {

    class ViewHolder(private val binding: RvItemNeonatalOutcomeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(patientWithPwr: PatientWithPwrDomain, clickListener: ClickListener) {
            binding.patientWithPwr = patientWithPwr
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemNeonatalOutcomeBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class ClickListener(val clickListener: (patientWithPwr: PatientWithPwrDomain) -> Unit) {
        fun onClick(patientWithPwr: PatientWithPwrDomain) = clickListener(patientWithPwr)
    }
}

class NeonatalOutcomeDiffCallback : DiffUtil.ItemCallback<PatientWithPwrDomain>() {
    override fun areItemsTheSame(
        oldItem: PatientWithPwrDomain,
        newItem: PatientWithPwrDomain
    ): Boolean {
        return oldItem.patient.patientID == newItem.patient.patientID
    }

    override fun areContentsTheSame(
        oldItem: PatientWithPwrDomain,
        newItem: PatientWithPwrDomain
    ): Boolean {
        return oldItem == newItem
    }
}
