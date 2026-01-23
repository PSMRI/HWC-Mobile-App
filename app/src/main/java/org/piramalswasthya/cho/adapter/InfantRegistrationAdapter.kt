package org.piramalswasthya.cho.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.databinding.RvItemInfantRegBinding
import org.piramalswasthya.cho.model.InfantRegDomain
import org.piramalswasthya.cho.utils.DateTimeUtil

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
            binding.clickListener = clickListener

            // Set button text and color based on registration status
            val isRegistered = item.isRegistered()
            binding.btnAction.text = if (isRegistered) "VIEW" else "REGISTER"
            binding.btnAction.setBackgroundColor(
                binding.root.resources.getColor(
                    if (isRegistered) android.R.color.holo_green_dark 
                    else android.R.color.holo_red_dark,
                    null
                )
            )

            // Set age
            item.motherPatient.dob?.let {
                binding.tvAge.text = DateTimeUtil.calculateAgeString(it)
            } ?: run {
                binding.tvAge.text = "NA"
            }

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
