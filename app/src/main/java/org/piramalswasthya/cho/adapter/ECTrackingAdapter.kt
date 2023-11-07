package org.piramalswasthya.cho.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.databinding.RvItemEcTrackBinding
import org.piramalswasthya.cho.model.EligibleCoupleTrackingCache

class ECTrackingAdapter(private val clickListener: ECTrackViewClickListener) :
    ListAdapter<EligibleCoupleTrackingCache, ECTrackingAdapter.ECTrackViewHolder>(
        MyDiffUtilCallBack
    ) {
    private object MyDiffUtilCallBack : DiffUtil.ItemCallback<EligibleCoupleTrackingCache>() {
        override fun areItemsTheSame(
            oldItem: EligibleCoupleTrackingCache, newItem: EligibleCoupleTrackingCache
        ) = oldItem.visitDate == newItem.visitDate

        override fun areContentsTheSame(
            oldItem: EligibleCoupleTrackingCache, newItem: EligibleCoupleTrackingCache
        ) = oldItem == newItem

    }

    class ECTrackViewHolder private constructor(private val binding: RvItemEcTrackBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ECTrackViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemEcTrackBinding.inflate(layoutInflater, parent, false)
                return ECTrackViewHolder(binding)
            }
        }

        fun bind(
            item: EligibleCoupleTrackingCache, clickListener: ECTrackViewClickListener
        ) {
            binding.visit = item
            binding.filledOnString = EligibleCoupleTrackingCache.getECTFilledDateFromLong(item.visitDate)
            binding.clickListener = clickListener
            binding.executePendingBindings()

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = ECTrackViewHolder.from(parent)

    override fun onBindViewHolder(holder: ECTrackViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }


    class ECTrackViewClickListener(
        private val clickedForm: (benId: String, created: Long) -> Unit,

        ) {
        fun onClickedVisit(item: EligibleCoupleTrackingCache) = clickedForm(
            item.patientID, item.visitDate
        )
    }

}