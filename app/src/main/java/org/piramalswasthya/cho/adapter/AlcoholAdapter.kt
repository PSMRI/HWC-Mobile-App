package org.piramalswasthya.cho.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import org.piramalswasthya.cho.model.AlcoholDropdown

class AlcoholAdapter (context: Context, resource: Int, private val alcohol: List<AlcoholDropdown>) :
    ArrayAdapter<AlcoholDropdown>(context, resource, alcohol) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val alc = alcohol[position]
        (view as? TextView)?.text = alc.habitValue
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val alc = alcohol[position]
        (view as? TextView)?.text = alc.habitValue
        return view
    }
    
}