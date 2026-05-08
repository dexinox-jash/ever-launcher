package com.everlauncher.ui.dashboard

import android.app.Application
import android.content.Context
import android.os.Process
import android.app.AppOpsManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.everlauncher.data.db.EverDatabase
import com.everlauncher.data.preferences.UserPreferencesStore
import com.everlauncher.data.repository.AnalyticsRepository
import com.everlauncher.data.repository.ModeRepository
import com.everlauncher.domain.model.DailyAnalytics
import com.everlauncher.domain.model.FocusMode
import com.everlauncher.domain.usecase.CalculateFocusScoreUseCase
import com.everlauncher.domain.usecase.GetCurrentModeUseCase
import com.everlauncher.service.UsageTrackingService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.first

data class DashboardUiState(
    val todayScore: Int = 0,
    val last7Days: List<DailyAnalytics> = emptyList(),
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val currentMode: FocusMode? = null,
    val allModes: List<FocusMode> = emptyList(),
    val hasUsagePermission: Boolean = false
)

class DashboardViewModel(
    application: Application,
    private val analyticsRepository: AnalyticsRepository,
    private val modeRepository: ModeRepository,
    private val getCurrentModeUseCase: GetCurrentModeUseCase,
    private val calculateScoreUseCase: CalculateFocusScoreUseCase,
    private val usageService: UsageTrackingService,
    private val prefsStore: UserPreferencesStore
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application,
        AnalyticsRepository(EverDatabase.getInstance(application).analyticsDao(), application),
        ModeRepository(EverDatabase.getInstance(application).modeDao(), EverDatabase.getInstance(application).appDao()),
        GetCurrentModeUseCase(
            ModeRepository(EverDatabase.getInstance(application).modeDao(), EverDatabase.getInstance(application).appDao()),
            UserPreferencesStore(application)
        ),
        CalculateFocusScoreUseCase(),
        UsageTrackingService(application, EverDatabase.getInstance(application).analyticsDao()),
        UserPreferencesStore(application)
    )

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(hasUsagePermission = checkUsagePermission()) }
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch { runCatching { usageService.syncTodayUsageStats() } }

        viewModelScope.launch {
            combine(
                analyticsRepository.getLast7DaysFlow(),
                getCurrentModeUseCase.currentModeFlow(),
                modeRepository.getAllModesFlow()
            ) { days, currentMode, allModes -> Triple(days, currentMode, allModes) }
                .collectLatest { (days, currentMode, allModes) ->
                    val focusDone = isFocusModeCompletedToday(allModes)
                    val today = days.firstOrNull()
                    val score = if (today != null) calculateScoreUseCase.calculate(today, focusDone) else 0

                    val allDays = runCatching { analyticsRepository.getAllAnalytics() }.getOrDefault(emptyList())
                    val savedBest = prefsStore.userPreferencesFlow.first().bestStreak
                    val (streak, best) = calculateStreak(allDays, savedBest)

                    _uiState.update {
                        it.copy(
                            last7Days = days, todayScore = score,
                            currentMode = currentMode, allModes = allModes,
                            currentStreak = streak, bestStreak = best
                        )
                    }
                    if (today != null) runCatching { analyticsRepository.updateFocusScore(score) }
                    prefsStore.updateStreak(streak, best)
                }
        }
    }

    fun switchMode(modeId: String?) {
        viewModelScope.launch {
            prefsStore.setModeOverride(modeId)
            runCatching { analyticsRepository.incrementModeOverrides() }
        }
    }

    private fun isFocusModeCompletedToday(modes: List<FocusMode>): Boolean {
        val now = LocalDateTime.now()
        val minuteOfDay = now.hour * 60 + now.minute
        val day = now.dayOfWeek
        return modes.any { m ->
            m.schedule.activeDays.contains(day) &&
            !m.schedule.isOvernight &&
            m.schedule.endMinutes in 1..minuteOfDay
        }
    }

    private fun calculateStreak(allDays: List<DailyAnalytics>, savedBest: Int): Pair<Int, Int> {
        val qualifying = allDays.filter { it.focusScore >= 60 }.map { it.date }.sortedDescending()
        if (qualifying.isEmpty()) return Pair(0, savedBest)
        val today = LocalDate.now()
        val mostRecent = qualifying.first()
        if (mostRecent != today && mostRecent != today.minusDays(1)) {
            return Pair(0, savedBest)
        }
        var streak = 1
        for (i in 1 until qualifying.size) {
            if (qualifying[i] == qualifying[i - 1].minusDays(1)) {
                streak++
            } else {
                break
            }
        }
        val best = maxOf(streak, savedBest)
        return Pair(streak, best)
    }

    private fun checkUsagePermission(): Boolean = try {
        val appOps = getApplication<Application>().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(),
            getApplication<Application>().packageName) == AppOpsManager.MODE_ALLOWED
    } catch (_: Exception) { false }
}
