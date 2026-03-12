package org.piramalswasthya.cho.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import okhttp3.internal.notifyAll
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.databinding.PharmacistListItemViewBinding
import org.piramalswasthya.cho.model.PrescriptionItemDTO
import timber.log.Timber


class PharmacistItemAdapter(
    private val context: Context,
    private var issueType: String,
    private val clickListener: PharmacistClickListener,
    private val networkAvailabilityCheck: () -> Boolean
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
            item: PrescriptionItemDTO,
            issueType:String,
            clickListener: PharmacistClickListener,
            networkAvailabilityCheck: () -> Boolean
        ) {
            Timber.d("*******************DAta Prescription DTO************** ",clickListener)
            binding.prescription = item
            binding.clickListener = clickListener

            binding.medicationName.text = (item.genericDrugName + " "+ item.drugStrength)
            binding.formValue.text = (item.drugForm ?: "")
            if(item.duration!=null){
                binding.durationValue.text = (item.duration ?:"") + " " + (item.durationUnit ?: "")
            }
            binding.frequencyValue.text = (item.frequency ?: "")
            binding.doseValue.text = item.dose ?: ""
            binding.quantityPrescribedValue.text = item.qtyPrescribed.toString() ?: ""
            binding.routeValue.text = item.route ?: ""
            // Local DTO has prescribed quantity only; dispensed quantity should not mirror prescribed.
            binding.quantityDispensedValue.text = "0"
            binding.specialInstructionValue.text = item.dose ?: ""
            
            // Handle button visibility and text based on issue type and network availability
            when (issueType) {
                "Manual Issue" -> {
                    val isNetworkAvailable = networkAvailabilityCheck()
                    if (isNetworkAvailable) {
                        binding.btnViewBatch.text = "Select Batch"
                        binding.btnViewBatch.visibility = android.view.View.VISIBLE
                        binding.btnViewBatch.isEnabled = true
                        binding.btnViewBatch.setOnClickListener {
                            clickListener.onClickSelectBatch(item)
                        }
                    } else {
                        binding.btnViewBatch.visibility = android.view.View.GONE
                        // Show a message that network is required for manual batch selection
                        android.widget.Toast.makeText(binding.root.context, 
                            binding.root.context.getString(R.string.network_required_manual_batch), 
                            android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
                "System Issue" -> {
                    // Hide/comment the view batch button for system issue
                    binding.btnViewBatch.visibility = android.view.View.GONE
                    // Alternatively, you can comment out the button functionality:
                    /*
                    binding.btnViewBatch.text = "View Batch"
                    binding.btnViewBatch.visibility = android.view.View.VISIBLE
                    binding.btnViewBatch.isEnabled = false
                    binding.btnViewBatch.setOnClickListener {
                        clickListener.onClickViewBatch(item)
                    }
                    */
                }
                else -> {
                    binding.btnViewBatch.visibility = android.view.View.GONE
                }
            }
            binding.executePendingBindings()

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = BenViewHolder.from(parent)

    override fun onBindViewHolder(holder: BenViewHolder, position: Int) {
        drugID = getItem(position).drugID.toString()
        holder.bind(getItem(position), issueType, clickListener, networkAvailabilityCheck)

//        holder.itemView.findViewById<MaterialButton>(R.id.submit_btn).setOnClickListener { // When submit button is clicked
//            network = isInternetAvailable(context)
////            callLoginDialog()
//        }

//        holder.itemView.findViewById<MaterialButton>(R.id.btn_view_batch).setOnClickListener { // When view batch button is clicked
////            network = isInternetAvailable(context)
//            Log.i("View batch Button", "")
//        }
    }

    class PharmacistClickListener(
        private val clickedViewBatch: (benVisitInfo: PrescriptionItemDTO) -> Unit,
        private val clickedSelectBatch: (item: PrescriptionItemDTO) -> Unit,
    ) {
        fun onClickViewBatch(item: PrescriptionItemDTO) = clickedViewBatch(
            item,
        )

        fun onClickSelectBatch(item: PrescriptionItemDTO) = clickedSelectBatch(
            item,
        )



//        fun onClickABHA(item: PrescriptionItemDTO) {
//            Log.i("View batch Button", "")
////            Log.d("ABHA Item Click", "ABHA item clicked")
//            clickedViewBatch(item)
//        }
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

    fun updateIssueType(newIssueType: String) {
        issueType = newIssueType
        notifyDataSetChanged()  // Refresh UI with updated label
    }

}