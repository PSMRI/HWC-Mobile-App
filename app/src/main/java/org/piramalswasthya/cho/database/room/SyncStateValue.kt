package org.piramalswasthya.cho.database.room

/**
 * Integer values for tables that persist sync state as Int columns.
 * Keep these aligned with SyncState enum ordinal positions.
 */
object SyncStateValue {
    const val UNSYNCED = 0
    const val SYNCING = 1
    const val SYNCED = 2
}
