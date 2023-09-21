package org.piramalswasthya.cho.model

import java.io.Serializable

data class ChiefComplaintValues(
    var id: Int = -1,
    var chiefComplaint: String = "",
    var duration: String = "",
    var durationUnit: String = "Days",
    var description: String? = ""
): Serializable
