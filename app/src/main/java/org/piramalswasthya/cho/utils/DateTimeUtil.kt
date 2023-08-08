package org.piramalswasthya.cho.utils

import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

class DateTimeUtil {

    companion object {

        const val format = "yyyy-MM-dd HH:mm:ss"

        fun formattedDate(date: Date): String {
            val localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
            val formatter = DateTimeFormatter.ofPattern(format)
            return localDateTime.format(formatter).split(" ")[0]
        }

        fun calculateAgeInYears(birthDate: Date): Int {
            val birthLocalDate = birthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            val currentDate = LocalDate.now()
            val age = Period.between(birthLocalDate, currentDate)
            return age.years
        }

    }

}