package com.everlauncher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.everlauncher.data.db.EverDatabase
import com.everlauncher.data.repository.ModeRepository
import com.everlauncher.domain.usecase.ScheduleModeTransitionsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ModeTransitionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope.launch {
            try {
                when (intent.action) {
                    Intent.ACTION_BOOT_COMPLETED -> handleBoot(context)
                    "com.everlauncher.MODE_TRANSITION" -> handleModeTransition(context, intent)
                }
            } finally {
                scope.cancel()
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleBoot(context: Context) {
        val db = EverDatabase.getInstance(context)
        val scheduleUseCase = ScheduleModeTransitionsUseCase(
            context,
            ModeRepository(db.modeDao(), db.appDao())
        )
        scheduleUseCase.scheduleNext24Hours()
    }

    private suspend fun handleModeTransition(context: Context, intent: Intent) {
        val refreshIntent = Intent("com.everlauncher.REFRESH_MODE").apply {
            setPackage(context.packageName)
            intent.getStringExtra(ScheduleModeTransitionsUseCase.EXTRA_MODE_ID)
                ?.let { putExtra(ScheduleModeTransitionsUseCase.EXTRA_MODE_ID, it) }
        }
        context.sendBroadcast(refreshIntent)

        val db = EverDatabase.getInstance(context)
        val scheduleUseCase = ScheduleModeTransitionsUseCase(
            context,
            ModeRepository(db.modeDao(), db.appDao())
        )
        scheduleUseCase.scheduleNext24Hours()
    }
}
