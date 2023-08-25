package org.piramalswasthya.cho.adapter.dropdown_adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

data class HigherCenter(val id: Int, val name: String, var isSelected: Boolean = false)

class HigherCenterAdapter(context: Context, resource: Int, private val centerList: List<HigherCenter>) :
    ArrayAdapter<HigherCenter>(context, resource, centerList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val center = centerList[position]
        (view as? TextView)?.text = center.name
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val center = centerList[position]
        (view as? TextView)?.text = center.name
        return view
    }
}