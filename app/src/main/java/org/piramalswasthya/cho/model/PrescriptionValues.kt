package org.piramalswasthya.cho.model

import java.io.Serializable

data class PrescriptionValues(
    var id:Int?=-1,
    var form: String = "",
    var frequency: String = "",
    var dosage:String = "",
    var duration: String = "",
    var instruction: String = "",
    var unit: String = ""
):Serializable
