package org.piramalswasthya.cho.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
//import org.piramalswasthya.sakhi.configuration.FormDataModel
//import org.piramalswasthya.sakhi.database.room.SyncState
//import org.piramalswasthya.sakhi.network.getLongFromDate

enum class ChildImmunizationCategory {
    BIRTH, WEEK_6, WEEK_10, WEEK_14, MONTH_9_12, MONTH_16_24, YEAR_5_6, YEAR_10, YEAR_16, CATCH_UP
}

enum class ImmunizationCategory {
    CHILD,
    MOTHER
}

//@Entity(tableName = "VACCINE")
//data class Vaccine(
//    @PrimaryKey
//    val id: Int,
//    val name: String,
//    val minAllowedAgeInMillis : Long,
//    val maxAllowedAgeInMillis : Long,
//    val category: ImmunizationCategory,
//    val childCategory: ChildImmunizationCategory,
////    val dueDuration: Long,
//    val overdueDurationSinceMinInMillis: Long = maxAllowedAgeInMillis,
//    val dependantVaccineId: Int? = null,
//    val dependantCoolDuration: Long? = null,
//)
//
//@Entity(
//    tableName = "IMMUNIZATION", primaryKeys = ["beneficiaryId", "vaccineId"], foreignKeys = [ForeignKey(
//        entity = BenRegCache::class,
//        parentColumns = arrayOf("beneficiaryId"),
//        childColumns = arrayOf("beneficiaryId"),
//        onUpdate = ForeignKey.CASCADE,
//        onDelete = ForeignKey.CASCADE
//    ), ForeignKey(
//        entity = Vaccine::class,
//        parentColumns = arrayOf("id"),
//        childColumns = arrayOf("vaccineId"),
//        onDelete = ForeignKey.CASCADE
//    )], indices = [Index(
//        name = "ind_imm", value = ["beneficiaryId"]
//    ), Index(name = "ind_vaccine", value = ["vaccineId"])]
//)
//data class ImmunizationCache(
//    val id: Long = 0,
//    val beneficiaryId: Long,
//    var vaccineId: Int,
//    var date: Long? = null,
//    var placeId: Int=0,
//    var place: String="",
//    var byWhoId: Int=0,
//    var byWho: String="",
//    var processed: String? = "N",
//    var createdBy: String,
//    var createdDate: Long = System.currentTimeMillis(),
//    var updatedBy: String,
//    val updatedDate: Long = System.currentTimeMillis(),
//    var syncState: SyncState
//) : FormDataModel {
//    fun asPostModel(): ImmunizationPost {
//        return ImmunizationPost(
//            id = id,
//            beneficiaryId = beneficiaryId,
//            vaccineName = "",
//            receivedDate = getDateStrFromLong(date),
//            vaccinationreceivedat = place,
//            vaccinatedBy = byWho,
//            createdDate = getDateStrFromLong(createdDate),
//            createdBy = createdBy,
//            modifiedBy = updatedBy,
//            lastModDate = getDateStrFromLong(updatedDate)
//        )
//    }
//}
//
//data class ChildImmunizationDetailsCache(
////    @ColumnInfo(name = "benId")
//    @Embedded
//    val ben: BenBasicCache,
////    @ColumnInfo(name = "benName") val benName : String,
//    @Relation(
//        parentColumn = "benId", entityColumn = "beneficiaryId"
//    ) val givenVaccines: List<ImmunizationCache>
//)
//
//data class MotherImmunizationDetailsCache(
////    @ColumnInfo(name = "benId")
//    @Embedded
//    val ben: BenBasicCache,
//
//    val lmp : Long,
////    @ColumnInfo(name = "benName") val benName : String,
//    @Relation(
//        parentColumn = "benId", entityColumn = "beneficiaryId"
//    ) val givenVaccines: List<ImmunizationCache>
//)
//
//data class ImmunizationDetailsDomain(
//    val ben : BenBasicDomain,
//    val vaccineStateList: List<VaccineDomain>,
////    val onClick: (Long, Int) -> Unit
//)
//
//data class VaccineCategoryDomain(
//    val category : ChildImmunizationCategory,
//    val categoryString : String = category.name,
//    val vaccineStateList: List<VaccineDomain>,
////    val onClick: (Long, Int) -> Unit
//)
//data class VaccineDomain(
////    val benId: Long,
//    val vaccineId: Int,
//    val vaccineName : String,
//    val vaccineCategory : ChildImmunizationCategory,
//    val state: VaccineState,
//)
//
//class VaccineClickListener(private val clickListener: (benId: Long, vaccineId: Int) -> Unit) {
//    fun onClick(benId: Long, vaccine: VaccineDomain) = clickListener(benId, vaccine.vaccineId)
//}
//
//data class ImmunizationDetailsHeader(
//    val list: List<String>
//)


enum class VaccineState {
    PENDING, OVERDUE, DONE, MISSED, UNAVAILABLE
}

//data class ImmunizationPost (
//    val id: Long = 0,
//    val beneficiaryId: Long,
//    var vaccineName: String = "",
//    val receivedDate: String? = null,
//    val vaccinationreceivedat: String? = null,
//    val vaccinatedBy: String? = null,
//    val createdDate: String? = null,
//    val createdBy: String,
//    var lastModDate: String? = null,
//    var modifiedBy: String
//) {
//    fun toCacheModel(): ImmunizationCache {
//        return ImmunizationCache(
//            id = id,
//            beneficiaryId = beneficiaryId,
//            vaccineId = 0,
//            date = getLongFromDate(receivedDate),
////            placeId = 0,
//            place = if(vaccinationreceivedat.isNullOrEmpty()) "" else vaccinationreceivedat,
////            byWhoId = 0,
//            byWho = if(vaccinatedBy.isNullOrEmpty()) "" else vaccinatedBy,
//            processed = "P",
//            createdBy = createdBy,
//            createdDate = getLongFromDate(createdDate),
//            updatedBy = modifiedBy,
//            updatedDate = getLongFromDate(lastModDate),
//            syncState = SyncState.SYNCED
//        )
//    }
//}