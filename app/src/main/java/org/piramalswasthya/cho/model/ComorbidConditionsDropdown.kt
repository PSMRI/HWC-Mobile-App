package org.piramalswasthya.cho.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "Comorbid_Condition_Dropdown")
@JsonClass(generateAdapter = true)
data class ComorbidConditionsDropdown(
    @PrimaryKey
    val comorbidConditionID:Int,
    val comorbidCondition:String
)
