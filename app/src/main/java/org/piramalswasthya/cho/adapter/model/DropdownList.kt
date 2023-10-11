package org.piramalswasthya.cho.adapter.model

import com.squareup.moshi.JsonClass
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

@JsonClass(generateAdapter = true)
data class DropdownList(
    val id: Int,
    var display: String?
)