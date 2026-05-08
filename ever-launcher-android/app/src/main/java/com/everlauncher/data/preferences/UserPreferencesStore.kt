package com.everlauncher.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.everlauncher.domain.model.FontChoice
import com.everlauncher.domain.model.FontSize
import com.everlauncher.domain.model.ThemePreference
import com.everlauncher.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesStore(context: Context) {
    private val context = context.applicationContext

    private object Keys {
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val GLOBAL_THEME = stringPreferencesKey("global_theme")
        val FONT_CHOICE = stringPreferencesKey("font_choice")
        val FONT_SIZE = stringPreferencesKey("font_size")
        val SHOW_HIDDEN_APP_COUNT = booleanPreferencesKey("show_hidden_app_count")
        val SHOW_MODE_TIMER = booleanPreferencesKey("show_mode_timer")
        val CURRENT_MODE_OVERRIDE_ID = stringPreferencesKey("current_mode_override_id")
        val CURRENT_STREAK = intPreferencesKey("current_streak")
        val BEST_STREAK = intPreferencesKey("best_streak")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { prefs ->
            UserPreferences(
                hasCompletedOnboarding = prefs[Keys.HAS_COMPLETED_ONBOARDING] ?: false,
                globalTheme = prefs[Keys.GLOBAL_THEME]
                    ?.let { runCatching { ThemePreference.valueOf(it) }.getOrNull() }
                    ?: ThemePreference.SYSTEM,
                fontChoice = prefs[Keys.FONT_CHOICE]
                    ?.let { runCatching { FontChoice.valueOf(it) }.getOrNull() }
                    ?: FontChoice.SYSTEM,
                fontSize = prefs[Keys.FONT_SIZE]
                    ?.let { runCatching { FontSize.valueOf(it) }.getOrNull() }
                    ?: FontSize.MEDIUM,
                showHiddenAppCount = prefs[Keys.SHOW_HIDDEN_APP_COUNT] ?: true,
                showModeTimer = prefs[Keys.SHOW_MODE_TIMER] ?: true,
                currentModeOverrideId = prefs[Keys.CURRENT_MODE_OVERRIDE_ID],
                currentStreak = prefs[Keys.CURRENT_STREAK] ?: 0,
                bestStreak = prefs[Keys.BEST_STREAK] ?: 0
            )
        }

    suspend fun setOnboardingComplete() {
        context.dataStore.edit { it[Keys.HAS_COMPLETED_ONBOARDING] = true }
    }

    suspend fun setGlobalTheme(theme: ThemePreference) {
        context.dataStore.edit { it[Keys.GLOBAL_THEME] = theme.name }
    }

    suspend fun setFontChoice(font: FontChoice) {
        context.dataStore.edit { it[Keys.FONT_CHOICE] = font.name }
    }

    suspend fun setFontSize(size: FontSize) {
        context.dataStore.edit { it[Keys.FONT_SIZE] = size.name }
    }

    suspend fun setShowHiddenAppCount(show: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_HIDDEN_APP_COUNT] = show }
    }

    suspend fun setShowModeTimer(show: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_MODE_TIMER] = show }
    }

    suspend fun setModeOverride(modeId: String?) {
        context.dataStore.edit {
            if (modeId != null) it[Keys.CURRENT_MODE_OVERRIDE_ID] = modeId
            else it.remove(Keys.CURRENT_MODE_OVERRIDE_ID)
        }
    }

    suspend fun updateStreak(current: Int, best: Int) {
        context.dataStore.edit {
            it[Keys.CURRENT_STREAK] = current
            it[Keys.BEST_STREAK] = best
        }
    }
}
