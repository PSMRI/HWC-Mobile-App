package org.piramalswasthya.cho.utils

import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.AutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import org.piramalswasthya.cho.R

fun TextInputLayout.setBoxColor(boolean: Boolean, errorText : String? = null) {
    if (!boolean) {
        isErrorEnabled = true
        error = errorText
    } else {
        error = null
        isErrorEnabled = false
    }
    invalidate()
}