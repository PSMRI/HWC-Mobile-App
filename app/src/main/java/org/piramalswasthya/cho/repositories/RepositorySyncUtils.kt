package org.piramalswasthya.cho.repositories

object RepositorySyncUtils {
    fun parseVillageIds(villageIds: String): List<Int> {
        if (villageIds.trim().isEmpty()) return emptyList()
        return villageIds.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
    }
}
