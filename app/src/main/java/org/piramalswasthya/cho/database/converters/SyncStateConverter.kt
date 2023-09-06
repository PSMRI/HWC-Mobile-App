package org.piramalswasthya.cho.database.converters

import androidx.room.TypeConverter
import org.piramalswasthya.cho.database.room.SyncState

object SyncStateConverter {

    @TypeConverter
    fun toInt(value: SyncState): Int {
        return value.ordinal
    }

    @TypeConverter
    fun fromInt(value : Int): SyncState {
        return SyncState.values()[value]
    }

}
