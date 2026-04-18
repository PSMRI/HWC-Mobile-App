package org.piramalswasthya.cho.repositories

import org.json.JSONArray
import org.json.JSONObject

object RepositorySyncUtils {
    fun parseVillageIds(villageIds: String): List<Int> {
        if (villageIds.trim().isEmpty()) return emptyList()
        return villageIds.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
    }

    fun extractDataArray(root: JSONObject): JSONArray {
        return when (val dataNode = root.opt("data")) {
            is JSONArray -> dataNode
            is JSONObject -> dataNode.optJSONArray("data")
            else -> null
        } ?: JSONArray()
    }
}
