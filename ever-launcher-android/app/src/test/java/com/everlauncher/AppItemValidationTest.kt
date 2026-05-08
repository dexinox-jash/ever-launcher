package com.everlauncher

import com.everlauncher.domain.model.AppItem
import com.everlauncher.domain.model.FocusMode
import com.everlauncher.domain.model.Schedule
import org.junit.Assert.*
import org.junit.Test
import java.time.DayOfWeek

class AppItemValidationTest {

    @Test
    fun `valid AppItem creates successfully`() {
        val app = AppItem(displayName = "Calendar", packageName = "com.google.android.calendar")
        assertEquals("Calendar", app.displayName)
        assertEquals("com.google.android.calendar", app.packageName)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `blank displayName throws IllegalArgumentException`() {
        AppItem(displayName = "   ", packageName = "com.example.app")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `blank packageName throws IllegalArgumentException`() {
        AppItem(displayName = "App", packageName = "")
    }

    @Test
    fun `AppItem defaults to not gated`() {
        val app = AppItem(displayName = "App", packageName = "com.example")
        assertFalse(app.isGated)
        assertNull(app.gateType)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `FocusMode rejects more than 8 apps`() {
        val apps = (1..9).map { AppItem(displayName = "App$it", packageName = "com.example.$it") }
        FocusMode(
            name = "Too Many",
            apps = apps,
            schedule = Schedule(startMinutes = 0, endMinutes = 60, activeDays = setOf(DayOfWeek.MONDAY))
        )
    }

    @Test
    fun `FocusMode allows exactly 8 apps`() {
        val apps = (1..8).map { AppItem(displayName = "App$it", packageName = "com.example.$it") }
        val mode = FocusMode(
            name = "Eight Apps",
            apps = apps,
            schedule = Schedule(startMinutes = 0, endMinutes = 60, activeDays = setOf(DayOfWeek.MONDAY))
        )
        assertEquals(8, mode.apps.size)
    }

    @Test
    fun `FocusMode rejects blank name`() {
        assertThrows(IllegalArgumentException::class.java) {
            FocusMode(
                name = "  ",
                schedule = Schedule(startMinutes = 0, endMinutes = 60)
            )
        }
    }

    @Test
    fun `Schedule validates minute range`() {
        assertThrows(IllegalArgumentException::class.java) {
            Schedule(startMinutes = -1, endMinutes = 60)
        }
        assertThrows(IllegalArgumentException::class.java) {
            Schedule(startMinutes = 0, endMinutes = 1440) // max is 1439
        }
    }

    @Test
    fun `Schedule detects overnight correctly`() {
        val overnight = Schedule(startMinutes = 1260, endMinutes = 420) // 21:00–07:00
        assertTrue(overnight.isOvernight)

        val daytime = Schedule(startMinutes = 540, endMinutes = 1020) // 09:00–17:00
        assertFalse(daytime.isOvernight)
    }
}
