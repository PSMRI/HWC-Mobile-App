package org.piramalswasthya.cho.adapter.dropdown_adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import org.piramalswasthya.cho.R
import org.piramalswasthya.cho.model.HigherHealthCenter

class HigherHealthAdapter(
    context: Context,
    resource: Int,
    private val dataList: List<HigherHealthCenter>,
    private val autoCompleteTextView: AutoCompleteTextView
) : ArrayAdapter<HigherHealthCenter>(context, android.R.layout.simple_dropdown_item_1line, dataList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            android.R.layout.simple_dropdown_item_1line,
            parent,
            false
        )

        val formData = dataList[position]
        (view as? TextView)?.text = formData.institutionName // Set institutionName as the text

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getView(position, convertView, parent)
    }
}

