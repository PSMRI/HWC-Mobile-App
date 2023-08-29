package org.piramalswasthya.cho.adapter

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.ui.commons.personal_details.PersonalDetailsViewModel


class PatientItemAdapter(
    private val items: List<PatientDisplay>,
    private val onItemClicked: (PatientDisplay) -> Unit
) : RecyclerView.Adapter<PatientItemAdapter.ItemViewHolder>() {

    private lateinit var viewHolder : ItemViewHolder;

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.patient_list_item_view, parent, false)
        viewHolder = ItemViewHolder(itemView)
        return viewHolder
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.name.text = (items[position].patient.firstName ?: "") + " " + (items[position].patient.lastName ?: "")
        holder.abhaNumber.text = ""
        holder.age.text = (items[position].patient.age?.toString() ?: "") + " " + items[position].ageUnit.name
        holder.phoneNo.text = items[position].patient.phoneNo ?: ""
        holder.gender.text = items[position].gender.genderName
        holder.itemView.setOnClickListener{ onItemClicked(items[position]) }
    }

    override fun getItemCount() = items.size

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.patient_name)
        var abhaNumber: TextView = itemView.findViewById(R.id.patient_abha_number)
        var age: TextView = itemView.findViewById(R.id.patient_age)
        var phoneNo: TextView = itemView.findViewById(R.id.patient_phone_no)
        var gender: TextView = itemView.findViewById(R.id.patient_gender)
    }


}