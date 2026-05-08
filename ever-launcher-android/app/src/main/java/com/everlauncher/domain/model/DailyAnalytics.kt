package com.everlauncher.domain.model

import java.time.LocalDate

/**
 * Aggregate metrics for a single calendar day (local timezone).
 * All data stays on device — never transmitted anywhere.
 */
data class DailyAnalytics(
    val date: LocalDate,
    val unlockCount: Int = 0,
    /** Total screen time in minutes (from UsageStatsManager, if permission granted) */
    val totalScreenTimeMinutes: Int = 0,
    val appsLaunchedFromLauncher: Int = 0,
    val gatedAppBypasses: Int = 0,
    val modeOverrides: Int = 0,
    /** Longest consecutive period without phone use, in minutes */
    val longestPhoneFreeStreakMinutes: Int = 0,
    /** Computed focus score 0–100 */
    val focusScore: Int = 0
)
