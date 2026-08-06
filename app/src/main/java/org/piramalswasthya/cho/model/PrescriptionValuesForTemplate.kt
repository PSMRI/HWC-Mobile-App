package org.piramalswasthya.cho.model

import java.io.Serializable

data class PrescriptionValuesForTemplate(
    var id:Int?=null,
    var tempName:String = "",
    var form: String = "",
    var frequency: String = "",
    var dosage:String = "",
    var duration: String = "",
    var instructions: String = "",
    var unit: String = ""
): Serializable

