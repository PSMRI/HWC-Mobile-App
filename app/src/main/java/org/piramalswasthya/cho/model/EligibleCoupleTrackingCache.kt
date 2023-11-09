package org.piramalswasthya.cho.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.squareup.moshi.JsonClass
import org.piramalswasthya.cho.configuration.FormDataModel
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.utils.DateTimeUtil.Companion.getDateTimeStringFromLong
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Entity(
    tableName = "ELIGIBLE_COUPLE_TRACKING",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = arrayOf("patientID"/* "householdId"*/),
        childColumns = arrayOf("patientID" /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_ect", value = ["patientID"/* "hhId"*/])]
)

data class EligibleCoupleTrackingCache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val patientID: String,
    var visitDate: Long = System.currentTimeMillis(),
    var isPregnancyTestDone: String? = null,
    var pregnancyTestResult: String? = null,
    var isPregnant: String? = null,
    var usingFamilyPlanning: Boolean? = null,
    var methodOfContraception: String? = null,
    val createdBy: String,
    val createdDate: Long = System.currentTimeMillis(),
    val updatedBy: String,
    val updatedDate: Long = System.currentTimeMillis(),
    var processed: String? = "N",
    var isActive: Boolean = true,
    var syncState: SyncState
) : FormDataModel {

    companion object {
        private val dateFormat = SimpleDateFormat("EEE, MMM dd yyyy", Locale.getDefault())

        fun getECTFilledDateFromLong(long: Long): String {
            return "Visited on ${dateFormat.format(long)}"
        }
    }

    fun asNetworkModel(benId: Long): ECTNetwork {
        return ECTNetwork(
            benId = benId,
            visitDate = getDateTimeStringFromLong(visitDate)!!,
            isPregnancyTestDone = isPregnancyTestDone,
            pregnancyTestResult = pregnancyTestResult,
            isPregnant = isPregnant,
            usingFamilyPlanning = usingFamilyPlanning,
            methodOfContraception = methodOfContraception,
            isActive = isActive,
            createdBy = createdBy,
            createdDate = getDateTimeStringFromLong(createdDate)!!,
            updatedBy = updatedBy,
            updatedDate = getDateTimeStringFromLong(updatedDate)!!,
        )
    }
}

@JsonClass(generateAdapter = true)
data class ECTNetwork(
    val benId: Long,
    val visitDate: String,
    val isPregnancyTestDone: String?,
    val pregnancyTestResult: String?,
    val isPregnant: String?,
    val usingFamilyPlanning: Boolean?,
    val methodOfContraception: String?,
    var isActive: Boolean?,
    val createdBy: String,
    val createdDate: String,
    val updatedBy: String,
    val updatedDate: String,
)

//data class BenWithEcTrackingCache(
////    @ColumnInfo(name = "benId")
////    val ecBenId: Long,
//
//    @Embedded
//    val ben: BenBasicCache,
//    @Relation(
//        parentColumn = "benId", entityColumn = "benId"
//    )
//    val ecr: EligibleCoupleRegCache,
//
//    @Relation(
//        parentColumn = "benId", entityColumn = "benId", entity = EligibleCoupleTrackingCache::class
//    )
//    val savedECTRecords: List<EligibleCoupleTrackingCache>
//) {
//
//    companion object {
//        private val dateFormat = SimpleDateFormat("EEE, MMM dd yyyy", Locale.getDefault())
//
//        private fun getECTFilledDateFromLong(long: Long): String {
//            return "Visited on ${dateFormat.format(long)}"
//        }
//    }
//
//    fun asDomainModel(): BenWithEctListDomain {
//        val recentFill = savedECTRecords.maxByOrNull { it.visitDate }
//        val allowFill = recentFill?.let {
//            val cal = Calendar.getInstance()
//            val currentMonth = cal.get(Calendar.MONTH)
//            val currentYear = cal.get(Calendar.YEAR)
//            cal.apply { timeInMillis = recentFill.visitDate }
//            val lastVisitMonth = cal.get(Calendar.MONTH)
//            val lastVisitYear = cal.get(Calendar.YEAR)
//            !(currentYear==lastVisitYear && currentMonth == lastVisitMonth)
//        } ?: true
//        return BenWithEctListDomain(
////            ecBenId,
//            ben.asBasicDomainModel(),
//            ecr.noOfLiveChildren.toString(),
//            allowFill,
//            savedECTRecords.map {
//                ECTDomain(
//                    it.benId,
//                    it.createdDate,
//                    it.visitDate,
//                    getECTFilledDateFromLong(it.visitDate),
//                    it.syncState
//                )
//            }
//        )
//    }
//}
//
//data class ECTDomain(
//    val benId: Long,
//    val created: Long,
//    val visited: Long,
//    val filledOnString: String,
//    val syncState: SyncState
//)
//
//data class BenWithEctListDomain(
////    val benId: Long,
//    val ben: BenBasicDomain,
//    val numChildren: String,
//    val allowFill: Boolean,
//    val savedECTRecords: List<ECTDomain>,
//    val allSynced: SyncState? = if (savedECTRecords.isEmpty()) null else
//        if (savedECTRecords.map { it.syncState }
//                .all { it == SyncState.SYNCED }) SyncState.SYNCED else SyncState.UNSYNCED
//
//)