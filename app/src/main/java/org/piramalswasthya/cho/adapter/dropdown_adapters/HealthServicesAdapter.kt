package org.piramalswasthya.cho.adapter.dropdown_adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView



data class HealthServices(val id: Int, val name: String, var isSelected: Boolean = false)

class HealthServicesAdapter(context: Context, resource: Int, private val servicesList: List<HealthServices>) :
    ArrayAdapter<HealthServices>(context, resource, servicesList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val service = servicesList[position]
        (view as? TextView)?.text = service.name
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val service = servicesList[position]
        (view as? TextView)?.text = service.name
        return view
    }
}