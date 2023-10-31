package org.piramalswasthya.cho.utils
import com.google.android.material.textfield.TextInputLayout
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.PatientDisplay
import org.piramalswasthya.cho.model.PatientDisplayWithVisitInfo
import org.piramalswasthya.cho.model.PatientVisitInfoSyncWithPatient



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

//fun filterBenList(list: List<PatientVisitInfoSyncWithPatient>, text: String): List<PatientVisitInfoSyncWithPatient> {
//    if (text == "")
//        return list
//    else {
//        val filterText = text.lowercase()
//        return list.filter {
//            filterForBen(it.patient, filterText)
//        }
//    }
//}

fun filterBenList(list: List<PatientDisplayWithVisitInfo>, text: String): List<PatientDisplayWithVisitInfo> {
    if (text == "")
        return list
    else {
        val filterText = text.lowercase()
        return list.filter {
            filterForBen(it.patient, filterText)
        }
    }
}

fun filterForBen(
    ben: Patient,
    filterText: String
) =     ben.firstName?.lowercase()?.contains(filterText) ?: false ||
        ben.lastName?.lowercase()?.contains(filterText) ?: false||
        ben.beneficiaryID?.toString()?.lowercase()?.contains(filterText) ?: false||
        ben.phoneNo?.contains(filterText) ?: false||
        ben.healthIdDetails?.healthIdNumber?.contains(filterText) ?: false
