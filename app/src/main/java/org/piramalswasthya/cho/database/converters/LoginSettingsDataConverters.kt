package org.piramalswasthya.cho.database.converters

import android.location.Location
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.piramalswasthya.cho.model.LocationEntity
import org.piramalswasthya.cho.model.LoginSettingsData
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.UserMasterLocation
import org.piramalswasthya.cho.model.VillageLocationData
import org.piramalswasthya.cho.network.District
import org.piramalswasthya.cho.network.DistrictBlock
import org.piramalswasthya.cho.network.State
import org.piramalswasthya.cho.network.Village
import java.util.Date

class LoginSettingsDataConverter {
    private val gson = Gson()

    @TypeConverter
    fun loginSettingsDataToString(loginSettingsData: LoginSettingsData?): String? {
        return gson.toJson(loginSettingsData)
    }

    @TypeConverter
    fun stringToLoginSettingsData(loginSettingsDataString: String?): LoginSettingsData? {
        return gson.fromJson(loginSettingsDataString, LoginSettingsData::class.java)
    }
}
class StateConverter {
    private val gson = Gson()

    @TypeConverter
    fun stateToString(state: State?): String? {
        return gson.toJson(state)
    }

    @TypeConverter
    fun stringToState(stateString: String?): State? {
        return gson.fromJson(stateString, State::class.java)
    }
}

class DistrictConverter {
    private val gson = Gson()

    @TypeConverter
    fun districtToString(district: District?): String? {
        return gson.toJson(district)
    }

    @TypeConverter
    fun stringToDistrict(districtString: String?): District? {
        return gson.fromJson(districtString, District::class.java)
    }
}

class DistrictBlockConverter {
    private val gson = Gson()

    @TypeConverter
    fun districtBlockToString(districtBlock: DistrictBlock?): String? {
        return gson.toJson(districtBlock)
    }

    @TypeConverter
    fun stringToDistrictBlock(districtBlockString: String?): DistrictBlock? {
        return gson.fromJson(districtBlockString, DistrictBlock::class.java)
    }
}

class VillageConverter {
    private val gson = Gson()

    @TypeConverter
    fun villageToString(village: Village?): String? {
        return gson.toJson(village)
    }

    @TypeConverter
    fun stringToVillage(villageString: String?): Village? {
        return gson.fromJson(villageString, Village::class.java)
    }
}

class LocationConverter {
    private val gson = Gson()

    @TypeConverter
    fun locationToString(location: Location?): String? {
        return gson.toJson(location)
    }

    @TypeConverter
    fun stringToLocation(locationString: String?): Location? {
        return gson.fromJson(locationString, Location::class.java)
    }
}

class DateConverter{

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

}
class UserMasterLocationConverter {
    @TypeConverter
    fun toUserMasterLocationList(value: String?): List<VillageLocationData> {
        val listType = object : TypeToken<List<LocationEntity?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromUserMasterLocationList(list: List<VillageLocationData>): String? {
        val gson = Gson()
        return gson.toJson(list)
    }
}