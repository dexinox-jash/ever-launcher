package com.everlauncher.data.db

import androidx.room.TypeConverter
import com.everlauncher.domain.model.GateType
import com.everlauncher.domain.model.ThemePreference
import org.json.JSONArray
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate

class Converters {

    // --- Instant <-> Long ---
    @TypeConverter fun instantToLong(value: Instant?): Long? = value?.toEpochMilli()
    @TypeConverter fun longToInstant(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

    // --- LocalDate <-> Long (epoch day) ---
    @TypeConverter fun localDateToLong(value: LocalDate?): Long? = value?.toEpochDay()
    @TypeConverter fun longToLocalDate(value: Long?): LocalDate? = value?.let { LocalDate.ofEpochDay(it) }

    // --- Set<DayOfWeek> <-> String (comma-separated ordinals) ---
    @TypeConverter
    fun daysToString(days: Set<DayOfWeek>?): String? =
        days?.joinToString(",") { it.value.toString() }

    @TypeConverter
    fun stringToDays(value: String?): Set<DayOfWeek>? =
        value?.split(",")?.mapNotNull { s ->
            s.trim().toIntOrNull()?.let { DayOfWeek.of(it) }
        }?.toSet()

    // --- List<String> (app IDs) <-> JSON array string ---
    @TypeConverter
    fun stringListToJson(list: List<String>?): String? {
        if (list == null) return null
        return JSONArray(list).toString()
    }

    @TypeConverter
    fun jsonToStringList(json: String?): List<String>? {
        if (json == null) return null
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- GateType? <-> String? ---
    @TypeConverter fun gateTypeToString(value: GateType?): String? = value?.name
    @TypeConverter fun stringToGateType(value: String?): GateType? =
        value?.let { runCatching { GateType.valueOf(it) }.getOrNull() }

    // --- ThemePreference <-> String ---
    @TypeConverter fun themeToString(value: ThemePreference): String = value.name
    @TypeConverter fun stringToTheme(value: String): ThemePreference =
        runCatching { ThemePreference.valueOf(value) }.getOrDefault(ThemePreference.SYSTEM)
}
