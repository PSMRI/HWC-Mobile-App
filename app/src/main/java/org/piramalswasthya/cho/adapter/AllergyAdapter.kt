package org.piramalswasthya.cho.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import org.piramalswasthya.cho.model.AllergicReactionDropdown
import org.piramalswasthya.cho.model.IllnessDropdown

class AllergyAdapter (context: Context, resource: Int, private val allergy: List<AllergicReactionDropdown>) :
    ArrayAdapter<AllergicReactionDropdown>(context, resource, allergy) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val allg = allergy[position]
        (view as? TextView)?.text = allg.name
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val allg = allergy[position]
        (view as? TextView)?.text = allg.name
        return view
    }
}