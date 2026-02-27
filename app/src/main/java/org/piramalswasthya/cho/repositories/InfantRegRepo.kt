package org.piramalswasthya.cho.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.database.room.dao.InfantRegDao
import org.piramalswasthya.cho.database.room.dao.PatientDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.ChildRegDomain
import org.piramalswasthya.cho.model.Gender
import org.piramalswasthya.cho.model.InfantRegApiPost
import org.piramalswasthya.cho.model.InfantRegCache
import org.piramalswasthya.cho.model.InfantRegDomain
import org.piramalswasthya.cho.network.AmritApiService
import timber.log.Timber
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class InfantRegRepo @Inject constructor(
    private val preferenceDao: PreferenceDao,
    private val amritApiService: AmritApiService,
    private val userRepo: UserRepo,
    private val patientDao: PatientDao,
    private val infantRegDao: InfantRegDao
) {
    /**
     * Get list of infants eligible for registration
     * Returns flattened list of InfantRegDomain (one per baby based on liveBirth count)
     */
    fun getListForInfantReg(): Flow<List<InfantRegDomain>> {
        return infantRegDao.getListForInfantRegister()
            .map { list -> list.flatMap { it.asDomainModel() } }
    }

    /**
     * Get count of infants eligible for registration
     */
    fun getInfantRegisterCount(): Flow<Int> {
        return infantRegDao.getInfantRegisterCount()
    }

    /**
     * Get all registered infants for child registration list
     */
    fun getRegisteredInfants(): Flow<List<ChildRegDomain>> {
        return infantRegDao.getAllRegisteredInfants()
            .map { list -> list.map { it.asDomainModel() } }
    }

    /**
     * Get count of registered infants
     */
    fun getRegisteredInfantsCount(): Flow<Int> {
        return infantRegDao.getAllRegisteredInfantsCount()
    }

    suspend fun getInfantReg(patientID: String, babyIndex: Int): InfantRegCache? =
        infantRegDao.getInfantReg(patientID, babyIndex)

    suspend fun getInfantRegFromChildPatientID(childPatientID: String): InfantRegCache? =
        infantRegDao.getInfantRegFromChildPatientID(childPatientID)

    suspend fun saveInfantReg(infantRegCache: InfantRegCache) {
        upsertInfantReg(infantRegCache)
    }

    suspend fun upsertInfantReg(infantRegCache: InfantRegCache) {
        val existing = if (infantRegCache.id != 0L) {
            infantRegCache
        } else {
            infantRegDao.getInfantReg(infantRegCache.motherPatientID, infantRegCache.babyIndex)
        }

        if (existing != null && existing.id != 0L) {
            infantRegDao.updateInfantReg(infantRegCache.copy(id = existing.id))
        } else {
            infantRegDao.saveInfantReg(infantRegCache)
        }
    }

    suspend fun getNumBabiesRegistered(patientID: String): Int =
        infantRegDao.getNumBabiesRegistered(patientID)

    suspend fun processNewInfantRegister(): Boolean {
        return withContext(Dispatchers.IO) {
            if (preferenceDao.getLoggedInUser() == null) {
                throw IllegalStateException("No user logged in")
            }

            val infantRegList = infantRegDao.getAllUnprocessedInfantReg()
            if (infantRegList.isEmpty()) return@withContext true

            infantRegList.forEach { infantReg ->
                val motherPatient = getPatientOrNull(infantReg.motherPatientID)
                val motherBenId = motherPatient?.beneficiaryID

                if (motherBenId == null) {
                    Timber.w("Skipping infant registration ${infantReg.id}: mother beneficiary ID missing")
                    return@forEach
                }

                val childBenId = infantReg.childPatientID
                    ?.let { getPatientOrNull(it)?.beneficiaryID }
                    ?: 0L

                val payload = infantReg.toApiPost(
                    motherBenId = motherBenId,
                    childBenId = childBenId
                )

                infantReg.syncState = SyncState.SYNCING
                infantRegDao.updateInfantReg(infantReg)

                val uploadDone = postDataToAmritServer(listOf(payload))
                if (uploadDone) {
                    infantReg.processed = "P"
                    infantReg.syncState = SyncState.SYNCED
                } else {
                    infantReg.syncState = SyncState.UNSYNCED
                }
                infantRegDao.updateInfantReg(infantReg)

                if (!uploadDone) return@withContext false
            }

            return@withContext true
        }
    }

    private suspend fun postDataToAmritServer(
        infantRegPostList: List<InfantRegApiPost>
    ): Boolean {
        if (infantRegPostList.isEmpty()) return true

        val user = preferenceDao.getLoggedInUser()
            ?: throw IllegalStateException("No user logged in")

        return try {
            val response = amritApiService.postInfantRegForm(infantRegPostList)
            if (response.code() != 200) {
                Timber.w("Infant sync failed with HTTP ${response.code()}")
                return false
            }

            val responseString = response.body()?.string() ?: return false
            val jsonObj = JSONObject(responseString)
            val responseStatusCode = jsonObj.optInt("statusCode", -1)

            when (responseStatusCode) {
                200 -> true
                401, 5002 -> {
                    if (userRepo.refreshTokenTmc(user.userName, user.password)) {
                        throw SocketTimeoutException("Token refreshed. Retrying infant sync")
                    }
                    false
                }
                else -> {
                    Timber.w("Infant sync failed with response statusCode=$responseStatusCode")
                    false
                }
            }
        } catch (e: SocketTimeoutException) {
            Timber.d("Retrying infant sync after token refresh")
            postDataToAmritServer(infantRegPostList)
        } catch (e: JSONException) {
            Timber.e(e, "Infant sync failed while parsing response")
            false
        } catch (e: Exception) {
            Timber.e(e, "Infant sync failed")
            false
        }
    }

    private suspend fun getPatientOrNull(patientID: String) =
        runCatching { patientDao.getPatient(patientID) }.getOrNull()

    private fun InfantRegCache.toApiPost(motherBenId: Long, childBenId: Long): InfantRegApiPost {
        return InfantRegApiPost(
            id = id,
            benId = motherBenId,
            childBenId = childBenId,
            isActive = isActive,
            babyName = babyName,
            babyIndex = babyIndex,
            infantTerm = infantTerm,
            corticosteroidGiven = corticosteroidGiven,
            gender = mapGender(genderID),
            babyCriedAtBirth = babyCriedAtBirth,
            resuscitation = resuscitation,
            referred = referred,
            hadBirthDefect = hadBirthDefect,
            birthDefect = birthDefect,
            otherDefect = otherDefect,
            weight = weight ?: 0.0,
            breastFeedingStarted = breastFeedingStarted,
            opv0Dose = getDateStringFromLong(opv0Dose),
            bcgDose = getDateStringFromLong(bcgDose),
            hepBDose = getDateStringFromLong(hepBDose),
            vitkDose = getDateStringFromLong(vitkDose),
            outcomeAtBirth = outcomeAtBirth,
            typeOfResuscitation = typeOfResuscitation,
            newbornComplications = newbornComplications,
            currentStatusOfBaby = currentStatusOfBaby,
            causeOfDeath = causeOfDeath,
            otherCauseOfDeath = otherCauseOfDeath,
            birthDoseVaccinesGiven = birthDoseVaccinesGiven,
            reasonForNoVaccines = reasonForNoVaccines,
            vitaminKInjectionGiven = vitaminKInjectionGiven,
            reasonForNoVitaminK = reasonForNoVitaminK,
            birthCertificateIssued = birthCertificateIssued,
            createdDate = getDateStringFromLong(createdDate),
            createdBy = createdBy,
            updatedDate = getDateStringFromLong(updatedDate),
            updatedBy = updatedBy
        )
    }

    private fun getDateStringFromLong(dateLong: Long?): String? {
        if (dateLong == null) return null
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        return dateFormat.format(dateLong)
    }

    private fun mapGender(genderID: Int?): String? {
        return when (genderID) {
            1 -> Gender.MALE.name
            2 -> Gender.FEMALE.name
            3 -> Gender.TRANSGENDER.name
            else -> null
        }
    }
}
