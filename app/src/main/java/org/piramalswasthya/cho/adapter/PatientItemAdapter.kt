package org.piramalswasthya.cho.adapter

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.databinding.DataBindingUtil
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
    private var items: List<PatientDisplay>,
    private val onItemClicked: (PatientDisplay) -> Unit,
    private val clickListener: BenClickListener? = null,
    private val showAbha: Boolean = false,
) : RecyclerView.Adapter<PatientItemAdapter.ItemViewHolder>() {
    private var usernameEs : String = ""
    private var passwordEs : String = ""
    private var errorEs : String = ""
    private var patientId : String = ""
    private var network : Boolean = false
    private lateinit var viewHolder : ItemViewHolder;
    private var dbu: PatientListItemViewBinding? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

        network = isInternetAvailable(context)
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.patient_list_item_view, parent, false)
        dbu = DataBindingUtil.getBinding<PatientListItemViewBinding>(itemView)
        viewHolder = ItemViewHolder(itemView)
        return viewHolder
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        patientId = items[position].patient.patientID
        holder.name.text = (items[position].patient.firstName ?: "") + " " + (items[position].patient.lastName ?: "")
        holder.abhaNumber.text = ""
        holder.age.text = (items[position].patient.age?.toString() ?: "") + " " + items[position].ageUnit.name
        holder.phoneNo.text = items[position].patient.phoneNo ?: ""
        holder.gender.text = items[position].gender.genderName
        holder.itemView.setOnClickListener{ onItemClicked(items[position]) }
        holder.btnEsanjeevani.setOnClickListener {
            network = isInternetAvailable(context)
            callLoginDialog()
        }

        dbu?.showAbha = showAbha
        dbu?.hasAbha = !items[position].patient.healthIdDetails?.healthIdNumber.isNullOrEmpty()
        dbu?.clickListener = clickListener
//        holder.showAbha = showAbha
//        holder.hasAbha = !items[position].patient.healthIdDetails?.healthIdNumber.isNullOrEmpty()
//        holder.clickListener = clickListener


//        holder.btnAbha.setOnClickListener {
//            if(items[position].patient.syncState == SyncState.SYNCED) {
//                val intent = Intent(context, AbhaIdActivity::class.java)
//                intent.putExtra("benId", items[position].patient.beneficiaryID)
//                intent.putExtra("benRegId", items[position].patient.beneficiaryRegID)
//                context.startActivity(intent)
//            }else{
//                Toast.makeText(context, "Beneficiary not synced yet!", Toast.LENGTH_SHORT).show()
//            }
//        }
    }

    override fun getItemCount() = items.size
    fun updateData(filteredPatients: List<PatientDisplay>):Int {
        items = filteredPatients
        notifyDataSetChanged()
        return filteredPatients.size
    }
    class BenClickListener(
        private val clickedABHA: (benId: Long?) -> Unit,
    ) {
        fun onClickABHA(item: Patient) = clickedABHA(item.beneficiaryID)
    }
    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.patient_name)
        var abhaNumber: TextView = itemView.findViewById(R.id.patient_abha_number)
        var age: TextView = itemView.findViewById(R.id.patient_age)
        var phoneNo: TextView = itemView.findViewById(R.id.patient_phone_no)
        var gender: TextView = itemView.findViewById(R.id.patient_gender)
        var btnEsanjeevani:MaterialButton = itemView.findViewById(R.id.btn_eSanjeevani)

//        var btnAbha:MaterialButton = itemView.findViewById(R.id.btn_abha)
//        var showAbha = dbu?.showAbha
//        var hasAbha = dbu?.hasAbha
//        dbu?.clickListener = clickListener
            }
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