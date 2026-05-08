package com.everlauncher.domain.usecase

import com.everlauncher.data.preferences.UserPreferencesStore
import com.everlauncher.data.repository.ModeRepository
import com.everlauncher.domain.model.FocusMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDateTime

class GetCurrentModeUseCase(
    private val modeRepository: ModeRepository,
    private val userPreferencesStore: UserPreferencesStore
) {
    fun currentModeFlow(): Flow<FocusMode?> =
        combine(
            modeRepository.getAllModesFlow(),
            userPreferencesStore.userPreferencesFlow
        ) { modes, prefs ->
            if (modes.isEmpty()) return@combine null
            if (prefs.currentModeOverrideId != null) {
                modes.find { it.id == prefs.currentModeOverrideId } ?: resolveBySchedule(modes)
            } else {
                resolveBySchedule(modes)
            }
        }

    suspend fun resolveCurrentMode(): FocusMode? {
        val modes = modeRepository.getAllModes()
        if (modes.isEmpty()) return null
        val prefs = userPreferencesStore.userPreferencesFlow.first()
        if (prefs.currentModeOverrideId != null) {
            return modes.find { it.id == prefs.currentModeOverrideId } ?: resolveBySchedule(modes)
        }
        return resolveBySchedule(modes)
    }

    private fun resolveBySchedule(modes: List<FocusMode>): FocusMode {
        val now = LocalDateTime.now()
        val minuteOfDay = now.hour * 60 + now.minute
        val dayOfWeek: DayOfWeek = now.dayOfWeek

        val activeModes = modes.filter { it.schedule.isActiveAt(minuteOfDay, dayOfWeek) }

        return when {
            activeModes.isEmpty() ->
                modes.maxByOrNull { it.createdAt } ?: modes.first()
            activeModes.size == 1 -> activeModes.first()
            else ->
                activeModes.maxByOrNull { it.createdAt }!!
        }
    }
}
