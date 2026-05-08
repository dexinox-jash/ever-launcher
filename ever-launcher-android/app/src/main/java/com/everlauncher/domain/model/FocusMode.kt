package com.everlauncher.domain.model

import java.time.DayOfWeek
import java.time.Instant
import java.util.UUID

/**
 * A named collection of apps that activates on a time-based schedule.
 *
 * Design rules (enforced here and in the UI):
 * - Maximum 8 apps per mode (reduces choice overload)
 * - Schedule conflict: latest createdAt wins
 */
data class FocusMode(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val apps: List<AppItem> = emptyList(),
    val schedule: Schedule,
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val createdAt: Instant = Instant.now(),
    val sortOrder: Int = 0,
    /**
     * Per-mode gate overrides keyed by packageName.
     * Value: a GateType.name ("BREATHING", "INTENTION", "DELAY") overrides the app-level gate,
     * or "NONE" to explicitly ungate the app within this mode regardless of the app-level setting.
     * If a package is absent, the app-level isGated / gateType applies.
     */
    val gateOverrides: Map<String, String> = emptyMap()
) {
    init {
        require(name.isNotBlank()) { "Mode name must not be blank" }
        require(apps.size <= MAX_APPS) { "A mode can have at most $MAX_APPS apps" }
    }

    companion object {
        const val MAX_APPS = 8

        /** Default Focus mode: Mon–Fri, 9:00–17:00 */
        fun defaultFocus() = FocusMode(
            id = "default-focus",
            name = "Focus",
            schedule = Schedule(
                startMinutes = 9 * 60,      // 09:00
                endMinutes = 17 * 60,        // 17:00
                activeDays = setOf(
                    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
                )
            ),
            theme = ThemePreference.DARK,
            createdAt = Instant.ofEpochMilli(1000L) // oldest, so personal/winddown override
        )

        /** Default Personal mode: Mon–Fri 17:00–21:00, Sat–Sun all day */
        fun defaultPersonal() = FocusMode(
            id = "default-personal",
            name = "Personal",
            schedule = Schedule(
                startMinutes = 17 * 60,     // 17:00
                endMinutes = 21 * 60,        // 21:00
                activeDays = DayOfWeek.entries.toSet()
            ),
            theme = ThemePreference.SYSTEM,
            createdAt = Instant.ofEpochMilli(2000L)
        )

        /** Default Wind Down mode: every day 21:00–07:00 (overnight) */
        fun defaultWindDown() = FocusMode(
            id = "default-winddown",
            name = "Wind Down",
            schedule = Schedule(
                startMinutes = 21 * 60,     // 21:00
                endMinutes = 7 * 60,         // 07:00 next day
                activeDays = DayOfWeek.entries.toSet()
            ),
            theme = ThemePreference.DARK,
            createdAt = Instant.ofEpochMilli(3000L)
        )
    }
}
