package org.piramalswasthya.cho.adapter

import android.content.Context
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
        val v = LayoutInflater.from(parent.context).inflate(R.layout.patient_details_card, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.name)
        private val abhaNumber: TextView = itemView.findViewById(R.id.abha_number)
        private val age: TextView = itemView.findViewById(R.id.age)
        private val phoneNo: TextView = itemView.findViewById(R.id.phone_no)
        private val gender: TextView = itemView.findViewById(R.id.gender)

        fun bind(info: PatientDisplayWithVisitInfo) {
            val first = info.patient.firstName ?: ""
            val last = info.patient.lastName ?: ""
            name.text = "Name: $first $last"
            gender.text = "Gender: ${info.genderName ?: "NA"}"
            val benIdText = info.patient.beneficiaryRegID?.toString()
                ?: info.patient.beneficiaryID?.toString() ?: "NA"
            abhaNumber.text = "Beneficiary ID: $benIdText"
            age.visibility = View.GONE
            phoneNo.visibility = View.GONE
        }
    }
}
