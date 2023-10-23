package org.piramalswasthya.cho.ui.commons.case_record

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Filter
import android.widget.TextView
import org.piramalswasthya.cho.model.ItemMasterList
import org.piramalswasthya.cho.model.PrescriptionCaseRecord
import org.piramalswasthya.cho.model.PrescriptionTemplateDB
import org.piramalswasthya.cho.model.PrescriptionValuesForTemplate

class TempNameAdapter(
                      context: Context,
                      resource: Int,
                      private val dataList: List<PrescriptionTemplateDB?>,
                      autoCompleteTextView: AutoCompleteTextView
) : ArrayAdapter<PrescriptionTemplateDB>(context, resource, dataList) {


    init {
        // Set the custom filter to the AutoCompleteTextView
        autoCompleteTextView.setAdapter(this)
    }

//    override fun getFilter(): Filter {
//        return object : Filter() {
//            override fun performFiltering(constraint: CharSequence?): FilterResults {
//                val results = FilterResults()
//                constraint?.let { query ->
//                    val filteredData = ArrayList<ItemMasterList>()
//                    val lowerCaseQuery = query.toString().lowercase()
//
//                    for (item in dataListConst) {
//                        if (item.itemName.lowercase().contains(lowerCaseQuery)) {
//                            filteredData.add(item)
//                        }
//                    }
//                    results.values = filteredData
//                    results.count = filteredData.size
//                }
//
//                return results
//            }
//            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
//                results?.let { filterResults ->
//                    clear()
//                    if (filterResults.count > 0) {
//                        addAll(filterResults.values as List<ItemMasterList>)
//                        notifyDataSetChanged()
//                    }
//                }
//            }
//        }
//    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val formData = dataList[position]
        (view as? TextView)?.text = formData?.templateName
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val formData = dataList[position]
        (view as? TextView)?.text = formData?.templateName
        return view
    }
}
