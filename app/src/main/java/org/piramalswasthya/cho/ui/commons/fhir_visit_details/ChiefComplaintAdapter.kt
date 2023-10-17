package org.piramalswasthya.cho.ui.commons.fhir_visit_details

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import org.piramalswasthya.cho.model.ChiefComplaintMaster

class ChiefComplaintAdapter(
    context: Context,
    resource: Int,
    private val dataList: List<ChiefComplaintMaster>,
    autoCompleteTextView: AutoCompleteTextView,
    private val dataListConst: List<ChiefComplaintMaster>,
) : ArrayAdapter<ChiefComplaintMaster>(context, resource, dataList) {


    init {
        // Set the custom filter to the AutoCompleteTextView
        autoCompleteTextView.setAdapter(this)
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val chiefComplaint = dataList[position]
        (view as? TextView)?.text = chiefComplaint.chiefComplaint
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val chiefComplaint = dataList[position]
        (view as? TextView)?.text = chiefComplaint.chiefComplaint
        return view
    }
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                constraint?.let { query ->
                    val filteredData = ArrayList<ChiefComplaintMaster>()
                    val lowerCaseQuery = query.toString().lowercase()

                    for (item in dataListConst) {
                        if (item.chiefComplaint.lowercase().startsWith(lowerCaseQuery)) {
                            filteredData.add(item)
                        }
                    }
                    results.values = filteredData
                    results.count = filteredData.size
                }

                return results
            }
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                results?.let { filterResults ->
                    clear()
                    if (filterResults.count > 0) {
                        addAll(filterResults.values as List<ChiefComplaintMaster>)
                        notifyDataSetChanged()
                    }
                }
            }
        }
    }
}

