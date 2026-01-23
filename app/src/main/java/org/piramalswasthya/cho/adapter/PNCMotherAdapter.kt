package org.piramalswasthya.cho.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.databinding.RvItemPncMotherBinding
import org.piramalswasthya.cho.model.PatientWithPncDomain
import org.piramalswasthya.cho.utils.DateTimeUtil

class PNCMotherAdapter(
    private val clickListener: ClickListener? = null
) : ListAdapter<PatientWithPncDomain, PNCMotherAdapter.PNCMotherViewHolder>(
    PNCMotherDiffUtilCallBack
) {

    private object PNCMotherDiffUtilCallBack : DiffUtil.ItemCallback<PatientWithPncDomain>() {
        override fun areItemsTheSame(
            oldItem: PatientWithPncDomain,
            newItem: PatientWithPncDomain
        ) = oldItem.patient.patientID == newItem.patient.patientID

        override fun areContentsTheSame(
            oldItem: PatientWithPncDomain,
            newItem: PatientWithPncDomain
        ) = oldItem == newItem
    }

    class PNCMotherViewHolder private constructor(
        private val binding: RvItemPncMotherBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): PNCMotherViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemPncMotherBinding.inflate(layoutInflater, parent, false)
                return PNCMotherViewHolder(binding)
            }
        }

        fun bind(
            item: PatientWithPncDomain,
            clickListener: ClickListener?
        ) {
            binding.patientWithPnc = item
            binding.clickListener = clickListener

            // Set age
            item.patient.dob?.let {
                binding.tvAge.text = DateTimeUtil.calculateAgeString(it)
            } ?: run {
                binding.tvAge.text = "NA"
            }

            // Set delivery date
            binding.tvDeliveryDate.text = item.getFormattedDeliveryDate()

            // Set days since delivery
            val daysSinceDelivery = item.getDaysSinceDelivery()
            binding.tvDaysSinceDelivery.text = if (daysSinceDelivery > 0) {
                "$daysSinceDelivery days"
            } else {
                "Today"
            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PNCMotherViewHolder.from(parent)

    override fun onBindViewHolder(holder: PNCMotherViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class ClickListener(
        private val clickedView: ((patientWithPnc: PatientWithPncDomain) -> Unit)? = null
    ) {
        fun onClickView(item: PatientWithPncDomain) =
            clickedView?.let { it(item) }
    }
}
