package com.example.contextmonitoring.DbUtils

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_data")
data class HealthData(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val nausea: Float,
    val headache: Float,
    val diarrhea: Float,
    val soar_throat: Float,
    val fever: Float,
    val muscle_ache: Float,
    val smell_loss: Float,
    val cough: Float,
    val breath: Float,
    val tiredness: Float,
    val heart_rate: String,
    val acclerometer: Int
) {
    override fun toString(): String {
        return "HealthData(id=$id, nausea=$nausea, headache=$headache, diarrhea=$diarrhea, soar_throat=$soar_throat, fever=$fever, muscle_ache=$muscle_ache, smell_loss=$smell_loss, cough=$cough, breath=$breath, tiredness=$tiredness)"
    }
}

