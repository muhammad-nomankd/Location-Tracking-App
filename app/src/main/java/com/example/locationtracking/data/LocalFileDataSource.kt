package com.example.locationtracking.data

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


class LocalFileDataSource(private val filesDir: File) {

    private val logFile = File(filesDir, "location_history.csv")

    init {
        if (!logFile.exists()) {
            logFile.createNewFile()
        }
    }

    fun appendLocationLine(timestampIso: String, lat: Double, lon: Double) {
        val formattedTime = formatTimestamp(timestampIso)

        FileWriter(logFile, true).use { fw ->
            BufferedWriter(fw).use { bw ->
                bw.append("$formattedTime, Lat: $lat, Lon: $lon\n")
                bw.flush()
            }
        }
    }

    private fun formatTimestamp(timestampIso: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")

            val outputFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val date = inputFormat.parse(timestampIso.trim())
            outputFormat.format(date!!)
        } catch (e: Exception) {
            e.printStackTrace()
            "Invalid Time"
        }
    }


    fun readAllLines(): List<String> {
        return if (logFile.exists()) {
            logFile.readLines()
        } else emptyList()
    }

    fun clearAllLines(){
        logFile.delete()
    }
}
