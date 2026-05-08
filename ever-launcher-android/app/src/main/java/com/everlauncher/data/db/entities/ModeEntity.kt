package com.everlauncher.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.everlauncher.domain.model.ThemePreference
import java.time.Instant

@Entity(tableName = "modes")
data class ModeEntity(
    @PrimaryKey val id: String,
    val name: String,
    /** JSON array of AppItem IDs in order */
    @ColumnInfo(name = "app_ids") val appIds: List<String> = emptyList(),
    @ColumnInfo(name = "start_minutes") val startMinutes: Int,
    @ColumnInfo(name = "end_minutes") val endMinutes: Int,
    /** Comma-separated DayOfWeek ordinals (1=MON … 7=SUN) */
    @ColumnInfo(name = "active_days") val activeDays: String,
    val theme: ThemePreference = ThemePreference.SYSTEM,
    @ColumnInfo(name = "created_at") val createdAt: Instant = Instant.now(),
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0,
    /**
     * Pipe-separated per-mode gate overrides: "pkg:GATE_TYPE|pkg:NONE"
     * Stored as plain String; parsed in ModeRepository.
     */
    @ColumnInfo(name = "gate_overrides") val gateOverrides: String = ""
)
