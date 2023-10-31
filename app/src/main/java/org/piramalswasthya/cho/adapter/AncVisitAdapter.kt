package org.piramalswasthya.cho.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.databinding.RvItemPregnancyAncBinding
import org.piramalswasthya.cho.model.AncStatus

class AncVisitAdapter(private val clickListener: AncVisitClickListener) :
    ListAdapter<AncStatus, AncVisitAdapter.AncViewHolder>(
        MyDiffUtilCallBack
    ) {
    private object MyDiffUtilCallBack : DiffUtil.ItemCallback<AncStatus>() {
        override fun areItemsTheSame(
            oldItem: AncStatus, newItem: AncStatus
        ) = oldItem.benId == newItem.benId

        override fun areContentsTheSame(
            oldItem: AncStatus, newItem: AncStatus
        ) = oldItem == newItem

    }

    class AncViewHolder private constructor(private val binding: RvItemPregnancyAncBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): AncViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemPregnancyAncBinding.inflate(layoutInflater, parent, false)
                return AncViewHolder(binding)
            }
        }

        fun bind(
            item: AncStatus, clickListener: AncVisitClickListener
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


    class AncVisitClickListener(
        private val clickedForm: (benId: Long, visitNumber: Int) -> Unit,

        ) {
        fun onClickedVisit(item: AncStatus) = clickedForm(
            item.benId, item.visitNumber
        )
    }

}