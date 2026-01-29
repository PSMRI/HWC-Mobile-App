package org.piramalswasthya.cho.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.databinding.RvItemChildrenUnderFiveYearsBinding
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.utils.DateTimeUtil

class ChildrenUnderFiveYearsAdapter(
    private val clickListener: ClickListener? = null
) : ListAdapter<PatientDisplay, ChildrenUnderFiveYearsAdapter.ChildrenUnderFiveViewHolder>(
    ChildrenUnderFiveDiffUtilCallBack
) {

    private object ChildrenUnderFiveDiffUtilCallBack : DiffUtil.ItemCallback<PatientDisplay>() {
        override fun areItemsTheSame(
            oldItem: PatientDisplay,
            newItem: PatientDisplay
        ) = oldItem.patient.patientID == newItem.patient.patientID

        override fun areContentsTheSame(
            oldItem: PatientDisplay,
            newItem: PatientDisplay
        ) = oldItem == newItem
    }

    class ChildrenUnderFiveViewHolder private constructor(
        private val binding: RvItemChildrenUnderFiveYearsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): ChildrenUnderFiveViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    RvItemChildrenUnderFiveYearsBinding.inflate(layoutInflater, parent, false)
                return ChildrenUnderFiveViewHolder(binding)
            }
        }

        fun bind(
            item: PatientDisplay,
            clickListener: ClickListener?
        ) {
            binding.patient = item
            binding.clickListener = clickListener

            // Child name in title
            val firstName = item.patient.firstName ?: ""
            val lastName = item.patient.lastName ?: ""
            binding.tvChildName.text = if (lastName.isNotEmpty()) {
                "$firstName $lastName"
            } else {
                firstName
            }

            // Age and DOB
            item.patient.dob?.let {
                binding.tvAge.text = DateTimeUtil.calculateAgeString(it)
                binding.tvDob.text = DateTimeUtil.formatDate(it)
            } ?: run {
                binding.tvAge.text = "NA"
                binding.tvDob.text = "NA"
            }

            // Beneficiary ID
            binding.tvBeneficiaryId.text = item.patient.beneficiaryID?.toString() ?: "NA"

            // Mobile number
            binding.tvPhoneNo.text = item.patient.phoneNo ?: "NA"

            // Gender
            binding.tvGender.text = item.gender?.genderName ?: "NA"

            // Parent names
            binding.tvMotherName.text = item.patient.parentName ?: "NA"
            binding.tvFatherName.text = item.patient.spouseName ?: "NA"

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ChildrenUnderFiveViewHolder.from(parent)

    override fun onBindViewHolder(holder: ChildrenUnderFiveViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class ClickListener(
        private val onCheckSam: ((patient: PatientDisplay) -> Unit)? = null,
        private val onOrs: ((patient: PatientDisplay) -> Unit)? = null,
        private val onIfa: ((patient: PatientDisplay) -> Unit)? = null
    ) {
        fun onCheckSam(patient: PatientDisplay) = onCheckSam?.invoke(patient)
        fun onOrs(patient: PatientDisplay) = onOrs?.invoke(patient)
        fun onIfa(patient: PatientDisplay) = onIfa?.invoke(patient)
    }
}

