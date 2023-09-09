package com.example.contextmonitoring.Db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface iHealthData {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(row: HealthData)

    @Query(value = "SELECT * FROM health_data ORDER BY ID DESC")
    fun readAllData(): LiveData<List<HealthData>>

}