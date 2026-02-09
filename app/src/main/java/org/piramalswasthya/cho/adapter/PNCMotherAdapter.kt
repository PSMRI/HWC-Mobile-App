package org.piramalswasthya.cho.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.databinding.RvItemPncMotherBinding
import org.piramalswasthya.cho.model.PatientWithPncDomain
import org.piramalswasthya.cho.utils.DateTimeUtil
import org.piramalswasthya.cho.repositories.PncRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PNCMotherAdapter(
    private val clickListener: ClickListener? = null,
    private val pncRepo: PncRepo
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
            clickListener: ClickListener?,
            pncRepo: PncRepo
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
            binding.tvDaysSinceDelivery.text = when {
                daysSinceDelivery == null -> "NA"
                daysSinceDelivery > 0 -> "$daysSinceDelivery days"
                else -> "Today"
            }

            // Enable/disable Add Visit button based on eligibility
            CoroutineScope(Dispatchers.Main).launch {
                val isEnabled = withContext(Dispatchers.IO) {
                    checkAddVisitEligibility(item, pncRepo)
                }
                binding.btnAddVisit.isEnabled = isEnabled
                binding.btnAddVisit.alpha = if (isEnabled) 1.0f else 0.5f
            }

            binding.executePendingBindings()
        }
        
        private suspend fun checkAddVisitEligibility(
            item: PatientWithPncDomain,
            pncRepo: PncRepo
        ): Boolean {
            // Get last visit number
            val lastVisitNumber = pncRepo.getLastVisitNumber(item.patient.patientID) ?: 0
            val availableVisits = listOf(1, 3, 7, 14, 21, 28, 42).filter { it > lastVisitNumber }
            
            // No more visits available
            if (availableVisits.isEmpty()) return false
            
            val nextVisitNumber = availableVisits.first()
            
            // First visit is always allowed
            if (lastVisitNumber == 0) return true
            
            // For subsequent visits, check days since delivery
            val daysSinceDelivery = item.getDaysSinceDelivery() ?: return false
            
            // Enable button if enough days have elapsed
            return daysSinceDelivery >= nextVisitNumber
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PNCMotherViewHolder.from(parent)

    override fun onBindViewHolder(holder: PNCMotherViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener, pncRepo)
    }

    class ClickListener(
        private val onAddVisit: ((patientWithPnc: PatientWithPncDomain) -> Unit)? = null,
        private val onViewVisits: ((patientWithPnc: PatientWithPncDomain) -> Unit)? = null
    ) {
        fun clickAddVisit(item: PatientWithPncDomain) = onAddVisit?.invoke(item)
        fun clickViewVisits(item: PatientWithPncDomain) = onViewVisits?.invoke(item)
    }
}
