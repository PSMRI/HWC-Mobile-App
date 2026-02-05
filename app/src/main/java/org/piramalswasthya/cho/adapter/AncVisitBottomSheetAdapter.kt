package org.piramalswasthya.cho.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.databinding.RvItemAncVisitSimpleBinding
import org.piramalswasthya.cho.helpers.Konstants
import org.piramalswasthya.cho.helpers.getWeeksOfPregnancy
import org.piramalswasthya.cho.helpers.getTodayMillis
import org.piramalswasthya.cho.model.PregnantWomanAncCache
import java.text.SimpleDateFormat
import java.util.*

class AncVisitBottomSheetAdapter(
    private val clickListener: AncVisitClickListener
) : ListAdapter<PregnantWomanAncCache, AncVisitBottomSheetAdapter.AncVisitViewHolder>(AncVisitDiffCallback()) {

    private var lmpDate: Long = 0L
    private var allAncVisits: List<PregnantWomanAncCache> = emptyList()

    fun updateData(visits: List<PregnantWomanAncCache>, lmpDate: Long) {
        this.lmpDate = lmpDate
        this.allAncVisits = visits
        submitList(visits)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AncVisitViewHolder {
        return AncVisitViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: AncVisitViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener, lmpDate, allAncVisits)
    }

    class AncVisitViewHolder private constructor(
        private val binding: RvItemAncVisitSimpleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: PregnantWomanAncCache, 
            clickListener: AncVisitClickListener,
            lmpDate: Long,
            allAncVisits: List<PregnantWomanAncCache>
        ) {
            binding.tvVisitTitle.text = "ANC Visit ${item.visitNumber}"
            
            // Format date
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val dateText = if (item.ancDate != null) {
                dateFormat.format(Date(item.ancDate))
            } else {
                "Not scheduled"
            }
            
            // Calculate gestational age
            val gaWeeks = getWeeksOfPregnancy(getTodayMillis(), lmpDate)
            
            // Check if completed (weight is filled)
            val isCompleted = item.weight != null
            
            if (isCompleted) {
                // COMPLETED
                binding.tvVisitDate.text = "Completed: $dateText"
                binding.tvVisitStatus.text = "✓ Completed"
                binding.tvVisitStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_green_dark))
            } else {
                // Check if DUE based on GA and previous visit completion
                val isDue = when (item.visitNumber) {
                    1 -> {
                        // ANC 1: GA ≤12 weeks
                        gaWeeks <= Konstants.maxAnc1Week
                    }
                    2 -> {
                        // ANC 2: ANC 1 completed AND GA ≥14 weeks AND <28 weeks
                        val anc1Completed = allAncVisits.any { it.visitNumber == 1 && it.weight != null }
                        anc1Completed && gaWeeks >= Konstants.minAnc2Week && gaWeeks < 28
                    }
                    3 -> {
                        // ANC 3: ANC 2 completed AND GA ≥28 weeks AND <36 weeks
                        val anc2Completed = allAncVisits.any { it.visitNumber == 2 && it.weight != null }
                        anc2Completed && gaWeeks >= Konstants.minAnc3Week && gaWeeks < 36
                    }
                    4 -> {
                        // ANC 4: ANC 3 completed AND GA ≥36 weeks AND ≤40 weeks
                        val anc3Completed = allAncVisits.any { it.visitNumber == 3 && it.weight != null }
                        anc3Completed && gaWeeks >= Konstants.minAnc4Week && gaWeeks <= Konstants.maxAnc4Week
                    }
                    else -> false
                }
                
                if (isDue) {
                    // DUE
                    binding.tvVisitDate.text = "Scheduled: $dateText (GA: $gaWeeks weeks)"
                    binding.tvVisitStatus.text = "⚠ Due"
                    binding.tvVisitStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_orange_dark))
                } else {
                    // PENDING (not yet due)
                    binding.tvVisitDate.text = "Scheduled: $dateText (GA: $gaWeeks weeks)"
                    binding.tvVisitStatus.text = "○ Pending"
                    binding.tvVisitStatus.setTextColor(binding.root.context.getColor(android.R.color.darker_gray))
                }
            }
            
            binding.root.setOnClickListener {
                clickListener.onClick(item)
            }
        }

        companion object {
            fun from(parent: ViewGroup): AncVisitViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemAncVisitSimpleBinding.inflate(layoutInflater, parent, false)
                return AncVisitViewHolder(binding)
            }
        }
    }

    class AncVisitClickListener(val clickListener: (ancVisit: PregnantWomanAncCache) -> Unit) {
        fun onClick(ancVisit: PregnantWomanAncCache) = clickListener(ancVisit)
    }
}

class AncVisitDiffCallback : DiffUtil.ItemCallback<PregnantWomanAncCache>() {
    override fun areItemsTheSame(oldItem: PregnantWomanAncCache, newItem: PregnantWomanAncCache): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: PregnantWomanAncCache, newItem: PregnantWomanAncCache): Boolean {
        return oldItem == newItem
    }
}

