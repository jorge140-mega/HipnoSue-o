package com.example.hipnosprueba.ui.databases.Entitys

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepSessionDao {
    @Insert
    suspend fun insert(session: SleepSession): Long

    @Query("SELECT * FROM sleep_sessions ORDER BY timestamp DESC")
    fun getAll(): Flow<List<SleepSession>>


    @Query("""
        SELECT * FROM sleep_sessions 
        WHERE (:startTimestamp IS NULL OR timestamp >= :startTimestamp)
        AND (:endTimestamp IS NULL OR timestamp <= :endTimestamp)
        AND ((:minQuality IS NULL OR :maxQuality IS NULL) OR 
             (movements BETWEEN 
                CASE WHEN :maxQuality >= 80 THEN 0 
                     WHEN :maxQuality >= 60 THEN 20 
                     ELSE 50 END 
                AND 
                CASE WHEN :minQuality >= 80 THEN 20 
                     WHEN :minQuality >= 60 THEN 50 
                     ELSE 1000 END))
        AND (:minDurationHours IS NULL OR :maxDurationHours IS NULL OR 
             (durationSeconds/3600.0 BETWEEN :minDurationHours AND :maxDurationHours))
        ORDER BY timestamp DESC
    """)
    suspend fun getFilteredSessions(
        startTimestamp: Long?,
        endTimestamp: Long?,
        minQuality: Float?,
        maxQuality: Float?,
        minDurationHours: Float?,
        maxDurationHours: Float?
    ): List<SleepSession>
}
