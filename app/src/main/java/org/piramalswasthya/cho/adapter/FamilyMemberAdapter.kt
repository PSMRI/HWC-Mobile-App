package org.piramalswasthya.cho.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import org.piramalswasthya.cho.model.FamilyMemberDropdown


class FamilyMemberAdapter (context: Context, resource: Int, private val family: List<FamilyMemberDropdown>) :
    ArrayAdapter<FamilyMemberDropdown>(context, resource, family) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val fam = family[position]
        (view as? TextView)?.text = fam.benRelationshipType
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val fam = family[position]
        (view as? TextView)?.text = fam.benRelationshipType
        return view
    }
}