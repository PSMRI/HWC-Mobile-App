package org.piramalswasthya.cho.model

import androidx.annotation.ArrayRes

data class FormElement(
    val id: Int,
    var inputType: InputType,
    var required: Boolean,
    var title: String,
    val subtitle: String? = null,
    @ArrayRes var arrayId : Int = -1,
    var entries: Array<String>? = null,
    var doubleStar: Boolean = false,
    var hasDependants: Boolean = false,
    var hasAlertError: Boolean = false,
    var value: String? = null,
    val regex: String? = null,
    val allCaps: Boolean = false,
    val etInputType: Int = android.text.InputType.TYPE_CLASS_TEXT,
    val isMobileNumber: Boolean = false,
    val etMaxLength: Int = 50,
    val multiLine : Boolean = false,
    var errorText: String? = null,
    var max: Long? = null,
    var min: Long? = null,
    var minDecimal: Double? = null,
    var maxDecimal: Double? = null,
    val orientation: Int? = null,
    var hasSpeechToText: Boolean = false,
    var showDateNumberPicker: Boolean = false,
    var showHighRisk: Boolean = false,
    var isEnabled: Boolean = true,
    var headingLine: Boolean = true,
    val showYearFirstInDatePicker : Boolean = false,
    /** Date format for DATE_PICKER (e.g. "dd/MM/yyyy"). Null = use default dd-MM-yyyy. */
    var dateFormat: String? = null,
    var booleanValue: Boolean? = null,
    var trueIndex: Int? = null,
    var falseIndex: Int? = null,
    /**
     * When true, editing this field rebinds the consecutive sibling rows that share a
     * cross-field validation rule (e.g. Delivery Outcome = Live Birth + Still Birth) so
     * their error states refresh together. Opt-in per field: do NOT key this off the
     * element id, because ids are reused across datasets (id 15/16/17 are height/weight/bmi
     * in the PW registration form) and rebinding the focused EditText on every keystroke
     * steals focus mid-typing.
     */
    val refreshSiblingsOnChange: Boolean = false,
)