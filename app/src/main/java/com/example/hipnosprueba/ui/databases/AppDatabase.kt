package com.example.hipnosprueba.ui.databases

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.hipnosprueba.ui.databases.Entitys.SleepSession
import com.example.hipnosprueba.ui.databases.Entitys.SleepSessionDao

@Database(entities = [SleepSession::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sleepSessionDao(): SleepSessionDao

    companion object {
        @Volatile private var instance: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, "hipnos.db"
                ).build().also { instance = it }
            }
    }
}
