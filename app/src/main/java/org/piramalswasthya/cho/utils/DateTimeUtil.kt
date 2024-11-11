package org.piramalswasthya.cho.utils

import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import android.widget.DatePicker
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import org.piramalswasthya.cho.model.FormattedDate
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class DateTimeUtil {

    private val nullDate: Date? = null

    val _selectedDate = MutableLiveData(nullDate)

    val selectedDate: MutableLiveData<Date?>
        get() = _selectedDate

    @RequiresApi(Build.VERSION_CODES.O)
    fun showDatePickerDialog(
        context: Context,
        initialDate: Date?,
        maxDays: Int?,
        minDays: Int?
    ): DatePickerDialog {
        val calendar = Calendar.getInstance()
        initialDate?.let {
            calendar.time = it
            _selectedDate.value = it
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            { view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                // This callback is called when the user selects a date
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                _selectedDate.value = calendar.time
            }, year, month, day
        )

        if (maxDays != null) {
            // Set max date to the current date
            val maxCalendar = Calendar.getInstance()
            maxCalendar.add(Calendar.DAY_OF_YEAR, maxDays)
            datePickerDialog.datePicker.maxDate = maxCalendar.timeInMillis
        }

        if (minDays != null) {
            // Set min date to 99 years ago from the current date
            val minCalendar = Calendar.getInstance()
            minCalendar.add(Calendar.DAY_OF_YEAR, minDays)
            datePickerDialog.datePicker.minDate = minCalendar.timeInMillis
        }

        return datePickerDialog
    }


    companion object {

        val ageUnitMap = mapOf(
            AgeUnitEnum.YEARS to "y",
            AgeUnitEnum.MONTHS to "m",
            AgeUnitEnum.WEEKS to "w",
            AgeUnitEnum.DAYS to "d",
        )

        const val format = "yyyy-MM-dd HH:mm:ss"

        fun formatDate(date: Date): String {
            val pattern = "dd-MM-yyyy"
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            return sdf.format(date)
        }

        fun formatActivityDate(inputDateString: String?): String? {
            if (inputDateString == null) {
                return null
            }

            // Parse the input date string
            val inputFormat = SimpleDateFormat("MMM d, yyyy h:mm:ss a", Locale.US)
            val inputDate = inputFormat.parse(inputDateString)

            // Format the date into the desired format
            val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            return outputFormat.format(inputDate)
        }

        fun getFormattedDate(date: Date): FormattedDate {

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            // Format the Date object to a string
            val formattedDate = dateFormat.format(date)

            // Extract day, month, and year from the formatted date string
            val day = formattedDate.substring(0, 2).toInt()
            val month = formattedDate.substring(3, 5).toInt()
            val year = formattedDate.substring(6).toInt()

            return FormattedDate(day, month, year)
        }

        fun getDateTimeStringFromLong(dateLong: Long?): String? {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            dateLong?.let {
                val dateString = dateFormat.format(dateLong)
                val timeString = timeFormat.format(dateLong)
                return "${dateString}T${timeString}.000Z"
            } ?: run {
                return null
            }

        }

        fun convertTimestampToISTDate(timestamp: Timestamp?): Date? {

            if (timestamp == null) {
                return null
            }

            val milliseconds = timestamp.time - (5.5 * 3600000).toLong()

            return Date(milliseconds)

        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun formattedDate(date: Date): String {
            val localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
            val formatter = DateTimeFormatter.ofPattern(format)
            return localDateTime.format(formatter).split(" ")[0]
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun calculateAgeInYears(birthDate: Date): Int {
            val birthLocalDate = birthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            val currentDate = LocalDate.now()
            val age = Period.between(birthLocalDate, currentDate)
            return age.years
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun calculateAge(birthDate: Date): Age {
            val birthLocalDate = birthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            val currentDate = LocalDate.now()

            val period = Period.between(birthLocalDate, currentDate)

            if (period.years > 0) {
                return Age(AgeUnitEnum.YEARS, period.years)
            } else if (period.months > 0) {
                return Age(AgeUnitEnum.MONTHS, period.months)
            } else if (period.days > 7) {
                return Age(AgeUnitEnum.WEEKS, period.days / 7)
            }
            return Age(AgeUnitEnum.DAYS, period.days)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun calculateAgePicker(dateOfBirth: Date): AgePicker {
            val birthLocalDate =
                dateOfBirth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            val currentDate = LocalDate.now()
            val period = Period.between(birthLocalDate, currentDate)
            if (period.days > 6) {
                val weeks = period.days / 7
                val days = period.days % 7
                return AgePicker(period.years, period.months, weeks, days)
            } else {
                return AgePicker(period.years, period.months, 0, period.days)
            }
        }


        @RequiresApi(Build.VERSION_CODES.O)
        fun calculateDateOfBirth(value: Int, unit: AgeUnitEnum): Date {
            val days = when (unit) {
                AgeUnitEnum.DAYS -> value
                AgeUnitEnum.WEEKS -> value * 7
                AgeUnitEnum.MONTHS -> value * 30
                AgeUnitEnum.YEARS -> value * 365
            }

            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -days)
            return calendar.time
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun calculateDateOfBirth(years: Int, months: Int, weeks: Int, days: Int): Date {

//
            val calendar = Calendar.getInstance()
//            calendar.add(Calendar.DAY_OF_YEAR, -days)
            calendar.add(Calendar.YEAR, -years)
            calendar.add(Calendar.MONTH, -months)

            // Subtract weeks and days
            val totalDaysToSubtract = (weeks * 7) + days
            calendar.add(Calendar.DAY_OF_YEAR, -totalDaysToSubtract)
            return calendar.time
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun formatDateToUTC(date: Date): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            return sdf.format(date)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun formatCustDateAndTime(timestamp: Long): String {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
            return Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .format(formatter)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun formatCustDate(timestamp: Long): String {
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            return Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .format(formatter)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun formatUTCToDate(dateString: String): Date? {
            val instant = Instant.parse(dateString)
            val date = Date.from(instant)
            return date
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun formatBenVisitDate(dateString: String?): String? {
            try {
                val inputDateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm:ss a")
                val outputDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val date = inputDateFormat.parse(dateString)
                return outputDateFormat.format(date);
            } catch (e: Exception) {
                return null;
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun formatVisitDate(dateString: String?): Date? {
            try {
                val inputDateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm:ss a")
                return inputDateFormat.parse(dateString);
            } catch (e: Exception) {
                return null;
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun formatVisitDateString(dateString: String?): String? {
            try {
                val inputFormat = SimpleDateFormat("MMM dd, yyyy hh:mm:ss a")
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                val date = inputFormat.parse(dateString)
                return outputFormat.format(date!!)
            } catch (e: Exception) {
                return null;
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun calculateAgeString(dateOfBirth: Date): String {
            val birthDate = dateOfBirth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            val currentDate = LocalDate.now()

            val period = Period.between(birthDate, currentDate)

            val years = period.years
            val months = period.months % 12
            val days = period.days % 30

            var ageString = "";
            if (years > 0) {
                ageString += "$years years"
            } else {
                if (months > 0) {
                    if (ageString.isNotEmpty()) ageString += ", "
                    ageString += "$months months"
                }
                if (days > 0) {
                    if (ageString.isNotEmpty()) ageString += ", "
                    ageString += "$days days"
                }

            }

            return ageString
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun getPatientTypeByAge(dateOfBirth: Date): String {
            val birthDate = dateOfBirth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            val currentDate = LocalDate.now()

            val period = Period.between(birthDate, currentDate)
            val years = period.years
            val months = period.months % 12
            val days = period.days % 30

            var type= ""
            if((days <= 30 || months<=1) && years<1 ){
                type = "new_born_baby"
            }
            if((years<1 && months<=12) || (months==0 && days==0 && years==1)){
                type = "infant"
            }

            if(years>=1&& years<=12){
                type ="child"
            }

            if(years>=12 && years<=18){
                type = "adolescence"
            }

            if(years>=18){
                type = "adult"
            }

            return type
        }

    }

}

data class AgePicker(val years: Int, val months: Int, val weeks: Int, val days: Int)


data class Age(
    val unit: AgeUnitEnum,
    val value: Int
)

enum class AgeUnitEnum {
    YEARS,
    MONTHS,
    WEEKS,
    DAYS
}

