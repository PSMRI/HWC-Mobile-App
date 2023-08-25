package org.piramalswasthya.cho.adapter.dropdown_multiselect

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import org.piramalswasthya.cho.R

data class Item(val id: Int, val name: String, var isSelected: Boolean = false)

class MultiSelectAdapter(context: Context, resource: Int, private val items: List<Item>) :
    ArrayAdapter<Item>(context, resource, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val checkBox = view.findViewById<CheckBox>(R.id.checkBox)
        val item = getItem(position)

        checkBox.text = item?.name
        checkBox.isChecked = item?.isSelected ?: false

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            item?.isSelected = isChecked
        }

        return view
    }
}