package org.piramalswasthya.cho.ui

import android.R
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.databinding.BindingAdapter

@BindingAdapter("listItems")
fun AutoCompleteTextView.setSpinnerItems(list: Array<String?>) {
    list?.let {
        this.setAdapter(ArrayAdapter(context, R.layout.simple_spinner_dropdown_item, it))
    }
}