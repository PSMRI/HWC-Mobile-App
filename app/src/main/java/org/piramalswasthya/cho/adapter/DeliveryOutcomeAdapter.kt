package org.piramalswasthya.cho.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.databinding.RvItemDeliveryOutcomeBinding
import org.piramalswasthya.cho.model.PatientWithPwrDomain
import org.piramalswasthya.cho.utils.DateTimeUtil

class DeliveryOutcomeAdapter(
    private val clickListener: ClickListener? = null
) : ListAdapter<PatientWithPwrDomain, DeliveryOutcomeAdapter.DeliveryOutcomeViewHolder>(
    DeliveryOutcomeDiffUtilCallBack
) {

    private object DeliveryOutcomeDiffUtilCallBack : DiffUtil.ItemCallback<PatientWithPwrDomain>() {
        override fun areItemsTheSame(
            oldItem: PatientWithPwrDomain,
            newItem: PatientWithPwrDomain
        ) = oldItem.patient.patientID == newItem.patient.patientID

        override fun areContentsTheSame(
            oldItem: PatientWithPwrDomain,
            newItem: PatientWithPwrDomain
        ) = oldItem == newItem
    }

    class DeliveryOutcomeViewHolder private constructor(
        private val binding: RvItemDeliveryOutcomeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): DeliveryOutcomeViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemDeliveryOutcomeBinding.inflate(layoutInflater, parent, false)
                return DeliveryOutcomeViewHolder(binding)
            }
        }

        fun bind(
            item: PatientWithPwrDomain,
            clickListener: ClickListener?
        ) {
            binding.patientWithPwr = item
            binding.clickListener = clickListener

            // Set age
            item.patient.dob?.let {
                binding.tvAge.text = DateTimeUtil.calculateAgeString(it)
            } ?: run {
                binding.tvAge.text = "NA"
            }

            // Set LMP date
            binding.tvLmp.text = item.getFormattedLMPDate()

            // Set EDD
            binding.tvEdd.text = item.getFormattedEDD()

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        DeliveryOutcomeViewHolder.from(parent)

    override fun onBindViewHolder(holder: DeliveryOutcomeViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class ClickListener(
        private val clickedView: ((patientWithPwr: PatientWithPwrDomain) -> Unit)? = null
    ) {
        fun onClickView(item: PatientWithPwrDomain) =
            clickedView?.let { it(item) }
    }
}
