package org.piramalswasthya.cho.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import org.piramalswasthya.cho.model.IllnessDropdown

class IllnessAdapter(context: Context, resource: Int, private val illness: List<IllnessDropdown>) :
    ArrayAdapter<IllnessDropdown>(context, resource, illness) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val ill = illness[position]
        (view as? TextView)?.text = ill.illnessType
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val ill = illness[position]
        (view as? TextView)?.text = ill.illnessType
        return view
    }
}