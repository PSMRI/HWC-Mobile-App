package org.piramalswasthya.cho.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.databinding.RvItemInfantRegBinding
import org.piramalswasthya.cho.model.InfantRegDomain

class InfantRegistrationAdapter(
    private val clickListener: ClickListener? = null
) : ListAdapter<InfantRegDomain, InfantRegistrationAdapter.InfantViewHolder>(
    InfantDiffUtilCallBack
) {

    private object InfantDiffUtilCallBack : DiffUtil.ItemCallback<InfantRegDomain>() {
        override fun areItemsTheSame(
            oldItem: InfantRegDomain,
            newItem: InfantRegDomain
        ) = oldItem.motherPatient.patientID == newItem.motherPatient.patientID && 
                oldItem.babyIndex == newItem.babyIndex

        override fun areContentsTheSame(
            oldItem: InfantRegDomain,
            newItem: InfantRegDomain
        ) = oldItem == newItem
    }

    class InfantViewHolder private constructor(
        private val binding: RvItemInfantRegBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): InfantViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemInfantRegBinding.inflate(layoutInflater, parent, false)
                return InfantViewHolder(binding)
            }
        }

        fun bind(
            item: InfantRegDomain,
            clickListener: ClickListener?
        ) {
            binding.item = item
            binding.clickListener = if (item.isActionEnabled()) clickListener else null

            // Set button color based on registration status (text is bound via app:infantRegActionText)
            val showRegister = item.shouldShowRegisterAction()
            binding.btnAction.setBackgroundColor(
                binding.root.resources.getColor(
                    if (showRegister) android.R.color.holo_red_dark else android.R.color.holo_green_dark,
                    null
                )
            )
            binding.btnAction.isEnabled = item.isActionEnabled()
            binding.btnAction.alpha = if (item.isActionEnabled()) 1f else 0.7f

            // Set sync icon visibility
            binding.ivSync.visibility = if (item.syncState != null && item.syncState == org.piramalswasthya.cho.database.room.SyncState.SYNCED) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        InfantViewHolder.from(parent)

    override fun onBindViewHolder(holder: InfantViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class ClickListener(
        private val clickedForm: ((patientID: String, babyIndex: Int) -> Unit)? = null
    ) {
        fun onClickForm(item: InfantRegDomain) =
            clickedForm?.let { it(item.motherPatient.patientID, item.babyIndex) }
    }
}
