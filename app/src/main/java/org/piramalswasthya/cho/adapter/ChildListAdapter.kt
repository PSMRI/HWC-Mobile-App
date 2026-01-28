package org.piramalswasthya.cho.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.databinding.RvItemChildListBinding
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.utils.DateTimeUtil

class ChildListAdapter(
    private val clickListener: ClickListener? = null
) : ListAdapter<PatientDisplay, ChildListAdapter.ChildViewHolder>(
    ChildDiffUtilCallBack
) {

    private object ChildDiffUtilCallBack : DiffUtil.ItemCallback<PatientDisplay>() {
        override fun areItemsTheSame(
            oldItem: PatientDisplay,
            newItem: PatientDisplay
        ) = oldItem.patient.patientID == newItem.patient.patientID

        override fun areContentsTheSame(
            oldItem: PatientDisplay,
            newItem: PatientDisplay
        ) = oldItem == newItem
    }

    class ChildViewHolder private constructor(
        private val binding: RvItemChildListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): ChildViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemChildListBinding.inflate(layoutInflater, parent, false)
                return ChildViewHolder(binding)
            }
        }

        fun bind(
            item: PatientDisplay,
            clickListener: ClickListener?
        ) {
            binding.patient = item
            binding.clickListener = clickListener

            // Set child name
            val firstName = item.patient.firstName ?: ""
            val lastName = item.patient.lastName ?: ""
            binding.tvChildName.text = if (lastName.isNotEmpty()) {
                "$firstName $lastName"
            } else {
                firstName
            }

            // Set age
            item.patient.dob?.let {
                binding.tvAge.text = DateTimeUtil.calculateAgeString(it)
            } ?: run {
                binding.tvAge.text = "NA"
            }

            // Set DOB
            item.patient.dob?.let {
                binding.tvDob.text = DateTimeUtil.formattedDate(it)
            } ?: run {
                binding.tvDob.text = "NA"
            }

            // Set beneficiary ID
            binding.tvBeneficiaryId.text = item.patient.beneficiaryID?.toString() ?: "NA"

            // Set phone number
            binding.tvPhoneNo.text = item.patient.phoneNo ?: "NA"

            // Set gender
            binding.tvGender.text = item.gender?.genderName ?: "NA"

            // Set parent name
            binding.tvParentName.text = item.patient.parentName ?: "NA"

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ChildViewHolder.from(parent)

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class ClickListener(
        private val clickedView: ((patient: PatientDisplay) -> Unit)? = null
    ) {
        fun onClickView(item: PatientDisplay) =
            clickedView?.let { it(item) }
    }
}
