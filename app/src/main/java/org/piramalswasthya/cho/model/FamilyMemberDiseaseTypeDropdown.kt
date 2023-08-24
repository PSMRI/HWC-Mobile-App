package org.piramalswasthya.cho.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "Family_Member_Disease_Type_Dropdown")
@JsonClass(generateAdapter = true)
data class FamilyMemberDiseaseTypeDropdown(
    @PrimaryKey
    val diseaseTypeID:Int,
    val diseaseType:String,
    val snomedCode:String?,
    val snomedTerm:String?
)
