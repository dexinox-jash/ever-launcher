package com.everlauncher.data.repository

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

class UsageStatsReader(private val context: Context) {
    data class Result(
        val unlockCount: Int,
        val totalScreenTimeMinutes: Int,
        val longestPhoneFreeStreakMinutes: Int
    )

    suspend fun readToday(): Result? = withContext(Dispatchers.IO) {
        try {
            val usageStatsManager =
                context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                    ?: return@withContext null
            val today = LocalDate.now()
            val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val now = System.currentTimeMillis()
            val events = usageStatsManager.queryEvents(startOfDay, now)
            var unlockCount = 0
            var totalScreenMs = 0L
            var lastScreenOnMs = -1L
            var longestFreeStreakMs = 0L
            var lastScreenOffMs = startOfDay
            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                when (event.eventType) {
                    UsageEvents.Event.KEYGUARD_HIDDEN -> {
                        unlockCount++
                        val streak = event.timeStamp - lastScreenOffMs
                        if (streak > longestFreeStreakMs) longestFreeStreakMs = streak
                    }
                    UsageEvents.Event.SCREEN_INTERACTIVE -> lastScreenOnMs = event.timeStamp
                    UsageEvents.Event.SCREEN_NON_INTERACTIVE -> {
                        if (lastScreenOnMs > 0) {
                            totalScreenMs += event.timeStamp - lastScreenOnMs
                            lastScreenOnMs = -1L
                        }
                        lastScreenOffMs = event.timeStamp
                    }
                }
            }
            if (lastScreenOnMs > 0) totalScreenMs += now - lastScreenOnMs
            Result(
                unlockCount = unlockCount,
                totalScreenTimeMinutes = (totalScreenMs / 60_000L).toInt(),
                longestPhoneFreeStreakMinutes = (longestFreeStreakMs / 60_000L).toInt()
            )
        } catch (_: SecurityException) { null }
        catch (_: Exception) { null }
    }
}
