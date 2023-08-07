package org.piramalswasthya.cho.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import org.piramalswasthya.cho.model.SubVisitCategory

class SubCategoryAdapter(context: Context, resource: Int, private val subCats: List<SubVisitCategory>) :
        ArrayAdapter<SubVisitCategory>(context, resource, subCats) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            val subCat = subCats[position]
            (view as? TextView)?.text = subCat.name
            return view
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getDropDownView(position, convertView, parent)
            val subCat = subCats[position]
            (view as? TextView)?.text = subCat.name
            return view
        }
}
