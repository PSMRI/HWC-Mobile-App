package org.piramalswasthya.cho.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import org.piramalswasthya.cho.databinding.RvItemImmVaccineCatBinding
import org.piramalswasthya.cho.model.VaccineCategoryDomain
import org.piramalswasthya.cho.model.VaccineDomain
import timber.log.Timber

class ImmunizationCategoryAdapter(private val clickListener: ImmunizationIconClickListener? = null) :
    ListAdapter<VaccineCategoryDomain, ImmunizationCategoryAdapter.VaccineCategoryViewHolder>(
        ImmunizationIconDiffCallback
    ) {
    object ImmunizationIconDiffCallback : DiffUtil.ItemCallback<VaccineCategoryDomain>() {
        override fun areItemsTheSame(
            oldItem: VaccineCategoryDomain,
            newItem: VaccineCategoryDomain
        ) =
            oldItem.category == newItem.category

        override fun areContentsTheSame(
            oldItem: VaccineCategoryDomain,
            newItem: VaccineCategoryDomain
        ) =
            (oldItem == newItem)

    }


    class VaccineCategoryViewHolder private constructor(private val binding: RvItemImmVaccineCatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): VaccineCategoryViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemImmVaccineCatBinding.inflate(layoutInflater, parent, false)
                return VaccineCategoryViewHolder(binding)
            }
        }

        fun bind(item: VaccineCategoryDomain, clickListener: ImmunizationIconClickListener?) {
            binding.category = item
            binding.clickListener = clickListener
            val layoutManager = FlexboxLayoutManager(binding.root.context)
            binding.rvVaccine.layoutManager = layoutManager
            val adapter = ImmunizationVaccineAdapter(
                clickListener
            )
            binding.rvVaccine.adapter = adapter

            Timber.d("Submitting at vaccine level 0 : ${item.vaccineStateList}")
            adapter.submitList(item.vaccineStateList)

//            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VaccineCategoryViewHolder.from(parent)

    override fun onBindViewHolder(holder: VaccineCategoryViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class ImmunizationIconClickListener(val selectedListener: (vaccineId: Int) -> Unit) {
        fun onClicked(vaccine: VaccineDomain) = selectedListener(vaccine.vaccineId)

    }
}