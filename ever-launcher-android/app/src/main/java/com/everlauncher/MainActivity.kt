package com.everlauncher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.everlauncher.data.preferences.UserPreferencesStore
import com.everlauncher.domain.model.UserPreferences
import com.everlauncher.ui.launcher.LauncherScreen
import com.everlauncher.ui.onboarding.AppPickerScreen
import com.everlauncher.ui.onboarding.ModeSetupScreen
import com.everlauncher.ui.onboarding.OnboardingStep
import com.everlauncher.ui.onboarding.OnboardingViewModel
import com.everlauncher.ui.onboarding.SetDefaultLauncherScreen
import com.everlauncher.ui.onboarding.WelcomeScreen
import com.everlauncher.ui.theme.EverLauncherTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Intentional no-op for launcher home screen (predictive-back compatible)
        onBackPressedDispatcher.addCallback(this) {
            // no-op
        }
        enableEdgeToEdge()
        setContent {
            val prefsStore = remember { UserPreferencesStore(this) }
            val prefs by prefsStore.userPreferencesFlow.collectAsState(initial = UserPreferences())

            EverLauncherTheme(
                themePreference = prefs.globalTheme,
                fontChoice = prefs.fontChoice,
                fontSize = prefs.fontSize
            ) {
                if (!prefs.hasCompletedOnboarding) {
                    OnboardingFlow(onComplete = { /* prefs updated reactively via ViewModel */ })
                } else {
                    LauncherScreen(
                        onOpenSettings = {
                            startActivity(Intent(this, SettingsActivity::class.java))
                        }
                    )
                }
            }
        }
    }

    // Called when Home is pressed while EverLauncher is already the foreground activity
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

@Composable
private fun OnboardingFlow(onComplete: () -> Unit) {
    val vm: OnboardingViewModel = viewModel()
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current

    when (state.step) {
        OnboardingStep.WELCOME -> WelcomeScreen(
            onGetStarted = vm::nextStep
        )

        OnboardingStep.APP_PICKER -> AppPickerScreen(
            state = state,
            onToggleApp = vm::toggleApp,
            onNext = vm::nextStep
        )

        OnboardingStep.MODE_SETUP -> ModeSetupScreen(
            state = state,
            onComplete = vm::nextStep,
            onMoveUp = vm::moveModeUp,
            onMoveDown = vm::moveModeDown
        )

        OnboardingStep.SET_DEFAULT -> SetDefaultLauncherScreen(
            state = state,
            onResumeCheck = { vm.checkIsDefaultLauncher(context) },
            onComplete = {
                vm.completeOnboarding()
                onComplete()
            },
            onSkip = {
                vm.completeOnboarding()
                onComplete()
            }
        )
    }
}
