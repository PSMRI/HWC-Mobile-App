package org.piramalswasthya.cho.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MasterLabProceduresRequestModel(val providerServiceMapID: Int?=null)