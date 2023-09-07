package org.piramalswasthya.cho.model

import java.io.Serializable

data class VitalsMasterDb(
    var weight: String? = "",
    var height: String? = "",
    var bmi: String? = "",
    var waistCircumference: String? = "",
    var temperature: String? = "",
    var pulseRate: String? = "",
    var spo2: String? = "",
    var bpSystolic: String? = "",
    var bpDiastolic: String? = "",
    var respiratoryRate: String? = "",
    var rbs: String? = ""
): Serializable
