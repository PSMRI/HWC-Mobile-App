package org.piramalswasthya.cho.adapter

import android.content.Context
import android.widget.ArrayAdapter

class OutreachDropdownAdapter (context: Context, resource: Int, textViewResourceId:Int, outR: List<String>) :
    ArrayAdapter<String>(context, resource,textViewResourceId, outR) {
}
