package com.example.contextmonitoring.DbUtils

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface iHealthData {
    @Query(value = "SELECT * FROM health_data ORDER BY ID DESC")
    fun readAllData(): LiveData<List<HealthData>>
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(row: HealthData)



}