package org.piramalswasthya.cho.utils

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateJsonAdapter {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)

    @ToJson
    fun toJson(date: Date?): String {
        return dateFormat.format(date)
    }

    @FromJson
    fun fromJson(dateString: String): Date? {
        return dateFormat.parse(dateString)
    }
}