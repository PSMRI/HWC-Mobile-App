package org.piramalswasthya.cho.utils

import java.util.UUID

fun generateIntFromUuid(): Int {
    val uuid = UUID.randomUUID()
    val hashCode = uuid.toString().hashCode()
    val positiveHashCode = Math.abs(hashCode)
    return positiveHashCode % 90000 + 10000 // To ensure it's a 5-digit number (between 10000 and 99999)
}
