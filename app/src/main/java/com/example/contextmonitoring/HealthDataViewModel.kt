package com.example.contextmonitoring


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HealthDataViewModel(application: Application): AndroidViewModel(application) {

    private val readAllData: LiveData<List<HealthData>>

    private val repository: HealthDataRepository

    init {
        val keyValueStoreDao = HealthDataDb.getDatabase(application).dataDao()
        repository = HealthDataRepository(keyValueStoreDao)
        readAllData = repository.readAllData
    }
    fun insert(data: HealthData){
        viewModelScope.launch(Dispatchers.IO){
            repository.insert(data)
        }
    }
}