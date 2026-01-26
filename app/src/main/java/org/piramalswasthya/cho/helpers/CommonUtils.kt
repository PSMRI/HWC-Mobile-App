package org.piramalswasthya.cho.helpers

import androidx.core.text.isDigitsOnly

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


fun getWeeksOfPregnancy(regLong: Long, lmpLong: Long) =
    (TimeUnit.MILLISECONDS.toDays(regLong - lmpLong) / 7).toInt()

/**
 * Formats gestational age as "X weeks Y days"
 * @param regLong Current date in milliseconds
 * @param lmpLong LMP date in milliseconds
 * @return Formatted string like "12 weeks 3 days" or "0 weeks" if regLong < lmpLong
 */
fun getGestationalAgeFormatted(regLong: Long, lmpLong: Long): String {
    val diff = regLong - lmpLong
    if (diff <= 0) {
        return "0 weeks"
    }
    val totalDays = TimeUnit.MILLISECONDS.toDays(diff).toInt().coerceAtLeast(0)
    val weeks = totalDays / 7
    val days = totalDays % 7
    return if (days == 0) {
        "$weeks weeks"
    } else {
        "$weeks weeks $days days"
    }
}


fun getTodayMillis() = Calendar.getInstance().setToStartOfTheDay().timeInMillis

fun Calendar.setToStartOfTheDay() = apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}


sealed class NetworkResponse<T>(val data: T? = null, val message: String? = null) {

    class Idle<T> : NetworkResponse<T>(null, null)
    class Loading<T> : NetworkResponse<T>(null, null)
    class Success<T>(data: T) : NetworkResponse<T>(data = data)
    class Error<T>(message: String) : NetworkResponse<T>(data = null, message = message)

}

fun getDateString(dateLong: Long?): String? {
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
    dateLong?.let {
        return dateFormat.format(Date(dateLong))
    } ?: run {
        return null
    }
}


