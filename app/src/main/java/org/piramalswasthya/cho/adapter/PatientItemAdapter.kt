package org.piramalswasthya.cho.adapter

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.databinding.PatientListItemViewBinding
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.network.ESanjeevaniApiService
import org.piramalswasthya.cho.ui.home.HomeViewModel
import org.piramalswasthya.cho.utils.DateTimeUtil
import org.piramalswasthya.cho.utils.ImgUtils

class PatientItemAdapter(
    private val apiService: ESanjeevaniApiService,
    var context: Context,
    private val clickListener: BenClickListener,
    private val showAbha: Boolean = false,
    private val showEditButton: Boolean = false,
) : ListAdapter<PatientDisplayWithVisitInfo, PatientItemAdapter.BenViewHolder>(BenDiffUtilCallBack) {

    companion object {
        // Bitmap cache for beneficiary photos to avoid repeated Base64 decode on scroll/rebind.
        private val benImageBitmapCache = object : LruCache<String, Bitmap>(6 * 1024) {
            override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
        }
    }

    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<PatientDisplayWithVisitInfo>() {
        private fun stableKey(item: PatientDisplayWithVisitInfo): String {
            return item.patient.beneficiaryID?.toString()
                ?: "${item.patient.patientID}_${item.benVisitNo ?: -1}"
        }

        override fun areItemsTheSame(
            oldItem: PatientDisplayWithVisitInfo, newItem: PatientDisplayWithVisitInfo
        ) = stableKey(oldItem) == stableKey(newItem)

        override fun areContentsTheSame(
            oldItem: PatientDisplayWithVisitInfo, newItem: PatientDisplayWithVisitInfo
        ): Boolean {
            val oldPatient = oldItem.patient
            val newPatient = newItem.patient

            return oldPatient.firstName == newPatient.firstName &&
                    oldPatient.lastName == newPatient.lastName &&
                    oldPatient.dob == newPatient.dob &&
                    oldPatient.phoneNo == newPatient.phoneNo &&
                    oldPatient.beneficiaryID == newPatient.beneficiaryID &&
                    oldPatient.syncState == newPatient.syncState &&
                    oldPatient.healthIdDetails?.healthIdNumber == newPatient.healthIdDetails?.healthIdNumber &&
                    oldPatient.benImage == newPatient.benImage &&
                    oldItem.genderName == newItem.genderName &&
                    oldItem.villageName == newItem.villageName &&
                    oldItem.visitDate == newItem.visitDate &&
                    oldItem.referDate == newItem.referDate &&
                    oldItem.referTo == newItem.referTo &&
                    oldItem.nurseFlag == newItem.nurseFlag &&
                    oldItem.doctorFlag == newItem.doctorFlag &&
                    oldItem.labtechFlag == newItem.labtechFlag &&
                    oldItem.pharmacist_flag == newItem.pharmacist_flag
        }

    }

    init {
        setHasStableIds(true)
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
            showEditButton: Boolean,
            mContext: Context
        ) {
            var gender = ""
            binding.benVisitInfo = item
            binding.clickListener = clickListener
            binding.showAbha = showAbha
            binding.showEditButton = showEditButton
            binding.hasAbha = !item.patient.healthIdDetails?.healthIdNumber.isNullOrEmpty()

            val firstName = item.patient.firstName ?: ""
            val lastName = item.patient.lastName ?: ""
            val capitalizedFirstName = firstName.split(" ")
                .joinToString(" ") { token -> token.replaceFirstChar { it.uppercaseChar() } }
            val capitalizedLastName = lastName.split(" ")
                .joinToString(" ") { token -> token.replaceFirstChar { it.uppercaseChar() } }

            val fullName = "$capitalizedFirstName $capitalizedLastName"
            binding.patientName.text = fullName
            binding.patientAbhaNumber.text = item.patient.healthIdDetails?.healthIdNumber ?: ""
            if (item.patient.dob != null) {
                binding.patientAge.text = DateTimeUtil.calculateAgeString(item.patient.dob!!)
            }

            val visitDateText = item.visitDate?.let { DateTimeUtil.formatDate(it) }
            if (visitDateText.isNullOrBlank()) {
                binding.visitDate.text = ""
                binding.referDate.text = ""
            } else {
                binding.visitDate.text = visitDateText
                binding.referDate.text = visitDateText
            }
            binding.patientPhoneNo.text = item.patient.phoneNo ?: ""
            if (item.villageName.isNullOrBlank()) {
                binding.village.text = ""
            } else binding.village.text = item.villageName

            binding.patientGender.text = item.genderName
            gender = item.genderName.toString()

            // Try to load the beneficiary photo first; fall back to age/gender icon if none
            val benImage = item.patient.benImage
            if (!benImage.isNullOrEmpty()) {
                val cacheKey = "${item.patient.patientID}:${benImage.hashCode()}"
                val cachedBitmap = PatientItemAdapter.benImageBitmapCache.get(cacheKey)
                if (cachedBitmap != null) {
                    Glide.with(mContext)
                        .load(cachedBitmap)
                        .placeholder(R.drawable.ic_person)
                        .circleCrop()
                        .into(binding.ivPatientIcon)
                } else {
                    val base64Data = if (benImage.contains(",")) benImage.substringAfter(",") else benImage
                    val bitmap = ImgUtils.decodeBase64ToBitmap(base64Data)
                    if (bitmap != null) {
                        PatientItemAdapter.benImageBitmapCache.put(cacheKey, bitmap)
                        Glide.with(mContext)
                            .load(bitmap)
                            .placeholder(R.drawable.ic_person)
                            .circleCrop()
                            .into(binding.ivPatientIcon)
                    } else {
                        setDefaultPatientIcon(item.patient.dob, gender, binding)
                    }
                }
            } else {
                setDefaultPatientIcon(item.patient.dob, gender, binding)
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


            // Show BeneficiaryID if it exists, regardless of sync state
            if (item.patient.beneficiaryID != null) {
                binding.patientBenId.text = item.patient.beneficiaryID.toString()
                binding.llBenId.visibility = View.VISIBLE
            } else {
                binding.llBenId.visibility = View.GONE
            }

            if (item.patient.syncState == SyncState.SYNCED ) {
                //  binding.ivSyncState.visibility = View.VISIBLE
                binding.btnAbha.isEnabled = true
            } else {
                binding.btnAbha.isEnabled = false
                //   binding.ivSyncState.visibility = View.GONE
            }
            // Show prescription download when doctor has submitted (medicine prescribed) or pharmacist has submitted
            // doctorFlag 2 = submitted with test pending, 3 = post-lab, 9 = submitted without test
            val showPrescriptionDownload = item.pharmacist_flag == 9 ||
                    item.doctorFlag == 2 || item.doctorFlag == 3 || item.doctorFlag == 9
            binding.prescriptionDownloadBtn.visibility = if (showPrescriptionDownload) View.VISIBLE else View.GONE

            if (item.referTo != null) {
                binding.referToLl.visibility = View.VISIBLE
                binding.referDateLl.visibility = View.VISIBLE
                binding.referFromLl.visibility = View.VISIBLE
                binding.referTo.text = item.referTo
                binding.referFrom.text = HomeViewModel.masterVillageName
            }


            binding.executePendingBindings()

        }

        private fun setDefaultPatientIcon(dob: java.util.Date?, gender: String, binding: PatientListItemViewBinding) {
            if (dob != null) {
                val type = DateTimeUtil.getPatientTypeByAge(dob)
                when (type) {
                    "new_born_baby" -> binding.ivPatientIcon.setImageResource(R.drawable.ic_new_born_baby)
                    "infant" -> binding.ivPatientIcon.setImageResource(R.drawable.ic_infant)
                    "child", "adolescence" -> {
                        if (gender == "Male") binding.ivPatientIcon.setImageResource(R.drawable.ic_boy)
                        else if (gender == "Female") binding.ivPatientIcon.setImageResource(R.drawable.ic_girl)
                    }
                    "adult" -> {
                        if (gender == "Male") binding.ivPatientIcon.setImageResource(R.drawable.ic_male)
                        else if (gender == "Female") binding.ivPatientIcon.setImageResource(R.drawable.ic_female)
                        else binding.ivPatientIcon.setImageResource(R.drawable.ic_unisex)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = BenViewHolder.from(parent)

    override fun getItemId(position: Int): Long {
        val item = getItem(position)
        val uniqueKey = "${item.patient.patientID}:${item.benVisitNo ?: -1}"
        return uniqueKey.hashCode().toLong()
    }

    override fun onBindViewHolder(holder: BenViewHolder, position: Int) {
//        patientId = getItem(position).patient.patientID
        holder.bind(getItem(position), clickListener, showAbha, showEditButton, holder.itemView.context)

    }

    class BenClickListener(
        private val clickedBen: (benVisitInfo: PatientDisplayWithVisitInfo) -> Unit,
        private val clickedABHA: (benVisitInfo: PatientDisplayWithVisitInfo) -> Unit,
        private val clickedEsanjeevani: (benVisitInfo: PatientDisplayWithVisitInfo) -> Unit,
        private val clickedDownloadPrescription: (benVisitInfo: PatientDisplayWithVisitInfo) -> Unit,
        private val syncIconButton: (benVisitInfo: PatientDisplayWithVisitInfo) -> Unit,
        private val clickedEdit: (benVisitInfo: PatientDisplayWithVisitInfo) -> Unit
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
        fun onClickEdit(item: PatientDisplayWithVisitInfo) {
            clickedEdit(item)
        }
    }


}