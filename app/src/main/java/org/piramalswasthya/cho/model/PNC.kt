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
import org.piramalswasthya.cho.network.getLongFromDate
import org.piramalswasthya.cho.utils.DateTimeUtil.Companion.getDateTimeStringFromLong
import java.util.Calendar
import java.util.concurrent.TimeUnit

@Entity(
    tableName = "PNC_VISIT",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = arrayOf("patientID"/* "householdId"*/),
        childColumns = arrayOf("patientID" /*"hhId"*/),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(name = "ind_pnc", value = ["patientID"/* "hhId"*/])],
)
data class PNCVisitCache(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientID: String,
    var pncPeriod: Int,
    var isActive: Boolean,
    var pncDate: Long = System.currentTimeMillis(),
    var ifaTabsGiven: Int? = 0,
    var anyContraceptionMethod: Boolean? = null,
    var contraceptionMethod: String? = null,
    var otherPpcMethod: String? = null,
    var motherDangerSign: String? = null,
    var otherDangerSign: String? = null,
    var referralFacility: String? = null,
    var motherDeath: Boolean = false,
    var deathDate: Long? = System.currentTimeMillis(),
    var causeOfDeath: String? = null,
    var otherDeathCause: String? = null,
    var placeOfDeath: String? = null,
    var remarks: String? = null,
    var processed: String? = "N",
    var createdBy: String,
    val createdDate: Long = System.currentTimeMillis(),
    var updatedBy: String,
    var updatedDate: Long = System.currentTimeMillis(),
    var syncState: SyncState
) : FormDataModel {

    fun asDomainModel(benId: Long): PncDomain = PncDomain(
        benId = benId,
        visitNumber = pncPeriod,
        syncState
    )


    fun asNetworkModel(benId: Long): PNCNetwork {
        return PNCNetwork(
            id = id,
            benId = benId,
            pncPeriod = pncPeriod,
            isActive = isActive,
            pncDate = getDateTimeStringFromLong(pncDate)!!,
            ifaTabsGiven = ifaTabsGiven,
            anyContraceptionMethod = anyContraceptionMethod,
            contraceptionMethod = contraceptionMethod,
            otherPpcMethod = otherPpcMethod,
            motherDangerSign = motherDangerSign,
            otherDangerSign = otherDangerSign,
            referralFacility = referralFacility,
            motherDeath = motherDeath,
            deathDate = deathDate?.let { getDateTimeStringFromLong(it) },
            causeOfDeath = causeOfDeath,
            otherDeathCause = otherDeathCause,
            placeOfDeath = placeOfDeath,
            remarks = remarks,
            createdBy = createdBy,
            createdDate = getDateTimeStringFromLong(createdDate)!!,
            updatedBy = updatedBy,
            updatedDate = getDateTimeStringFromLong(updatedDate)!!,
        )
    }
}

@JsonClass(generateAdapter = true)
data class PNCNetwork(
    val id: Long,
    val benId: Long,
    var pncPeriod: Int,
    var isActive: Boolean,
    var pncDate: String,
    var ifaTabsGiven: Int?,
    var anyContraceptionMethod: Boolean?,
    var contraceptionMethod: String?,
    var otherPpcMethod: String?,
    var motherDangerSign: String?,
    var otherDangerSign: String?,
    var referralFacility: String?,
    var motherDeath: Boolean,
    var deathDate: String?,
    var causeOfDeath: String?,
    var otherDeathCause: String?,
    var placeOfDeath: String?,
    var remarks: String?,
    var createdBy: String,
    val createdDate: String,
    var updatedBy: String,
    val updatedDate: String,
) {
//    fun asCacheModel(): PNCVisitCache {
//        return PNCVisitCache(
//            benId = benId,
//            pncPeriod = pncPeriod,
//            isActive = isActive,
//            pncDate = getLongFromDate(pncDate),
//            ifaTabsGiven = ifaTabsGiven,
//            anyContraceptionMethod = anyContraceptionMethod,
//            contraceptionMethod = contraceptionMethod,
//            otherPpcMethod = otherPpcMethod,
//            motherDangerSign = motherDangerSign,
//            otherDangerSign = otherDangerSign,
//            referralFacility = referralFacility,
//            motherDeath = motherDeath,
//            deathDate = getLongFromDate(deathDate),
//            causeOfDeath = causeOfDeath,
//            otherDeathCause = otherDeathCause,
//            placeOfDeath = placeOfDeath,
//            remarks = remarks,
//            processed = "P",
//            createdBy = createdBy,
//            createdDate = getLongFromDate(createdDate),
//            updatedBy = updatedBy,
//            updatedDate = getLongFromDate(updatedDate),
//            syncState = SyncState.SYNCED,
//        )
//    }
}

//data class BenWithDoAndPncCache(
////    @ColumnInfo(name = "benId")
////    val ecBenId: Long,
//
//    @Embedded
//    val ben: BenBasicCache,
//    @Relation(
//        parentColumn = "benId", entityColumn = "benId", entity = DeliveryOutcomeCache::class
//    )
//    val deliveryOutcomeCache: List<DeliveryOutcomeCache>,
//
//    @Relation(
//        parentColumn = "benId", entityColumn = "benId", entity = PNCVisitCache::class
//    )
//    val savedPncRecords: List<PNCVisitCache>
//) {
//    fun asBasicDomainModelForPNC(): BenPncDomain {
//        val activeDo = deliveryOutcomeCache.first { it.isActive }
//        val latestPnc = savedPncRecords.maxByOrNull { it.pncPeriod }
//        val daysSinceDeliveryMillis = Calendar.getInstance()
//            .setToStartOfTheDay().timeInMillis - activeDo.dateOfDelivery!!.let {
//            val cal = Calendar.getInstance()
//            cal.timeInMillis = it
//            cal.setToStartOfTheDay()
//            cal.timeInMillis
//        }
//        val daysSinceDelivery = TimeUnit.MILLISECONDS.toDays(daysSinceDeliveryMillis)
//        val availFillDates =
//            listOf(
//                1,
//                3,
//                7,
//                14,
//                21,
//                28,
//                42
//            ).filter { if (daysSinceDelivery == 0L) it <= 1 else it <= daysSinceDelivery }
//                .filter { it > (latestPnc?.pncPeriod ?: 0) }
//        return BenPncDomain(
//            ben.asBasicDomainModel(),
//            activeDo.dateOfDelivery?.let {
//                getDateStrFromLong(
//                    it
//                )
//            } ?: "",
//            availFillDates.isNotEmpty(),
//            savedPncRecords
//        )
//    }
//
//}
//
//data class BenPncDomain(
//
//    val ben: BenBasicDomain,
//    val deliveryDate: String,
//    val allowFill: Boolean,
//    val savedPncRecords: List<PNCVisitCache>,
//    val syncState: SyncState? = savedPncRecords.takeIf { it.isNotEmpty() }?.map { it.syncState }
//        ?.let { syncStates ->
//            if (syncStates.any { it != SyncState.SYNCED })
//                SyncState.UNSYNCED
//            else
//                SyncState.SYNCED
//        }
//)

data class PncDomain(
    val benId: Long,
    val visitNumber: Int,
    val syncState: SyncState? = null
)