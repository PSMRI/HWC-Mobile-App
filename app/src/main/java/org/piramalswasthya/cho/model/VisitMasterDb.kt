package org.piramalswasthya.cho.model

import java.io.Serializable

data class VisitMasterDb(
    var category: String? = null,
    var reason: String? = null,
    var subCategory: String? = null,
    var chiefComplaint: List<ChiefComplaintValues>? = null
): Serializable
