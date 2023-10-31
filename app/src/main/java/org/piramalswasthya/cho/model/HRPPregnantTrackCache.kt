package org.piramalswasthya.cho.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import org.piramalswasthya.cho.configuration.FormDataModel
//import org.piramalswasthya.sakhi.configuration.FormDataModel
//import org.piramalswasthya.sakhi.database.room.SyncState
//import org.piramalswasthya.sakhi.helpers.getDateString
//import org.piramalswasthya.sakhi.helpers.getTodayMillis
//import org.piramalswasthya.sakhi.network.HRPPregnantTrackDTO
//import org.piramalswasthya.sakhi.utils.HelperUtil
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

//@Entity(
//    tableName = "HRP_PREGNANT_TRACK",
//    foreignKeys = [ForeignKey(
//        entity = BenRegCache::class,
//        parentColumns = arrayOf("beneficiaryId"/* "householdId"*/),
//        childColumns = arrayOf("benId" /*"hhId"*/),
//        onUpdate = ForeignKey.CASCADE,
//        onDelete = ForeignKey.CASCADE
//    )],
//    indices = [Index(name = "ind_hpt", value = ["benId"/* "hhId"*/])]
//)
//
//data class HRPPregnantTrackCache(
//    @PrimaryKey(autoGenerate = true)
//    val id: Int = 0,
//    val benId: Long,
//    var visitDate: Long? = null,
//    var rdPmsa: String? = null,
//    var rdDengue: String? = null,
//    var rdFilaria: String? = null,
//    var severeAnemia: String? = null,
//    var pregInducedHypertension: String? = null,
//    var gestDiabetesMellitus: String? = null,
//    var hypothyrodism: String? = null,
//    var polyhydromnios: String? = null,
//    var oligohydromnios: String? = null,
//    var antepartumHem: String? = null,
//    var malPresentation: String? = null,
//    var hivsyph: String? = null,
//    var visit: String? = null,
//    var syncState: SyncState = SyncState.UNSYNCED
//) : FormDataModel {
//
//    fun asDomainModel(): HRPPregnantTrackDomain {
//        return HRPPregnantTrackDomain(
//            id = id,
//            dateOfVisit = visit + " : " + getDateStrFromLong(visitDate),
//            filledOnString = visit + HelperUtil.getTrackDate(visitDate),
//            syncState = syncState
//        )
//    }
//
//    fun toDTO(): HRPPregnantTrackDTO {
//        return HRPPregnantTrackDTO(
//            id = 0,
//            benId = benId,
//            visitDate = getDateTimeStringFromLong(visitDate),
//            rdPmsa = rdPmsa,
//            rdDengue = rdDengue,
//            rdFilaria = rdFilaria,
//            severeAnemia = severeAnemia,
//            pregInducedHypertension = pregInducedHypertension,
//            gestDiabetesMellitus = gestDiabetesMellitus,
//            hypothyrodism = hypothyrodism,
//            polyhydromnios = polyhydromnios,
//            oligohydromnios = oligohydromnios,
//            antepartumHem = antepartumHem,
//            malPresentation = malPresentation,
//            hivsyph = hivsyph,
//            visit = visit
//        )
//    }
//}
//
//data class HRPPregnantTrackDomain(
//    val id: Int = 0,
//    val dateOfVisit: String?,
//    val filledOnString: String?,
//    val syncState: SyncState?
//)
//
//data class HRPPregnantTrackBen(
//    val ben: BenBasicDomain,
//    val trackList: List<HRPPregnantTrackCache>,
////    val onClick: (Long, Int) -> Unit
//)
//
fun getDateStrFromLong(dateLong: Long?): String? {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
    dateLong?.let {
        val dateString = dateFormat.format(dateLong)
        val timeString = timeFormat.format(dateLong)
        return dateString
    } ?: run {
        return null
    }

}
//
//
//data class BenWithHRPTrackingCache(
//
//    @Embedded
//    val ben: BenBasicCache,
//    @Relation(
//        parentColumn = "benId", entityColumn = "benId"
//    )
//    val assessCache: HRPPregnantAssessCache,
//
//    @Relation(
//        parentColumn = "benId", entityColumn = "benId", entity = HRPPregnantTrackCache::class
//    )
//    val savedTrackings: List<HRPPregnantTrackCache>
//) {
//
//    companion object {
//        private val dateFormat = SimpleDateFormat("EEE, MMM dd yyyy", Locale.getDefault())
//
//        private fun getHRPTFilledDateFromLong(long: Long?): String {
//            return "Visited on ${dateFormat.format(long)}"
//        }
//    }
//
//    fun asDomainModel(): BenWithHRPTListDomain {
//        return BenWithHRPTListDomain(
//            ben.asBasicDomainModel(),
//        lmpString = getDateString(assessCache.lmpDate),
//         eddString = getDateString(assessCache.lmpDate + TimeUnit.DAYS.toMillis(280)),
//         weeksOfPregnancy = (TimeUnit.MILLISECONDS.toDays(getTodayMillis() - assessCache.lmpDate) / 7).takeIf { it <= 40 }
//             ?.toString() ?: "NA",
//        savedTrackings.map {
//                HRPTDomain(
//                    it.benId,
//                    it.visitDate,
//                    getHRPTFilledDateFromLong(it.visitDate),
//                    it.syncState
//                )
//            }
//        )
//    }
//}
//
//data class HRPTDomain(
//    val benId: Long,
//    val visited: Long?,
//    val filledOnString: String,
//    val syncState: SyncState
//)
//
//data class BenWithHRPTListDomain(
//    val ben: BenBasicDomain,
//    val lmpString: String?,
//    val eddString: String?,
//    val weeksOfPregnancy: String?,
//    val savedTrackings: List<HRPTDomain>,
//    val allSynced: SyncState? = if (savedTrackings.isEmpty()) null else
//        if (savedTrackings.map { it.syncState }
//                .all { it == SyncState.SYNCED}) SyncState.SYNCED else SyncState.UNSYNCED
//
//)