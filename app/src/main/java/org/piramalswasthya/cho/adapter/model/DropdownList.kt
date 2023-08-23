package org.piramalswasthya.cho.adapter.model

import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.codesystems.V3MaritalStatus
import org.piramalswasthya.cho.fhir_utils.extension_names.block
import org.piramalswasthya.cho.fhir_utils.extension_names.district
import org.piramalswasthya.cho.fhir_utils.extension_names.districtBranch
import org.piramalswasthya.cho.fhir_utils.extension_names.state
import org.piramalswasthya.cho.model.AgeUnit
import org.piramalswasthya.cho.model.GenderMaster
import org.piramalswasthya.cho.model.MaritalStatusMaster

data class DropdownList(
    val id: Int,
    var display: String
){
    constructor(genderMaster: GenderMaster) : this(genderMaster.genderID, genderMaster.genderName,)
    constructor(ageUnit: AgeUnit) : this(ageUnit.id, ageUnit.name,)
    constructor(maritalStatus: MaritalStatusMaster) : this(maritalStatus.maritalStatusID, maritalStatus.status,)

}
