package org.piramalswasthya.cho.ui.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.DeliveryOutcomeDao
import org.piramalswasthya.cho.database.room.dao.EcrDao
import org.piramalswasthya.cho.database.room.dao.PatientVisitInfoSyncDao
import org.piramalswasthya.cho.model.ChildRegDomain
import org.piramalswasthya.cho.model.PatientWithPwrDomain
import org.piramalswasthya.cho.model.SyncStatusCache
import org.piramalswasthya.cho.repositories.InfantRegRepo
import org.piramalswasthya.cho.repositories.MaternalHealthRepo
import org.piramalswasthya.cho.repositories.PatientRepo
import org.piramalswasthya.cho.repositories.PncRepo
import javax.inject.Inject
import java.util.concurrent.TimeUnit

@HiltViewModel
class SyncViewModel @Inject constructor(
    patientVisitInfoSyncDao: PatientVisitInfoSyncDao,
    private val ecrDao: EcrDao,
    private val deliveryOutcomeDao: DeliveryOutcomeDao,
    private val maternalHealthRepo: MaternalHealthRepo,
    private val pncRepo: PncRepo,
    private val infantRegRepo: InfantRegRepo,
    private val patientRepo: PatientRepo,
) : ViewModel() {

    private val choSyncStatus: Flow<List<SyncStatusCache>> = patientVisitInfoSyncDao.getSyncStatus()

    private val rmnchSyncStatus: Flow<List<SyncStatusCache>> = combineStatusFlows(
        ecrDao.getEligibleCoupleTrackingSyncStatus(),
        pwrSyncStatus(),
        ancSyncStatus(),
        deliveryOutcomeDao.getDeliveryOutcomeSyncStatus(),
        pncSyncStatus(),
        infantSyncStatus(),
        childSyncStatus(),
        abortionSyncStatus(),
        adolescentSyncStatus(),
    )

    val syncStatus: Flow<List<SyncStatusCache>> = combine(choSyncStatus, rmnchSyncStatus) { cho, rmnch ->
        (cho + rmnch).sortedBy { it.id }
    }

    private fun pwrSyncStatus(): Flow<List<SyncStatusCache>> {
        return maternalHealthRepo.getAllPatientsWithPWR().map { patients ->
            val visibleRows = patients.map { it.asDomainModel() }
                .filter { it.isVisibleForPwr() }
            listOf(
                buildStatus(
                    id = 7,
                    name = "PW Registration",
                    syncStates = visibleRows.mapNotNull { it.pwr?.syncState }
                )
            )
        }
    }

    private fun ancSyncStatus(): Flow<List<SyncStatusCache>> {
        return maternalHealthRepo.getAllPatientsWithANC().map { patients ->
            val visibleRows = patients.map { it.asDomainModel() }
                .filter { it.isVisibleForAnc() }
            listOf(
                buildStatus(
                    id = 8,
                    name = "PW ANC",
                    syncStates = visibleRows.mapNotNull { it.lastAnc?.syncState }
                )
            )
        }
    }

    private fun pncSyncStatus(): Flow<List<SyncStatusCache>> {
        return pncRepo.getAllPNCMothers().map { patients ->
            val visibleRows = patients.map { it.asDomainModel() }
            listOf(
                buildStatus(
                    id = 10,
                    name = "PNC",
                    syncStates = visibleRows.mapNotNull { it.syncState }
                )
            )
        }
    }

    private fun infantSyncStatus(): Flow<List<SyncStatusCache>> {
        return infantRegRepo.getListForInfantReg().map { infants ->
            val visibleRows = infants.filter { it.isRegistered() }
            listOf(
                buildStatus(
                    id = 11,
                    name = "Infant Reg.",
                    syncStates = visibleRows.mapNotNull { it.syncState }
                )
            )
        }
    }

    private fun childSyncStatus(): Flow<List<SyncStatusCache>> {
        return infantRegRepo.getRegisteredInfants().map { children ->
            val visibleRows = children
                .groupBy { it.motherPatient.patientID to it.infant.babyIndex }
                .map { (_, rows) ->
                    rows.maxWithOrNull(
                        compareBy<ChildRegDomain> { it.isChildRegistered() }
                            .thenByDescending { maxOf(it.infant.updatedDate, it.infant.createdDate) }
                    ) ?: rows.first()
                }
                .filter { it.isChildRegistered() }
                .sortedWith(
                    compareBy<ChildRegDomain> { it.isChildRegistered() }
                        .thenByDescending { maxOf(it.infant.updatedDate, it.infant.createdDate) }
                        .thenByDescending { it.infant.updatedDate }
                )

            listOf(
                buildStatus(
                    id = 12,
                    name = "Child Reg.",
                    syncStates = visibleRows.mapNotNull { it.syncState }
                )
            )
        }
    }

    private fun abortionSyncStatus(): Flow<List<SyncStatusCache>> {
        return maternalHealthRepo.getAbortionPregnantWomanList().map { abortingWomen ->
            listOf(
                buildStatus(
                    id = 13,
                    name = "Abortion List",
                    syncStates = abortingWomen.mapNotNull { it.syncState }
                )
            )
        }
    }

    private fun adolescentSyncStatus(): Flow<List<SyncStatusCache>> {
        return patientRepo.getAdolescentList().map { adolescents ->
            listOf(
                buildStatus(
                    id = 14,
                    name = "Adolescent List",
                    syncStates = adolescents.mapNotNull { it.patient.syncState }
                )
            )
        }
    }

    private fun buildStatus(
        id: Int,
        name: String,
        syncStates: Iterable<SyncState?>
    ): SyncStatusCache {
        val stateList = syncStates.toList()
        return SyncStatusCache(
            id = id,
            name = name,
            synced = stateList.count { it == SyncState.SYNCED },
            notSynced = stateList.count { it == SyncState.UNSYNCED },
            syncing = stateList.count { it == SyncState.SYNCING }
        )
    }

    private fun combineStatusFlows(vararg flows: Flow<List<SyncStatusCache>>): Flow<List<SyncStatusCache>> {
        return combine(flows.toList()) { statusGroups: Array<out List<SyncStatusCache>> ->
            statusGroups.asList()
                .flatten()
                .sortedBy { it.id }
        }
    }

    private fun PatientWithPwrDomain.isVisibleForPwr(): Boolean {
        val isFemale = patient.genderID == 2
        val age = patient.age ?: 0
        val isReproductiveAge = age in 15..49
        val isPostnatal = patient.statusOfWomanID == 3
        return pwr != null && isFemale && isReproductiveAge && !isPostnatal
    }

    private fun PatientWithPwrDomain.isVisibleForAnc(): Boolean {
        val hasActivePWR = pwr != null && isActive()
        val isFemale = patient.genderID == 2
        val age = patient.age ?: 0
        val isReproductiveAge = age in 15..49
        val minAncDays = 5L * 7L
        val isEligibleForANC = pwr?.lmpDate?.let { lmpDate ->
            val daysSinceLMP = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - lmpDate)
            daysSinceLMP >= minAncDays
        } ?: false
        return hasActivePWR && isFemale && isReproductiveAge && isEligibleForANC
    }
}
