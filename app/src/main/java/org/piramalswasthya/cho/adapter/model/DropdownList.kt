package org.piramalswasthya.cho.adapter.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DropdownList(
    val id: Int,
    var display: String?
)