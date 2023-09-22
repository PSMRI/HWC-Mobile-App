package org.piramalswasthya.cho.model

import java.io.Serializable

data class VitalsMasterDb(
    var weight: String? = null,
    var height: String? = null,
    var bmi: String? = null,
    var waistCircumference: String? = null,
    var temperature: String? = null,
    var pulseRate: String? = null,
    var spo2: String? = null,
    var bpSystolic: String? = null,
    var bpDiastolic: String? = null,
    var respiratoryRate: String? = null,
    var rbs: String? = null
): Serializable
