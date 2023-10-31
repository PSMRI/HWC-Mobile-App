package org.piramalswasthya.cho.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import org.piramalswasthya.cho.helpers.Languages
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

object HelperUtil {

    private val dateFormat = SimpleDateFormat("EEE, MMM dd yyyy", Locale.getDefault())

    fun getLocalizedResources(context: Context, currentLanguage: Languages): Resources {
        val desiredLocale = Locale(currentLanguage.symbol)
        var conf = context.resources.configuration
        conf = Configuration(conf)
        conf.setLocale(desiredLocale)
        val localizedContext: Context = context.createConfigurationContext(conf)
        return localizedContext.resources
    }

    fun getLocalizedContext(context: Context, currentLanguage: Languages): Context {
        val desiredLocale = Locale(currentLanguage.symbol)
        Locale.setDefault(desiredLocale)
        var conf = context.resources.configuration
        conf = Configuration(conf)
        conf.setLocale(desiredLocale)
        return context.createConfigurationContext(conf)
    }

    fun formatNumber(number: Int, languages: Languages): Int {
        val locale = Locale(languages.symbol)
        val numberFormatter = NumberFormat.getInstance(locale)
        return numberFormatter.format(number).replace(",","").toInt()
    }

    fun getDateStringFromLong(dateLong: Long?): String? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        dateLong?.let {
            val dateString = dateFormat.format(dateLong)
            return dateString
        } ?: run {
            return null
        }

    }


    fun getTrackDate(long: Long?): String? {
        long?.let {
            return " on ${dateFormat.format(long)}"
        }
        return null
    }
}