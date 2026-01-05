package org.piramalswasthya.cho.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Filter
import android.widget.TextView
import org.piramalswasthya.cho.model.VillageLocationData

class VillageDropdownAdapter(
    context: Context,
    resource: Int,
    private val dataList: List<VillageLocationData>,
    private val autoCompleteTextView: AutoCompleteTextView,
    private val dataListConst: List<VillageLocationData>,
) : ArrayAdapter<VillageLocationData>(context, resource, dataList) {

    var onDataUpdated: (() -> Unit)? = null
    var shouldAutoShowDropdown: Boolean = true

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        (view as? TextView)?.text = dataList[position].villageName
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        (view as? TextView)?.text = dataList[position].villageName
        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                results.values =
                    if (constraint.isNullOrBlank()) dataListConst
                    else dataListConst.filter {
                        it.villageName?.contains(constraint, true) == true
                    }
                results.count = (results.values as List<*>).size
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                clear()
                @Suppress("UNCHECKED_CAST")
                addAll(results?.values as List<VillageLocationData>)
                notifyDataSetChanged()

                if (shouldAutoShowDropdown) {
                    autoCompleteTextView.post {
                        if (!autoCompleteTextView.isPopupShowing) {
                            autoCompleteTextView.showDropDown()
                        }
                    }
                }
                onDataUpdated?.invoke()
            }
        }
    }
}

