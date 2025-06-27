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
}
