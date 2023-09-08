package com.example.contextmonitoring


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HealthData::class], version = 1, exportSchema = false)
abstract class HealthDataDb: RoomDatabase() {
    abstract fun dataDao(): iHealthData

    companion object{
        @Volatile
        private var INSTANCE: HealthDataDb? = null

        fun getDatabase(context: Context): HealthDataDb{
            val tempInstance = INSTANCE
            if(tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HealthDataDb::class.java,
                    "medical_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }


    }
}