package org.piramalswasthya.cho.adapter.dropdown_adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import org.piramalswasthya.cho.adapter.model.DropdownList

class DropdownAdapter (
    context: Context,
    resource: Int,
    private val dropdownList:  List<DropdownList>,
    autoCompleteTextView: AutoCompleteTextView
) : ArrayAdapter<DropdownList>(context, resource, dropdownList) {

    init {
        autoCompleteTextView.setAdapter(this)
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
}