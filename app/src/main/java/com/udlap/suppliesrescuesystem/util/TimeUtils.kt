package com.udlap.suppliesrescuesystem.util

import java.util.Calendar

object TimeUtils {

    fun formatTo12h(hour: Int): String {
        return when {
            hour == 0 -> "12AM"
            hour < 12 -> "${hour}AM"
            hour == 12 -> "12PM"
            else -> "${hour - 12}PM"
        }
    }

    fun isCurrentTimeInWindow(window: String): Boolean {
        if (window.isBlank()) return true
        
        val trimmedWindow = window.replace(" ", "").uppercase()
        val parts = trimmedWindow.split("-")
        if (parts.size != 2) return true
        
        val startStr = parts[0]
        val endStr = parts[1]
        
        val startHour = parseHour(startStr)
        val endHour = parseHour(endStr)
        
        if (startHour == -1 || endHour == -1) return true
        
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        
        return if (startHour < endHour) {
            currentHour in startHour until endHour
        } else if (startHour > endHour) {
            // Over midnight range, e.g., 11PM-2AM (23 to 2)
            currentHour >= startHour || currentHour < endHour
        } else {
            // Fallback for same-hour windows (though prevented by UI)
            currentHour == startHour
        }
    }

    private fun parseHour(hourStr: String): Int {
        return try {
            val isPM = hourStr.contains("PM")
            val isAM = hourStr.contains("AM")
            val numStr = hourStr.replace("PM", "").replace("AM", "")
            val num = numStr.toInt()
            
            when {
                isPM && num < 12 -> num + 12
                isPM && num == 12 -> 12
                isAM && num == 12 -> 0
                isAM -> num
                else -> num % 24 // Fallback for plain numbers
            }
        } catch (e: Exception) {
            -1
        }
    }
}
