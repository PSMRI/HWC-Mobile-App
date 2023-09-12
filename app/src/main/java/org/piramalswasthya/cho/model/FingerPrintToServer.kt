package org.piramalswasthya.cho.model

data class FingerPrintToServer(
    val id: Int,
    val userName: String?,
    val rightThumb: String?,
    val rightIndexFinger: String?,
    val leftThumb: String?,
    val leftIndexFinger: String?,
)
