package org.piramalswasthya.cho.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import org.piramalswasthya.cho.model.IllnessDropdown
import org.piramalswasthya.cho.model.TobaccoDropdown

class TobaccoAdapter (context: Context, resource: Int, private val tobacco: List<TobaccoDropdown>) :
    ArrayAdapter<TobaccoDropdown>(context, resource, tobacco) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val tob = tobacco[position]
        (view as? TextView)?.text = tob.habitValue
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val tob = tobacco[position]
        (view as? TextView)?.text = tob.habitValue
        return view
    }
}