package com.everlauncher.domain.model

enum class FontChoice { SYSTEM, SERIF, MONOSPACE, ROUNDED }
enum class FontSize { SMALL, MEDIUM, LARGE }

/**
 * User settings and state stored in DataStore.
 */
data class UserPreferences(
    val hasCompletedOnboarding: Boolean = false,
    val globalTheme: ThemePreference = ThemePreference.SYSTEM,
    val fontChoice: FontChoice = FontChoice.SYSTEM,
    val fontSize: FontSize = FontSize.MEDIUM,
    val showHiddenAppCount: Boolean = true,
    val showModeTimer: Boolean = true,
    /** ID of the manually-selected mode override, null = follow schedule */
    val currentModeOverrideId: String? = null,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0
)
