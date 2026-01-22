package org.piramalswasthya.cho.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.RvItemPatientEcWithFormBinding
import org.piramalswasthya.cho.model.PatientWithEcrDomain
import org.piramalswasthya.cho.utils.DateTimeUtil
import java.util.concurrent.TimeUnit

class ECRegistrationAdapter(
    private val clickListener: ClickListener? = null
) : ListAdapter<PatientWithEcrDomain, ECRegistrationAdapter.PatientViewHolder>(PatientDiffUtilCallBack) {

    private object PatientDiffUtilCallBack : DiffUtil.ItemCallback<PatientWithEcrDomain>() {
        override fun areItemsTheSame(
            oldItem: PatientWithEcrDomain,
            newItem: PatientWithEcrDomain
        ) = oldItem.patient.patientID == newItem.patient.patientID

        override fun areContentsTheSame(
            oldItem: PatientWithEcrDomain,
            newItem: PatientWithEcrDomain
        ) = oldItem == newItem
    }

    class PatientViewHolder private constructor(
        private val binding: RvItemPatientEcWithFormBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): PatientViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemPatientEcWithFormBinding.inflate(layoutInflater, parent, false)
                return PatientViewHolder(binding)
            }
        }

        fun bind(
            item: PatientWithEcrDomain,
            clickListener: ClickListener?
        ) {
            binding.patientWithEcr = item
            binding.clickListener = clickListener

            // Set age
            item.patient.dob?.let {
                binding.tvAge.text = DateTimeUtil.calculateAgeString(it)
            } ?: run {
                binding.tvAge.text = "NA"
            }

            // Handle ECR status display
            if (item.ecr == null) {
                // Not registered yet
                binding.llLmpDate.visibility = View.INVISIBLE
                binding.llBenStatus.visibility = View.INVISIBLE
                binding.btnFormEc1.text = binding.root.context.getString(R.string.register)
                binding.btnFormEc1.setBackgroundColor(
                    binding.root.resources.getColor(android.R.color.holo_red_dark, null)
                )
            } else {
                // Already registered
                binding.llLmpDate.visibility = View.VISIBLE
                binding.llBenStatus.visibility = View.VISIBLE
                binding.btnFormEc1.text = binding.root.context.getString(R.string.view)
                binding.btnFormEc1.setBackgroundColor(
                    binding.root.resources.getColor(android.R.color.holo_green_dark, null)
                )

                // Set LMP Date
                val lmpDate = item.ecr.lmpDate
                if (lmpDate != null && lmpDate > 0L) {
                    binding.benLmpDate.text = org.piramalswasthya.cho.utils.HelperUtil.getDateStringFromLong(lmpDate)

                    // Calculate status based on LMP date
                    val daysSinceLMP = TimeUnit.MILLISECONDS.toDays(
                        System.currentTimeMillis() - lmpDate
                    )

                    if (daysSinceLMP > 35) {
                        // Missed Period
                        binding.ivMissState.visibility = View.VISIBLE
                        binding.benStatus.text = binding.root.context.getString(R.string.missed_period)
                    } else {
                        // Under Review
                        binding.ivMissState.visibility = View.GONE
                        binding.benStatus.text = binding.root.context.getString(R.string.under_review)
                    }
                } else {
                    binding.benLmpDate.text = "NA"
                    binding.ivMissState.visibility = View.GONE
                    binding.llLmpDate.visibility = View.INVISIBLE
                    binding.llBenStatus.visibility = View.INVISIBLE
                }
            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PatientViewHolder.from(parent)

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class ClickListener(
        private val clickedForm: ((patientWithEcr: PatientWithEcrDomain) -> Unit)? = null
    ) {
        fun onClickForm(item: PatientWithEcrDomain) =
            clickedForm?.let { it(item) }
    }
}
