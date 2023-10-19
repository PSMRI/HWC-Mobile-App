package org.piramalswasthya.cho.model

import android.location.Location
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.piramalswasthya.cho.network.District
import org.piramalswasthya.cho.network.DistrictBlock
import org.piramalswasthya.cho.network.State
import org.piramalswasthya.cho.network.Village

@Entity(tableName = "login_settings_data", primaryKeys = ["username"])
data class LoginSettingsData(
    @ColumnInfo(name = "state")
    val state: State?,
    @ColumnInfo(name = "district")
    val district: District?,
    @ColumnInfo(name = "district_block")
    val districtBlock: DistrictBlock?,
    @ColumnInfo(name = "street")
    val street: Village?,
    @ColumnInfo(name = "longitude")
    val longitude: Double,
    @ColumnInfo(name = "latitude")
    val latitude: Double,
    @ColumnInfo(name = "location")
    val location: Location,
    @ColumnInfo(name = "username")
    val username: String
)


data class LocationRequest(val vanID: Int, val spPSMID: String, val userID: Int)