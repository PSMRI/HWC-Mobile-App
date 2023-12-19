package org.piramalswasthya.cho.utils

import org.piramalswasthya.cho.model.FormattedDate
import java.util.Date

fun String?.nullIfEmpty(): String? {
    return if (this.isNullOrEmpty()) null else this
}

fun Date?.formattedDate(): String? {
    if (this == null)
        return null
    else
        return DateTimeUtil.formatDateToUTC(this)
}