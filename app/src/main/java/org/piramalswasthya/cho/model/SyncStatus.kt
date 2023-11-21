package org.piramalswasthya.cho.model

data class SyncStatusCache(
   val id : Int,
   val name: String,
   val synced: Int,
   val notSynced: Int,
   val syncing: Int,
)


//fun List<SyncStatusCache>.asDomainModel(localNames: Array<String>, englishNames: Array<String>): List<SyncStatusDomain> {
//    return groupBy { it.name }.map { mapEntry ->
//        SyncStatusDomain(
//            name = if (englishNames.contains(mapEntry.key)) localNames[englishNames.indexOf(mapEntry.key)] else mapEntry.key,
//            synced = mapEntry.value.firstOrNull { it.syncState == SyncState.SYNCED }?.count ?: 0,
//            notSynced = mapEntry.value.firstOrNull { it.syncState == SyncState.UNSYNCED }?.count
//                ?: 0,
//            syncing = mapEntry.value.firstOrNull { it.syncState == SyncState.SYNCING }?.count ?: 0
//        )
//    }
//}
//
//data class SyncStatusDomain(
//    val name: String,
//    val synced: Int,
//    val notSynced: Int,
//    val syncing: Int,
//    val totalCount: Int = synced + notSynced + syncing
//)
