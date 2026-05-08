package com.everlauncher.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Defines when a FocusMode is active.
 *
 * Times are stored as integer minutes-since-midnight (0–1439) in the user's
 * local timezone to avoid floating-point comparison errors and DST issues.
 *
 * Overnight schedules are supported: if endMinutes < startMinutes the mode
 * spans midnight (e.g., 22:00–06:00 = startMinutes=1320, endMinutes=360).
 */
data class Schedule(
    /** Minutes since midnight, 0–1439 */
    val startMinutes: Int,
    /** Minutes since midnight, 0–1439 */
    val endMinutes: Int,
    val activeDays: Set<DayOfWeek> = DayOfWeek.entries.toSet()
) {
    init {
        require(startMinutes in 0..1439) { "startMinutes must be 0–1439" }
        require(endMinutes in 0..1439) { "endMinutes must be 0–1439" }
    }

    val isOvernight: Boolean get() = endMinutes < startMinutes

    fun isActiveAt(minuteOfDay: Int, dayOfWeek: DayOfWeek): Boolean {
        if (dayOfWeek !in activeDays) return false
        return if (isOvernight) {
            minuteOfDay >= startMinutes || minuteOfDay < endMinutes
        } else {
            minuteOfDay in startMinutes until endMinutes
        }
    }

    companion object {
        fun fromLocalTime(start: LocalTime, end: LocalTime, days: Set<DayOfWeek> = DayOfWeek.entries.toSet()) =
            Schedule(
                startMinutes = start.hour * 60 + start.minute,
                endMinutes = end.hour * 60 + end.minute,
                activeDays = days
            )
    }
}
