package com.deicon.kmp_playground.utils

/**
 * Utility object for parsing and formatting time values.
 *
 * Supports flexible time input formats:
 * - "2h" or "2.5h" = hours (stored as-is)
 * - "1d" or "1.5d" = workdays (1 day = 8 hours)
 * - "1w" = work weeks (1 week = 40 hours = 5 days)
 *
 * All time values are stored internally as decimal hours.
 */
object TimeUtils {
    private const val HOURS_PER_DAY = 8.0
    private const val HOURS_PER_WEEK = 40.0

    /**
     * Parses a time string into decimal hours.
     *
     * @param timeString Time string in format: "2h", "1.5d", "1w"
     * @return Time in decimal hours, or null if format is invalid
     *
     * Examples:
     * - "2h" -> 2.0
     * - "2.5h" -> 2.5
     * - "1d" -> 8.0
     * - "1.5d" -> 12.0
     * - "1w" -> 40.0
     * - "0.5w" -> 20.0
     */
    fun parseTime(timeString: String): Double? {
        if (timeString.isBlank()) return null

        val trimmed = timeString.trim().lowercase()

        return when {
            trimmed.endsWith("h") -> {
                trimmed.dropLast(1).toDoubleOrNull()
            }
            trimmed.endsWith("d") -> {
                trimmed.dropLast(1).toDoubleOrNull()?.let { it * HOURS_PER_DAY }
            }
            trimmed.endsWith("w") -> {
                trimmed.dropLast(1).toDoubleOrNull()?.let { it * HOURS_PER_WEEK }
            }
            else -> null
        }
    }

    /**
     * Formats decimal hours into a human-readable string.
     * Automatically chooses the most appropriate unit.
     *
     * @param hours Time in decimal hours
     * @return Formatted string with appropriate unit
     *
     * Examples:
     * - 2.0 -> "2h"
     * - 2.5 -> "2.5h"
     * - 8.0 -> "1d"
     * - 12.0 -> "1.5d"
     * - 40.0 -> "1w"
     */
    fun formatTime(hours: Double): String {
        if (hours == 0.0) return "0h"

        // Check if it's a clean week value
        val weeks = hours / HOURS_PER_WEEK
        if (hours % HOURS_PER_WEEK == 0.0) {
            // Clean multiple of weeks
            return if (weeks == weeks.toInt().toDouble()) {
                "${weeks.toInt()}w"
            } else {
                "${weeks}w"
            }
        }

        // Check if it's a clean day value
        val days = hours / HOURS_PER_DAY
        if (hours % HOURS_PER_DAY == 0.0) {
            // Clean multiple of days
            return if (days == days.toInt().toDouble()) {
                "${days.toInt()}d"
            } else {
                "${days}d"
            }
        }

        // Check if it's a half-week (20h = 0.5w)
        if (hours % (HOURS_PER_WEEK / 2) == 0.0 && hours >= (HOURS_PER_WEEK / 2)) {
            return if (weeks == weeks.toInt().toDouble()) {
                "${weeks.toInt()}w"
            } else {
                "${weeks}w"
            }
        }

        // Check if it's a half-day or multiple of half-day (4h, 12h = 1.5d)
        if (hours % (HOURS_PER_DAY / 2) == 0.0 && hours >= HOURS_PER_DAY) {
            return if (days == days.toInt().toDouble()) {
                "${days.toInt()}d"
            } else {
                "${days}d"
            }
        }

        // Otherwise use hours
        return if (hours == hours.toInt().toDouble()) {
            "${hours.toInt()}h"
        } else {
            "${hours}h"
        }
    }

    /**
     * Validates if a time string is in a valid format.
     *
     * @param timeString Time string to validate
     * @return true if valid, false otherwise
     */
    fun isValidTimeFormat(timeString: String): Boolean {
        return parseTime(timeString) != null
    }
}
