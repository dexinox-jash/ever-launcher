package com.everlauncher.service

import android.content.Context
import com.everlauncher.data.db.AnalyticsDao
import com.everlauncher.data.db.entities.AnalyticsEntity
import com.everlauncher.data.repository.UsageStatsReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class UsageTrackingService(
    context: Context,
    private val analyticsDao: AnalyticsDao
) {
    private val usageStatsReader = UsageStatsReader(context)

    suspend fun syncTodayUsageStats() = withContext(Dispatchers.IO) {
        val result = usageStatsReader.readToday() ?: return@withContext
        val epochDay = LocalDate.now().toEpochDay()
        val existing = analyticsDao.getByDate(epochDay) ?: AnalyticsEntity(dateEpochDay = epochDay)
        analyticsDao.upsert(
            existing.copy(
                unlockCount = result.unlockCount,
                totalScreenTimeMinutes = result.totalScreenTimeMinutes,
                longestPhoneFreeStreakMinutes = result.longestPhoneFreeStreakMinutes
            )
        )
    }
}
