package org.piramalswasthya.cho.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FaceVectorConvertor {

    @TypeConverter
    fun fromString(value: String): List<Float> {
        val listType = object : TypeToken<List<Float>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<Float>): String {
        val gson = Gson()
        return gson.toJson(list)
    }

}