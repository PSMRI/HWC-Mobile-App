package org.piramalswasthya.cho.adapter

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.databinding.PatientListItemViewBinding
import org.piramalswasthya.cho.databinding.PatientListViewBinding
import org.piramalswasthya.cho.model.NetworkBody
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.network.ESanjeevaniApiService
import org.piramalswasthya.cho.network.interceptors.TokenESanjeevaniInterceptor
import org.piramalswasthya.cho.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.cho.ui.web_view_activity.WebViewActivity
import org.piramalswasthya.cho.utils.DateTimeUtil
import timber.log.Timber
import java.security.MessageDigest


class PatientItemAdapter(
    private val apiService: ESanjeevaniApiService,
    private val context: Context,
    private val clickListener: BenClickListener,
    private val showAbha: Boolean = false,
) : ListAdapter<PatientDisplayWithVisitInfo,PatientItemAdapter.BenViewHolder>(BenDiffUtilCallBack) {

    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<PatientDisplayWithVisitInfo>() {
        override fun areItemsTheSame(
            oldItem: PatientDisplayWithVisitInfo, newItem: PatientDisplayWithVisitInfo
        ) = oldItem.patient.beneficiaryID == newItem.patient.beneficiaryID

        override fun areContentsTheSame(
            oldItem: PatientDisplayWithVisitInfo, newItem: PatientDisplayWithVisitInfo
        ) = oldItem == newItem

    }

    private var usernameEs: String = ""
    private var passwordEs: String = ""
    private var errorEs: String = ""
    private var patientId: String = ""
    private var network: Boolean = false

    class BenViewHolder private constructor(private val binding: PatientListItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): BenViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = PatientListItemViewBinding.inflate(layoutInflater, parent, false)
                return BenViewHolder(binding)
            }
        }

        fun bind(
            item:PatientDisplayWithVisitInfo,
            clickListener: BenClickListener?,
            showAbha: Boolean,
        ) {
            binding.benVisitInfo = item
            binding.clickListener = clickListener
            binding.showAbha = showAbha
            binding.hasAbha = !item.patient.healthIdDetails?.healthIdNumber.isNullOrEmpty()

            val firstName = item.patient.firstName ?: ""
            val lastName = item.patient.lastName ?: ""
            val capitalizedFirstName = firstName.split(" ").joinToString(" ") { it -> it.replaceFirstChar { it.uppercaseChar() } }
            val capitalizedLastName = lastName.split(" ").joinToString(" ") { it -> it.replaceFirstChar { it.uppercaseChar() } }

            val fullName = "$capitalizedFirstName $capitalizedLastName"
            binding.patientName.text = fullName
            binding.patientAbhaNumber.text = item.patient.healthIdDetails?.healthIdNumber ?:""
            if(item.patient.dob != null){
                binding.patientAge.text = DateTimeUtil.calculateAgeString(item.patient.dob!!)
            }
//            if(!item.patient.age?.toString().isNullOrEmpty() && item.patient.age!! <= 1){
//                val unit = item.ageUnit?.dropLast(1)
//                binding.patientAge.text = (item.patient.age?.toString() ?: "") + " " + unit
//            }else{
//                binding.patientAge.text = (item.patient.age?.toString() ?: "") + " " + item.ageUnit
//            }
            if(item.visitDate != null){
                binding.visitDate.text = DateTimeUtil.formatDate(item.visitDate)
            }
            binding.patientPhoneNo.text = item.patient.phoneNo ?: ""
            binding.village.text = item.villageName ?: ""
            binding.patientGender.text = item.genderName
            if(item.patient.syncState == SyncState.SYNCED){
                binding.ivSyncState.visibility = View.VISIBLE
                binding.patientBenId.text = item.patient.beneficiaryID.toString()
                binding.llBenId.visibility = View.VISIBLE
                binding.btnAbha.isEnabled = true
            } else {
                binding.btnAbha.isEnabled = false
                binding.llBenId.visibility = View.GONE
                binding.ivSyncState.visibility = View.GONE
            }
            if(item.doctorFlag == 9){
                binding.prescriptionDownloadBtn.visibility = View.VISIBLE
            }else{
                binding.prescriptionDownloadBtn.visibility = View.GONE
            }

            if(item.referTo != null){
                binding.referToLl.visibility = View.VISIBLE
                binding.referTo.text = item.referTo
            }

            if(item.referDate != null){
                binding.referDateLl.visibility = View.VISIBLE
                binding.referDate.text = item.referDate
            }

            binding.executePendingBindings()

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = BenViewHolder.from(parent)

    override fun onBindViewHolder(holder: BenViewHolder, position: Int) {
//        patientId = getItem(position).patient.patientID
        holder.bind(getItem(position), clickListener, showAbha)

    }

    class BenClickListener(
        private val clickedBen: (benVisitInfo: PatientDisplayWithVisitInfo) -> Unit,
        private val clickedABHA: (benVisitInfo: PatientDisplayWithVisitInfo) -> Unit,
        private val clickedEsanjeevani: (benVisitInfo: PatientDisplayWithVisitInfo) -> Unit,
        private val clickedDownloadPrescription:(benVisitInfo: PatientDisplayWithVisitInfo) -> Unit,
        private val syncIconButton:(benVisitInfo: PatientDisplayWithVisitInfo) -> Unit,
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