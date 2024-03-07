package org.piramalswasthya.cho.adapter.dropdown_adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Filter
import android.widget.TextView
import org.piramalswasthya.cho.network.State

class StatesAdapter(
    context: Context,
    resource: Int,
    private val dataList: List<State>,
    private val autoCompleteTextView: AutoCompleteTextView
) : ArrayAdapter<State>(context, resource, dataList) {

    private val filterList = ArrayList<State>(dataList)

    init {
        // Set the custom filter to the AutoCompleteTextView
        autoCompleteTextView.setAdapter(this)
    }
    fun updateData(newData: List<State>) {
        filterList.clear()
        filterList.addAll(newData)
        notifyDataSetChanged()
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val state = dataList[position]
        (view as? TextView)?.text = state.stateName
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val state = dataList[position]
        (view as? TextView)?.text = state.stateName
        return view
    }


}