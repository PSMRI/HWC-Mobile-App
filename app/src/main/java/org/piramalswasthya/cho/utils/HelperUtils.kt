package org.piramalswasthya.cho.utils
import com.google.android.material.textfield.TextInputLayout
import org.piramalswasthya.cho.model.PatientDisplay

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
fun filterBenList(list: List<PatientDisplay>, text: String): List<PatientDisplay> {
    if (text == "")
        return list
    else {
        val filterText = text.lowercase()
        return list.filter {
            filterForBen(it, filterText)
        }
    }
}

fun filterForBen(
    ben: PatientDisplay,
    filterText: String
) =     ben.patient.firstName?.lowercase()?.contains(filterText) ?: false ||
        ben.patient.lastName?.lowercase()?.contains(filterText) ?: false||
        ben.patient.beneficiaryID?.toString()?.lowercase()?.contains(filterText) ?: false||
        ben.patient.phoneNo?.contains(filterText) ?: false||
        ben.patient.healthIdDetails?.healthIdNumber?.contains(filterText) ?: false
