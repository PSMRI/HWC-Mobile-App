package org.piramalswasthya.cho.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.RvItemPatientEcWithFormBinding
import org.piramalswasthya.cho.model.PatientWithEcrDomain
import java.util.concurrent.TimeUnit

class ECRegistrationAdapter(
    private val clickListener: ClickListener? = null
) : ListAdapter<PatientWithEcrDomain, ECRegistrationAdapter.PatientViewHolder>(PatientDiffUtilCallBack) {

    private object PatientDiffUtilCallBack : DiffUtil.ItemCallback<PatientWithEcrDomain>() {
        override fun areItemsTheSame(
            oldItem: PatientWithEcrDomain,
            newItem: PatientWithEcrDomain
        ) = oldItem.patient.patientID == newItem.patient.patientID

        override fun areContentsTheSame(
            oldItem: PatientWithEcrDomain,
            newItem: PatientWithEcrDomain
        ) = oldItem == newItem &&
                oldItem.lastVisitDate == newItem.lastVisitDate &&
                oldItem.methodOfContraception == newItem.methodOfContraception &&
                oldItem.antraNextDueDate == newItem.antraNextDueDate &&
                oldItem.antraInjectionDate == newItem.antraInjectionDate &&
                oldItem.lmpDateFromTracking == newItem.lmpDateFromTracking
    }

    class PatientViewHolder private constructor(
        private val binding: RvItemPatientEcWithFormBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): PatientViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemPatientEcWithFormBinding.inflate(layoutInflater, parent, false)
                return PatientViewHolder(binding)
            }
        }

        fun bind(
            item: PatientWithEcrDomain,
            clickListener: ClickListener?
        ) {
            binding.patientWithEcr = item
            binding.clickListener = clickListener

            // Always show LMP and Status layout blocks
            binding.llLmpDate.visibility = View.VISIBLE

            val lmpDate = item.ecr?.lmpDate ?: item.lmpDateFromTracking
            if (lmpDate != null && lmpDate > 0L) {
                val daysSinceLMP = TimeUnit.MILLISECONDS.toDays(
                    System.currentTimeMillis() - lmpDate
                )
                binding.ivMissState.visibility = if (daysSinceLMP > 35) View.VISIBLE else View.GONE
            } else {
                binding.ivMissState.visibility = View.GONE
            }

            // Show Last Visit Date if available
            binding.llLastVisitDate.visibility = if (item.lastVisitDate != null && item.lastVisitDate!! > 0L) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }

            // Show ANTRA details if selected
            binding.llAntraDetails.visibility = if (item.methodOfContraception == "ANTRA Injection") {
                View.VISIBLE
            } else {
                View.GONE
            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PatientViewHolder.from(parent)

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class ClickListener(
        private val onAddVisit: ((patientWithEcr: PatientWithEcrDomain) -> Unit)? = null,
        private val onViewVisit: ((patientWithEcr: PatientWithEcrDomain) -> Unit)? = null
    ) {
        fun onAddVisit(item: PatientWithEcrDomain) =
            onAddVisit?.let { it(item) }

        fun onViewVisit(item: PatientWithEcrDomain) =
            onViewVisit?.let { it(item) }
    }
}