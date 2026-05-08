package com.everlauncher.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.everlauncher.R
import com.everlauncher.domain.model.FontChoice
import com.everlauncher.domain.model.FontSize
import com.everlauncher.domain.model.ThemePreference

@Composable
fun SettingsScreen(
    onEditModes: () -> Unit,
    onManageApps: () -> Unit,
    onGateConfig: () -> Unit,
    modifier: Modifier = Modifier,
    vm: SettingsViewModel = viewModel()
) {
    val prefs by vm.prefs.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val versionName = remember {
        try {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "—"
        } catch (e: Exception) { "—" }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .systemBarsPadding()
    ) {
        item { Spacer(Modifier.height(48.dp)) }
        item {
            Text(
                stringResource(R.string.settings),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(32.dp))
        }

        // APPEARANCE
        item {
            SectionHeader(stringResource(R.string.section_appearance))
            SettingsRow(stringResource(R.string.theme), prefs.globalTheme.name.lowercase().replaceFirstChar { it.uppercase() }) {
                // Cycle through themes
                val next = when (prefs.globalTheme) {
                    ThemePreference.SYSTEM -> ThemePreference.LIGHT
                    ThemePreference.LIGHT -> ThemePreference.DARK
                    ThemePreference.DARK -> ThemePreference.AMOLED
                    ThemePreference.AMOLED -> ThemePreference.SYSTEM
                }
                vm.setTheme(next)
            }
            SettingsRow(stringResource(R.string.font), prefs.fontChoice.name.lowercase().replaceFirstChar { it.uppercase() }) {
                val entries = FontChoice.entries
                vm.setFontChoice(entries[(entries.indexOf(prefs.fontChoice) + 1) % entries.size])
            }
            SettingsRow(stringResource(R.string.font_size), prefs.fontSize.name.lowercase().replaceFirstChar { it.uppercase() }) {
                val entries = FontSize.entries
                vm.setFontSize(entries[(entries.indexOf(prefs.fontSize) + 1) % entries.size])
            }
            Spacer(Modifier.height(24.dp))
        }

        // LAUNCHER
        item {
            SectionHeader(stringResource(R.string.section_launcher))
            SettingsToggleRow(stringResource(R.string.show_hidden_app_count), prefs.showHiddenAppCount, vm::setShowHiddenAppCount)
            SettingsToggleRow(stringResource(R.string.show_mode_timer), prefs.showModeTimer, vm::setShowModeTimer)
            Spacer(Modifier.height(24.dp))
        }

        // FOCUS
        item {
            SectionHeader(stringResource(R.string.section_focus))
            SettingsRow(stringResource(R.string.mindful_gates), stringResource(R.string.configure)) { onGateConfig() }
            SettingsRow(stringResource(R.string.focus_score_target), "60") {}
            Spacer(Modifier.height(24.dp))
        }

        // ABOUT
        item {
            SectionHeader(stringResource(R.string.section_about))
            SettingsRow(stringResource(R.string.privacy_policy), "") { uriHandler.openUri("https://dexinox-jash.github.io/ever-launcher/privacy") }
            SettingsRow(stringResource(R.string.terms_of_use), "") { uriHandler.openUri("https://dexinox-jash.github.io/ever-launcher/terms") }
            SettingsRow(label = stringResource(R.string.version_label), value = versionName) {}
            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun SettingsRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), thickness = 0.5.dp)
}

@Composable
private fun SettingsToggleRow(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
        Switch(checked = checked, onCheckedChange = onToggle)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), thickness = 0.5.dp)
}
