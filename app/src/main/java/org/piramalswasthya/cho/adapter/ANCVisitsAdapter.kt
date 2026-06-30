package org.piramalswasthya.cho.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.databinding.RvItemAncVisitBinding
import org.piramalswasthya.cho.model.PatientWithPwrDomain
import org.piramalswasthya.cho.utils.DateTimeUtil

class ANCVisitsAdapter(
    private val clickListener: ClickListener? = null
) : ListAdapter<PatientWithPwrDomain, ANCVisitsAdapter.ANCVisitViewHolder>(
    ANCVisitDiffUtilCallBack
) {

    private object ANCVisitDiffUtilCallBack : DiffUtil.ItemCallback<PatientWithPwrDomain>() {
        override fun areItemsTheSame(
            oldItem: PatientWithPwrDomain,
            newItem: PatientWithPwrDomain
        ) = oldItem.patient.patientID == newItem.patient.patientID

        override fun areContentsTheSame(
            oldItem: PatientWithPwrDomain,
            newItem: PatientWithPwrDomain
        ) = oldItem == newItem
    }

    class ANCVisitViewHolder private constructor(
        private val binding: RvItemAncVisitBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): ANCVisitViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemAncVisitBinding.inflate(layoutInflater, parent, false)
                return ANCVisitViewHolder(binding)
            }
        }

        fun bind(
            item: PatientWithPwrDomain,
            clickListener: ClickListener?
        ) {
            binding.patientWithPwr = item
            binding.clickListener = clickListener
            binding.ivCall.setOnClickListener {
                clickListener?.onClickCall(item)
            }

            // Set weeks of pregnancy
            val weeks = item.getWeeksOfPregnancy()
            binding.tvWeeksPregnancy.text = if (weeks > 0 && weeks <= 40) {
                weeks.toString()
            } else {
                "N/A"
            }

            // Set LMP date
            binding.tvLmp.text = item.getFormattedLMPDate()

            // Set EDD
            binding.tvEdd.text = item.getFormattedEDD()

            binding.tvLastVisit.text = "N/A"

            // Hide "Add Visit" button if ANC 4 is completed and patient is not HRP
            val isAnc4Completed = item.ancRecords.any { it.visitNumber == 4 && it.weight != null }
            val isHRP = item.pwr?.isHrp ?: false
            binding.btnAddAnc.visibility = if (isAnc4Completed && !isHRP) View.GONE else View.VISIBLE

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ANCVisitViewHolder.from(parent)

    override fun onBindViewHolder(holder: ANCVisitViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class ClickListener(
        private val clickedAddANC: ((patientWithPwr: PatientWithPwrDomain) -> Unit)? = null,
        private val clickedANCVisits: ((patientWithPwr: PatientWithPwrDomain) -> Unit)? = null,
        private val clickedAddPMSMA: ((patientWithPwr: PatientWithPwrDomain) -> Unit)? = null,
        private val clickedCall: ((patientWithPwr: PatientWithPwrDomain) -> Unit)? = null
    ) {
        fun onClickAddANC(item: PatientWithPwrDomain) =
            clickedAddANC?.let { it(item) }

        fun onClickANCVisits(item: PatientWithPwrDomain) =
            clickedANCVisits?.let { it(item) }

        fun onClickAddPMSMA(item: PatientWithPwrDomain) =
            clickedAddPMSMA?.let { it(item) }

        fun onClickCall(item: PatientWithPwrDomain) =
            clickedCall?.let { it(item) }
    }
}
