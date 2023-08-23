package org.piramalswasthya.cho.adapter.dropdown_adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import org.piramalswasthya.cho.adapter.model.DropdownList
import org.piramalswasthya.cho.model.AllergicReactionDropdown

class DropdownAdapter (context: Context, resource: Int, private val dropdownList:  List<DropdownList>) :
    ArrayAdapter<DropdownList>(context, resource, dropdownList) {

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