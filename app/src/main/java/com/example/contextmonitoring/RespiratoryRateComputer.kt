package com.example.contextmonitoring

import android.net.Uri
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class RespiratoryRateComputer {
    companion object {
        // Static-like function
        fun compute(inputStream: InputStream?, uri: Uri): Int {
            val rows = 1280
            val col = 0
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String? = ""
            val columnData = StringBuilder()
            var recordCount = 0
            var ret = 0.00
            try {
                var previousValue = 0f
                var currentValue = 0f
                previousValue = 10f
                var k = 0
                while (recordCount < rows && reader.readLine().also { line = it } != null) {
                    val columns = line?.split(",")
                    if (columns != null && columns.size > col) {
                        columnData.append(columns[col]).append("\n")
                        currentValue = sqrt(
                            columns[col].toDouble().pow(2.0).toDouble()
                        ).toFloat()
                        if (abs(previousValue - currentValue) > 0.15) {
                            k++
                        }
                        previousValue = currentValue

                    }
                    recordCount++
                }
                ret = (k / 45.00)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                reader.close()
            }
            return (ret * 30).toInt()
        }
    }

}