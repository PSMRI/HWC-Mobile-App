package org.piramalswasthya.cho.utils

fun String?.nullIfEmpty(): String? {
    return if (this.isNullOrEmpty()) null else this
}