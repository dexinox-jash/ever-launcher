package com.everlauncher.data.repository

import android.content.Context
import com.everlauncher.data.db.AnalyticsDao
import com.everlauncher.data.db.entities.AnalyticsEntity
import com.everlauncher.domain.model.DailyAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate

class AnalyticsRepository(
    private val analyticsDao: AnalyticsDao,
    context: Context
) {
    private val usageStatsReader = UsageStatsReader(context)

    fun getLast7DaysFlow(): Flow<List<DailyAnalytics>> =
        analyticsDao.getLast7DaysFlow().map { list -> list.map { it.toDomain() } }

    suspend fun getTodayAnalytics(): DailyAnalytics {
        val today = LocalDate.now()
        return analyticsDao.getByDate(today.toEpochDay())?.toDomain()
            ?: DailyAnalytics(date = today)
    }

    suspend fun ensureTodayExists() {
        val epochDay = LocalDate.now().toEpochDay()
        if (analyticsDao.getByDate(epochDay) == null) {
            analyticsDao.upsert(AnalyticsEntity(dateEpochDay = epochDay))
        }
    }

    suspend fun incrementAppsLaunched() {
        ensureTodayExists()
        analyticsDao.incrementAppsLaunched(LocalDate.now().toEpochDay())
    }

    suspend fun incrementGatedBypasses() {
        ensureTodayExists()
        analyticsDao.incrementGatedBypasses(LocalDate.now().toEpochDay())
    }

    suspend fun incrementModeOverrides() {
        ensureTodayExists()
        analyticsDao.incrementModeOverrides(LocalDate.now().toEpochDay())
    }

    suspend fun updateFocusScore(score: Int) {
        val epochDay = LocalDate.now().toEpochDay()
        val existing = analyticsDao.getByDate(epochDay) ?: AnalyticsEntity(dateEpochDay = epochDay)
        analyticsDao.upsert(existing.copy(focusScore = score))
    }

    suspend fun getAllAnalytics(): List<DailyAnalytics> =
        analyticsDao.getAll().map { it.toDomain() }

    suspend fun syncUsageStats() = withContext(Dispatchers.IO) {
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
