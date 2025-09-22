package org.piramalswasthya.cho.model

import java.io.Serializable

data class CaseRecordMasterDb(
    var testName:String = "",
    var provisionalDiagnosis: List<String> = listOf(),
    var externalInvestigations: String = "",
    var prescriptionValues: PrescriptionValues = PrescriptionValues()
):Serializable
