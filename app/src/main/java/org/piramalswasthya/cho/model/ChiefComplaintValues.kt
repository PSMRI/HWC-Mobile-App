package org.piramalswasthya.cho.model

data class ChiefComplaintValues(
    var id: Int = -1,
    var chiefComplaint: String = "",
    var duration: String = "",
    var durationUnit: String = "",
    var description: String = ""
)
