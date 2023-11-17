package org.piramalswasthya.cho.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.databinding.ChoListItemViewBinding
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.utils.DateTimeUtil

class CHOCaseRecordItemAdapter (
    private val clickListener: BenClickListener,
): ListAdapter<PatientVisitInfoSync, CHOCaseRecordItemAdapter.BenViewHolder>(
    BenDiffUtilCallBack
) {

    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<PatientVisitInfoSync>() {
        override fun areItemsTheSame(
            oldItem: PatientVisitInfoSync, newItem: PatientVisitInfoSync
        ) = oldItem.benVisitNo == newItem.benVisitNo

        override fun areContentsTheSame(
            oldItem: PatientVisitInfoSync, newItem: PatientVisitInfoSync
        ) = oldItem == newItem

    }

    class BenViewHolder private constructor(private val binding: ChoListItemViewBinding) :

        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): BenViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ChoListItemViewBinding.inflate(layoutInflater, parent, false)
                return BenViewHolder(binding)
            }
        }

        fun bind(
            item:PatientVisitInfoSync,
            clickListener: BenClickListener?,
        ) {
            binding.benVisitInfo = item
            binding.clickListener = clickListener

            binding.visitNumber.text = item.benVisitNo.toString() ?: ""
            if(item.visitDate != null){
                binding.visitDate.text = DateTimeUtil.formatDate(item.visitDate!!)
            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = BenViewHolder.from(parent)

    override fun onBindViewHolder(holder: BenViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class BenClickListener(
        private val clickedBen: (benVisitInfo: PatientVisitInfoSync) -> Unit,

    ) {
        fun onClickedBen(item: PatientVisitInfoSync) = clickedBen(
            item,
        )
    }
}