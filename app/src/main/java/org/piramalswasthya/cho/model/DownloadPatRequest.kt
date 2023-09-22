package org.piramalswasthya.cho.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DownloadPatRequest(
    val villageID : List<Int>,
    val lastSyncDate : String
)