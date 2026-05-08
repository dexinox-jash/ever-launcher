package com.everlauncher.ui.settings

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.everlauncher.R
import com.everlauncher.data.db.EverDatabase
import com.everlauncher.data.repository.AppRepository
import com.everlauncher.data.repository.ModeRepository
import com.everlauncher.domain.model.AppItem
import com.everlauncher.domain.model.FocusMode
import com.everlauncher.domain.model.GateType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GateConfigUiState(
    val allApps: List<AppItem> = emptyList(),
    val modes: List<FocusMode> = emptyList(),
    val selectedModeId: String? = null   // null = "Global" tab
)

class GateConfigViewModel(
    application: Application,
    private val appRepository: AppRepository,
    private val modeRepository: ModeRepository
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application,
        AppRepository(EverDatabase.getInstance(application).appDao()),
        ModeRepository(EverDatabase.getInstance(application).modeDao(), EverDatabase.getInstance(application).appDao())
    )

    private val _selectedModeId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<GateConfigUiState> = combine(
        appRepository.getAllAppsFlow(),
        modeRepository.getAllModesFlow(),
        _selectedModeId
    ) { apps, modes, modeId ->
        GateConfigUiState(allApps = apps, modes = modes, selectedModeId = modeId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GateConfigUiState())

    fun selectMode(modeId: String?) { _selectedModeId.value = modeId }

    /** Toggle global gate for an app. */
    fun setGlobalGate(app: AppItem, isGated: Boolean, gateType: GateType?) {
        viewModelScope.launch {
            appRepository.updateApp(app.copy(isGated = isGated, gateType = gateType))
        }
    }

    /**
     * Set a per-mode gate override for [packageName] in [modeId].
     * [overrideValue]: null removes the override; "NONE" = explicitly ungate; GateType.name = force that gate.
     */
    fun setModeGateOverride(modeId: String, packageName: String, overrideValue: String?) {
        viewModelScope.launch {
            val mode = modeRepository.getModeById(modeId) ?: return@launch
            val updated = if (overrideValue == null) {
                mode.gateOverrides - packageName
            } else {
                mode.gateOverrides + (packageName to overrideValue)
            }
            modeRepository.updateModeGateOverrides(modeId, updated)
        }
    }
}

@Composable
fun GateConfigScreen(
    modifier: Modifier = Modifier,
    vm: GateConfigViewModel = viewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .systemBarsPadding()
    ) {
        Spacer(Modifier.height(48.dp))
        Text(
            stringResource(R.string.mindful_gates),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.mindful_gates_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        // Tab row: Global + one tab per mode
        val tabs = listOf(null) + state.modes.map { it.id }
        val tabLabels = listOf(stringResource(R.string.gate_tab_global)) + state.modes.map { it.name }
        val selectedIndex = tabs.indexOf(state.selectedModeId).coerceAtLeast(0)

        ScrollableTabRow(
            selectedTabIndex = selectedIndex,
            edgePadding = 0.dp,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, modeId ->
                Tab(
                    selected = index == selectedIndex,
                    onClick = { vm.selectMode(modeId) },
                    text = {
                        Text(
                            tabLabels[index],
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        if (state.selectedModeId == null) {
            // Global tab: configure app-level default gates
            GlobalGateList(
                apps = state.allApps,
                modifier = Modifier.weight(1f),
                onToggle = { app, enabled ->
                    vm.setGlobalGate(app, enabled, if (enabled) GateType.BREATHING else null)
                },
                onGateTypeChange = { app, type -> vm.setGlobalGate(app, true, type) }
            )
        } else {
            // Mode tab: configure per-mode gate overrides
            val mode = state.modes.find { it.id == state.selectedModeId }
            if (mode != null) {
                ModeGateList(
                    mode = mode,
                    allApps = state.allApps,
                    modifier = Modifier.weight(1f),
                    onOverrideChange = { pkg, value -> vm.setModeGateOverride(mode.id, pkg, value) }
                )
            }
        }
    }
}

@Composable
private fun GlobalGateList(
    apps: List<AppItem>,
    modifier: Modifier = Modifier,
    onToggle: (AppItem, Boolean) -> Unit,
    onGateTypeChange: (AppItem, GateType) -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(apps, key = { it.id }) { app ->
            GlobalGateAppRow(app = app, onToggle = onToggle, onGateTypeChange = onGateTypeChange)
        }
    }
}

@Composable
private fun GlobalGateAppRow(
    app: AppItem,
    onToggle: (AppItem, Boolean) -> Unit,
    onGateTypeChange: (AppItem, GateType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(app.displayName, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                if (app.isGated && app.gateType != null) {
                    Text(
                        app.gateType.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (app.isGated) {
                TextButton(onClick = { expanded = true }) { Text(stringResource(R.string.change)) }
            }
            Switch(checked = app.isGated, onCheckedChange = { onToggle(app, it) })
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), thickness = 0.5.dp)

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            GateType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    onClick = { onGateTypeChange(app, type); expanded = false }
                )
            }
        }
    }
}

/**
 * Per-mode gate override list. Shows ALL apps (not just mode apps) so users can configure
 * overrides for any app that might appear in this mode.
 *
 * Override options:
 * - "Use global default" (null) — no override stored
 * - "No gate in this mode" ("NONE") — explicitly ungate
 * - GateType names — override with that gate
 */
@Composable
private fun ModeGateList(
    mode: FocusMode,
    allApps: List<AppItem>,
    modifier: Modifier = Modifier,
    onOverrideChange: (packageName: String, overrideValue: String?) -> Unit
) {
    if (allApps.isEmpty()) {
        Box(modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_apps_in_mode), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn(modifier = modifier) {
        items(allApps, key = { it.id }) { app ->
            ModeGateAppRow(
                app = app,
                currentOverride = mode.gateOverrides[app.packageName],
                onOverrideChange = { value -> onOverrideChange(app.packageName, value) }
            )
        }
    }
}

@Composable
private fun ModeGateAppRow(
    app: AppItem,
    currentOverride: String?,   // null=use default, "NONE"=ungate, GateType.name=override
    onOverrideChange: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val overrideLabel = when (currentOverride) {
        null -> stringResource(R.string.gate_override_default)
        "NONE" -> stringResource(R.string.gate_override_none)
        else -> runCatching { GateType.valueOf(currentOverride).name.lowercase().replaceFirstChar { it.uppercase() } }
            .getOrDefault(stringResource(R.string.gate_override_default))
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(app.displayName, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                Text(
                    overrideLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = { expanded = true }) { Text(stringResource(R.string.change)) }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), thickness = 0.5.dp)

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            // Use global default
            DropdownMenuItem(
                text = { Text(stringResource(R.string.gate_override_default)) },
                onClick = { onOverrideChange(null); expanded = false }
            )
            // Explicitly ungate
            DropdownMenuItem(
                text = { Text(stringResource(R.string.gate_override_none)) },
                onClick = { onOverrideChange("NONE"); expanded = false }
            )
            // Each gate type
            GateType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    onClick = { onOverrideChange(type.name); expanded = false }
                )
            }
        }
    }
}
