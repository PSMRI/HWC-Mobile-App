package org.piramalswasthya.cho.utils

import java.util.UUID

fun generateUuid(): String {
    return UUID.randomUUID().toString()
}