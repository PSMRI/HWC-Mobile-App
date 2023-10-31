package org.piramalswasthya.cho.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.databinding.RvItemImmVaccineBinding
import org.piramalswasthya.cho.model.VaccineDomain

class ImmunizationVaccineAdapter(private val clickListener: ImmunizationCategoryAdapter.ImmunizationIconClickListener? = null) :
    ListAdapter<VaccineDomain, ImmunizationVaccineAdapter.IconViewHolder>(
        ImmunizationIconDiffCallback
    ) {
    object ImmunizationIconDiffCallback : DiffUtil.ItemCallback<VaccineDomain>() {
        override fun areItemsTheSame(oldItem: VaccineDomain, newItem: VaccineDomain) =
            oldItem.vaccineId == newItem.vaccineId

        override fun areContentsTheSame(oldItem: VaccineDomain, newItem: VaccineDomain) =
            (oldItem == newItem)

    }


    class IconViewHolder private constructor(private val binding: RvItemImmVaccineBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): IconViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemImmVaccineBinding.inflate(layoutInflater, parent, false)
                return IconViewHolder(binding)
            }
        }

        fun bind(
            item: VaccineDomain,
            clickListener: ImmunizationCategoryAdapter.ImmunizationIconClickListener?
        ) {
            binding.vaccine = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        IconViewHolder.from(parent)

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }
}