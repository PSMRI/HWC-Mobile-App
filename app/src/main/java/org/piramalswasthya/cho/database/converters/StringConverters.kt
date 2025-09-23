package org.piramalswasthya.cho.database.converters

import androidx.room.TypeConverter

object StringConverters {

    @TypeConverter
    fun fromStringList(value: ArrayList<String>?): String? {
        return value?.joinToString(separator = ",")
    }

    @TypeConverter
    fun toStringList(value: String?): ArrayList<String>? {
        return value?.split(",")?.let { ArrayList(it) }
    }

    @TypeConverter
    fun fromStringListToStore(value: List<String>?): String? {
        return value?.joinToString(separator = ",")
    }

    @TypeConverter
    fun fromStringToRead(value: String?): List<String>? {
        return value?.split(",")?.filter { it.isNotBlank() }
    }
}
