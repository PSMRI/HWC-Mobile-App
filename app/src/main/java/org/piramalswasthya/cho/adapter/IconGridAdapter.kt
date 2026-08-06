package org.piramalswasthya.cho.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavDirections
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.databinding.RvItemIconGridBinding
import org.piramalswasthya.cho.model.Icon

class IconGridAdapter(
    private val clickListener: GridIconClickListener
) :
    ListAdapter<Icon, IconGridAdapter.IconViewHolder>(IconDiffCallback) {
    object IconDiffCallback : DiffUtil.ItemCallback<Icon>() {
        override fun areItemsTheSame(oldItem: Icon, newItem: Icon) =
            oldItem.title == newItem.title

        override fun areContentsTheSame(oldItem: Icon, newItem: Icon) =
            (oldItem == newItem)

    }


    class IconViewHolder private constructor(private val binding: RvItemIconGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): IconViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemIconGridBinding.inflate(layoutInflater, parent, false)
                return IconViewHolder(binding)
            }
        }

        fun bind(item: Icon, clickListener: GridIconClickListener) {
            binding.homeIcon = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        IconViewHolder.from(parent)

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class GridIconClickListener(val selectedListener: (dest: NavDirections) -> Unit) {
        fun onClicked(icon: Icon) = selectedListener(icon.navAction)

    }
}
