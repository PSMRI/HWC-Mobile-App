package org.piramalswasthya.cho.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.databinding.ActivityOutreachItemBinding
import org.piramalswasthya.cho.databinding.ChoListItemViewBinding
import org.piramalswasthya.cho.model.OutreachActivityNetworkModel
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.PatientVisitInfoSync
import org.piramalswasthya.cho.utils.DateTimeUtil

class ActivityItemAdapter (
    private val clickListener: ActivityClickListener,
): ListAdapter<OutreachActivityNetworkModel, ActivityItemAdapter.ActivityViewHolder>(
    BenDiffUtilCallBack
) {

    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<OutreachActivityNetworkModel>() {
        override fun areItemsTheSame(
            oldItem: OutreachActivityNetworkModel, newItem: OutreachActivityNetworkModel
        ) = oldItem.activityId == newItem.activityId

        override fun areContentsTheSame(
            oldItem: OutreachActivityNetworkModel, newItem: OutreachActivityNetworkModel
        ) = oldItem == newItem

    }

    class ActivityViewHolder private constructor(private val binding: ActivityOutreachItemBinding) :

        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ActivityViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ActivityOutreachItemBinding.inflate(layoutInflater, parent, false)
                return ActivityViewHolder(binding)
            }
        }

        fun bind(
            item:OutreachActivityNetworkModel,
            clickListener: ActivityClickListener?,
        ) {
            binding.activity = item
            binding.clickListener = clickListener
            binding.activityName.text = item.activityName
            binding.createDate.text = DateTimeUtil.formatActivityDate(item.activityDate)

//            binding.visitNumber.text = item.benVisitNo.toString() ?: ""
//            if(item.visitDate != null){
//                binding.visitDate.text = DateTimeUtil.formatDate(item.visitDate!!)
//            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = ActivityViewHolder.from(parent)

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class ActivityClickListener(
        private val clickedActivity: (activity: OutreachActivityNetworkModel) -> Unit,

    ) {
        fun onClickedActivity(item: OutreachActivityNetworkModel) = clickedActivity(
            item,
        )
    }
}