package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "languages")
@JsonClass(generateAdapter = true)
data class Language(
    @PrimaryKey val languageID: String,
    @ColumnInfo(name = "language_name") val languageName: String
)