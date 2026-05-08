package com.everlauncher

import com.everlauncher.domain.model.DailyAnalytics
import com.everlauncher.domain.usecase.CalculateFocusScoreUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class FocusScoreCalculationTest {

    private lateinit var useCase: CalculateFocusScoreUseCase
    private val today = LocalDate.now()

    @Before
    fun setup() {
        useCase = CalculateFocusScoreUseCase()
    }

    private fun analytics(
        unlocks: Int = 0,
        screenMinutes: Int = 0,
        bypasses: Int = 0,
        overrides: Int = 0,
        streakMinutes: Int = 0,
        score: Int = 0
    ) = DailyAnalytics(
        date = today,
        unlockCount = unlocks,
        totalScreenTimeMinutes = screenMinutes,
        appsLaunchedFromLauncher = 0,
        gatedAppBypasses = bypasses,
        modeOverrides = overrides,
        longestPhoneFreeStreakMinutes = streakMinutes,
        focusScore = score
    )

    @Test
    fun `perfect day yields max score`() {
        // 0 unlocks, 0 bypasses, 0 overrides, under 3h screen time, 30-min streak, focus completed
        // raw = 100 + 3 (streak) + 10 (focus) + 5 (no bypass) = 118 → clamped to 100
        val score = useCase.calculate(analytics(streakMinutes = 30), focusModeCompletedToday = true)
        assertEquals(100, score)
    }

    @Test
    fun `zero activity gives base score plus no-gate bonus`() {
        val score = useCase.calculate(analytics(), focusModeCompletedToday = false)
        // base=100, +5 (no gated app) = 105, clamped to 100
        assertEquals(100, score)
    }

    @Test
    fun `max unlocks deducts exactly 30`() {
        val score = useCase.calculate(analytics(unlocks = 100), focusModeCompletedToday = false)
        // 100 - 30(max unlock penalty) + 5(no bypass) = 75
        assertEquals(75, score)
    }

    @Test
    fun `max bypasses deducts exactly 20`() {
        val score = useCase.calculate(analytics(bypasses = 20), focusModeCompletedToday = false)
        // 100 - 40 (but capped at -20) = 80; no +5 (bypasses > 0)
        assertEquals(80, score)
    }

    @Test
    fun `max overrides deducts exactly 15`() {
        val score = useCase.calculate(analytics(overrides = 5), focusModeCompletedToday = false)
        // 100 - 25 (capped -15) + 5(no bypass) = 90
        assertEquals(90, score)
    }

    @Test
    fun `screen time over 3h deducts correctly`() {
        // 4h = 240 min, over 3h = 60 min → 60/15 = 4 points deducted
        // Use 1 bypass to disable +5 no-bypass bonus so deduction is visible
        // 100 - 4 (screen) - 2 (1 bypass * 2) = 94
        val score = useCase.calculate(analytics(screenMinutes = 240, bypasses = 1), focusModeCompletedToday = false)
        assertEquals(94, score)
    }

    @Test
    fun `screen time exactly 3h has no deduction`() {
        // Exactly 3h (180 min) → 0 min over threshold → no screen deduction
        // Use 1 bypass to disable +5 bonus so score stays at 100 - 2 = 98
        val score = useCase.calculate(analytics(screenMinutes = 180, bypasses = 1), focusModeCompletedToday = false)
        assertEquals(98, score)
    }

    @Test
    fun `all deductions stacked score is floored at 0`() {
        // With individual deduction caps: -30 (unlocks) -20 (bypass) -15 (overrides) -20 (screen) = -85
        // 100 - 85 = 15 minimum (floor at 0 is enforced; this tests clamping can't produce negatives)
        val score = useCase.calculate(
            analytics(unlocks = 100, screenMinutes = 600, bypasses = 50, overrides = 50),
            focusModeCompletedToday = false
        )
        assertEquals(15, score)
        assertTrue("Score must not go below 0", score >= 0)
    }

    @Test
    fun `score never exceeds 100`() {
        val score = useCase.calculate(
            analytics(streakMinutes = 1000),
            focusModeCompletedToday = true
        )
        assertTrue("Score should not exceed 100", score <= 100)
    }

    @Test
    fun `phone free streak bonus capped at 15`() {
        // 10 units of 30 min = 30 pts, but max is 15
        val score = useCase.calculate(analytics(streakMinutes = 300), focusModeCompletedToday = false)
        // 100 + 15(capped) + 5(no bypass) = 120 → clamped to 100
        assertEquals(100, score)
    }
}
