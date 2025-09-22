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
}
