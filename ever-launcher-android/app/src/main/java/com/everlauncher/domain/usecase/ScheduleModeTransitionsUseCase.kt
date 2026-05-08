package com.everlauncher.domain.usecase

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.everlauncher.data.repository.ModeRepository
import com.everlauncher.domain.model.FocusMode
import com.everlauncher.receiver.ModeTransitionReceiver
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class ScheduleModeTransitionsUseCase(
    private val context: Context,
    private val modeRepository: ModeRepository
) {
    companion object {
        const val EXTRA_MODE_ID = "mode_id"
        private const val REQUEST_CODE_BASE_START = 10_000
        private const val REQUEST_CODE_BASE_END = 20_000
    }

    suspend fun scheduleNext24Hours() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val modes = modeRepository.getAllModes()
        if (modes.isEmpty()) return
        val now = LocalDateTime.now()
        val zoneId = ZoneId.systemDefault()

        modes.forEachIndexed { index, mode ->
            scheduleAlarm(alarmManager, mode, now, zoneId, index, isStart = true)
            scheduleAlarm(alarmManager, mode, now, zoneId, index, isStart = false)
        }
    }

    private fun scheduleAlarm(
        alarmManager: AlarmManager,
        mode: FocusMode,
        now: LocalDateTime,
        zoneId: ZoneId,
        index: Int,
        isStart: Boolean
    ) {
        val nextTrigger = findNextTriggerTime(mode, now, isStart) ?: return
        val triggerMs = nextTrigger.atZone(zoneId).toInstant().toEpochMilli()

        val intent = Intent(context, ModeTransitionReceiver::class.java).apply {
            action = "com.everlauncher.MODE_TRANSITION"
            putExtra(EXTRA_MODE_ID, mode.id)
        }
        val requestCode = if (isStart) REQUEST_CODE_BASE_START + index else REQUEST_CODE_BASE_END + index
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent)
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent)
            }
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent)
        }
    }

    private fun findNextTriggerTime(mode: FocusMode, from: LocalDateTime, isStart: Boolean): LocalDateTime? {
        val minutes = if (isStart) mode.schedule.startMinutes else mode.schedule.endMinutes
        val time = LocalTime.of(minutes / 60, minutes % 60)
        for (daysAhead in 0..7) {
            val date: LocalDate = from.toLocalDate().plusDays(daysAhead.toLong())
            if (date.dayOfWeek !in mode.schedule.activeDays) continue
            val candidate = LocalDateTime.of(date, time)
            if (candidate.isAfter(from)) return candidate
        }
        return null
    }
}
