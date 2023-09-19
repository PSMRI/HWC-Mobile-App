package org.piramalswasthya.cho.repositories

import com.google.gson.Gson
import org.json.JSONObject
import org.piramalswasthya.cho.database.room.dao.BenFlowDao
import org.piramalswasthya.cho.database.shared_preferences.PreferenceDao
import org.piramalswasthya.cho.model.BenFlow
import org.piramalswasthya.cho.network.AmritApiService
import org.piramalswasthya.cho.network.NetworkResponse
import org.piramalswasthya.cho.network.NetworkResult
import org.piramalswasthya.cho.network.NurseDataResponse
import org.piramalswasthya.cho.network.VillageIdList
import org.piramalswasthya.cho.network.networkResultInterceptor
import org.piramalswasthya.cho.network.refreshTokenInterceptor
import org.piramalswasthya.cho.network.socketTimeoutException
import java.net.SocketTimeoutException
import javax.inject.Inject

class BenFlowRepo @Inject constructor(
    private val userRepo: UserRepo,
    private val apiService: AmritApiService,
    private val preferenceDao: PreferenceDao,
    private val benFlowDao: BenFlowDao,
    private val visitReasonsAndCategoriesRepo: VisitReasonsAndCategoriesRepo,
    private val vitalsRepo: VitalsRepo,
) {

    suspend fun getBenFlowByBenRegId(beneficiaryRegID: Long) : BenFlow?{
        return benFlowDao.getBenFlowByBenRegId(beneficiaryRegID)
    }

    private fun convertStringToIntList(villageIds : String) : List<Int>{
        return villageIds.split(",").map {
            it.trim().toInt()
        }
    }

    suspend fun downloadAndSyncFlowRecords(): Boolean {

        val user = userRepo.getLoggedInUser()
        val villageList = VillageIdList(
            convertStringToIntList(user?.assignVillageIds ?: ""),
            preferenceDao.getLastSyncTime()
        )

        when(val response = syncFlowIds(villageList)){
            is NetworkResult.Success -> {
                return true
            }
            is NetworkResult.Error -> {
                if(response.code == socketTimeoutException){
                    throw SocketTimeoutException("This is an example exception message")
                }
                return false
            }
            else -> {}
        }
        return true
    }

    suspend fun syncFlowIds(villageList: VillageIdList): NetworkResult<NetworkResponse> {

        return networkResultInterceptor {
            val response = apiService.getBenFlowRecords(villageList)
            val responseBody = response.body()?.string()
            refreshTokenInterceptor(
                responseBody = responseBody,
                onSuccess = {
                    val benflowArray = responseBody.let { JSONObject(it).getJSONArray("data") }
                    for (i in 0 until benflowArray.length()) {
                        val data = benflowArray.getString(i)
                        val benFlow = Gson().fromJson(data, BenFlow::class.java)
                        benFlowDao.insertBenFlow(benFlow)
                        visitReasonsAndCategoriesRepo.updateBenFlowId(
                            benFlowId = benFlow.benFlowID,
                            beneficiaryRegID = benFlow.beneficiaryRegID!!
                        )
                        vitalsRepo.updateBenFlowId(
                            benFlowId = benFlow.benFlowID,
                            beneficiaryRegID = benFlow.beneficiaryRegID!!
                        )
                    }
                    NetworkResult.Success(NetworkResponse())
                },
                onTokenExpired = {
                    val user = userRepo.getLoggedInUser()!!
                    userRepo.refreshTokenTmc(user.userName, user.password)
                    syncFlowIds(villageList)
                },
            )
        }

    }

    suspend fun updateNurseCompletedAndVisitCode(visitCode: Long, benVisitID: Long, benFlowID: Long){
        benFlowDao.updateNurseCompleted(visitCode, benVisitID, benFlowID)
    }

    suspend fun updateDoctorCompleted(benFlowID: Long){
        benFlowDao.updateDoctorCompleted(benFlowID)
    }

}