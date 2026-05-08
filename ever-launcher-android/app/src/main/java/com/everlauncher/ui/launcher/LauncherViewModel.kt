package com.everlauncher.ui.launcher

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.everlauncher.data.db.EverDatabase
import com.everlauncher.data.preferences.UserPreferencesStore
import com.everlauncher.data.repository.AnalyticsRepository
import com.everlauncher.data.repository.ModeRepository
import com.everlauncher.domain.model.AppItem
import com.everlauncher.domain.model.FocusMode
import com.everlauncher.domain.model.GateType
import com.everlauncher.domain.model.ThemePreference
import com.everlauncher.domain.usecase.DetectInstalledAppsUseCase
import com.everlauncher.domain.usecase.GetCurrentModeUseCase
import com.everlauncher.domain.usecase.TrackAppLaunchUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class LauncherUiState(
    val currentTime: String = "",
    val currentDate: String = "",
    val currentMode: FocusMode? = null,
    val visibleApps: List<AppItem> = emptyList(),
    val allInstalledApps: List<AppItem> = emptyList(),
    val hiddenAppCount: Int = 0,
    val showHiddenCount: Boolean = true,
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val isSearchVisible: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<AppItem> = emptyList(),
    val pendingGateApp: AppItem? = null,
)

class LauncherViewModel(
    application: Application,
    private val prefsStore: UserPreferencesStore,
    private val modeRepository: ModeRepository,
    private val getCurrentModeUseCase: GetCurrentModeUseCase,
    private val detectInstalledAppsUseCase: DetectInstalledAppsUseCase,
    private val trackAppLaunchUseCase: TrackAppLaunchUseCase
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application,
        UserPreferencesStore(application),
        ModeRepository(EverDatabase.getInstance(application).modeDao(), EverDatabase.getInstance(application).appDao()),
        GetCurrentModeUseCase(
            ModeRepository(EverDatabase.getInstance(application).modeDao(), EverDatabase.getInstance(application).appDao()),
            UserPreferencesStore(application)
        ),
        DetectInstalledAppsUseCase(application),
        TrackAppLaunchUseCase(AnalyticsRepository(EverDatabase.getInstance(application).analyticsDao(), application))
    )

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d")

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    private var refreshModeReceiver: BroadcastReceiver? = null

    init {
        startClock()
        observeCurrentMode()
        observePrefs()
        initDefaults()
        loadInstalledApps()
        registerRefreshReceiver()
    }

    private fun startClock() {
        viewModelScope.launch {
            while (true) {
                val now = LocalDateTime.now()
                _uiState.update {
                    it.copy(
                        currentTime = now.format(timeFormatter),
                        currentDate = now.format(dateFormatter)
                    )
                }
                delay(30_000L)
            }
        }
    }

    private fun observeCurrentMode() {
        viewModelScope.launch {
            getCurrentModeUseCase.currentModeFlow().collectLatest { mode ->
                _uiState.update { state ->
                    val visible = applyGateOverrides(mode?.apps ?: emptyList(), mode)
                    state.copy(
                        currentMode = mode,
                        visibleApps = visible,
                        hiddenAppCount = (state.allInstalledApps.size - visible.size).coerceAtLeast(0)
                    )
                }
            }
        }
    }

    private fun applyGateOverrides(apps: List<AppItem>, mode: FocusMode?): List<AppItem> {
        val overrides = mode?.gateOverrides ?: return apps
        if (overrides.isEmpty()) return apps
        return apps.map { app ->
            when (val override = overrides[app.packageName]) {
                "NONE" -> app.copy(isGated = false, gateType = null)
                null -> app
                else -> {
                    val type = runCatching { GateType.valueOf(override) }.getOrNull()
                    if (type != null) app.copy(isGated = true, gateType = type) else app
                }
            }
        }
    }

    private fun observePrefs() {
        viewModelScope.launch {
            prefsStore.userPreferencesFlow.collectLatest { prefs ->
                _uiState.update {
                    it.copy(
                        showHiddenCount = prefs.showHiddenAppCount,
                        theme = prefs.globalTheme
                    )
                }
            }
        }
    }

    private fun initDefaults() {
        viewModelScope.launch {
            runCatching { modeRepository.initDefaultModesIfEmpty() }
        }
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            runCatching {
                val allApps = detectInstalledAppsUseCase.getInstalledLaunchableApps()
                _uiState.update { state ->
                    state.copy(
                        allInstalledApps = allApps,
                        hiddenAppCount = (allApps.size - state.visibleApps.size).coerceAtLeast(0)
                    )
                }
            }
        }
    }

    @android.annotation.SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun registerRefreshReceiver() {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                viewModelScope.launch {
                    runCatching {
                        val mode = getCurrentModeUseCase.resolveCurrentMode()
                        _uiState.update { state ->
                            val visible = applyGateOverrides(mode?.apps ?: emptyList(), mode)
                            state.copy(
                                currentMode = mode,
                                visibleApps = visible,
                                hiddenAppCount = (state.allInstalledApps.size - visible.size).coerceAtLeast(0)
                            )
                        }
                    }
                }
            }
        }
        val filter = IntentFilter("com.everlauncher.REFRESH_MODE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getApplication<Application>().registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            getApplication<Application>().registerReceiver(receiver, filter)
        }
        refreshModeReceiver = receiver
    }

    override fun onCleared() {
        super.onCleared()
        refreshModeReceiver?.let {
            getApplication<Application>().unregisterReceiver(it)
        }
        refreshModeReceiver = null
    }

    fun showSearch() = _uiState.update { it.copy(isSearchVisible = true) }

    fun hideSearch() = _uiState.update {
        it.copy(isSearchVisible = false, searchQuery = "", searchResults = emptyList())
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            val results = if (query.isBlank()) emptyList()
            else state.allInstalledApps.filter {
                it.displayName.contains(query, ignoreCase = true)
            }
            state.copy(searchQuery = query, searchResults = results)
        }
    }

    fun presentGate(app: AppItem) = _uiState.update { it.copy(pendingGateApp = app) }
    fun dismissGate() = _uiState.update { it.copy(pendingGateApp = null) }

    fun dismissGateAndLaunch(app: AppItem, context: Context) {
        dismissGate()
        launchAndTrack(app, context)
    }

    fun launchAndTrack(app: AppItem, context: Context) {
        viewModelScope.launch { runCatching { trackAppLaunchUseCase.trackLaunch() } }
        startAppActivity(context, app)
    }

    fun onGatePassed(app: AppItem, context: Context) {
        dismissGate()
        viewModelScope.launch {
            runCatching {
                trackAppLaunchUseCase.trackGatedBypass()
                trackAppLaunchUseCase.trackLaunch()
            }
        }
        startAppActivity(context, app)
    }

    private fun startAppActivity(context: Context, app: AppItem) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                context.startActivity(intent)
            } else {
                Toast.makeText(context,
                    context.getString(com.everlauncher.R.string.unable_to_open_reinstall, app.displayName),
                    Toast.LENGTH_SHORT).show()
            }
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context,
                context.getString(com.everlauncher.R.string.unable_to_open_app, app.displayName),
                Toast.LENGTH_SHORT).show()
        }
    }
}
