package org.piramalswasthya.cho.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo

class ApiSearchAdapter(
    private val context: Context,
    private val onItemClick: (PatientDisplayWithVisitInfo) -> Unit
) : RecyclerView.Adapter<ApiSearchAdapter.ViewHolder>() {

    private val items = mutableListOf<PatientDisplayWithVisitInfo>()

    fun submitList(list: List<PatientDisplayWithVisitInfo>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_api_search_patient, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvBenId: TextView = itemView.findViewById(R.id.tvBenId)
        private val tvGender: TextView = itemView.findViewById(R.id.tvGender)

        fun bind(info: PatientDisplayWithVisitInfo) {
            val first = info.patient.firstName.orEmpty()
            val last = info.patient.lastName.orEmpty()

            tvName.text = "$first $last"

            val benId = info.patient.beneficiaryRegID
                ?: info.patient.beneficiaryID

            tvBenId.text = "Beneficiary ID: ${benId ?: "NA"}"
            tvGender.text = "Gender: ${info.genderName ?: "NA"}"

        }
    }

}
