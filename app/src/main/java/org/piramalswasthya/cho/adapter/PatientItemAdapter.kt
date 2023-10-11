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

            binding.patientName.text = (item.patient.firstName ?: "") + " " + (item.patient.lastName ?: "")
            binding.patientAbhaNumber.text = item.patient.healthIdDetails?.healthIdNumber ?:""
            binding.patientAge.text = (item.patient.age?.toString() ?: "") + " " + item.ageUnit
            binding.patientPhoneNo.text = item.patient.phoneNo ?: ""
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

            binding.executePendingBindings()

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = BenViewHolder.from(parent)

    override fun onBindViewHolder(holder: BenViewHolder, position: Int) {
        patientId = getItem(position).patient.patientID
        holder.bind(getItem(position), clickListener, showAbha)

    }

    class BenClickListener(
        private val clickedBen: (benVisitInfo: PatientDisplayWithVisitInfo) -> Unit,
        private val clickedABHA: (benVisitInfo: PatientDisplayWithVisitInfo) -> Unit,
        private val clickedEsanjeevani: (benVisitInfo: PatientDisplayWithVisitInfo) -> Unit,
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
    }


}