package org.piramalswasthya.cho.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Filter
import android.widget.TextView
import org.piramalswasthya.cho.model.PrescriptionTemplateDB
import org.piramalswasthya.cho.model.VillageLocationData

class TempDropdownAdapter (context: Context,
                           resource: Int,
                           private val dataList: List<PrescriptionTemplateDB?>,
                           autoCompleteTextView: AutoCompleteTextView,
) : ArrayAdapter<PrescriptionTemplateDB>(context, resource, dataList) {


    init {
        autoCompleteTextView.setAdapter(this)
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val temp = dataList[position]
        (view as? TextView)?.text = temp?.templateName
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val temp = dataList[position]
        (view as? TextView)?.text = temp?.templateName
        return view
    }
//    override fun getFilter(): Filter {
//        return object : Filter() {
//            override fun performFiltering(constraint: CharSequence?): FilterResults {
//                val results = FilterResults()
//                constraint?.let { query ->
//                    val filteredData = ArrayList<PrescriptionTemplateDB>()
//                    val lowerCaseQuery = query.toString().lowercase()
//
//                    for (item in dataListConst) {
//                        if (item.templateName?.lowercase()?.contains(lowerCaseQuery) == true) {
//                            filteredData.add(item)
//                        }
//                    }
//                    results.values = filteredData
//                    results.count = filteredData.size
//                }
//                return results
//            }
//            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
//                results?.let { filterResults ->
//                    clear()
//                    if (filterResults.count > 0) {
//                        addAll(filterResults.values as List<PrescriptionTemplateDB>)
//                        notifyDataSetChanged()
//                    }
//                }
//            }
//        }
//    }
}

