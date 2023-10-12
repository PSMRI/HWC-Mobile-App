package org.piramalswasthya.cho.adapter

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.PharmacistListItemViewBinding
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PrescriptionItemDTO


class PharmacistItemAdapter(
    private val context: Context
) : ListAdapter<PrescriptionItemDTO,PharmacistItemAdapter.BenViewHolder>(BenDiffUtilCallBack) {

    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<PrescriptionItemDTO>() {
        override fun areItemsTheSame(
            oldItem: PrescriptionItemDTO, newItem: PrescriptionItemDTO
        ) = oldItem.drugID == newItem.drugID

        override fun areContentsTheSame(
            oldItem: PrescriptionItemDTO, newItem: PrescriptionItemDTO
        ) = oldItem == newItem

    }

    private var drugID : String = ""
    private var network : Boolean = false

    class BenViewHolder private constructor(private val binding: PharmacistListItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): BenViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = PharmacistListItemViewBinding.inflate(layoutInflater, parent, false)
                return BenViewHolder(binding)
            }
        }

        fun bind(
            item:PrescriptionItemDTO,
        ) {
            binding.prescription = item

            binding.formValue.text = (item.drugForm ?: "")
            binding.durationValue.text = (item.duration ?:"") + " " + (item.durationUnit ?: "")
            binding.frequencyValue.text = (item.frequency ?: "")
            binding.doseValue.text = item.dose ?: ""
            binding.quantityPrescribedValue.text = item.qtyPrescribed.toString() ?: ""
            binding.routeValue.text = item.route ?: ""
            binding.quantityDispensedValue.text = item.qtyPrescribed.toString() ?: ""
            binding.specialInstructionValue.text = item.dose ?: ""

            binding.executePendingBindings()

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = BenViewHolder.from(parent)

    override fun onBindViewHolder(holder: BenViewHolder, position: Int) {
        drugID = getItem(position).drugID.toString()

        holder.itemView.findViewById<MaterialButton>(R.id.submit_btn).setOnClickListener { // When submit button is clicked
            network = isInternetAvailable(context)
//            callLoginDialog()
        }

        holder.itemView.findViewById<MaterialButton>(R.id.btn_view_batch).setOnClickListener { // When view batch button is clicked
//            network = isInternetAvailable(context)
//            callLoginDialog()
        }
    }

//    class BenClickListener(
//        private val clickedViewBatch: (benId: Long?) -> Unit,
//    ) {
//        fun onClickABHA(item: Patient) {
//            Log.d("ABHA Item Click", "ABHA item clicked")
//            clickedABHA(item.beneficiaryID)
//        }
//    }

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
}