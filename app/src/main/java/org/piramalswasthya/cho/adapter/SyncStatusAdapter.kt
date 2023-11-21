package org.piramalswasthya.cho.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.databinding.RvItemSyncStatusBinding
import org.piramalswasthya.cho.model.SyncStatusCache

class SyncStatusAdapter :
    ListAdapter<SyncStatusCache, SyncStatusAdapter.SyncStatusViewHolder>(
        SyncItemDiffCallback
    ) {
    object SyncItemDiffCallback : DiffUtil.ItemCallback<SyncStatusCache>() {
        override fun areItemsTheSame(
            oldItem: SyncStatusCache,
            newItem: SyncStatusCache
        ) =
            oldItem.name == newItem.name

        override fun areContentsTheSame(
            oldItem: SyncStatusCache,
            newItem: SyncStatusCache
        ) =
            (oldItem == newItem)

    }


    class SyncStatusViewHolder private constructor(private val binding: RvItemSyncStatusBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): SyncStatusViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemSyncStatusBinding.inflate(layoutInflater, parent, false)
                return SyncStatusViewHolder(binding)
            }
        }

        fun bind(item: SyncStatusCache) {
            binding.sync = item
            binding.executePendingBindings()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        SyncStatusViewHolder.from(parent)

    override fun onBindViewHolder(holder: SyncStatusViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}