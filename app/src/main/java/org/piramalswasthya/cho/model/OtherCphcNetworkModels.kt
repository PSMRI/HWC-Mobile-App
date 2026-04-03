package org.piramalswasthya.cho.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EarDiagnosisNetwork(
    val beneficiaryID: String,
    val beneficiaryRegID: String,
    val data: EarDiagnosisAssessment
)

@JsonClass(generateAdapter = true)
data class NoseDiagnosisNetwork(
    val beneficiaryID: String,
    val beneficiaryRegID: String,
    val data: NoseDiagnosisAssessment
)

@JsonClass(generateAdapter = true)
data class ThroatDiagnosisNetwork(
    val beneficiaryID: String,
    val beneficiaryRegID: String,
    val data: ThroatDiagnosisAssessment
)

@JsonClass(generateAdapter = true)
data class OphthalmicNetwork(
    val beneficiaryID: String,
    val beneficiaryRegID: String,
    val data: OphthalmicVisit
)

@JsonClass(generateAdapter = true)
data class OralHealthNetwork(
    val beneficiaryID: String,
    val beneficiaryRegID: String,
    val data: OralHealth
)

@JsonClass(generateAdapter = true)
data class ElderlyHealthNetwork(
    val beneficiaryID: String,
    val beneficiaryRegID: String,
    val data: ElderlyHealthAssessment
)

@JsonClass(generateAdapter = true)
data class MentalHealthNetwork(
    val beneficiaryID: String,
    val beneficiaryRegID: String,
    val data: MentalHealthScreeningCache
)
