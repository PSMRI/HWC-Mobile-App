package org.piramalswasthya.cho.model

import java.io.Serializable

data class MasterDb(
    var visitMasterDb: VisitMasterDb = VisitMasterDb(),
    var vitalsMasterDb: VitalsMasterDb = VitalsMasterDb(),
    var caseRecordMasterDb: CaseRecordMasterDb = CaseRecordMasterDb()
):Serializable
