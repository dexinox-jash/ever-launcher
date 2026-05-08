package com.everlauncher.domain.usecase

import com.everlauncher.domain.model.DailyAnalytics
import kotlin.math.max
import kotlin.math.min

/**
 * Computes the daily focus score 0–100 using the exact formula from the spec §3.2.2.
 *
 * Deductions:
 *   -1 per unlock (max -30)
 *   -2 per gated bypass (max -20)
 *   -5 per mode override (max -15)
 *   -1 per 15 min screen time over 3h (max -20)
 *
 * Bonuses:
 *   +3 per 30-min phone-free streak (max +15)
 *   +10 if all focus mode hours completed today
 *   +5 if no gated app was opened
 *
 * Final = max(0, min(100, base + deductions + bonuses))
 */
class CalculateFocusScoreUseCase {

    fun calculate(analytics: DailyAnalytics, focusModeCompletedToday: Boolean): Int {
        var score = 100

        // Deductions
        score -= min(analytics.unlockCount, 30)
        score -= min(analytics.gatedAppBypasses * 2, 20)
        score -= min(analytics.modeOverrides * 5, 15)
        val screenTimeOverMinutes = max(0, analytics.totalScreenTimeMinutes - 180)
        score -= min(screenTimeOverMinutes / 15, 20)

        // Bonuses
        val streakUnits = analytics.longestPhoneFreeStreakMinutes / 30
        score += min(streakUnits * 3, 15)
        if (focusModeCompletedToday) score += 10
        if (analytics.gatedAppBypasses == 0) score += 5

        return max(0, min(100, score))
    }
}
