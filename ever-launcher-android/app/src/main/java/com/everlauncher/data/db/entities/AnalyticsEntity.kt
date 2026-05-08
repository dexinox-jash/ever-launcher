package com.everlauncher.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.everlauncher.domain.model.DailyAnalytics
import java.time.LocalDate

@Entity(tableName = "analytics")
data class AnalyticsEntity(
    /** Epoch day — unique per calendar day */
    @PrimaryKey @ColumnInfo(name = "date_epoch_day") val dateEpochDay: Long,
    @ColumnInfo(name = "unlock_count") val unlockCount: Int = 0,
    @ColumnInfo(name = "total_screen_time_minutes") val totalScreenTimeMinutes: Int = 0,
    @ColumnInfo(name = "apps_launched") val appsLaunchedFromLauncher: Int = 0,
    @ColumnInfo(name = "gated_bypasses") val gatedAppBypasses: Int = 0,
    @ColumnInfo(name = "mode_overrides") val modeOverrides: Int = 0,
    @ColumnInfo(name = "longest_free_streak_minutes") val longestPhoneFreeStreakMinutes: Int = 0,
    @ColumnInfo(name = "focus_score") val focusScore: Int = 0
) {
    fun toDomain(): DailyAnalytics = DailyAnalytics(
        date = LocalDate.ofEpochDay(dateEpochDay),
        unlockCount = unlockCount,
        totalScreenTimeMinutes = totalScreenTimeMinutes,
        appsLaunchedFromLauncher = appsLaunchedFromLauncher,
        gatedAppBypasses = gatedAppBypasses,
        modeOverrides = modeOverrides,
        longestPhoneFreeStreakMinutes = longestPhoneFreeStreakMinutes,
        focusScore = focusScore
    )

    companion object {
        fun fromDomain(a: DailyAnalytics) = AnalyticsEntity(
            dateEpochDay = a.date.toEpochDay(),
            unlockCount = a.unlockCount,
            totalScreenTimeMinutes = a.totalScreenTimeMinutes,
            appsLaunchedFromLauncher = a.appsLaunchedFromLauncher,
            gatedAppBypasses = a.gatedAppBypasses,
            modeOverrides = a.modeOverrides,
            longestPhoneFreeStreakMinutes = a.longestPhoneFreeStreakMinutes,
            focusScore = a.focusScore
        )
    }
}
