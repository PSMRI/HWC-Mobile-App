package org.piramalswasthya.cho.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.databinding.RvItemPncBottomsheetBinding
import org.piramalswasthya.cho.model.PNCVisitCache


class PncVisitAdapter(private val clickListener: PncVisitClickListener) :
    ListAdapter<PNCVisitCache, PncVisitAdapter.AncViewHolder>(
        MyDiffUtilCallBack
    ) {
    private object MyDiffUtilCallBack : DiffUtil.ItemCallback<PNCVisitCache>() {
        override fun areItemsTheSame(
            oldItem: PNCVisitCache, newItem: PNCVisitCache
        ) = oldItem.patientID == newItem.patientID

        override fun areContentsTheSame(
            oldItem: PNCVisitCache, newItem: PNCVisitCache
        ) = oldItem == newItem

    }

    class AncViewHolder private constructor(private val binding: RvItemPncBottomsheetBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): AncViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemPncBottomsheetBinding.inflate(layoutInflater, parent, false)
                return AncViewHolder(binding)
            }
        }

        fun bind(
            item: PNCVisitCache, clickListener: PncVisitClickListener
        ) {
            binding.visit = item
            binding.clickListener = clickListener
            binding.executePendingBindings()

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = AncViewHolder.from(parent)

    override fun onBindViewHolder(holder: AncViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }


    class PncVisitClickListener(
        private val clickedForm: (benId: String, visitNumber: Int) -> Unit,

        ) {
        fun onClickedVisit(item: PNCVisitCache) = clickedForm(
            item.patientID, item.pncPeriod
        )
    }

}