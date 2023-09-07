package org.piramalswasthya.cho.model

import java.io.Serializable

data class VisitMasterDb(
    var category: String = "",
    var reason: String = "",
    var subCategory: String = "",
    var chiefComplaint: List<ChiefComplaintValues> = listOf()
): Serializable
