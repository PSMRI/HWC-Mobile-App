package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "BENFLOW")
@JsonClass(generateAdapter = true)
data class BenFlow(
    @PrimaryKey
    val benFlowID: Long,

    @ColumnInfo(name = "beneficiaryRegID")
    val beneficiaryRegID: Long?,

    @ColumnInfo(name = "benVisitID")
    val benVisitID: Long?,

    @ColumnInfo(name = "visitCode")
    val visitCode: Long?,

    @ColumnInfo(name = "VisitReason")
    val VisitReason: String?,

    @ColumnInfo(name = "VisitCategory")
    val VisitCategory: String?,

    @ColumnInfo(name = "benVisitNo")
    val benVisitNo: Int?,

    @ColumnInfo(name = "nurseFlag")
    val nurseFlag: Int?,

    @ColumnInfo(name = "doctorFlag")
    val doctorFlag: Int?,

    @ColumnInfo(name = "pharmacist_flag")
    val pharmacist_flag: Int?,

    @ColumnInfo(name = "lab_technician_flag")
    val lab_technician_flag: Int?,

    @ColumnInfo(name = "radiologist_flag")
    val radiologist_flag: Int?,

    @ColumnInfo(name = "oncologist_flag")
    val oncologist_flag: Int?,

    @ColumnInfo(name = "specialist_flag")
    val specialist_flag: Int?,

    @ColumnInfo(name = "tC_SpecialistLabFlag")
    val tC_SpecialistLabFlag: Int?,

    @ColumnInfo(name = "agentId")
    val agentId: String?,

    @ColumnInfo(name = "visitDate")
    val visitDate: String?,

    @ColumnInfo(name = "modified_date")
    val modified_date: String?,

    @ColumnInfo(name = "benName")
    val benName: String?,

    @ColumnInfo(name = "deleted")
    val deleted: Boolean?,

    @ColumnInfo(name = "age")
    val age: String?,

    @ColumnInfo(name = "ben_age_val")
    val ben_age_val: Int?,

    @ColumnInfo(name = "dOB")
    val dOB: String?,

    @ColumnInfo(name = "genderID")
    val genderID: Int?,

    @ColumnInfo(name = "genderName")
    val genderName: String?,

    @ColumnInfo(name = "preferredPhoneNum")
    val preferredPhoneNum: String?,

    @ColumnInfo(name = "fatherName")
    val fatherName: String?,

    @ColumnInfo(name = "spouseName")
    val spouseName: String?,

    @ColumnInfo(name = "districtName")
    val districtName:String?,

    @ColumnInfo(name = "registrationDate")
    val registrationDate: String?,

    @ColumnInfo(name = "districtID")
    val districtID: Int?,

    @ColumnInfo(name = "villageID")
    val villageID: Int?,

    @ColumnInfo(name = "vanID")
    val vanID: Int?,

//    val masterVan: {
//
//            "vanID": 4,
//
//            "vanName": "Reserved for TM",
//
//            "deleted": false,
//
//            "vanFoetalMonitorMappedId": false
//
//        },

    @ColumnInfo(name = "providerServiceMapId")
    val providerServiceMapId: Int?,

    @ColumnInfo(name = "villageName")
    val villageName: String?,

    @ColumnInfo(name = "beneficiaryID")
    val beneficiaryID: Long?,

    @ColumnInfo(name = "parkingPlaceID")
    val parkingPlaceID: Int? = null,

    @ColumnInfo(name = "processed")
    val processed: String?,

    @ColumnInfo(name = "benArrivedFlag")
    val benArrivedFlag: Boolean?,

    @ColumnInfo(name = "referredVisitCode")
    val referredVisitCode: Long?,

    @ColumnInfo(name = "referred_visit_id")
    val referred_visit_id: Long?,
)

//"benFlowID": 19271,
//
//"beneficiaryRegID": 32120,
//
//"benVisitID": null,
//
//"visitCode": null,
//
//"VisitReason": null,
//
//"VisitCategory": null,
//
//"benVisitNo": 1,
//
//"nurseFlag": 1,
//
//"doctorFlag": 0,
//
//"pharmacist_flag": 0,
//
//"lab_technician_flag": null,
//
//"radiologist_flag": null,
//
//"oncologist_flag": null,
//
//"specialist_flag": null,
//
//"tC_SpecialistLabFlag": null,
//
//"agentId": "Beehyv",
//
//"visitDate": "Sep 13, 2023 5:21:14 PM",
//
//"modified_by": null,
//
//"modified_date": "Sep 13, 2023 5:21:14 PM",
//
//"deleted": false,
//
//"benVisitDate": String,
//
//"visitSession": String,
//
//"servicePointID": String,
//
//"districtID": Int,
//
//"villageID": 463185,
//
//"vanID": 64,
//
//"beneficiaryID": 932096037700,
//
//"processed": "N",
//
//"benArrivedFlag": false,
//
//"referredVisitCode": Long,
//
//"referred_visit_id": Long