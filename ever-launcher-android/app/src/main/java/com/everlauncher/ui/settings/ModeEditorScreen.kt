package com.everlauncher.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import com.everlauncher.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.everlauncher.domain.model.AppItem
import com.everlauncher.domain.model.FocusMode
import com.everlauncher.domain.model.Schedule
import java.time.DayOfWeek

@Composable
fun ModeEditorScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    vm: SettingsViewModel = viewModel()
) {
    val modes by vm.modes.collectAsStateWithLifecycle()
    val installedApps by vm.installedApps.collectAsStateWithLifecycle()
    var editingMode by remember { mutableStateOf<FocusMode?>(null) }

    if (editingMode != null) {
        ModeEditForm(
            mode = editingMode!!,
            allModes = modes,
            installedApps = installedApps,
            onSave = { updated ->
                vm.saveMode(updated)
                editingMode = null
            },
            onCancel = { editingMode = null }
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .systemBarsPadding()
    ) {
        Spacer(Modifier.height(48.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.edit_modes), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
            TextButton(onClick = { editingMode = FocusMode.defaultFocus().copy(id = java.util.UUID.randomUUID().toString(), name = "") }) {
                Text(stringResource(R.string.add_mode))
            }
        }
        Spacer(Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(modes, key = { it.id }) { mode ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                mode.name.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(2.dp))
                            val start = formatMinutes(mode.schedule.startMinutes)
                            val end = formatMinutes(mode.schedule.endMinutes)
                            val dayAbbrevs = mode.schedule.activeDays
                                .sortedBy { it.value }
                                .joinToString("") { it.name.take(1) }
                            Text(
                                "$start – $end · $dayAbbrevs · ${stringResource(R.string.mode_apps_count, mode.apps.size)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row {
                            TextButton(onClick = { editingMode = mode }) { Text(stringResource(R.string.edit)) }
                            IconButton(onClick = { vm.deleteMode(mode.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeEditForm(
    mode: FocusMode,
    allModes: List<FocusMode>,
    installedApps: List<AppItem>,
    onSave: (FocusMode) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(mode.name) }
    var startMinutes by remember { mutableIntStateOf(mode.schedule.startMinutes) }
    var endMinutes by remember { mutableIntStateOf(mode.schedule.endMinutes) }
    var activeDays by remember { mutableStateOf(mode.schedule.activeDays) }
    val selectedApps = remember { mutableStateListOf<AppItem>().also { it.addAll(mode.apps) } }

    // Detect schedule conflict with other modes
    val hasConflict = remember(startMinutes, endMinutes, activeDays) {
        allModes.filter { it.id != mode.id }.any { other ->
            activeDays.any { day ->
                day in other.schedule.activeDays && schedulesOverlap(
                    startMinutes, endMinutes,
                    other.schedule.startMinutes, other.schedule.endMinutes
                )
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .systemBarsPadding()
    ) {
        item {
            Spacer(Modifier.height(48.dp))
            Text(
                if (mode.name.isEmpty()) stringResource(R.string.new_mode_title) else stringResource(R.string.edit_mode_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(24.dp))

            val context = LocalContext.current
            val alarmManager = remember(context) { context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            stringResource(R.string.exact_alarm_permission_body),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = {
                            val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = android.net.Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }) {
                            Text(stringResource(R.string.allow_exact_alarms), color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.mode_name_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))

            // --- Schedule section ---
            Text(stringResource(R.string.schedule_label), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(12.dp))

            // Start time slider
            Text(
                stringResource(R.string.start_time, formatMinutes(startMinutes)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = startMinutes.toFloat(),
                onValueChange = { startMinutes = snapTo15(it) },
                valueRange = 0f..1440f,
                steps = 95, // 15-minute resolution: 96 positions
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // End time slider
            Text(
                stringResource(R.string.end_time, formatMinutes(endMinutes)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = endMinutes.toFloat(),
                onValueChange = { endMinutes = snapTo15(it) },
                valueRange = 0f..1440f,
                steps = 95,
                modifier = Modifier.fillMaxWidth()
            )

            if (endMinutes < startMinutes) {
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.overnight_note, formatMinutes(startMinutes), formatMinutes(endMinutes)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(12.dp))

            // Day-of-week chips
            Text(stringResource(R.string.active_days_label), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DayOfWeek.entries.forEach { day ->
                    val selected = day in activeDays
                    FilterChip(
                        selected = selected,
                        onClick = {
                            activeDays = if (selected) {
                                (activeDays - day).ifEmpty { activeDays } // keep at least one day
                            } else {
                                activeDays + day
                            }
                        },
                        label = { Text(day.name.take(1), style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            if (hasConflict) {
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.schedule_conflict_warning),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(24.dp))

            // --- App assignment section ---
            Text(stringResource(R.string.apps_max_title, FocusMode.MAX_APPS), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Text(
                stringResource(R.string.apps_selected_count, selectedApps.size, FocusMode.MAX_APPS),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(8.dp))
        }

        items(installedApps, key = { it.packageName }) { app ->
            val isSelected = selectedApps.any { it.packageName == app.packageName }
            val atMax = selectedApps.size >= FocusMode.MAX_APPS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { checked ->
                        if (checked && !isSelected && !atMax) {
                            selectedApps.add(app)
                        } else if (!checked && isSelected) {
                            selectedApps.removeIf { it.packageName == app.packageName }
                        }
                    },
                    enabled = isSelected || !atMax
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    app.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected || !atMax) MaterialTheme.colorScheme.onBackground
                            else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.cancel)) }
                Button(
                    onClick = {
                        onSave(
                            mode.copy(
                                name = name,
                                schedule = Schedule(
                                    startMinutes = startMinutes,
                                    endMinutes = endMinutes,
                                    activeDays = activeDays
                                ),
                                apps = selectedApps.toList()
                            )
                        )
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.save)) }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

private fun formatMinutes(minutes: Int): String {
    val h = minutes / 60
    val m = (minutes % 60).toString().padStart(2, '0')
    return "$h:$m"
}

/** Snaps a raw slider float to the nearest 15-minute boundary. */
private fun snapTo15(value: Float): Int = (value / 15f).roundToInt() * 15

private fun schedulesOverlap(aStart: Int, aEnd: Int, bStart: Int, bEnd: Int): Boolean {
    fun segments(start: Int, end: Int): List<IntRange> =
        if (end > start) listOf(start..end) else listOf(start..1440, 0..end)
    val aSegments = segments(aStart, aEnd)
    val bSegments = segments(bStart, bEnd)
    return aSegments.any { a -> bSegments.any { b -> a.first <= b.last && b.first <= a.last } }
}
