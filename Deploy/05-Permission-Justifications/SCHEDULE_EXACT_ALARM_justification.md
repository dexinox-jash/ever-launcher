# SCHEDULE_EXACT_ALARM — Permission Justification

## Play Console Question
> Why does your app need the `SCHEDULE_EXACT_ALARM` permission?

## Answer (Copy-Paste Ready)

Ever Launcher uses time-based Focus Modes (e.g., "Focus" from 9:00–17:00, "Wind Down" from 21:00–07:00). The `SCHEDULE_EXACT_ALARM` permission is required to ensure these mode transitions happen at the exact scheduled time using `AlarmManager.setExactAndAllowWhileIdle()`.

### Core functionality that depends on this permission
1. **Automatic Mode Switching** — Transitions the launcher UI between Focus, Personal, and Wind Down modes at the exact user-configured times.
2. **Daily Schedule Enforcement** — Ensures users see only the apps relevant to their current context without manual intervention.
3. **Boot Rescheduling** — Re-schedules all transitions after device reboot via `RECEIVE_BOOT_COMPLETED`.

### Why exact alarms are necessary
- Inexact alarms (the default without this permission) can be delayed by Android's Doze/App Standby by 15+ minutes or even hours. This would break the core promise of the app: "Your phone, focused. At the right time."
- Users rely on the launcher changing modes precisely at the start/end of their work day or sleep schedule.

### Fallback behavior
If a user denies exact alarm permission on Android 12+:
- The app falls back to inexact alarms and displays an in-app banner in Settings > Edit Modes explaining that mode transitions may be delayed.
- The banner provides a one-tap deep-link to `Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM` so users can grant the permission.

### Data handling
- Alarms are local-only. No data is transmitted.
- Alarm payloads contain only an internal mode ID string.

---

## Supporting Evidence
- `ScheduleModeTransitionsUseCase.kt` — alarm scheduling logic
- `ModeTransitionReceiver.kt` — alarm receiver with boot rescheduling
- `ModeEditorScreen.kt` — in-app exact-alarm permission banner
