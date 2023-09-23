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
    val villageName: String,
)