package org.piramalswasthya.sakhi.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.piramalswasthya.cho.model.LocationEntity

object LocationEntityListConverter {

    @TypeConverter
    fun toLocationEntityList(value: String?): List<LocationEntity> {
        val listType = object : TypeToken<List<LocationEntity?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromLocationEntityList(list: List<LocationEntity>): String? {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int> {
        val listType = object : TypeToken<List<Int?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromIntList(list: List<Int>): String? {
        val gson = Gson()
        return gson.toJson(list)
    }
}