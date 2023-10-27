package org.piramalswasthya.cho.model

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import org.piramalswasthya.cho.utils.DateTimeUtil
import java.util.Date

@Entity(
    tableName = "USER",
    foreignKeys = [
        ForeignKey(
            entity = StateMaster::class,
            parentColumns = ["stateID"],
            childColumns = ["stateID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DistrictMaster::class,
            parentColumns = ["districtID"],
            childColumns = ["districtID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BlockMaster::class,
            parentColumns = ["blockID"],
            childColumns = ["blockID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = VillageMaster::class,
            parentColumns = ["districtBranchID"],
            childColumns = ["districtBranchID"],
            onDelete = ForeignKey.CASCADE
        ),
    ]
)
data class UserCache(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: Int,

    @ColumnInfo(name = "username")
    var userName: String,

    @ColumnInfo(name = "Password")
    val password: String,

    @ColumnInfo(name = "name")
    var name: String,

    @ColumnInfo(name = "service_map_id")
    val serviceMapId : Int,

    @ColumnInfo(name = "service_id")
    val serviceId : Int,

    @ColumnInfo(name = "service_point_id")
    val servicePointId : Int,

    @ColumnInfo(name = "service_point_name")
    val servicePointName : String,

    @ColumnInfo(name = "parking_place_id")
    val parkingPlaceId : Int,

    @ColumnInfo(name = "parking_place_name")
    val parkingPlaceName : String,

    @ColumnInfo(name = "zone_id")
    val zoneId : Int,

    @ColumnInfo(name = "facilityID")
    val facilityID: Int?,

    @ColumnInfo(name = "zone_name")
    val zoneName : String,

    @ColumnInfo(name = "van_id")
    val vanId : Int,

    @Embedded(prefix = "country_")
    val country : LocationEntity,

//    val stateIds: List<Int>,
//
//    val districtIds:List<Int>,
//
//    val blockIds:List<Int> ,
//
//    val villageIds:List<Int>,

    val states: List<LocationEntity>,

    val districts: List<LocationEntity>,

    val blocks: List<LocationEntity>,

    val villages: List<LocationEntity>,

    @ColumnInfo(name = "emergency_contact_number")
    val emergencyContactNo: String,

    @ColumnInfo(name = "user_type")
    val userType: String,

    @ColumnInfo(name="logged_in")
    var loggedIn : Boolean,

    @ColumnInfo(name="roles")
    val roles : String,

    @ColumnInfo(name="stateID")
    var stateID : Int?,

    @ColumnInfo(name="districtID")
    var districtID : Int?,

    @ColumnInfo(name="blockID")
    var blockID : Int?,

    @ColumnInfo(name="districtBranchID")
    var districtBranchID : Int?,

    @ColumnInfo(name="masterBlockID")
    var masterBlockID : Int?,

    @ColumnInfo(name="masterVillageID")
    var masterVillageID : Int?,

    @ColumnInfo(name="masterVillageName")
    var masterVillageName : String?,

    @ColumnInfo(name="loginDistance")
    var loginDistance : Int?,

    @ColumnInfo(name="masterLocationAddress")
    var masterLocationAddress : String?,

    @ColumnInfo(name="masterLatitude")
    var masterLatitude : Double?,

    @ColumnInfo(name="masterLongitude")
    var masterLongitude : Double?,

    @ColumnInfo(name="assignVillageIds")
    var assignVillageIds : String,

    @ColumnInfo(name="assignVillageNames")
    var assignVillageNames : String,
    @ColumnInfo(name="lastLogoutTime")
    var lastLogoutTime : Date?,
){
    fun asDomainModel() : UserDomain{
        return UserDomain(
            userId = userId,
            userName = userName,
            password = password,
            name = name,
            serviceMapId = serviceMapId,
            servicePointId = servicePointId,
            serviceId = serviceId,
            servicePointName = servicePointName,
            vanId = vanId,
            facilityID = facilityID,
            zoneId = zoneId,
            zoneName = zoneName,
            parkingPlaceId = parkingPlaceId,
            parkingPlaceName = parkingPlaceName,
            country = country,
            states = states,
            districts = districts,
            blocks = blocks,
            villages = villages,
            stateID = stateID,
            districtID = districtID,
            blockID = blockID,
            districtBranchID = districtBranchID,
            masterVillageID = masterVillageID,
            masterBlockID = masterBlockID,
            masterLocationAddress = masterLocationAddress,
            loginDistance = loginDistance,
            masterLongitude = masterLongitude,
            masterLatitude = masterLatitude,
            masterVillageName = masterVillageName,
            assignVillageIds = assignVillageIds,
//            countryId = countryId,
//            stateIds = stateIds,
//            districtIds = districtIds,
//            blockIds = blockIds,
//            villageIds = villageIds,
//            stateEnglish = stateEnglish,
//            stateHindi = stateHindi,
//            stateAssamese = emptyList(),
//            districtEnglish = districtEnglish,
//            districtHindi = districtHindi,
//            districtAssamese = emptyList(),
//            blockEnglish = blockEnglish,
//            blockHindi = blockHindi,
//            blockAssamese = emptyList(),
//            villageEnglish = villageEnglish,
//            villageHindi = villageHindi,
//            villageAssamese = emptyList(),

            contactNo = emergencyContactNo,
            userType = userType,
            roles = roles,
            loggedIn = loggedIn,
        )
    }
}



data class UserDomain(
    val userId: Int,
    val userName: String,
    val password: String,
    val name: String,
    val serviceMapId: Int,
    val serviceId: Int,
    val servicePointId: Int,
    val servicePointName: String,
    val parkingPlaceId: Int,
    val parkingPlaceName: String,
    val zoneId: Int,
    val zoneName: String,
    val vanId: Int,
    val facilityID : Int?,
    val country: LocationEntity,
    val states : List<LocationEntity>,
    val districts : List<LocationEntity>,
    val blocks : List<LocationEntity>,
    val villages : List<LocationEntity>,
    val stateID : Int?,
    val districtID : Int?,
    val blockID : Int?,
    var districtBranchID : Int? = null,
    var masterVillageID: Int?,
    var masterBlockID : Int? = null,
    var masterVillageName : String? = null,
    var loginDistance : Int? = null,
    var masterLocationAddress: String? = null,
    var masterLatitude : Double? = null,
    var masterLongitude : Double? = null,

//    val stateIds: List<Int>,
//    val districtIds: List<Int>,
//    val blockIds: List<Int>,
//    val villageIds: List<Int>,
//
//    val stateEnglish: List<String>,
//    val stateHindi: List<String>,
//    val stateAssamese: List<String>,
//    val districtEnglish: List<String>,
//    val districtHindi: List<String>,
//    val districtAssamese: List<String>,
//    val blockEnglish: List<String>,
//    val blockHindi: List<String>,
//    val blockAssamese: List<String>,
//    val villageEnglish: List<String>,
//    val villageHindi: List<String>,
//    val villageAssamese: List<String>,
    val contactNo: String,
    val userType: String,
    var loggedIn: Boolean,
    val roles: String,
    val assignVillageIds: String,
)


data class UserNetwork(
    val userId: Int,
    val userName: String,
    val password: String,
    var name: String,
    val roles: String,
    var serviceMapId : Int = -1,
    var serviceId : Int = -1,
    var servicePointId : Int = -1,
    var parkingPlaceId: Int = -1,
    var zoneId: Int = -1,
    var vanId : Int = -1,
    var facilityID:Int=-1,
    var parkingPlaceName: String?=null,
    var servicePointName: String?=null,
    var zoneName : String?=null,
    var stateId : Int? = null,
    var districtID : Int? = null,
    var blockID : Int? = null,
    var districtBranchID : Int? = null,
    var masterVillageID: Int? = null,
    var masterBlockID : Int? = null,
    var masterVillageName : String? = null,
    var loginDistance : Int? = null,
    var masterLocationAddress: String? = null,
    var masterLatitude : Double? = null,
    var masterLongitude : Double? = null,
    var assignVillageIds: String? = null,
    var assignVillageNames: String? = null,

    var country : LocationEntity? = null,

    var states : MutableList<LocationEntity> = mutableListOf(),
    var districts : MutableList<LocationEntity> = mutableListOf(),
    var blocks : MutableList<LocationEntity> = mutableListOf(),
    var villages : MutableList<LocationEntity> = mutableListOf(),

//    var stateID: Int = -1,
//    var districtID: Int = -1,
//    var blockID: Int = -1,
//    var districtBranchID: Int = -1,


//    var stateIds: MutableList<Int> = mutableListOf(),
//    var stateEnglish: MutableList<String> = mutableListOf(),
//    var stateHindi: MutableList<String> = mutableListOf(),
//
//    var districtIds:MutableList<Int> = mutableListOf(),
//    var districtEnglish:MutableList<String> = mutableListOf(),
//    var districtHindi:MutableList<String> = mutableListOf(),
//
//    var blockIds:MutableList<Int> = mutableListOf(),
//    var blockEnglish:MutableList<String> = mutableListOf(),
//    var blockHindi:MutableList<String> = mutableListOf(),
//
//    var villageIds:MutableList<Int> = mutableListOf(),
//    var villageEnglish:MutableList<String> = mutableListOf(),
//    var villageHindi:MutableList<String> = mutableListOf(),

//    var countryId : Int = -1,

    var emergencyContactNo: String? = null,
    var userType: String? = null,
    var loggedIn : Boolean? = false,
    var lastLogoutTime: Date? = null
) {
    fun asCacheModel() : UserCache{
        return UserCache(
            userId = userId,
            userName = userName,
            password = password,
            name = name,
            serviceMapId = serviceMapId,
            servicePointId = servicePointId,
            serviceId = serviceId,
            servicePointName = servicePointName?:"",
            vanId = vanId,
            zoneId = zoneId,
            facilityID = facilityID,
            zoneName = zoneName?:"",
            parkingPlaceId = parkingPlaceId,
            parkingPlaceName = parkingPlaceName?:"",
            country = country?:LocationEntity(1,"India"),
            states = states,
            districts = districts,
            blocks = blocks,
            villages = villages,


//            countryId = countryId,
//
//            stateIds = stateIds,
//            districtIds = districtIds,
//            blockIds = blockIds,
//            villageIds = villageIds,
//
//            stateEnglish = stateEnglish,
//            stateHindi = stateHindi,
//            districtEnglish = districtEnglish,
//            districtHindi = districtHindi,
//            blockEnglish = blockEnglish,
//            blockHindi = blockHindi,
//            villageEnglish = villageEnglish,
//            villageHindi = villageHindi,

            emergencyContactNo = emergencyContactNo?:"",
            userType = userType?:"",
            loggedIn = loggedIn?:false,
            roles = roles,
            stateID = stateId,
            districtID = districtID,
            blockID = blockID,
            districtBranchID = districtBranchID,
            masterLocationAddress = masterLocationAddress,
            loginDistance = loginDistance,
            masterLongitude = masterLongitude,
            masterLatitude = masterLatitude,
            masterVillageName = masterVillageName,
            assignVillageIds = assignVillageIds ?: "",
            assignVillageNames = assignVillageNames ?: "",
            lastLogoutTime = lastLogoutTime,
            masterVillageID = masterVillageID,
            masterBlockID = masterBlockID,
        )
    }
}

data class UserMasterVillage(
    val masterVillageID: Int?,
    val userID: Int?,
)
data class MasterLocationModel(
    val latitude : Double?,
    val longitude : Double?,
    var address : String? = null,
    val districtBranchID: Int?,
)

data class NetworkBody(
    val userName: String,
    val password : String,
    val salt : String,
    val Source : String
)

data class ModelObject(
    val success : Boolean,
    val msgCode : Int,
    val message : String,
    val model : Model,
    val lstModel: String?,
    val token : String?,
    val totalRecords: Int,
    val msgType : String?
)

data class Model(
    val id : String,
    val referenceId : String,
    val token : String
)


data class EsanjeevniObject(
    val success : Boolean,
    val msgCode : Int?,
    val message : String,
    val model : EsanjeevniModel?,
    val lstModel: String?,
    val token : String?,
    val totalRecords: Int?,
    val msgType : String?
)

data class EsanjeevniModel(
    val access_token : String,
    val expires_in : Int,
    val token_type : String,
    val refresh_token : String,
    val scope : String,
    val roleId : Int
)

@JsonClass(generateAdapter = true)
data class EsanjeevniPatient(
    val abhaAddress: String,
    val abhaNumber: String,
    val age: Int,
    val birthdate: String,
    val displayName: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val genderCode: Int,
    val genderDisplay: String,
    val isBlock: Boolean,
    val lstPatientAddress: List<EsanjeevniPatientAddress>?,
    val lstPatientContactDetail: List<EsanjeevniPatientContactDetails>,
    val source: String
){

    @RequiresApi(Build.VERSION_CODES.O)
    constructor(patientDisp: PatientDisplay) : this(
        "",
        "",
        DateTimeUtil.calculateAgeInYears(patientDisp.patient.dob!!),
        DateTimeUtil.formattedDate(patientDisp.patient.dob!!),
        patientDisp.patient.firstName!!+" "+patientDisp.patient.lastName!!,
        patientDisp.patient.firstName!!,
    "",
        patientDisp.patient.lastName!!,
    1,
    patientDisp.gender.genderName,
    false,
    arrayListOf(), //sending address as null for now
    arrayListOf(EsanjeevniPatientContactDetails(patientDisp)),
    "11001"
    )
}

@JsonClass(generateAdapter = true)
data class EsanjeevniPatientAddress(
    val addressLine1: String,
    val addressType: String,
    val addressUse: String,
    val blockCode: Int?,
    val blockDisplay: String,
    val cityCode: Int,
    val cityDisplay: String,
    val countryCode: Int?,
    val countryDisplay: String,
    val districtCode: Int?,
    val districtDisplay: String?,
    val postalCode: String,
    val stateCode: Int?,
    val stateDisplay: String,
){
    constructor(patientDisp: PatientDisplay) : this(
        "",
        "",
        "",
        patientDisp.block?.govLGDSubDistrictID,
        patientDisp.block?.blockName!!,
    1,
    "",
        null,
    "India",
        patientDisp.district?.govtLGDDistrictID,
    patientDisp.district?.districtName,
//    patientDisp.district?.districtName!!,
    "",
    patientDisp.state?.govtLGDStateID,
    patientDisp.state?.stateName!!
    )
}

@JsonClass(generateAdapter = true)
data class EsanjeevniPatientContactDetails(
    val contactPointStatus : Boolean,
    val contactPointType : String,
    val contactPointUse : String,
    val contactPointValue : String,
){
    constructor(patientDisp: PatientDisplay) : this(
        true,
        "Phone",
        "Work",
        patientDisp.patient.phoneNo!!
    )
}
