package com.example.contextmonitoring.DbUtils


import androidx.lifecycle.LiveData

class HealthDataRepository(private val dataDao: iHealthData) {
    val readAllData: LiveData<List<HealthData>> = dataDao.readAllData()


    suspend fun insert(data: HealthData) {
        dataDao.insert(data)
    }
}