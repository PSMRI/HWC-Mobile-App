package org.piramalswasthya.cho.ui.commons.fhir_visit_details

import android.content.Context
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.AutoCompleteTextView

class CustomAdapter(
    context: Context,
    resource: Int,
    private val dataList: List<String>,
    private val autoCompleteTextView: AutoCompleteTextView
) : ArrayAdapter<String>(context, resource, dataList) {

    private val filterList = ArrayList<String>(dataList)

    init {
        // Set the custom filter to the AutoCompleteTextView
        autoCompleteTextView.setAdapter(this)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()

                constraint?.let { query ->
                    val filteredData = ArrayList<String>()
                    for (item in filterList) {
                        if (item.lowercase().contains(query.toString().lowercase())) {
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
                        addAll(filterResults.values as List<String>)
                        notifyDataSetChanged()
                    }
                }
            }
        }
    }
}

