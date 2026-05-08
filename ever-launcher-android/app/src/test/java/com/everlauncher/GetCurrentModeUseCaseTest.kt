package com.everlauncher

import com.everlauncher.domain.model.FocusMode
import com.everlauncher.domain.model.Schedule
import org.junit.Assert.*
import org.junit.Test
import java.time.DayOfWeek
import java.time.Instant

/**
 * Tests for the schedule-resolution algorithm used by GetCurrentModeUseCase.
 * We test the core `resolveBySchedule` logic directly via the Schedule.isActiveAt() API.
 */
class GetCurrentModeUseCaseTest {

    private fun mode(
        name: String,
        startH: Int, startM: Int = 0,
        endH: Int, endM: Int = 0,
        days: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
        createdAt: Instant = Instant.now()
    ) = FocusMode(
        name = name,
        schedule = Schedule(
            startMinutes = startH * 60 + startM,
            endMinutes = endH * 60 + endM,
            activeDays = days
        ),
        createdAt = createdAt
    )

    // --- isActiveAt tests ---

    @Test
    fun `daytime mode active within its window`() {
        val m = mode("Focus", startH = 9, endH = 17)
        assertTrue(m.schedule.isActiveAt(minuteOfDay(10, 0), DayOfWeek.MONDAY))
        assertTrue(m.schedule.isActiveAt(minuteOfDay(9, 0), DayOfWeek.MONDAY))
        assertFalse(m.schedule.isActiveAt(minuteOfDay(17, 0), DayOfWeek.MONDAY)) // end is exclusive
        assertFalse(m.schedule.isActiveAt(minuteOfDay(8, 59), DayOfWeek.MONDAY))
    }

    @Test
    fun `overnight mode active after midnight`() {
        val windDown = mode("Wind Down", startH = 21, endH = 7) // 21:00–07:00
        assertTrue(windDown.schedule.isActiveAt(minuteOfDay(22, 0), DayOfWeek.MONDAY))
        assertTrue(windDown.schedule.isActiveAt(minuteOfDay(0, 0), DayOfWeek.TUESDAY))
        assertTrue(windDown.schedule.isActiveAt(minuteOfDay(6, 59), DayOfWeek.TUESDAY))
        assertFalse(windDown.schedule.isActiveAt(minuteOfDay(7, 0), DayOfWeek.TUESDAY))
        assertFalse(windDown.schedule.isActiveAt(minuteOfDay(12, 0), DayOfWeek.TUESDAY))
    }

    @Test
    fun `mode inactive on non-scheduled days`() {
        val weekday = mode("Weekday", startH = 9, endH = 17, days = setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY))
        assertFalse(weekday.schedule.isActiveAt(minuteOfDay(10, 0), DayOfWeek.SATURDAY))
        assertTrue(weekday.schedule.isActiveAt(minuteOfDay(10, 0), DayOfWeek.MONDAY))
    }

    @Test
    fun `boundary start time inclusive end time exclusive`() {
        val m = mode("Precise", startH = 14, startM = 30, endH = 16, endM = 45)
        assertTrue(m.schedule.isActiveAt(minuteOfDay(14, 30), DayOfWeek.WEDNESDAY))
        assertFalse(m.schedule.isActiveAt(minuteOfDay(14, 29), DayOfWeek.WEDNESDAY))
        assertFalse(m.schedule.isActiveAt(minuteOfDay(16, 45), DayOfWeek.WEDNESDAY))
        assertTrue(m.schedule.isActiveAt(minuteOfDay(16, 44), DayOfWeek.WEDNESDAY))
    }

    @Test
    fun `schedule uses integer minutes never floating point`() {
        // 9h 30m = 570 minutes exactly — no floating-point error
        val schedule = Schedule(startMinutes = 570, endMinutes = 1020) // 9:30–17:00
        assertEquals(570, schedule.startMinutes)
        assertEquals(1020, schedule.endMinutes)
    }

    // --- Conflict resolution tests ---

    @Test
    fun `latest created mode wins on schedule conflict`() {
        val older = mode("Older", startH = 9, endH = 17, createdAt = Instant.ofEpochMilli(1000L))
        val newer = mode("Newer", startH = 9, endH = 17, createdAt = Instant.ofEpochMilli(2000L))
        val modes = listOf(older, newer)
        val minuteOfDay = minuteOfDay(12, 0)
        val day = DayOfWeek.MONDAY
        val active = modes.filter { it.schedule.isActiveAt(minuteOfDay, day) }
        val winner = active.maxByOrNull { it.createdAt }
        assertEquals("Newer", winner?.name)
    }

    @Test
    fun `no active schedule falls back to most recently created mode`() {
        val mode1 = mode("Old", startH = 9, endH = 11, createdAt = Instant.ofEpochMilli(1000L))
        val mode2 = mode("New", startH = 13, endH = 15, createdAt = Instant.ofEpochMilli(2000L))
        val modes = listOf(mode1, mode2)
        val minuteOfDay = minuteOfDay(12, 0) // gap between modes
        val day = DayOfWeek.MONDAY
        val active = modes.filter { it.schedule.isActiveAt(minuteOfDay, day) }
        assertTrue("Should be no active modes in gap", active.isEmpty())
        val fallback = modes.maxByOrNull { it.createdAt }
        assertEquals("New", fallback?.name)
    }

    private fun minuteOfDay(hour: Int, minute: Int) = hour * 60 + minute
}
