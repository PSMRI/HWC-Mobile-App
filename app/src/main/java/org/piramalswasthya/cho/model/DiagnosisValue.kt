package org.piramalswasthya.cho.model

data class DiagnosisValue(
    var id: Int = -1,
    var diagnosis: String = "",
    /** True when loaded from DB (already saved); row is shown but not editable. */
    var isPreFilled: Boolean = false
)
