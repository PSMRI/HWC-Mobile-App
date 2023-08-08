package org.piramalswasthya.cho.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "Family_member_Dropdown")
@JsonClass(generateAdapter = true)
data class FamilyMemberDropdown(
    @PrimaryKey
    val benRelationshipID:Int,
    val benRelationshipType:String,
    val gender:String
)
