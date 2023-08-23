package org.piramalswasthya.cho.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.util.Date

@Entity(
    tableName = "PATIENT",
    foreignKeys = [
        ForeignKey(
            entity = AgeUnit::class,
            parentColumns = ["id"],
            childColumns = ["ageUnitID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MaritalStatusMaster::class,
            parentColumns = ["maritalStatusID"],
            childColumns = ["maritalStatusID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = GenderMaster::class,
            parentColumns = ["genderID"],
            childColumns = ["genderID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StateMaster::class,
            parentColumns = ["stateID"],
            childColumns = ["stateID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DistrictMaster::class,
            parentColumns = ["districtID"],
            childColumns = ["districtID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BlockMaster::class,
            parentColumns = ["blockID"],
            childColumns = ["blockID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = VillageMaster::class,
            parentColumns = ["districtBranchID"],
            childColumns = ["districtBranchID"],
            onDelete = ForeignKey.CASCADE
        ),
    ]
)
@JsonClass(generateAdapter = true)
data class Patient (
    @PrimaryKey
    val patientID: String?,

    @ColumnInfo(name = "firstName")
    val firstName: String?,

    @ColumnInfo(name = "lastName")
    val lastName: String?,

    @ColumnInfo(name = "dob")
    val dob: Date?,

    @ColumnInfo(name = "age")
    val age: Int?,

    @ColumnInfo(name = "ageUnitID")
    val ageUnitID: Int?,

    @ColumnInfo(name = "maritalStatusID")
    val maritalStatusID: Int?,

    @ColumnInfo(name = "spouseName")
    val spouseName: String?,

    @ColumnInfo(name = "ageAtMarriage")
    val ageAtMarriage: Int?,

    @ColumnInfo(name = "phoneNo")
    val phoneNo: String?,

    @ColumnInfo(name = "genderID")
    val genderID: Int?,

    @ColumnInfo(name = "registrationDate")
    val registrationDate: Date?,

    @ColumnInfo(name="stateID")
    var stateID : Int?,

    @ColumnInfo(name="districtID")
    var districtID : Int?,

    @ColumnInfo(name="blockID")
    var blockID : Int?,

    @ColumnInfo(name="districtBranchID")
    var districtBranchID : Int?,

)