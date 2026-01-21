package org.piramalswasthya.cho.ui.commons.maternal_health.infant_reg

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.databinding.ItemInfantRegBinding
import org.piramalswasthya.cho.model.InfantRegDomain

class InfantRegListAdapter(
    private val clickListener: (Int) -> Unit
) : ListAdapter<InfantRegDomain, InfantRegListAdapter.ViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<InfantRegDomain>() {
            override fun areItemsTheSame(oldItem: InfantRegDomain, newItem: InfantRegDomain): Boolean {
                return oldItem.babyIndex == newItem.babyIndex &&
                        oldItem.motherPatientID == newItem.motherPatientID
            }

            override fun areContentsTheSame(oldItem: InfantRegDomain, newItem: InfantRegDomain): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class ViewHolder private constructor(
        private val binding: ItemInfantRegBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: InfantRegDomain, clickListener: (Int) -> Unit) {
            binding.tvBabyName.text = item.customName
            binding.tvStatus.text = if (item.savedIr != null) {
                "✓ Registered"
            } else {
                "Pending Registration"
            }
            binding.root.setOnClickListener {
                clickListener(item.babyIndex)
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemInfantRegBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}
