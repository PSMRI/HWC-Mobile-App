package org.piramalswasthya.cho.adapter

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.PharmacistListItemViewBinding
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.PrescriptionItemDTO
import timber.log.Timber


class PharmacistItemAdapter(
    private val context: Context,
    private val clickListener: BenClickListener,
) : ListAdapter<PrescriptionItemDTO,PharmacistItemAdapter.BenViewHolder>(BenDiffUtilCallBack) {

    private object BenDiffUtilCallBack : DiffUtil.ItemCallback<PrescriptionItemDTO>() {
        override fun areItemsTheSame(
            oldItem: PrescriptionItemDTO, newItem: PrescriptionItemDTO
        ) = oldItem.id == newItem.id

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
            clickListener: BenClickListener?
        ) {
            Timber.d("*******************DAta Prescription DTO************** ",item)
            binding.prescription = item
            binding.clickListener = clickListener

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
        holder.bind(getItem(position), clickListener)

//        holder.itemView.findViewById<MaterialButton>(R.id.submit_btn).setOnClickListener { // When submit button is clicked
//            network = isInternetAvailable(context)
////            callLoginDialog()
//        }

//        holder.itemView.findViewById<MaterialButton>(R.id.btn_view_batch).setOnClickListener { // When view batch button is clicked
////            network = isInternetAvailable(context)
//
//        }
    }

    class BenClickListener(
        private val clickedABHA: (benVisitInfo: PrescriptionItemDTO) -> Unit,
    ) {
        fun onClickABHA(item: PrescriptionItemDTO) {
            Log.i("View batch Button", "")
            Log.d("ABHA Item Click", "ABHA item clicked")
            clickedABHA(item)
        }
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
}