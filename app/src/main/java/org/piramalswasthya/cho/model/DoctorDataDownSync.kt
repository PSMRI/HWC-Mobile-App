package org.piramalswasthya.cho.model

import androidx.room.Entity
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DoctorDataDownSync(
    val Refer: ReferData?,
    val prescription: List<PrescriptionData>?,
    val LabReport: List<LabReportData>?,
    val diagnosis: DiagnosisUpsync?,
    val investigation: InvestigationData?,
    val ArchivedVisitcodeForLabResult: List<Any>,
    val GraphData: Any,
)

@JsonClass(generateAdapter = true)
data class ReferData(
    val benReferID: Int?,
    val beneficiaryRegID: Int?,
    val benVisitID: Int?,
    val providerServiceMapID: Int?,
    val visitCode: Long?,
    val serviceName: String?,
    val referredToInstituteID:Int?,
    val referredToInstituteName: String?,
    val deleted: Boolean?,
    val processed: String?,
    val createdBy: String?,
    val createdDate: String?,
    val lastModDate: String?,
    val vanID: Int?,
    val parkingPlaceID: Int?,
    val refrredToAdditionalServiceList: List<String>,
    val referralReason: String?,
    val referralReasonList: List<Any>
)

@JsonClass(generateAdapter = true)
data class PrescriptionData(
    val id: Int,
    val prescriptionID: Int,
    val formName: String,
    val drugID: Int,
    val drugName: String,
    val drugStrength: String,
    val dose: String,
    val route: String,
    val frequency: String,
    val duration: String,
    val unit: String,
    val qtyPrescribed: Int,
    val createdDate: String,
    val isEDL: Boolean,
    val instructions: String
)

@JsonClass(generateAdapter = true)
data class LabReportData(
    val prescriptionID: Int?,
    val procedureID: Int?,
    val createdDate: String?,
    val procedureName: String?,
    val procedureType: String?,
    val componentList: List<ComponentData>?
)

@JsonClass(generateAdapter = true)
data class ComponentData(
    val testResultValue: String?,
    val fileIDs: List<Any>?,
    val testResultUnit: String?,
    val testComponentID: Int?,
    val componentName: String?,
    val remarks: String?
)


@JsonClass(generateAdapter = true)
data class InvestigationData(
    val beneficiaryRegID: Int,
    val benVisitID: Int,
    val visitCode: Long,
    val providerServiceMapID: Int,
    val laboratoryList: List<LaboratoryData>
)

@JsonClass(generateAdapter = true)
data class LaboratoryData(
    val procedureID: Int,
    val procedureName: String
)

