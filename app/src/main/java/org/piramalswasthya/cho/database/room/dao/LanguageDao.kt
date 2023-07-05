package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.piramalswasthya.cho.model.Language

@Dao
interface LanguageDao {
    @Query("SELECT * FROM languages")
    suspend fun getAllLanguages(): List<Language>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLanguages(language: Language)
}
