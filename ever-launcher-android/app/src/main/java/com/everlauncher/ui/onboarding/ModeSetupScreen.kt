package com.everlauncher.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.everlauncher.R

@Composable
fun ModeSetupScreen(
    state: OnboardingUiState,
    onComplete: () -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp)
            .systemBarsPadding()
    ) {
        Spacer(Modifier.height(48.dp))
        Text(
            text = stringResource(R.string.set_your_modes),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.onboarding_modes_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(state.modes) { index, mode ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mode.name.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(4.dp))
                            val start = "${mode.schedule.startMinutes / 60}:${(mode.schedule.startMinutes % 60).toString().padStart(2, '0')}"
                            val end = "${mode.schedule.endMinutes / 60}:${(mode.schedule.endMinutes % 60).toString().padStart(2, '0')}"
                            Text(
                                text = "$start – $end",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column {
                            IconButton(onClick = { onMoveUp(index) }, enabled = index > 0) {
                                Icon(Icons.Default.KeyboardArrowUp,
                                    contentDescription = stringResource(R.string.move_up),
                                    tint = if (index > 0) MaterialTheme.colorScheme.onSurface
                                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                            }
                            IconButton(onClick = { onMoveDown(index) }, enabled = index < state.modes.lastIndex) {
                                Icon(Icons.Default.KeyboardArrowDown,
                                    contentDescription = stringResource(R.string.move_down),
                                    tint = if (index < state.modes.lastIndex) MaterialTheme.colorScheme.onSurface
                                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(onClick = onComplete, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.looks_good))
        }
        Spacer(Modifier.height(24.dp))
    }
}
