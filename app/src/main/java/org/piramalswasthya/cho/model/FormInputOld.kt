package org.piramalswasthya.cho.model

import android.text.InputType.TYPE_CLASS_TEXT
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File


data class FormInputOld(
    val inputType: InputType,
    var title: String,
    val subtitle: String? = null,
    var entries: Array<String>? = null,
    var required: Boolean,
    var value: MutableStateFlow<String?> = MutableStateFlow(null),
    val regex: String? = null,
    val allCaps: Boolean = false,
    val etInputType: Int = TYPE_CLASS_TEXT,
    val isMobileNumber: Boolean = false,
    val etMaxLength: Int = 50,
    var errorText: String? = null,
    var max: Long? = null,
    var min: Long? = null,
    var minDecimal: Double? = null,
    var maxDecimal: Double? = null,
    val orientation: Int? = null,
    var imageFile: File? = null
)

