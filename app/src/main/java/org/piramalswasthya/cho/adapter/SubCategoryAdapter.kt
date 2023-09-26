package org.piramalswasthya.cho.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import org.piramalswasthya.cho.model.SubVisitCategory

class SubCategoryAdapter(context: Context, resource: Int,textViewResourceId:Int,  subCats: List<String>) :
        ArrayAdapter<String>(context, resource,textViewResourceId, subCats) {
}
