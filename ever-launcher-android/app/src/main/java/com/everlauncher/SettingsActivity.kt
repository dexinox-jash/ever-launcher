package com.everlauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.everlauncher.data.preferences.UserPreferencesStore
import com.everlauncher.domain.model.FontChoice
import com.everlauncher.domain.model.FontSize
import com.everlauncher.domain.model.UserPreferences
import com.everlauncher.ui.dashboard.DashboardScreen
import com.everlauncher.ui.settings.GateConfigScreen
import com.everlauncher.ui.settings.ModeEditorScreen
import com.everlauncher.ui.settings.SettingsScreen
import com.everlauncher.ui.theme.EverLauncherTheme

private object Routes {
    const val DASHBOARD = "dashboard"
    const val SETTINGS = "settings"
    const val MODE_EDITOR = "mode_editor"
    const val GATE_CONFIG = "gate_config"
}

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefsStore = UserPreferencesStore(this)

        setContent {
            val prefs by prefsStore.userPreferencesFlow.collectAsState(initial = UserPreferences())
            EverLauncherTheme(themePreference = prefs.globalTheme, fontChoice = prefs.fontChoice, fontSize = prefs.fontSize) {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = Routes.SETTINGS) {
                    composable(Routes.SETTINGS) {
                        SettingsScreen(
                            onEditModes = { navController.navigate(Routes.MODE_EDITOR) },
                            onManageApps = { navController.navigate(Routes.DASHBOARD) },
                            onGateConfig = { navController.navigate(Routes.GATE_CONFIG) }
                        )
                    }
                    composable(Routes.DASHBOARD) {
                        DashboardScreen(
                            onEditModes = { navController.navigate(Routes.MODE_EDITOR) },
                            onManageApps = { navController.popBackStack() }
                        )
                    }
                    composable(Routes.MODE_EDITOR) {
                        ModeEditorScreen(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.GATE_CONFIG) {
                        GateConfigScreen()
                    }
                }
            }
        }
    }
}
