package org.piramalswasthya.cho.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.databinding.RvItemAbortionBinding
import org.piramalswasthya.cho.model.AbortionDomain
import org.piramalswasthya.cho.utils.DateTimeUtil

class AbortionListAdapter(
    private val clickListener: ClickListener? = null
) : ListAdapter<AbortionDomain, AbortionListAdapter.AbortionViewHolder>(
    AbortionDiffUtilCallBack
) {

    private object AbortionDiffUtilCallBack : DiffUtil.ItemCallback<AbortionDomain>() {
        override fun areItemsTheSame(
            oldItem: AbortionDomain,
            newItem: AbortionDomain
        ) = oldItem.patient.patientID == newItem.patient.patientID

        override fun areContentsTheSame(
            oldItem: AbortionDomain,
            newItem: AbortionDomain
        ) = oldItem == newItem
    }

    class AbortionViewHolder private constructor(
        private val binding: RvItemAbortionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): AbortionViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemAbortionBinding.inflate(layoutInflater, parent, false)
                return AbortionViewHolder(binding)
            }
        }

        fun bind(
            item: AbortionDomain,
            clickListener: ClickListener?
        ) {
            binding.item = item
            binding.clickListener = clickListener

            // Set button text and color based on abortion form status
            val isFormFilled = item.isAbortionFormFilled
            binding.btnAction.text = if (isFormFilled) "VIEW" else "ADD"
            binding.btnAction.setBackgroundColor(
                binding.root.resources.getColor(
                    if (isFormFilled) android.R.color.holo_green_dark 
                    else android.R.color.holo_red_dark,
                    null
                )
            )

            // Set age
            item.patient.dob?.let {
                binding.tvAge.text = DateTimeUtil.calculateAgeString(it)
            } ?: run {
                binding.tvAge.text = "NA"
            }

            // Set sync icon visibility
            binding.ivSync.visibility = if (item.abortionRecord?.syncState == org.piramalswasthya.cho.database.room.SyncState.SYNCED) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        AbortionViewHolder.from(parent)

    override fun onBindViewHolder(holder: AbortionViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class ClickListener(
        private val clickedView: ((patientID: String) -> Unit)? = null,
        private val clickedAdd: ((patientID: String) -> Unit)? = null
    ) {
        fun onClickView(item: AbortionDomain) =
            clickedView?.let { it(item.patient.patientID) }

        fun onClickAdd(item: AbortionDomain) =
            clickedAdd?.let { it(item.patient.patientID) }
    }
}
