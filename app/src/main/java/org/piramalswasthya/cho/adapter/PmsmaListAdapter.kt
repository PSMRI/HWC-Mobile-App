package org.piramalswasthya.cho.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.databinding.RvItemPmsmaBinding
import org.piramalswasthya.cho.model.PmsmaDomain
import org.piramalswasthya.cho.utils.DateTimeUtil

class PmsmaListAdapter(
    private val clickListener: ClickListener? = null
) : ListAdapter<PmsmaDomain, PmsmaListAdapter.PmsmaViewHolder>(
    PmsmaDiffUtilCallBack
) {

    private object PmsmaDiffUtilCallBack : DiffUtil.ItemCallback<PmsmaDomain>() {
        override fun areItemsTheSame(
            oldItem: PmsmaDomain,
            newItem: PmsmaDomain
        ) = oldItem.patient.patientID == newItem.patient.patientID

        override fun areContentsTheSame(
            oldItem: PmsmaDomain,
            newItem: PmsmaDomain
        ) = oldItem == newItem
    }

    class PmsmaViewHolder private constructor(
        private val binding: RvItemPmsmaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): PmsmaViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemPmsmaBinding.inflate(layoutInflater, parent, false)
                return PmsmaViewHolder(binding)
            }
        }

        fun bind(
            item: PmsmaDomain,
            clickListener: ClickListener?
        ) {
            binding.item = item
            binding.clickListener = clickListener

            // Set age
            item.patient.dob?.let {
                binding.tvAge.text = DateTimeUtil.calculateAgeString(it)
            } ?: run {
                binding.tvAge.text = "NA"
            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PmsmaViewHolder.from(parent)

    override fun onBindViewHolder(holder: PmsmaViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class ClickListener(
        private val clickedView: ((patientID: String) -> Unit)? = null
    ) {
        fun onClickView(item: PmsmaDomain) =
            clickedView?.let { it(item.patient.patientID) }
    }
}
