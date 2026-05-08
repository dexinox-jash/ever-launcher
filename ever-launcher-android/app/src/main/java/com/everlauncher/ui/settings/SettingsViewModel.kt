package com.everlauncher.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.everlauncher.data.db.EverDatabase
import com.everlauncher.data.preferences.UserPreferencesStore
import com.everlauncher.data.repository.ModeRepository
import com.everlauncher.domain.model.AppItem
import com.everlauncher.domain.model.FontChoice
import com.everlauncher.domain.model.FontSize
import com.everlauncher.domain.model.FocusMode
import com.everlauncher.domain.model.ThemePreference
import com.everlauncher.domain.model.UserPreferences
import com.everlauncher.domain.usecase.DetectInstalledAppsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
    private val prefsStore: UserPreferencesStore,
    private val modeRepository: ModeRepository,
    private val detectInstalledAppsUseCase: DetectInstalledAppsUseCase
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application,
        UserPreferencesStore(application),
        ModeRepository(EverDatabase.getInstance(application).modeDao(), EverDatabase.getInstance(application).appDao()),
        DetectInstalledAppsUseCase(application)
    )

    val prefs: StateFlow<UserPreferences> = prefsStore.userPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences())

    val modes: StateFlow<List<FocusMode>> = modeRepository.getAllModesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _installedApps = MutableStateFlow<List<AppItem>>(emptyList())
    val installedApps: StateFlow<List<AppItem>> = _installedApps.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { _installedApps.value = detectInstalledAppsUseCase.getInstalledLaunchableApps() }
        }
    }

    fun setTheme(theme: ThemePreference) = viewModelScope.launch { prefsStore.setGlobalTheme(theme) }
    fun setFontChoice(font: FontChoice) = viewModelScope.launch { prefsStore.setFontChoice(font) }
    fun setFontSize(size: FontSize) = viewModelScope.launch { prefsStore.setFontSize(size) }
    fun setShowHiddenAppCount(show: Boolean) = viewModelScope.launch { prefsStore.setShowHiddenAppCount(show) }
    fun setShowModeTimer(show: Boolean) = viewModelScope.launch { prefsStore.setShowModeTimer(show) }
    fun setModeOverride(modeId: String?) = viewModelScope.launch { prefsStore.setModeOverride(modeId) }

    fun deleteMode(modeId: String) = viewModelScope.launch { runCatching { modeRepository.deleteMode(modeId) } }
    fun saveMode(mode: FocusMode) = viewModelScope.launch {
        runCatching {
            if (modeRepository.getModeById(mode.id) != null) modeRepository.updateMode(mode)
            else modeRepository.insertMode(mode)
        }
    }
}
