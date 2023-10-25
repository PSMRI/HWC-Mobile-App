package org.piramalswasthya.cho.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapInfo
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.piramalswasthya.cho.model.DeliveryOutcomeCache

@Dao
interface DeliveryOutcomeDao {

    @Query("SELECT * FROM DELIVERY_OUTCOME WHERE patientID = :patientID and isActive=1 limit 1")
    fun getDeliveryOutcome(patientID: String): DeliveryOutcomeCache?

    @Query("SELECT * FROM DELIVERY_OUTCOME WHERE patientID in (:patientID) and isActive = 1")
    fun getAllDeliveryOutcomes(patientID: Set<String>): List<DeliveryOutcomeCache>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun saveDeliveryOutcome(deliveryOutcomeCache: DeliveryOutcomeCache)

    @Query("SELECT * FROM DELIVERY_OUTCOME WHERE processed in ('N','U')")
    suspend fun getAllUnprocessedDeliveryOutcomes(): List<DeliveryOutcomeCache>

    @Update
    suspend fun updateDeliveryOutcome(it: DeliveryOutcomeCache)

//    @MapInfo(keyColumn = "benId", valueColumn ="dateOfDelivery")
//    @Query("select * from delivery_outcome where isActive = 1")
//    suspend fun getAllBenIdAndDeliverDate(): Map<Long, Long>
}