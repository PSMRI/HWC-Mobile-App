package org.piramalswasthya.cho.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.databinding.RvItemInfantListBinding
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.utils.DateTimeUtil
import java.util.concurrent.TimeUnit

class InfantListAdapter(
    private val clickListener: ClickListener? = null
) : ListAdapter<PatientDisplay, InfantListAdapter.InfantViewHolder>(
    InfantDiffUtilCallBack
) {

    private object InfantDiffUtilCallBack : DiffUtil.ItemCallback<PatientDisplay>() {
        override fun areItemsTheSame(
            oldItem: PatientDisplay,
            newItem: PatientDisplay
        ) = oldItem.patient.patientID == newItem.patient.patientID

        override fun areContentsTheSame(
            oldItem: PatientDisplay,
            newItem: PatientDisplay
        ) = oldItem == newItem
    }

    class InfantViewHolder private constructor(
        private val binding: RvItemInfantListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): InfantViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemInfantListBinding.inflate(layoutInflater, parent, false)
                return InfantViewHolder(binding)
            }
        }

        fun bind(
            item: PatientDisplay,
            clickListener: ClickListener?
        ) {
            binding.patient = item
            binding.clickListener = clickListener

            // Set infant name
            val firstName = item.patient.firstName ?: ""
            val lastName = item.patient.lastName ?: ""
            binding.tvInfantName.text = if (lastName.isNotEmpty()) {
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

            // Set days old (for overdue indicator)
            item.patient.dob?.let { dob ->
                val daysOld = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - dob.time)
                binding.dueIcon.visibility = if (daysOld > 42) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }
            } ?: run {
                binding.dueIcon.visibility = android.view.View.GONE
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
        private val clickedView: ((patient: PatientDisplay) -> Unit)? = null
    ) {
        fun onClickView(item: PatientDisplay) =
            clickedView?.let { it(item) }
    }
}
