package org.piramalswasthya.cho.repositories

import android.content.Context
import android.content.res.Resources
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.piramalswasthya.cho.database.room.InAppDb
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.database.room.SyncState
import org.piramalswasthya.cho.helpers.Konstants
import org.piramalswasthya.cho.model.Patient
import org.piramalswasthya.cho.model.CbacCache
//import org.piramalswasthya.sakhi.network.GetDataPaginatedRequest
import org.piramalswasthya.cho.network.getLongFromDate
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CbacRepo @Inject constructor(
    @ApplicationContext context: Context,
    private val database: InAppDb,
    private val userRepo: UserRepo,
    private val amritApiService: AmritApiService,
    private val prefDao: PreferenceDao
) {

//    private val resources: Resources
//
//    init {
//        resources = context.resources
//    }
//
    suspend fun saveCbacData(cbacCache: CbacCache,): Boolean {
        return withContext(Dispatchers.IO) {

            val user =
                prefDao.getLoggedInUser()
                    ?: throw IllegalStateException("No user logged in!!")
            try {
                cbacCache.apply {
                    createdBy = user.userName
                    createdDate = System.currentTimeMillis()
                    serverUpdatedStatus = 0
                    cbac_tracing_all_fm =
                        if (cbac_sufferingtb_pos == 1 || cbac_antitbdrugs_pos == 1)
                            "1"
                        else
                            "0"
                    cbac_sputemcollection = if (cbac_tbhistory_pos == 1 ||
                        cbac_coughing_pos == 1 ||
                        cbac_bloodsputum_pos == 1 ||
                        cbac_fivermore_pos == 1 ||
                        cbac_loseofweight_pos == 1 ||
                        cbac_nightsweats_pos == 1
                    )
                        "1"
                    else
                        "0"
                    Processed = "N"
//                    ProviderServiceMapID = user.serviceMapId
//                    VanID = user.vanId

                }

                database.cbacDao.upsert(cbacCache)
                true
            } catch (e: java.lang.Exception) {
                Timber.d("Error : $e raised at saveCbacData")
                false
            }
        }
    }
//
    suspend fun getCbacCacheFromId(cbacId: Int): CbacCache {
        return withContext(Dispatchers.IO) {
            database.cbacDao.getCbacFromBenId(cbacId)
                ?: throw IllegalStateException("No CBAC entry found!")
        }

    }

    suspend fun getLastFilledCbac(benId: String): CbacCache? {
        return withContext(Dispatchers.IO) {
            database.cbacDao.getLastFilledCbacFromBenId(benId = benId)
        }
    }


}