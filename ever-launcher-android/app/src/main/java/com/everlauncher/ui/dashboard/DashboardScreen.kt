package com.everlauncher.ui.dashboard

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.everlauncher.R

@Composable
fun DashboardScreen(
    onEditModes: () -> Unit,
    onManageApps: () -> Unit,
    modifier: Modifier = Modifier,
    vm: DashboardViewModel = viewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showUsageDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { Spacer(Modifier.height(48.dp)) }

        if (!state.hasUsagePermission) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showUsageDialog = true },
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.usage_access_banner),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { showUsageDialog = true }) {
                            Text(stringResource(R.string.enable_usage_access),
                                style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        item {
            FocusScoreRing(score = state.todayScore)
            Spacer(Modifier.height(24.dp))
        }

        if (state.currentStreak > 0) {
            item {
                Text(
                    text = "\uD83D\uDD25 ${stringResource(R.string.day_streak, state.currentStreak)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.best_streak, state.bestStreak),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))
            }
        }

        item {
            if (state.last7Days.isNotEmpty()) {
                WeeklyChart(days = state.last7Days, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(32.dp))
            }
        }

        item {
            state.currentMode?.let { mode ->
                Text(
                    text = stringResource(R.string.current_mode, mode.name),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
            }

            if (state.allModes.size > 1) {
                val otherModes = state.allModes.filter { it.id != state.currentMode?.id }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    otherModes.take(3).forEach { mode ->
                        OutlinedButton(
                            onClick = { vm.switchMode(mode.id) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(mode.name, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { vm.switchMode(null) }) {
                    Text(stringResource(R.string.follow_schedule),
                        style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(32.dp))
        }

        item {
            Button(onClick = onEditModes, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.edit_modes))
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onManageApps, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.manage_apps))
            }
            Spacer(Modifier.height(48.dp))
        }
    }

    if (showUsageDialog) {
        AlertDialog(
            onDismissRequest = { showUsageDialog = false },
            title = { Text(stringResource(R.string.usage_permission_disclosure_title)) },
            text = { Text(stringResource(R.string.usage_permission_disclosure_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showUsageDialog = false
                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }) {
                    Text(stringResource(R.string.open_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = { showUsageDialog = false }) {
                    Text(stringResource(R.string.dismiss))
                }
            }
        )
    }
}
