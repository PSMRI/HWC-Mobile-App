package org.piramalswasthya.cho.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.databinding.RvItemChildRegBinding
import org.piramalswasthya.cho.model.ChildRegDomain
import org.piramalswasthya.cho.utils.DateTimeUtil

class ChildRegistrationAdapter(
    private val clickListener: ClickListener? = null
) : ListAdapter<ChildRegDomain, ChildRegistrationAdapter.ChildViewHolder>(
    ChildDiffUtilCallBack
) {

    private object ChildDiffUtilCallBack : DiffUtil.ItemCallback<ChildRegDomain>() {
        override fun areItemsTheSame(
            oldItem: ChildRegDomain,
            newItem: ChildRegDomain
        ) = oldItem.motherPatient.patientID == newItem.motherPatient.patientID && 
                oldItem.infant.babyIndex == newItem.infant.babyIndex

        override fun areContentsTheSame(
            oldItem: ChildRegDomain,
            newItem: ChildRegDomain
        ) = oldItem == newItem
    }

    class ChildViewHolder private constructor(
        private val binding: RvItemChildRegBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): ChildViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemChildRegBinding.inflate(layoutInflater, parent, false)
                return ChildViewHolder(binding)
            }
        }

        fun bind(
            item: ChildRegDomain,
            clickListener: ClickListener?
        ) {
            binding.item = item
            binding.clickListener = clickListener

            // Set button text and color based on child registration status
            val isChildRegistered = item.isChildRegistered()
            binding.btnAction.text = if (isChildRegistered) "VIEW" else "REGISTER"
            binding.btnAction.setBackgroundColor(
                binding.root.resources.getColor(
                    if (isChildRegistered) android.R.color.holo_green_dark 
                    else android.R.color.holo_red_dark,
                    null
                )
            )

            // Set age
            item.childPatient?.dob?.let {
                binding.tvAge.text = DateTimeUtil.calculateAgeString(it)
            } ?: run {
                binding.tvAge.text = "NA"
            }

            // Set sync icon visibility
            binding.ivSync.visibility = if (item.infant.syncState == org.piramalswasthya.cho.database.room.SyncState.SYNCED) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ChildViewHolder.from(parent)

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class ClickListener(
        private val clickedForm: ((patientID: String, babyIndex: Int, childPatientID: String?) -> Unit)? = null
    ) {
        fun onClickForm(item: ChildRegDomain) =
            clickedForm?.let { 
                it(
                    item.motherPatient.patientID, 
                    item.infant.babyIndex,
                    item.childPatient?.patientID
                ) 
            }
    }
}
