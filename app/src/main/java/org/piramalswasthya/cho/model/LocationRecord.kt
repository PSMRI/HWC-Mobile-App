package org.piramalswasthya.cho.model

import androidx.room.Embedded

data class LocationRecord(
    @Embedded(prefix = "country_")
    val country : LocationEntity,
    @Embedded(prefix = "state_")
    val state : LocationEntity,
    @Embedded(prefix = "district_")
    val district : LocationEntity,
    @Embedded(prefix = "block_")
    val block :  LocationEntity,
    @Embedded(prefix = "village_")
    val village : LocationEntity,
) : java.io.Serializable

data class LocationEntity(
    val id : Int,
    val name : String,
    val nameHindi : String? = null,
    val nameAssamese : String? = null
) : java.io.Serializable

data class UserVanSpDetails(
    val ID : Int,
    val userID : Int,
    val vanID : Int,
    val vanNoAndType : String,
    val vanSession : Int,
    val servicePointID : Int,
    val servicePointName : String,
    val parkingPlaceID : Int,
    val facilityID : Int
)

data class UserStateDetails(
    val stateID : Int,
    val stateName : String
)

data class UserDistrictDetails(
    val districtID : Int,
    val districtName : String
)

data class UserBlockDetails(
    val blockID : Int,
    val blockName : String
)

data class UserVillageDetails(
    val districtBranchID : Int,
    val villageName : String
)

data class UserVanSpDetailsData(
    val userVanSpDetails: List<UserVanSpDetails>
)

data class UserStateDetailsData(
    val userStateDetails: List<UserStateDetails>
)

data class UserDistrictDetailsData(
    val userDistrictDetails: List<UserDistrictDetails>
)

data class UserBlockDetailsData(
    val userBlockDetails: List<UserBlockDetails>
)

data class UserVillageDetailsData(
    val userVillageDetails: List<UserVillageDetails>
)

data class LocationData(
    val stateId: Int,
    val stateName: String,
    val districtId: Int,
    val districtName: String,
    val blockId: Int,
    val blockName: String,
    val villageList: List<VillageLocationData>

)
data class VillageLocationData(
    val districtBranchID: String,
    val villageName: String?,
)