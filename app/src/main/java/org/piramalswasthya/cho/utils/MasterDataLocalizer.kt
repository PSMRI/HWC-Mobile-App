package org.piramalswasthya.cho.utils

import android.content.Context
import android.content.res.Resources
import org.piramalswasthya.cho.R
import java.util.Locale

object MasterDataLocalizer {

    private data class LocaleMaps(
        val forward: Map<String, String>,
        val reverse: Map<String, String>
    )

    private val chiefComplaintCache = mutableMapOf<Locale, LocaleMaps>()
    private val durationUnitCache = mutableMapOf<Locale, LocaleMaps>()

    fun localizeChiefComplaint(context: Context, englishCanonical: String?): String {
        if (englishCanonical.isNullOrEmpty()) return englishCanonical.orEmpty()
        return chiefComplaintMaps(context).forward[englishCanonical] ?: englishCanonical
    }

    fun canonicalizeChiefComplaint(context: Context, displayedText: String?): String? {
        if (displayedText.isNullOrEmpty()) return null
        val maps = chiefComplaintMaps(context)
        if (maps.forward.containsKey(displayedText)) return displayedText
        return maps.reverse[displayedText]
    }

    fun localizeDurationUnit(context: Context, englishCanonical: String?): String {
        if (englishCanonical.isNullOrEmpty()) return englishCanonical.orEmpty()
        return durationUnitMaps(context).forward[englishCanonical] ?: englishCanonical
    }

    fun canonicalizeDurationUnit(context: Context, displayedText: String?): String? {
        if (displayedText.isNullOrEmpty()) return null
        val maps = durationUnitMaps(context)
        if (maps.forward.containsKey(displayedText)) return displayedText
        return maps.reverse[displayedText]
    }

    private fun chiefComplaintMaps(context: Context): LocaleMaps =
        cachedMaps(
            context,
            chiefComplaintCache,
            R.array.chief_complaint_en_keys,
            R.array.chief_complaint_localized
        )

    private fun durationUnitMaps(context: Context): LocaleMaps =
        cachedMaps(
            context,
            durationUnitCache,
            R.array.duration_units_en_keys,
            R.array.duration_units_localized
        )

    private fun cachedMaps(
        context: Context,
        cache: MutableMap<Locale, LocaleMaps>,
        keysArrayId: Int,
        valuesArrayId: Int
    ): LocaleMaps {
        val locale = currentLocale(context.resources)
        cache[locale]?.let { return it }
        val keys = context.resources.getStringArray(keysArrayId)
        val values = context.resources.getStringArray(valuesArrayId)
        val pairs = keys.zip(values).filter { it.first.isNotEmpty() && it.second.isNotEmpty() }
        val forward = pairs.toMap()
        val reverse = pairs.associate { (en, localized) -> localized to en }
        val maps = LocaleMaps(forward, reverse)
        cache[locale] = maps
        return maps
    }

    @Suppress("DEPRECATION")
    private fun currentLocale(resources: Resources): Locale {
        val config = resources.configuration
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            config.locales.get(0) ?: Locale.getDefault()
        } else {
            config.locale ?: Locale.getDefault()
        }
    }
}
