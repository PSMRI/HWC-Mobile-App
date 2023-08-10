package org.piramalswasthya.cho.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import org.piramalswasthya.cho.model.IllnessDropdown
import org.piramalswasthya.cho.model.SurgeryDropdown

class SurgeryAdapter (context: Context, resource: Int, private val surgery: List<SurgeryDropdown>) :
    ArrayAdapter<SurgeryDropdown>(context, resource, surgery) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val sur = surgery[position]
        (view as? TextView)?.text = sur.surgeryType
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val sur = surgery[position]
        (view as? TextView)?.text = sur.surgeryType
        return view
    }
}