package com.everlauncher.data.db

import androidx.room.*
import com.everlauncher.data.db.entities.AnalyticsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalyticsDao {
    @Query("SELECT * FROM analytics WHERE date_epoch_day = :epochDay")
    suspend fun getByDate(epochDay: Long): AnalyticsEntity?

    @Query("SELECT * FROM analytics ORDER BY date_epoch_day DESC LIMIT 7")
    suspend fun getLast7Days(): List<AnalyticsEntity>

    @Query("SELECT * FROM analytics ORDER BY date_epoch_day DESC LIMIT 7")
    fun getLast7DaysFlow(): Flow<List<AnalyticsEntity>>

    @Query("SELECT * FROM analytics ORDER BY date_epoch_day DESC")
    suspend fun getAll(): List<AnalyticsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: AnalyticsEntity)

    @Query("""
        UPDATE analytics SET
            unlock_count = unlock_count + 1
        WHERE date_epoch_day = :epochDay
    """)
    suspend fun incrementUnlocks(epochDay: Long)

    @Query("""
        UPDATE analytics SET
            apps_launched = apps_launched + 1
        WHERE date_epoch_day = :epochDay
    """)
    suspend fun incrementAppsLaunched(epochDay: Long)

    @Query("""
        UPDATE analytics SET
            gated_bypasses = gated_bypasses + 1
        WHERE date_epoch_day = :epochDay
    """)
    suspend fun incrementGatedBypasses(epochDay: Long)

    @Query("""
        UPDATE analytics SET
            mode_overrides = mode_overrides + 1
        WHERE date_epoch_day = :epochDay
    """)
    suspend fun incrementModeOverrides(epochDay: Long)
}
