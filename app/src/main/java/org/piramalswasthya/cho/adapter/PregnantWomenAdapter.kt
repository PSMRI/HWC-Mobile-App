package org.piramalswasthya.cho.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.databinding.RvItemPregnantWomanBinding
import org.piramalswasthya.cho.model.PatientWithPwrDomain
import org.piramalswasthya.cho.utils.DateTimeUtil

class PregnantWomenAdapter(
    private val clickListener: ClickListener? = null
) : ListAdapter<PatientWithPwrDomain, PregnantWomenAdapter.PregnantWomanViewHolder>(
    PregnantWomanDiffUtilCallBack
) {

    private object PregnantWomanDiffUtilCallBack : DiffUtil.ItemCallback<PatientWithPwrDomain>() {
        override fun areItemsTheSame(
            oldItem: PatientWithPwrDomain,
            newItem: PatientWithPwrDomain
        ) = oldItem.patient.patientID == newItem.patient.patientID

        override fun areContentsTheSame(
            oldItem: PatientWithPwrDomain,
            newItem: PatientWithPwrDomain
        ) = oldItem == newItem
    }

    class PregnantWomanViewHolder private constructor(
        private val binding: RvItemPregnantWomanBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): PregnantWomanViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemPregnantWomanBinding.inflate(layoutInflater, parent, false)
                return PregnantWomanViewHolder(binding)
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

            // Handle PWR status display (registered or not)
            if (item.pwr == null || !item.isActive()) {
                // Not registered or inactive pregnancy
                binding.btnViewPwr.text = binding.root.context.getString(org.piramalswasthya.cho.R.string.register)
                binding.btnViewPwr.setBackgroundColor(
                    binding.root.resources.getColor(android.R.color.holo_red_dark, null)
                )
            } else {
                // Already registered and active
                binding.btnViewPwr.text = binding.root.context.getString(org.piramalswasthya.cho.R.string.view)
                binding.btnViewPwr.setBackgroundColor(
                    binding.root.resources.getColor(android.R.color.holo_green_dark, null)
                )
            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PregnantWomanViewHolder.from(parent)

    override fun onBindViewHolder(holder: PregnantWomanViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class ClickListener(
        private val clickedView: ((patientWithPwr: PatientWithPwrDomain) -> Unit)? = null
    ) {
        fun onClickView(item: PatientWithPwrDomain) =
            clickedView?.let { it(item) }
    }
}
