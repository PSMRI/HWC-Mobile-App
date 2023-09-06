package org.piramalswasthya.cho.adapter.dropdown_adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Filter
import android.widget.TextView
import org.piramalswasthya.cho.adapter.model.DropdownList
import org.piramalswasthya.cho.model.AllergicReactionDropdown
import org.piramalswasthya.cho.network.DistrictBlock
import org.piramalswasthya.cho.network.State

class DropdownAdapter (
    context: Context,
    resource: Int,
    private val dropdownList:  List<DropdownList>,
    autoCompleteTextView: AutoCompleteTextView
) : ArrayAdapter<DropdownList>(context, resource, dropdownList) {

    private val filterList = ArrayList<DropdownList>(dropdownList)

    init {
        // Set the custom filter to the AutoCompleteTextView
        autoCompleteTextView.setAdapter(this)
    }
    fun updateData(newData: List<DropdownList>) {
        filterList.clear()
        filterList.addAll(newData)
        notifyDataSetChanged()
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val item = dropdownList[position]
        (view as? TextView)?.text = item.display
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val item = dropdownList[position]
        (view as? TextView)?.text = item.display
        return view
    }
//    override fun getFilter(): Filter {
//        return object : Filter() {
//            override fun performFiltering(constraint: CharSequence?): FilterResults {
//                val results = FilterResults()
//
//                constraint?.let { query ->
//                    val filteredData = ArrayList<DropdownList>()
//                    for (item in filterList) {
//                        if (item.display.lowercase().contains(query.toString().lowercase())) {
//                            filteredData.add(item)
//                        }
//                    }
//                    results.values = filteredData
//                    results.count = filteredData.size
//                }
//
//                return results
//            }
//
//            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
//                results?.let { filterResults ->
//                    clear()
//                    if (filterResults.count > 0) {
//                        addAll(filterResults.values as List<DropdownList>)
//                        notifyDataSetChanged()
//                    }
//                }
//            }
//        }
//    }

}