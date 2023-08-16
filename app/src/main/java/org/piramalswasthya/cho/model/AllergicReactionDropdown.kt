package org.piramalswasthya.cho.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "Allergic_Reaction_Dropdown")
@JsonClass(generateAdapter = true)
data class AllergicReactionDropdown (
    @PrimaryKey
    val allergicReactionTypeID:Int,
    val name:String,
    val deleted:Boolean,
    val processed:String,
    val createdBy:String,
    val createdDate:String,
    val lastModDate:String
    )