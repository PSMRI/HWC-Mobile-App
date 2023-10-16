//package org.piramalswasthya.cho.adapter
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//
//import com.google.android.material.textfield.TextInputEditText
//import org.piramalswasthya.cho.R
//import org.piramalswasthya.cho.model.LabReportValues
//
//class ReportAdapter (private val labItems: List<LabReportValues>) :
//    RecyclerView.Adapter<ReportAdapter.ViewHolder>() {
//
//    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val testName: TextInputEditText = itemView.findViewById(R.id.testNameDropdown)
//        val comp: TextInputEditText = itemView.findViewById(R.id.inputComp)
//        val result: TextInputEditText = itemView.findViewById(R.id.inputMeasUnit)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val itemView = LayoutInflater.from(parent.context)
//            .inflate(R.layout.report_custom_layout, parent, false)
//        return ViewHolder(itemView)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val lab = labItems[position]
//        holder.testName.setText(lab.testName)
//        holder.comp.setText(lab.componentListString)
//        holder.result.setText(lab.result)
//
//    }
//
//    override fun getItemCount(): Int {
//        return labItems.size
//    }
//}