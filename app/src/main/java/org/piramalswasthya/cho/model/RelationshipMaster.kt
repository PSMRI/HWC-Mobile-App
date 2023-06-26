package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass



@Entity(tableName = "RELATIONSHIP_MASTER")
@JsonClass(generateAdapter = true)
data class RelationshipMaster (
    @PrimaryKey val benRelationshipID: Int,
    @ColumnInfo(name = "benRelationship_Type") val benRelationshipType: String,
    @ColumnInfo(name = "gender") val gender: String
)