package org.piramalswasthya.cho.adapter

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.databinding.PatientListItemViewBinding
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.network.ESanjeevaniApiService
import org.piramalswasthya.cho.ui.home.HomeViewModel
import org.piramalswasthya.cho.utils.Constants.pattern
import org.piramalswasthya.cho.utils.DateTimeUtil

class PatientItemAdapter(
    private val apiService: ESanjeevaniApiService,
    var context: Context,
    private val clickListener: BenClickListener,
    private val showAbha: Boolean = false,
) : ListAdapter<PatientDisplayWithVisitInfo, PatientItemAdapter.BenViewHolder>(BenDiffUtilCallBack) {
    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<PatientDisplayWithVisitInfo>() {
        override fun areItemsTheSame(
            oldItem: PatientDisplayWithVisitInfo, newItem: PatientDisplayWithVisitInfo
        ) = oldItem.patient.beneficiaryID == newItem.patient.beneficiaryID

        override fun areContentsTheSame(
            oldItem: PatientDisplayWithVisitInfo, newItem: PatientDisplayWithVisitInfo
        ) = oldItem == newItem

    }

    class BenViewHolder private constructor(private val binding: PatientListItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): BenViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = PatientListItemViewBinding.inflate(layoutInflater, parent, false)
                return BenViewHolder(binding)
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(
            item: PatientDisplayWithVisitInfo,
            clickListener: BenClickListener?,
            showAbha: Boolean,
            mContext: Context
        ) {
            var gender = ""
            binding.benVisitInfo = item
            binding.clickListener = clickListener
            binding.showAbha = showAbha
            binding.hasAbha = !item.patient.healthIdDetails?.healthIdNumber.isNullOrEmpty()

            val firstName = item.patient.firstName ?: ""
            val lastName = item.patient.lastName ?: ""
            val capitalizedFirstName = firstName.split(" ")
                .joinToString(" ") { it -> it.replaceFirstChar { it.uppercaseChar() } }
            val capitalizedLastName = lastName.split(" ")
                .joinToString(" ") { it -> it.replaceFirstChar { it.uppercaseChar() } }

            val fullName = "$capitalizedFirstName $capitalizedLastName"
            binding.patientName.text = fullName
            binding.patientAbhaNumber.text = item.patient.healthIdDetails?.healthIdNumber ?: ""
            if (item.patient.dob != null) {
                binding.patientAge.text = DateTimeUtil.calculateAgeString(item.patient.dob!!)
            }

            val visitDateText = item.visitDate?.let { DateTimeUtil.formatDate(it) }
            if (visitDateText.isNullOrBlank()) {
                binding.visitDate.text = "NA"
                binding.referDate.text = "NA"
            } else {
                binding.visitDate.text = visitDateText
                binding.referDate.text = visitDateText
            }
            binding.patientPhoneNo.text = item.patient.phoneNo ?: "NA"
            if (item.villageName.isNullOrBlank()) {
                binding.village.text = "NA"
            } else binding.village.text = item.villageName

            binding.patientGender.text = item.genderName
            gender = item.genderName.toString()

            if (item.patient.dob != null) {
                val type = DateTimeUtil.getPatientTypeByAge(item.patient.dob!!)
                if (type == "new_born_baby") {
                    binding.ivPatientIcon.setImageResource(R.drawable.ic_new_born_baby)
                } else if (type == "infant") {
                    binding.ivPatientIcon.setImageResource(R.drawable.ic_infant)
                } else if (type == "child") {
                    //male female check
                    if (gender == "Male") {
                        binding.ivPatientIcon.setImageResource(R.drawable.ic_boy)
                    } else if (gender == "Female") {
                        binding.ivPatientIcon.setImageResource(R.drawable.ic_girl)
                    } else {

                    }

                } else if (type == "adolescence") {
                    if (gender == "Male") {
                        binding.ivPatientIcon.setImageResource(R.drawable.ic_boy)
                    } else if (gender == "Female") {
                        binding.ivPatientIcon.setImageResource(R.drawable.ic_girl)
                    } else {

                    }

                } else if (type == "adult") {
                    if (gender == "Male") {
                        binding.ivPatientIcon.setImageResource(R.drawable.ic_male)
                    } else if (gender == "Female") {
                        binding.ivPatientIcon.setImageResource(R.drawable.ic_female)
                    } else {
                        binding.ivPatientIcon.setImageResource(R.drawable.ic_unisex)
                    }
                }
            }

            if (item.patient.syncState == SyncState.SYNCED) {
                binding.ivSyncState.setColorFilter(
                    ContextCompat.getColor(mContext, R.color.green),
                    android.graphics.PorterDuff.Mode.MULTIPLY
                )
            } else if (item.patient.syncState == SyncState.UNSYNCED) {
                binding.ivSyncState.setColorFilter(
                    ContextCompat.getColor(
                        mContext,
                        R.color.button_danger
                    ), android.graphics.PorterDuff.Mode.MULTIPLY
                )

            } else if (item.patient.syncState == SyncState.SYNCING) {
                binding.ivSyncState.setColorFilter(
                    ContextCompat.getColor(
                        mContext,
                        R.color.background_gradient_end
                    ), android.graphics.PorterDuff.Mode.MULTIPLY
                )

            }


            if (item.patient.syncState == SyncState.SYNCED) {
                //  binding.ivSyncState.visibility = View.VISIBLE
                binding.patientBenId.text = item.patient.beneficiaryID.toString()
                binding.llBenId.visibility = View.VISIBLE
                binding.btnAbha.isEnabled = true
            } else {
                binding.btnAbha.isEnabled = false
                binding.llBenId.visibility = View.GONE
                //   binding.ivSyncState.visibility = View.GONE
            }
            /*   Commented as prescription button should not display to user

            if(item.doctorFlag == 9){
                   binding.prescriptionDownloadBtn.visibility = View.VISIBLE
               }else{
                   binding.prescriptionDownloadBtn.visibility = View.GONE
               }*/

            if (item.referTo != null) {
                binding.referToLl.visibility = View.VISIBLE
                binding.referDateLl.visibility = View.VISIBLE
                binding.referFromLl.visibility = View.VISIBLE
                binding.referTo.text = item.referTo
                binding.referFrom.text = HomeViewModel.masterVillageName
            }

          /*  if (item.referDate != null) {
                binding.referDateLl.visibility = View.VISIBLE
                binding.referDate.text = item.referDate
            }

            if (item.referralReason != null) {
                val arr = item.referralReason.split(pattern)
                if (arr.size > 1) {
                    binding.referFromLl.visibility = View.VISIBLE
                    binding.referFrom.text = arr[1]
                }
            }*/

            binding.executePendingBindings()

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = BenViewHolder.from(parent)

    override fun onBindViewHolder(holder: BenViewHolder, position: Int) {
//        patientId = getItem(position).patient.patientID
        holder.bind(getItem(position), clickListener, showAbha, holder.itemView.context)

    }

    class BenClickListener(
        private val clickedBen: (benVisitInfo: PatientDisplayWithVisitInfo) -> Unit,
        private val clickedABHA: (benVisitInfo: PatientDisplayWithVisitInfo) -> Unit,
        private val clickedEsanjeevani: (benVisitInfo: PatientDisplayWithVisitInfo) -> Unit,
        private val clickedDownloadPrescription: (benVisitInfo: PatientDisplayWithVisitInfo) -> Unit,
        private val syncIconButton: (benVisitInfo: PatientDisplayWithVisitInfo) -> Unit,
    ) {
        fun onClickedBen(item: PatientDisplayWithVisitInfo) = clickedBen(
            item,
        )

        fun onClickABHA(item: PatientDisplayWithVisitInfo) {
            Log.d("ABHA Item Click", "ABHA item clicked")
            clickedABHA(item)
        }

        fun onClickEsanjeevani(item: PatientDisplayWithVisitInfo) {
            clickedEsanjeevani(item)
        }

        fun onClickPrescription(item: PatientDisplayWithVisitInfo) {
            clickedDownloadPrescription(item)
        }

        fun onClickSync(item: PatientDisplayWithVisitInfo) {
            syncIconButton(item)
        }
    }


}