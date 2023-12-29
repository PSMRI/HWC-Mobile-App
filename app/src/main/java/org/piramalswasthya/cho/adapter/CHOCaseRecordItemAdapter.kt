package org.piramalswasthya.cho.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.ChoListItemViewBinding
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.utils.DateTimeUtil

class CHOCaseRecordItemAdapter (
    private val clickListener: BenClickListener,
): ListAdapter<PatientDisplayWithVisitInfo, CHOCaseRecordItemAdapter.BenViewHolder>(
    BenDiffUtilCallBack
) {

    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<PatientDisplayWithVisitInfo>() {
        override fun areItemsTheSame(
            oldItem: PatientDisplayWithVisitInfo, newItem: PatientDisplayWithVisitInfo
        ) = oldItem.benVisitNo == newItem.benVisitNo

        override fun areContentsTheSame(
            oldItem: PatientDisplayWithVisitInfo, newItem: PatientDisplayWithVisitInfo
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

        @SuppressLint("ResourceAsColor")
        fun bind(
            item:PatientDisplayWithVisitInfo,
            clickListener: BenClickListener?,
        ) {
            binding.benVisitInfo = item
            binding.clickListener = clickListener
            if(item.referDate != null){
                binding.itemll.setCardBackgroundColor(R.color.referBackground)
            }
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
        private val clickedBen: (benVisitInfo: PatientDisplayWithVisitInfo) -> Unit,

    ) {
        fun onClickedBen(item: PatientDisplayWithVisitInfo) = clickedBen(
            item,
        )
    }
}