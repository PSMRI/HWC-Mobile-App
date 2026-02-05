package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import org.piramalswasthya.cho.model.AshaDueListCache

@Dao
fun interface AshaDueListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: AshaDueListCache): Long
}
