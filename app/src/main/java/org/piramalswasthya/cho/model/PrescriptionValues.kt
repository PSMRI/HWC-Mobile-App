package org.piramalswasthya.cho.model

import java.io.Serializable

data class PrescriptionValues(
    var form: String = "",
    var frequency: String = "",
    var duration: String = "",
    var instruction: String = "",
    var unit: String = ""
):Serializable
