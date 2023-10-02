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
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.network.ESanjeevaniApiService
import org.piramalswasthya.cho.network.interceptors.TokenESanjeevaniInterceptor
import org.piramalswasthya.cho.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.cho.ui.web_view_activity.WebViewActivity
import timber.log.Timber
import java.security.MessageDigest


class PatientItemAdapter(
    private val apiService: ESanjeevaniApiService,
    private val context: Context,
//    private val onItemClicked: (PatientDisplay) -> Unit,
    private val clickListener: BenClickListener,
    private val showAbha: Boolean = false,
) : ListAdapter<PatientDisplay,PatientItemAdapter.BenViewHolder>(BenDiffUtilCallBack) {

    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<PatientDisplay>() {
        override fun areItemsTheSame(
            oldItem: PatientDisplay, newItem: PatientDisplay
        ) = oldItem.patient.beneficiaryID == newItem.patient.beneficiaryID

        override fun areContentsTheSame(
            oldItem: PatientDisplay, newItem: PatientDisplay
        ) = oldItem == newItem

    }
    private var usernameEs : String = ""
    private var passwordEs : String = ""
    private var errorEs : String = ""
    private var patientId : String = ""
    private var network : Boolean = false

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
            item:PatientDisplay,
            clickListener: BenClickListener?,
            showAbha: Boolean,
        ) {
            binding.ben = item.patient
            binding.clickListener = clickListener
            binding.showAbha = showAbha
            binding.hasAbha = !item.patient.healthIdDetails?.healthIdNumber.isNullOrEmpty()

            binding.patientName.text = (item.patient.firstName ?: "") + " " + (item.patient.lastName ?: "")
            binding.patientAbhaNumber.text = item.patient.healthIdDetails?.healthIdNumber ?:""
            binding.patientAge.text = (item.patient.age?.toString() ?: "") + " " + item.ageUnit.name
            binding.patientPhoneNo.text = item.patient.phoneNo ?: ""
            binding.patientGender.text = item.gender.genderName
            if(item.patient.syncState == SyncState.SYNCED){
                binding.ivSyncState.visibility = View.VISIBLE
                binding.patientBenId.text = item.patient.beneficiaryID.toString()
                binding.llBenId.visibility = View.VISIBLE
                binding.btnAbha.isEnabled = true
            }else {
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
//        holder.itemView.setOnClickListener{
//            if (position != RecyclerView.NO_POSITION) {
//                onItemClicked(getItem(position))
//            }
//        }
        holder.itemView.findViewById<MaterialButton>(R.id.btn_eSanjeevani).setOnClickListener {
            network = isInternetAvailable(context)
            callLoginDialog()
        }
    }

    class BenClickListener(
        private val clickedBen: (patientID: String) -> Unit,
        private val clickedABHA: (benId: Long?) -> Unit,
    ) {
        fun onClickedBen(item: Patient) = clickedBen(
            item.patientID,
    )
        fun onClickABHA(item: Patient) {
            Log.d("ABHA Item Click", "ABHA item clicked")
            clickedABHA(item.beneficiaryID)
        }
    }


    // E-SANJEEVANI
    private fun encryptSHA512(input: String): String {
        val digest = MessageDigest.getInstance("SHA-512")
        val hashBytes = digest.digest(input.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            return networkCapabilities != null &&
                    (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }
     private fun callLoginDialog() {

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_esanjeevani_login, null)
            val dialog = MaterialAlertDialogBuilder(context)
            .setTitle("eSanjeevani Login")
            .setView(dialogView)
            .setNegativeButton("Cancel") { dialog, _ ->
                // Handle cancel button click
                dialog.dismiss()
            }
            .create()
            dialog.show()
        if (network) {
            // Internet is available
            dialogView.findViewById<ConstraintLayout>(R.id.cl_error_es).visibility = View.GONE
            dialogView.findViewById<LinearLayout>(R.id.ll_login_es).visibility = View.VISIBLE
        }
        else {
            dialogView.findViewById<LinearLayout>(R.id.ll_login_es).visibility = View.GONE
            dialogView.findViewById<ConstraintLayout>(R.id.cl_error_es).visibility = View.VISIBLE
        }

        val loginBtn = dialogView.findViewById<MaterialButton>(R.id.loginButton)
        loginBtn.setOnClickListener {
            usernameEs = dialogView.findViewById<TextInputEditText>(R.id.et_username_es).text.toString().trim()
            passwordEs = dialogView.findViewById<TextInputEditText>(R.id.et_password_es).text.toString().trim()
                CoroutineScope(Dispatchers.Main).launch {
                try {
                    var passWord = encryptSHA512(encryptSHA512(passwordEs) + encryptSHA512("token"))

                    var networkBody = NetworkBody(
                        usernameEs,
                        passWord,
                        "token",
                        "11001"
                    )
                    val errorTv = dialogView.findViewById<MaterialTextView>(R.id.tv_error_es)
                    network = isInternetAvailable(context)
                    if (!network) {
                        errorTv.text = context.getString(R.string.network_error)
                        errorTv.visibility = View.VISIBLE
                    }
                    else{
                        errorTv.text = ""
                        errorTv.visibility = View.GONE
                    val responseToken = apiService.getJwtToken(networkBody)
                    if (responseToken.message == "Success") {
                        val token = responseToken.model?.access_token;
                        if (token != null) {
                            TokenESanjeevaniInterceptor.setToken(token)
                        }
                        val intent = Intent(context, WebViewActivity::class.java)
                        intent.putExtra("patientId", patientId);
                        intent.putExtra("usernameEs", usernameEs);
                        intent.putExtra("passwordEs", passwordEs);
                        context.startActivity(intent)
                        dialog.dismiss()
                    } else {
                        errorEs = responseToken.message
                        errorTv.text = errorEs
                        errorTv.visibility = View.VISIBLE
                    }
                }
                } catch (e: Exception){
                    Timber.d("GHere is error $e")
                    }
                }
        }
    }
}