package org.piramalswasthya.cho.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.squareup.moshi.JsonClass
import java.io.Serializable
import java.util.Date

@Entity(
    tableName = "PATIENT",
    foreignKeys = [
        ForeignKey(
            entity = AgeUnit::class,
            parentColumns = ["id"],
            childColumns = ["ageUnitID"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = MaritalStatusMaster::class,
            parentColumns = ["maritalStatusID"],
            childColumns = ["maritalStatusID"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = GenderMaster::class,
            parentColumns = ["genderID"],
            childColumns = ["genderID"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = StateMaster::class,
            parentColumns = ["stateID"],
            childColumns = ["stateID"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = DistrictMaster::class,
            parentColumns = ["districtID"],
            childColumns = ["districtID"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = BlockMaster::class,
            parentColumns = ["blockID"],
            childColumns = ["blockID"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = VillageMaster::class,
            parentColumns = ["districtBranchID"],
            childColumns = ["districtBranchID"],
            onDelete = ForeignKey.NO_ACTION
        ),
    ]
)
@JsonClass(generateAdapter = true)
data class Patient (

    @PrimaryKey
    @NonNull
    var patientID: String = "",

    @ColumnInfo(name = "firstName")
    var firstName: String? = null,

    @ColumnInfo(name = "lastName")
    var lastName: String? = null,

    @ColumnInfo(name = "dob")
    var dob: Date? = null,

    @ColumnInfo(name = "age")
    var age: Int? = null,

    @ColumnInfo(name = "ageUnitID")
    var ageUnitID: Int? = null,

    @ColumnInfo(name = "maritalStatusID")
    var maritalStatusID: Int? = null,

    @ColumnInfo(name = "spouseName")
    var spouseName: String? = null,

    @ColumnInfo(name = "ageAtMarriage")
    var ageAtMarriage: Int? = null,

    @ColumnInfo(name = "phoneNo")
    var phoneNo: String? = null,

    @ColumnInfo(name = "genderID")
    var genderID: Int? = null,

    @ColumnInfo(name = "registrationDate")
    var registrationDate: Date? = null,

    @ColumnInfo(name="stateID")
    var stateID : Int? = null,

    @ColumnInfo(name="districtID")
    var districtID : Int? = null,

    @ColumnInfo(name="blockID")
    var blockID : Int? = null,

    @ColumnInfo(name="districtBranchID")
    var districtBranchID : Int? = null,

) : Serializable


data class PatientDisplay(
    @Embedded val patient: Patient,
    @Relation(
        parentColumn = "genderID",
        entityColumn = "genderID"
    )
    val gender: GenderMaster,
    @Relation(
        parentColumn = "ageUnitID",
        entityColumn = "id"
    )
    val ageUnit: AgeUnit,
    @Relation(
        parentColumn = "maritalStatusID",
        entityColumn = "maritalStatusID"
    )
    val maritalStatus: MaritalStatusMaster,
)