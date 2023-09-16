package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "Investigation_Case_Record")
@JsonClass(generateAdapter = true)
data class InvestigationCaseRecord(
    @PrimaryKey
    var investigationCaseRecordId:String,
    @ColumnInfo(name = "testName") val testName: String?,
    @ColumnInfo(name = "externalInvestigation") val externalInvestigation: String?,
    @ColumnInfo(name = "CounsellingTypes") val counsellingTypes:  String?,
    @ColumnInfo(name = "refer") val refer:  String?
)
