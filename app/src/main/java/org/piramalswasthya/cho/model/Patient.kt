package org.piramalswasthya.cho.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.squareup.moshi.JsonClass
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.utils.DateTimeUtil
import java.io.Serializable
import java.util.Date

@Entity(
    tableName = "PATIENT",
    foreignKeys = [
        ForeignKey(
            entity = AgeUnit::class,
            parentColumns = ["id"],
            childColumns = ["ageUnitID"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = MaritalStatusMaster::class,
            parentColumns = ["maritalStatusID"],
            childColumns = ["maritalStatusID"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = GenderMaster::class,
            parentColumns = ["genderID"],
            childColumns = ["genderID"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = StateMaster::class,
            parentColumns = ["stateID"],
            childColumns = ["stateID"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = DistrictMaster::class,
            parentColumns = ["districtID"],
            childColumns = ["districtID"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = BlockMaster::class,
            parentColumns = ["blockID"],
            childColumns = ["blockID"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = VillageMaster::class,
            parentColumns = ["districtBranchID"],
            childColumns = ["districtBranchID"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = CommunityMaster::class,
            parentColumns = ["communityID"],
            childColumns = ["communityID"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = ReligionMaster::class,
            parentColumns = ["religionID"],
            childColumns = ["religionID"],
            onDelete = ForeignKey.NO_ACTION
        ),
    ]
)
@JsonClass(generateAdapter = true)
data class Patient (

    @PrimaryKey
    @NonNull
    var patientID: String = "",

    @ColumnInfo(name = "firstName")
    var firstName: String? = null,

    @ColumnInfo(name = "lastName")
    var lastName: String? = null,

    @ColumnInfo(name = "dob")
    var dob: Date? = null,

    @ColumnInfo(name = "age")
    var age: Int? = null,

    @ColumnInfo(name = "ageUnitID")
    var ageUnitID: Int? = null,

    @ColumnInfo(name = "maritalStatusID")
    var maritalStatusID: Int? = null,

    @ColumnInfo(name = "spouseName")
    var spouseName: String? = null,

    @ColumnInfo(name = "ageAtMarriage")
    var ageAtMarriage: Int? = null,

    @ColumnInfo(name = "phoneNo")
    var phoneNo: String? = null,

    @ColumnInfo(name = "genderID")
    var genderID: Int? = null,

    @ColumnInfo(name = "registrationDate")
    var registrationDate: Date? = null,

    @ColumnInfo(name="stateID")
    var stateID : Int? = null,

    @ColumnInfo(name="districtID")
    var districtID : Int? = null,

    @ColumnInfo(name="blockID")
    var blockID : Int? = null,

    @ColumnInfo(name="districtBranchID")
    var districtBranchID : Int? = null,

    @ColumnInfo(name="communityID")
    var communityID : Int? = null,

    @ColumnInfo(name="religionID")
    var religionID : Int? = null,

    @ColumnInfo(name="parentName")
    var parentName : String? = null,

    @ColumnInfo(name="syncState")
    var syncState: SyncState = SyncState.UNSYNCED,

    @ColumnInfo(name="beneficiaryID")
    var beneficiaryID: Long? = null,

    @ColumnInfo(name="beneficiaryRegID")
    var beneficiaryRegID: Long? = null,
    @Embedded(prefix = "abha_")
    var healthIdDetails: BenHealthIdDetails? = null,

) : Serializable


data class PatientDisplay(
    @Embedded val patient: Patient,
    @Relation(
        parentColumn = "genderID",
        entityColumn = "genderID"
    )
    val gender: GenderMaster,
    @Relation(
        parentColumn = "ageUnitID",
        entityColumn = "id"
    )
    val ageUnit: AgeUnit,
    @Relation(
        parentColumn = "maritalStatusID",
        entityColumn = "maritalStatusID"
    )
    val maritalStatus: MaritalStatusMaster?,
    @Relation(
        parentColumn = "stateID",
        entityColumn = "stateID"
    )
    val state: StateMaster?,
    @Relation(
        parentColumn = "districtID",
        entityColumn = "districtID"
    )
    val district: DistrictMaster?,
    @Relation(
        parentColumn = "blockID",
        entityColumn = "blockID"
    )
    val block: BlockMaster?,
    @Relation(
        parentColumn = "districtBranchID",
        entityColumn = "districtBranchID"
    )
    val village: VillageMaster?,
    @Relation(
        parentColumn = "communityID",
        entityColumn = "communityID"
    )
    val community: CommunityMaster?,
    @Relation(
        parentColumn = "religionID",
        entityColumn = "religionID"
    )
    val religion: ReligionMaster?,
)
data class BenHealthIdDetails(
    var healthId: String? = null,
    var healthIdNumber: String? = null
)
@JsonClass(generateAdapter = true)
data class PatientNetwork(
    val accountNo: String?,
    val ageAtMarriage: Int?,
    val bankName: String?,
    val benImage: String?,
    val benPhoneMaps: List<BenPhone>?,
    val beneficiaryConsent: Boolean?,
    val beneficiaryIdentities: List<String>?,
    val branchName: String?,
    val createdBy: String?,
    val dOB: String?,
    val email: String?,
    val emergencyRegistration: Boolean?,
    val fatherName: String?,
    val firstName: String?,
    val genderID: Int?,
    val genderName: String?,
    val govtIdentityNo: String?,
    val govtIdentityTypeID: Int?,
    val i_bendemographics :Bendemographics?,
    val ifscCode: String?,
    val lastName: String?,
    val literacyStatus: String?,
    val maritalStatusID: Int?,
    val maritalStatusName: String?,
    val motherName: String?,
    val name: String?,
    val parkingPlaceID: Int?,
    val providerServiceMapID: String?,
    val providerServiceMapId: String?,
    val spouseName: String?,
    val titleId: String?,
    val vanID: Int?
){

//    accountNo:null
//    ageAtMarriage:null
//    bankName:null
//    benImage:null
//
//
//    benPhoneMaps: []
//
//    beneficiaryConsent:true
//    beneficiaryIdentities:[]
//    branchName:null
//    createdBy:"Pranathi"
//    dOB:"2023-07-31T18:30:00.000Z"
//    email:null
//    emergencyRegistration:false
//    fatherName:null
//    firstName:"outbreak"
//    genderID:1
//    genderName:"Male"
//    govtIdentityNo:null
//    govtIdentityTypeID:null
//
//    ifscCode:null
//    lastName:"General"
//    literacyStatus:null
//    maritalStatusID:null
//    maritalStatusName:null
//    motherName:null
//    name:null
//    parkingPlaceID:10
//    providerServiceMapID:"13"
//    providerServiceMapId:"13"
//    spouseName:null
//    titleId:null
//    vanID:61

    constructor(patientDisplay: PatientDisplay, user: UserDomain?) : this(
        null,
        patientDisplay.patient.ageAtMarriage,
        null,
        null,
        arrayListOf(BenPhone(patientDisplay.patient, user)),
        true,
        emptyList(),
        null,
        user?.userName,
        DateTimeUtil.formatDateToUTC(patientDisplay.patient.dob!!),
        null,
        false,
        patientDisplay.patient.parentName,
        patientDisplay.patient.firstName,
        patientDisplay.patient.genderID,
        patientDisplay.gender.genderName,
        null,
        null,
        Bendemographics(patientDisplay, user),
        null,
        patientDisplay.patient.lastName,
        null,
        patientDisplay.patient.maritalStatusID,
        patientDisplay.maritalStatus?.status,
        null,
        null,
        user?.parkingPlaceId,
        user?.serviceMapId.toString(),
        user?.serviceMapId.toString(),
        patientDisplay.patient.spouseName,
        null,
        user?.vanId
    )

}

@JsonClass(generateAdapter = true)
data class BenPhone(
    val alternateContactNumber: String?,
    val benRelationshipID: Int?,
    val createdBy: String?,
    val parentBenRegID: Int?,
    val parkingPlaceID: Int?,
    val phoneNo: String?,
    val phoneTypeID: Int?,
    val vanID: Int?
){
//    alternateContactNumber:null
//    benRelationshipID:11
//    createdBy:"Pranathi"
//    parentBenRegID:877
//    parkingPlaceID:10
//    phoneNo:"8989898989"
//    phoneTypeID:1
//    vanID:61
    constructor(patient: Patient, user: UserDomain?) : this(
        null,
        11,
        user?.userName,
        877,
        user?.parkingPlaceId,
        patient.phoneNo,
        1,
        user?.vanId
    )
}

@JsonClass(generateAdapter = true)
data class Bendemographics(
    val addressLine1: String?,
    val addressLine2: String?,
    val addressLine3: String?,
    val blockID: Int?,
    val blockName: String?,
    val communityID: Int?,
    val communityName: String?,
    val countryID: Int?,
    val countryName: String?,
    val districtBranchID: Int?,
    val districtBranchName: String?,
    val districtID: Int?,
    val districtName: String?,
    val educationID: Int?,
    val educationName: String?,
    val habitation: String?,
    val incomeStatusID: Int?,
    val incomeStatusName: String?,
    val occupationID: Int?,
    val occupationName: String?,
    val parkingPlaceID: Int?,
    val pinCode: String?,
    val religionID: Int?,
    val religionName: String?,
    val servicePointID: String?,
    val servicePointName: String?,
    val stateID: Int?
){
    constructor(patientDisplay: PatientDisplay, user: UserDomain?) : this(
        null,
        null,
        null,
        patientDisplay.patient.blockID,
        patientDisplay.block?.blockName,
        patientDisplay.patient.communityID,
        patientDisplay.community?.communityType,
        1,
        "India",
        patientDisplay.patient.districtBranchID,
        patientDisplay.village?.villageName,
        patientDisplay.patient.districtID,
        patientDisplay.district?.districtName,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        user?.parkingPlaceId,
        null,
        patientDisplay.patient.religionID,
        patientDisplay.religion?.religionType,
        user?.servicePointId.toString(),
        user?.servicePointName,
        patientDisplay.patient.stateID
    )
//    addressLine1:null
//    addressLine2:null
//    addressLine3:null
//    blockID:4304
//    blockName:"Pakyong"
//    communityID:null
//    communityName:null
//    countryID:1
//    countryName:"India"
//    districtBranchID:463124
//    districtBranchName:"Amba"
//    districtID:525
//    districtName:"East District"
//    educationID:null
//    educationName:null
//    habitation:null
//    incomeStatusID:null
//    incomeStatusName:null
//    occupationID:null
//    occupationName:null
//    parkingPlaceID:10
//    pinCode:null
//    religionID:null
//    religionName:null
//    servicePointID:"7"
//    servicePointName:"SP1"
//    stateID:31

}


//accountNo:null
//ageAtMarriage:null
//bankName:null
//benImage:null
//benPhoneMaps:
//[{parentBenRegID: 877, phoneNo: "8989898989", alternateContactNumber: null, phoneTypeID: 1,…}]
//0
//:
//{parentBenRegID: 877, phoneNo: "8989898989", alternateContactNumber: null, phoneTypeID: 1,…}
//alternateContactNumber:null
//benRelationshipID:11
//createdBy:"Pranathi"
//parentBenRegID:877
//parkingPlaceID:10
//phoneNo:"8989898989"
//phoneTypeID:1
//vanID:61
//beneficiaryConsent:true
//beneficiaryIdentities:[]
//branchName:null
//createdBy:"Pranathi"
//dOB:"2023-07-31T18:30:00.000Z"
//email:null
//emergencyRegistration:false
//fatherName:null
//firstName:"outbreak"
//genderID:1
//genderName:"Male"
//govtIdentityNo:null
//govtIdentityTypeID:null
//i_bendemographics:
//{incomeStatusID: null, incomeStatusName: null, occupationID: null, occupationName: null,…}
//addressLine1:null
//addressLine2:null
//addressLine3:null
//blockID:4304
//blockName:"Pakyong"
//communityID:null
//communityName:null
//countryID:1
//countryName:"India"
//districtBranchID:463124
//districtBranchName:"Amba"
//districtID:525
//districtName:"East District"
//educationID:null
//educationName:null
//habitation:null
//incomeStatusID:null
//incomeStatusName:null
//occupationID:null
//occupationName:null
//parkingPlaceID:10
//pinCode:null
//religionID:null
//religionName:null
//servicePointID:"7"
//servicePointName:"SP1"
//stateID:31
//ifscCode:null
//lastName:"General"
//literacyStatus:null
//maritalStatusID:null
//maritalStatusName:null
//motherName:null
//name:null
//parkingPlaceID:10
//providerServiceMapID:"13"
//providerServiceMapId:"13"
//spouseName:null
//titleId:null
//vanID:61

