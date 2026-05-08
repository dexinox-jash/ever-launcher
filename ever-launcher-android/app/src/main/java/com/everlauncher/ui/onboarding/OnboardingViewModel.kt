package com.everlauncher.ui.onboarding

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.everlauncher.data.db.EverDatabase
import com.everlauncher.data.preferences.UserPreferencesStore
import com.everlauncher.data.repository.AppRepository
import com.everlauncher.data.repository.ModeRepository
import com.everlauncher.domain.model.AppItem
import com.everlauncher.domain.model.FocusMode
import com.everlauncher.domain.usecase.DetectInstalledAppsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val installedApps: List<AppItem> = emptyList(),
    val selectedAppIds: Set<String> = emptySet(),
    val modes: List<FocusMode> = listOf(
        FocusMode.defaultFocus(),
        FocusMode.defaultPersonal(),
        FocusMode.defaultWindDown()
    ),
    val isLoading: Boolean = false,
    val isDefaultLauncher: Boolean = false
)

enum class OnboardingStep { WELCOME, APP_PICKER, MODE_SETUP, SET_DEFAULT }

private val DEFAULT_APP_NAMES = setOf(
    "calendar", "mail", "notes", "slack", "files",
    "phone", "messages", "camera", "maps", "music",
    "clock", "contacts", "settings"
)

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = EverDatabase.getInstance(application)
    private val appRepository = AppRepository(db.appDao())
    private val modeRepository = ModeRepository(db.modeDao(), db.appDao())
    private val prefsStore = UserPreferencesStore(application)
    private val detectAppsUseCase = DetectInstalledAppsUseCase(application)

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun loadInstalledApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val apps = detectAppsUseCase.getInstalledLaunchableApps()
            val preSelected = apps
                .filter { app -> DEFAULT_APP_NAMES.any { app.displayName.lowercase().contains(it) } }
                .map { it.id }
                .toSet()
            _uiState.update {
                it.copy(
                    installedApps = apps,
                    selectedAppIds = preSelected,
                    isLoading = false
                )
            }
        }
    }

    fun toggleApp(appId: String) {
        _uiState.update { state ->
            val newSelected = state.selectedAppIds.toMutableSet()
            if (appId in newSelected) newSelected.remove(appId) else newSelected.add(appId)
            state.copy(selectedAppIds = newSelected)
        }
    }

    fun nextStep() {
        _uiState.update { state ->
            val next = when (state.step) {
                OnboardingStep.WELCOME -> OnboardingStep.APP_PICKER
                OnboardingStep.APP_PICKER -> OnboardingStep.MODE_SETUP
                OnboardingStep.MODE_SETUP -> OnboardingStep.SET_DEFAULT
                OnboardingStep.SET_DEFAULT -> OnboardingStep.SET_DEFAULT
            }
            state.copy(step = next)
        }
        if (_uiState.value.step == OnboardingStep.APP_PICKER) loadInstalledApps()
    }

    fun moveModeUp(index: Int) {
        if (index <= 0) return
        _uiState.update { state ->
            val list = state.modes.toMutableList()
            val tmp = list[index - 1]; list[index - 1] = list[index]; list[index] = tmp
            state.copy(modes = list)
        }
    }

    fun moveModeDown(index: Int) {
        _uiState.update { state ->
            if (index >= state.modes.size - 1) return@update state
            val list = state.modes.toMutableList()
            val tmp = list[index + 1]; list[index + 1] = list[index]; list[index] = tmp
            state.copy(modes = list)
        }
    }

    fun checkIsDefaultLauncher(context: Context) {
        val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }
        val resolveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.resolveActivity(
                intent,
                android.content.pm.PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        }
        val isDefault = resolveInfo?.activityInfo?.packageName == context.packageName
        _uiState.update { it.copy(isDefaultLauncher = isDefault) }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            val state = _uiState.value
            val selectedApps = state.installedApps.filter { it.id in state.selectedAppIds }
            appRepository.insertApps(selectedApps)
            val chunked = selectedApps.chunked(FocusMode.MAX_APPS)
            state.modes.forEachIndexed { index, mode ->
                val appsForMode = chunked.getOrElse(index) { emptyList() }
                modeRepository.insertMode(mode.copy(apps = appsForMode, sortOrder = index))
            }
            prefsStore.setOnboardingComplete()
        }
    }
}
