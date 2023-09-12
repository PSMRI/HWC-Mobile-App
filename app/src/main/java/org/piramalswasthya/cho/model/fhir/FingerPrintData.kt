package org.piramalswasthya.cho.model.fhir

data class FingerPrintData(
    val id: Int,
    val userName: String?,
    val fpVal : String?,
    val fingerType: String?
)