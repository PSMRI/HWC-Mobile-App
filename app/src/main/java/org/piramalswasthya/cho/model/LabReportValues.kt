package org.piramalswasthya.cho.model

import java.io.Serializable

data class LabReportValues(
    var id:Int?=null,
    var testName: String = "",
    var componentListString:String = "",
    var result: String = "",
): Serializable

