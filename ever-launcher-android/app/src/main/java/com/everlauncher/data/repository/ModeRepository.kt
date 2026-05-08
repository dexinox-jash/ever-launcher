package com.everlauncher.data.repository

import com.everlauncher.data.db.AppDao
import com.everlauncher.data.db.ModeDao
import com.everlauncher.data.db.entities.ModeEntity
import com.everlauncher.domain.model.AppItem
import com.everlauncher.domain.model.FocusMode
import com.everlauncher.domain.model.Schedule
import com.everlauncher.domain.model.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.Instant

class ModeRepository(
    private val modeDao: ModeDao,
    private val appDao: AppDao
) {
    fun getAllModesFlow(): Flow<List<FocusMode>> =
        modeDao.getAllFlow().map { entities ->
            val allAppIds = entities.flatMap { it.appIds }.distinct()
            val appMap = if (allAppIds.isEmpty()) emptyMap() else {
                appDao.getByIds(allAppIds).associate { it.id to it.toDomain() }
            }
            entities.map { entity ->
                val apps = entity.appIds.mapNotNull { appMap[it] }
                entity.toDomain(apps)
            }
        }

    suspend fun getAllModes(): List<FocusMode> {
        val entities = modeDao.getAll()
        val allAppIds = entities.flatMap { it.appIds }.distinct()
        val appMap = if (allAppIds.isEmpty()) emptyMap() else {
            appDao.getByIds(allAppIds).associate { it.id to it.toDomain() }
        }
        return entities.map { entity ->
            val apps = entity.appIds.mapNotNull { appMap[it] }
            entity.toDomain(apps)
        }
    }

    suspend fun getModeById(id: String): FocusMode? {
        val entity = modeDao.getById(id) ?: return null
        val apps = loadAppsForEntity(entity)
        return entity.toDomain(apps)
    }

    suspend fun insertMode(mode: FocusMode) =
        modeDao.insert(mode.toEntity())

    suspend fun updateMode(mode: FocusMode) =
        modeDao.update(mode.toEntity())

    suspend fun deleteMode(modeId: String) =
        modeDao.deleteById(modeId)

    suspend fun updateModeGateOverrides(modeId: String, overrides: Map<String, String>) {
        val entity = modeDao.getById(modeId) ?: return
        val overridesString = overrides.entries.joinToString("|") { "${it.key}:${it.value}" }
        modeDao.update(entity.copy(gateOverrides = overridesString))
    }

    suspend fun initDefaultModesIfEmpty() {
        if (modeDao.count() == 0) {
            val defaults = listOf(
                FocusMode.defaultFocus(),
                FocusMode.defaultPersonal(),
                FocusMode.defaultWindDown()
            )
            defaults.forEach { modeDao.insert(it.toEntity()) }
        }
    }

    private suspend fun loadAppsForEntity(entity: ModeEntity): List<AppItem> {
        if (entity.appIds.isEmpty()) return emptyList()
        val appMap = appDao.getByIds(entity.appIds).associate { it.id to it.toDomain() }
        return entity.appIds.mapNotNull { appMap[it] }
    }

    private fun ModeEntity.toDomain(apps: List<AppItem>): FocusMode {
        val days = activeDays.split(",")
            .mapNotNull { s -> s.trim().toIntOrNull()?.let { DayOfWeek.of(it) } }
            .toSet()
            .ifEmpty { DayOfWeek.entries.toSet() }
        val overrides = if (gateOverrides.isBlank()) emptyMap()
        else gateOverrides.split("|").mapNotNull { entry ->
            val parts = entry.split(":", limit = 2)
            if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank())
                parts[0] to parts[1]
            else null
        }.toMap()
        return FocusMode(
            id = id,
            name = name,
            apps = apps,
            schedule = Schedule(startMinutes = startMinutes, endMinutes = endMinutes, activeDays = days),
            theme = theme,
            createdAt = createdAt,
            sortOrder = sortOrder,
            gateOverrides = overrides
        )
    }

    private fun FocusMode.toEntity(): ModeEntity {
        val daysString = schedule.activeDays.joinToString(",") { it.value.toString() }
        val overridesString = gateOverrides.entries.joinToString("|") { "${it.key}:${it.value}" }
        return ModeEntity(
            id = id,
            name = name,
            appIds = apps.map { it.id },
            startMinutes = schedule.startMinutes,
            endMinutes = schedule.endMinutes,
            activeDays = daysString,
            theme = theme,
            createdAt = createdAt,
            sortOrder = sortOrder,
            gateOverrides = overridesString
        )
    }
}
