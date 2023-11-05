package org.piramalswasthya.cho.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.databinding.RvItemPregnancyAncBinding
import org.piramalswasthya.cho.model.PregnantWomanAncCache

class AncVisitAdapter(private val clickListener: AncVisitClickListener) :
    ListAdapter<PregnantWomanAncCache, AncVisitAdapter.AncViewHolder>(
        MyDiffUtilCallBack
    ) {
    private object MyDiffUtilCallBack : DiffUtil.ItemCallback<PregnantWomanAncCache>() {
        override fun areItemsTheSame(
            oldItem: PregnantWomanAncCache, newItem: PregnantWomanAncCache
        ) = oldItem.patientID == newItem.patientID

        override fun areContentsTheSame(
            oldItem: PregnantWomanAncCache, newItem: PregnantWomanAncCache
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
            item: PregnantWomanAncCache, clickListener: AncVisitClickListener
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
        private val clickedForm: (patientID: String, visitNumber: Int) -> Unit,

        ) {
        fun onClickedVisit(item: PregnantWomanAncCache) = clickedForm(
            item.patientID, item.visitNumber
        )
    }

}