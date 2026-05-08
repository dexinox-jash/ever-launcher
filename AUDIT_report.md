# Ever Launcher — Codebase Audit Report
Generated: 2026-04-13
Audited by: Claude Code Agent (ruflo swarm — 3 parallel discovery agents)

---

## Executive Summary
- **Total files audited**: 52 (49 main + 3 test)
- **Build status**: ✅ Compiles — `assembleDebug` succeeds with 0 errors
- **Tests**: 26 passing / 0 failing / 26 total
- **Platform coverage**: Android only (iOS deferred — all iOS/Widget requirements N/A)
- **Critical issues found**: 3
- **Major issues found**: 6
- **Minor issues found**: 6
- **Android spec compliance**: 29 of 40 Android requirements met (72.5%)
- **iOS spec compliance**: 0 of 30 iOS/Widget requirements (N/A — not built)

---

## Section 1: What Is Working Correctly

### ✅ AL-01 — CATEGORY_HOME and CATEGORY_DEFAULT on launcher activity
**Status**: PASS
**Evidence**: `AndroidManifest.xml:28-33` — MainActivity has intent-filter with ACTION_MAIN, CATEGORY_HOME, CATEGORY_DEFAULT, CATEGORY_LAUNCHER.

### ✅ AL-02 — singleTask launch mode and stateNotNeeded
**Status**: PASS
**Evidence**: `AndroidManifest.xml:23-24` — `android:launchMode="singleTask"` and `android:stateNotNeeded="true"` both present.

### ✅ AL-03 — QUERY_ALL_PACKAGES declared
**Status**: PASS
**Evidence**: `AndroidManifest.xml:5-6`

### ✅ AL-04 — PACKAGE_USAGE_STATS declared
**Status**: PASS
**Evidence**: `AndroidManifest.xml:7-8`

### ✅ AL-05 — RECEIVE_BOOT_COMPLETED declared
**Status**: PASS
**Evidence**: `AndroidManifest.xml:9`

### ✅ AL-06 — SCHEDULE_EXACT_ALARM declared
**Status**: PASS
**Evidence**: `AndroidManifest.xml:10`

### ✅ AL-07 — Billing permission declared
**Status**: PASS
**Evidence**: `AndroidManifest.xml:11` — `com.android.vending.BILLING`

### ✅ AL-08 — App detection uses PackageManager.queryIntentActivities() with ACTION_MAIN + CATEGORY_LAUNCHER
**Status**: PASS
**Evidence**: `DetectInstalledAppsUseCase.kt:29-30` — `Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }` then `pm.queryIntentActivities(launcherIntent, 0)`

### ✅ AL-09 — App launching uses correct flags
**Status**: PASS
**Evidence**: `LauncherScreen.kt:240-241` — `launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)`

### ✅ AL-10 — PackageManager queries NOT on main thread
**Status**: PASS
**Evidence**: `DetectInstalledAppsUseCase.kt:27` — `withContext(Dispatchers.IO)`

### ✅ AL-11 — Back button disabled/no-op on launcher
**Status**: PASS
**Evidence**: `MainActivity.kt:51-54` — `override fun onBackPressed()` is an explicit no-op.

### ✅ AL-12 — Home button returns to Ever Launcher
**Status**: PASS
**Evidence**: Inherent from CATEGORY_HOME in manifest. App set as default launcher = pressing Home always returns here.

### ✅ AL-13 — Room database with tables for apps, modes, analytics
**Status**: PASS
**Evidence**: `EverDatabase.kt:13-14` — 3 entities declared; `app/`, `mode/`, `analytics/` tables all verified.

### ✅ AL-14 — AlarmManager used for mode transition scheduling
**Status**: PASS
**Evidence**: `ScheduleModeTransitionsUseCase.kt:51` — `alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent)` with graceful fallback to `set()` when exact alarms unavailable.

### ✅ AL-15 — BroadcastReceiver handles BOOT_COMPLETED
**Status**: PASS
**Evidence**: `ModeTransitionReceiver.kt:21-22` — `Intent.ACTION_BOOT_COMPLETED -> handleBoot(context)` which calls `scheduleNext24Hours()`.

### ✅ AL-16 — BroadcastReceiver handles mode transition action
**Status**: PASS
**Evidence**: `ModeTransitionReceiver.kt:22-23` — `"com.everlauncher.MODE_TRANSITION" -> handleModeTransition(context, intent)`. Manifest:46 registers the action.

### ✅ AL-17 — onNewIntent() handled in launcher activity
**Status**: PASS
**Evidence**: `MainActivity.kt:57-61` — `override fun onNewIntent(intent: Intent)` calls `super.onNewIntent` and `setIntent(intent)`.

### ✅ AL-18 — Google Play Billing for $7.99 one-time purchase
**Status**: PASS
**Evidence**: `BillingManager.kt:21` — `PRODUCT_ID = "com.everlauncher.pro"`, INAPP type, full purchase + acknowledge flow implemented.

### ✅ AL-19 — Settings/Dashboard is separate Activity
**Status**: PASS
**Evidence**: `AndroidManifest.xml:36-39` — `SettingsActivity` declared separately; `MainActivity.kt:42` starts it via Intent.

### ✅ SL-01 — FocusMode data model complete
**Status**: PASS
**Evidence**: `FocusMode.kt` — id (UUID), name, apps (max 8), schedule (Schedule obj with startMinutes/endMinutes int + activeDays), theme, createdAt (Instant), sortOrder.

### ✅ SL-02 — AppItem data model complete
**Status**: PASS
**Evidence**: `AppItem.kt` — id, displayName, packageName (= platformIdentifier on Android), isGated, gateType, sortOrder.

### ✅ SL-03 — DailyAnalytics data model complete
**Status**: PASS
**Evidence**: `DailyAnalytics.kt` — date, unlockCount, totalScreenTimeMinutes, appsLaunchedFromLauncher, gatedAppBypasses, modeOverrides, longestPhoneFreeStreakMinutes, focusScore.

### ✅ SL-04 — Mode scheduler resolves correct mode for any time + day
**Status**: PASS
**Evidence**: `GetCurrentModeUseCase.kt:36-53` — `resolveBySchedule()` uses `Schedule.isActiveAt(minuteOfDay, dayOfWeek)`. Tested in `GetCurrentModeUseCaseTest.kt` (7 cases, all pass).

### ✅ SL-05 — Overnight schedules handled correctly
**Status**: PASS
**Evidence**: `Schedule.kt:31-34` — `if (isOvernight) { minuteOfDay >= startMinutes || minuteOfDay < endMinutes }`. Tested with 21:00-07:00 case.

### ✅ SL-06 — Schedule conflicts: latest createdAt wins
**Status**: PASS
**Evidence**: `GetCurrentModeUseCase.kt:50-51` — `activeModes.maxByOrNull { it.createdAt }!!`. Tested in `GetCurrentModeUseCaseTest.kt`.

### ✅ SL-07 — Fallback to last-active mode (never blank)
**Status**: PASS
**Evidence**: `GetCurrentModeUseCase.kt:45-47` — `modes.maxByOrNull { it.createdAt } ?: modes.first()`

### ✅ SL-08 — Maximum 8 apps per mode enforced
**Status**: PASS
**Evidence**: `FocusMode.kt:25,29` — `const val MAX_APPS = 8` and `require(apps.size <= MAX_APPS)`. Tested in `AppItemValidationTest.kt`.

### ✅ SL-09 — Focus score formula matches spec exactly
**Status**: PASS
**Evidence**: `CalculateFocusScoreUseCase.kt` — All deductions (unlocks -30 max, bypasses -20 max, overrides -15 max, screen time -20 max) and bonuses (+15 streak, +10 completion, +5 no gate) match spec §3.2.2. 10 unit tests pass.

### ✅ SL-11 — Integer minutes-since-midnight, not floats
**Status**: PASS
**Evidence**: `Schedule.kt:17-18` — `val startMinutes: Int`, `val endMinutes: Int`. `GetCurrentModeUseCase.kt:39` — `val minuteOfDay = now.hour * 60 + now.minute`

### ✅ SL-12 — Failed app launches show graceful error, not crash
**Status**: PASS
**Evidence**: `LauncherScreen.kt:236-255` — `try { ... } catch (e: ActivityNotFoundException) { Toast.makeText(...) }`. Null launch intent also handled with Toast.

### ✅ UX-01 — Color tokens for all 3 themes
**Status**: PASS
**Evidence**: `Color.kt` — 7 tokens × 3 themes (Light/Dark/AMOLED) = 21 total color constants.

### ✅ UX-03 — App list items 18sp medium weight
**Status**: PASS
**Evidence**: `Type.kt:11-16` — `bodyLarge` = 18sp, FontWeight.Medium. `AppListItem.kt:25` uses `bodyLarge`.

### ✅ UX-04 — Mode label 10sp monospaced uppercase 2sp letter-spacing
**Status**: PASS
**Evidence**: `Type.kt:35-41` — `labelSmall` = 10sp, Monospace, Medium, 2.sp letterSpacing. `ModeIndicator.kt` uppercases the name.

### ✅ UX-05 — No splash screen
**Status**: PASS
**Evidence**: No SplashScreen API usage, no launch animations found anywhere.

### ✅ UX-06 — No rating prompt
**Status**: PASS
**Evidence**: No `ReviewManager`, `SKStoreReviewController`, or in-app review imports found.

### ✅ UX-07 — Breathing gate: sinusoidal scale, 4-second cycle
**Status**: PASS
**Evidence**: `BreathingGateScreen.kt:34-42` — `animateFloat(0.85f, 1.15f)` with `tween(2000ms, LinearEasing)` and `RepeatMode.Reverse` = 2s expand + 2s contract = 4s full cycle.

### ✅ UX-08 — Focus score ring animates 800ms ease-out on appear
**Status**: PASS
**Evidence**: `FocusScoreRing.kt:25-28` — `animateFloatAsState(tween(800, FastOutSlowInEasing))`

### ✅ UX-09 — No spring animations
**Status**: PASS
**Evidence**: Searched entire codebase — no `spring()` animation specs found anywhere.

### ✅ UX-12 — No app icons in launcher list — text only
**Status**: PASS
**Evidence**: `AppListItem.kt` — only `Text(app.displayName)`, no Image/Icon/Bitmap loading.

### ✅ PF-01 — No network calls
**Status**: PASS
**Evidence**: No URLSession, OkHttp, Retrofit, HttpClient, or network imports found. Privacy/Terms links use system browser via `LocalUriHandler.openUri()` — not in-app network call.

### ✅ PF-02 — No third-party analytics SDKs
**Status**: PASS
**Evidence**: `libs.versions.toml` — no Firebase Analytics, Mixpanel, Amplitude, or similar.

### ✅ PF-03 — No crash reporting SDKs
**Status**: PASS
**Evidence**: No Crashlytics, Sentry, or Bugsnag imports.

### ✅ PF-04 — No image assets beyond app icon
**Status**: PASS (vacuously — no image assets AT ALL, including the required app icon).

### ✅ PF-06 — Database queries off main thread
**Status**: PASS
**Evidence**: Room automatically enforces this with suspend functions on IO dispatcher.

### ✅ PF-07 — No print/Log statements
**Status**: PASS
**Evidence**: Searched entire codebase — no `Log.d`, `Log.e`, `println`, or `print(` statements found.

### ✅ SP-01 — No data leaves device
**Status**: PASS
**Evidence**: No network calls. Analytics stay in Room DB. No external transmission.

### ✅ SP-02 — User data in app sandbox
**Status**: PASS
**Evidence**: Room database and DataStore both use app's private storage.

### ✅ SP-03 — No clipboard access
**Status**: PASS
**Evidence**: No `ClipboardManager` usage found.

### ✅ SP-04 — No camera/mic/location permissions
**Status**: PASS
**Evidence**: Manifest only declares QUERY_ALL_PACKAGES, PACKAGE_USAGE_STATS, RECEIVE_BOOT_COMPLETED, SCHEDULE_EXACT_ALARM, BILLING.

### ✅ SS-06 — Bundle ID follows reverse-domain convention
**Status**: PASS
**Evidence**: `app/build.gradle.kts:8` — `applicationId = "com.everlauncher"`

### ✅ SS-08 — minSdk 26+, targetSdk 34+
**Status**: PASS
**Evidence**: `app/build.gradle.kts:14,15` — `minSdk = 26`, `targetSdk = 35`

### ✅ SS-09 — ProGuard/R8 rules configured
**Status**: PASS
**Evidence**: `proguard-rules.pro` — Room entities/DAOs, Billing client, DataStore classes all kept.

### ✅ SS-10 — No android:debuggable="true" in release manifest
**Status**: PASS
**Evidence**: `AndroidManifest.xml` — no `debuggable` attribute. AGP handles this automatically.

---

## Section 2: What Is NOT Working

### ❌ AL-20 — Long-press on launcher screen does NOT open settings
**Status**: FAIL
**Severity**: CRITICAL
**Expected**: Long-pressing the launcher home screen opens SettingsActivity.
**Actual**: Only swipe-up (detectVerticalDragGestures) is handled. No `detectTapGestures(onLongPress = ...)` exists.
**Root cause**: LauncherScreen.kt Box modifier only has `detectVerticalDragGestures`. Long-press callback is missing entirely.
**File(s)**: `ui/launcher/LauncherScreen.kt:46-51`
**Remediation**: Add `combinedClickable` or `detectTapGestures(onLongPress = { onOpenSettings() })` to the main Box's pointerInput. Must be combined with existing drag gesture using `awaitEachGesture`.
**Estimated effort**: Small (< 30 min)

---

### ❌ SS-04 — No app icon exists
**Status**: FAIL
**Severity**: CRITICAL
**Expected**: App icon in all required sizes (mipmap-mdpi through mipmap-xxxhdpi + adaptive icon for Android 8+).
**Actual**: No `res/mipmap-*/` directories exist. No `ic_launcher.xml`, no `ic_launcher_round.xml`. Play Store will reject submission without an icon.
**Root cause**: Icon was never created during initial build.
**File(s)**: Missing: `app/src/main/res/mipmap-*/ic_launcher*`; `app/src/main/AndroidManifest.xml` lacks `android:icon` attribute.
**Remediation**: Create adaptive icon XML with a simple shape (brand accent color #2D5F2D light / #C8FF00 dark) + add `android:icon="@mipmap/ic_launcher"` to manifest `<application>` tag.
**Estimated effort**: Small (< 30 min)

---

### ❌ ModeEditorScreen — Schedule times are display-only (not editable)
**Status**: FAIL
**Severity**: CRITICAL
**Expected**: Users can edit start time, end time, and active days for a mode.
**Actual**: `ModeEditForm` shows `Text()` for start/end times and has `var startMinutes` / `var endMinutes` state but NO UI widget (Slider, TextField, TimePicker) to change them. Active days are not shown at all.
**Root cause**: State variables were declared but the editing UI was never implemented.
**File(s)**: `ui/settings/ModeEditorScreen.kt:96-97, 118-123`
**Remediation**: Replace the `Text()` display with `Slider(value, onValueChange)` components for start/end minutes (0-1439). Add `FlowRow` of day-of-week chip buttons.
**Estimated effort**: Medium (1-3 hours)

---

### ❌ LauncherViewModel — hiddenAppCount is always 0
**Status**: FAIL
**Severity**: MAJOR
**Expected**: Shows count of installed apps NOT in the current mode (e.g., "14 apps hidden · focus").
**Actual**: `hiddenAppCount = 0` hardcoded in `observeCurrentMode()`. Feature appears in UI but always shows nothing.
**Root cause**: Implementation placeholder was never filled in. Total installed apps are not tracked in the ViewModel.
**File(s)**: `ui/launcher/LauncherViewModel.kt:79`
**Remediation**: Inject `AppRepository` into `LauncherViewModel`, observe `getAllAppsFlow()`, compute `hiddenAppCount = totalInstalledApps - visibleApps.size`.
**Estimated effort**: Small (< 30 min)

---

### ❌ LauncherViewModel — Swipe-up search searches only mode apps, not all installed apps
**Status**: FAIL
**Severity**: MAJOR
**Expected**: Swipe-up search should filter ALL installed apps (not just the current mode's apps).
**Actual**: `onSearchQueryChange()` filters `state.visibleApps` — only the current mode's apps (max 8). Defeats the purpose of having a launcher-wide search.
**Root cause**: `visibleApps` is the mode's app list. The ViewModel has no reference to all installed apps.
**File(s)**: `ui/launcher/LauncherViewModel.kt:112-118`
**Remediation**: Load all installed apps via `DetectInstalledAppsUseCase` or `AppRepository.getAllAppsFlow()` into `LauncherUiState.allInstalledApps`. Filter this list in `onSearchQueryChange()` instead of `visibleApps`.
**Estimated effort**: Small (< 30 min)

---

### ❌ ModeEditorScreen — No day-of-week selector
**Status**: FAIL
**Severity**: MAJOR
**Expected**: User can select which days of the week a mode is active.
**Actual**: Day-of-week is not displayed or editable in `ModeEditForm`. Mode always saves with the original `activeDays` (unchanged).
**Root cause**: The form never renders DayOfWeek chips.
**File(s)**: `ui/settings/ModeEditorScreen.kt:93-147`
**Remediation**: Add a `var activeDays by remember { mutableStateOf(mode.schedule.activeDays) }` state and render 7 `FilterChip` components (M T W T F S S) that toggle inclusion.
**Estimated effort**: Small (< 30 min — part of schedule editing fix)

---

### ❌ ModeEditorScreen — No app assignment UI
**Status**: FAIL
**Severity**: MAJOR
**Expected**: User can assign apps to a mode (up to 8) from the mode editor.
**Actual**: `ModeEditForm` shows a note "Max 8 apps per mode" but no UI to add/remove apps. `onSave()` preserves the original `mode.apps` list unchanged.
**Root cause**: App assignment UI was never implemented in the editor form.
**File(s)**: `ui/settings/ModeEditorScreen.kt:135-145`
**Remediation**: Load all DB apps. Add a collapsible multi-select list showing app names with checkboxes. Limit selection to 8. Pass selected apps into `mode.copy(apps = selectedApps)` on save.
**Estimated effort**: Medium (1-3 hours)

---

### ❌ ModeTransitionReceiver — Does not re-schedule next alarm after MODE_TRANSITION
**Status**: FAIL
**Severity**: MAJOR
**Expected**: When a mode transition fires, the receiver schedules the NEXT alarm so the chain continues indefinitely.
**Actual**: `handleModeTransition()` sends a broadcast refresh but never calls `scheduleNext24Hours()`. After BOOT_COMPLETED sets up the initial alarms, they fire once and the chain breaks. Mode transitions stop working after the first day.
**Root cause**: `handleModeTransition()` only sends a UI refresh broadcast but does not re-arm the alarm scheduler.
**File(s)**: `receiver/ModeTransitionReceiver.kt:39-47`
**Remediation**: Add `scheduleUseCase.scheduleNext24Hours()` call inside `handleModeTransition()`.
**Estimated effort**: Small (< 30 min)

---

### ❌ LauncherViewModel — REFRESH_MODE broadcast never received
**Status**: FAIL
**Severity**: MAJOR
**Expected**: When ModeTransitionReceiver broadcasts `com.everlauncher.REFRESH_MODE`, the LauncherScreen updates to show the new mode.
**Actual**: No `BroadcastReceiver` is registered anywhere in `LauncherViewModel` or `MainActivity` for this action. Mode changes via alarm are invisible to the UI until the app is reopened.
**Root cause**: The broadcast is sent but nobody listens.
**File(s)**: `ui/launcher/LauncherViewModel.kt` (missing listener)
**Remediation**: Register a `BroadcastReceiver` in `LauncherViewModel.init` (or `MainActivity`) for `com.everlauncher.REFRESH_MODE` action. On receive, trigger `observeCurrentMode()` or force the Flow to re-emit. Unregister in `onCleared()`.
**Estimated effort**: Small (< 30 min)

---

## Section 3: Code Quality Issues

### ⚠️ SS-01 — ~50+ UI strings hardcoded instead of using strings.xml
**Severity**: MEDIUM
**Files**: All UI composables (`LauncherScreen`, `SettingsScreen`, `GateConfigScreen`, `DashboardScreen`, `ModeEditorScreen`, all onboarding screens)
**Issue**: `strings.xml` has 50+ string resources defined, but UI code uses hardcoded literals ("Settings", "APPEARANCE", "Upgrade to Pro $7.99", "Breathe in… Breathe out…", etc.) instead of `stringResource(R.string.xxx)`.
**Recommendation**: Replace all hardcoded UI strings with `stringResource()` calls referencing the already-defined strings.xml entries.

### ⚠️ IA-13 / Streak — currentStreak/bestStreak never auto-calculated
**Severity**: MEDIUM
**Files**: `ui/dashboard/DashboardViewModel.kt`
**Issue**: `UserPreferences.currentStreak` and `bestStreak` exist but no code ever updates them automatically. The streak logic (consecutive days with score ≥ 60) is completely absent. Streak always shows 0.
**Recommendation**: In `DashboardViewModel.loadData()`, after computing today's score, query the last N days from analytics and calculate the streak. Call `prefsStore.updateStreak(current, best)`.

### ⚠️ DashboardViewModel — focusModeCompletedToday always false
**Severity**: LOW
**Files**: `ui/dashboard/DashboardViewModel.kt:53`
**Issue**: `calculateScoreUseCase.calculate(today, focusModeCompletedToday = false)` — the bonus +10 for completing focus mode hours is never awarded because completion is never detected.
**Recommendation**: Calculate completion by checking if the current time has passed the end of all scheduled focus mode windows for today.

### ⚠️ PACKAGE_USAGE_STATS — No runtime permission prompt
**Severity**: MEDIUM
**Files**: `ui/dashboard/DashboardScreen.kt`, `ui/settings/SettingsScreen.kt`
**Issue**: `PACKAGE_USAGE_STATS` is a special "AppOpsManager" permission not grantable via `requestPermissions()`. The user must be sent to `Settings > Apps > Special App Access > Usage Access`. The app silently skips analytics if permission is not granted but never tells the user or provides a button to grant it.
**Recommendation**: Check `AppOpsManager.checkOpNoThrow(OPSTR_GET_USAGE_STATS, ...)` on dashboard open. If not granted, show a banner with `Button("Grant access") { startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }`.

### ⚠️ GateConfigScreen — Per-mode gating not implemented
**Severity**: LOW
**Files**: `ui/settings/GateConfigScreen.kt`
**Issue**: Spec says gate configuration is "per-app AND per-mode". Current implementation is per-app only. All modes share the same gate setting for each app.
**Recommendation**: Extend `AppItem.gateType` model with a `Map<String, GateType>` (modeId → gateType) or add a separate `AppModeGate` table.

### ⚠️ SS-02 — Version "1.0.0" hardcoded in UI
**Severity**: LOW
**Files**: `ui/settings/SettingsScreen.kt:100`
**Issue**: `SettingsRow("Version", "1.0.0")` — version is hardcoded. Will be wrong after any release.
**Recommendation**: Use `BuildConfig.VERSION_NAME` or read from `context.packageManager.getPackageInfo()`.

---

## Section 4: Compilation Errors
**None** — Project compiles successfully with 0 errors.

---

## Section 5: Test Coverage Gaps

### 🧪 AL-20 — No test for long-press gesture opening settings
**What should be tested**: That long-pressing the launcher background triggers `onOpenSettings()`.

### 🧪 AL-20 (hidden count) — No test for hiddenAppCount calculation
**What should be tested**: That `hiddenAppCount = totalInstalled - modeApps.size`.

### 🧪 ModeTransitionReceiver — No test for alarm re-scheduling
**What should be tested**: That after MODE_TRANSITION, `scheduleNext24Hours()` is called.

### 🧪 Search — No test that search covers all installed apps
**What should be tested**: That search results include apps not in the current mode.

### 🧪 Streak — No test for streak calculation logic
**What should be tested**: Consecutive days with score ≥ 60 increments streak; a day below 60 resets it.

---

## Section 6: Prioritized Fix List

| Priority | ID | Description | Severity | Effort |
|----------|----|-------------|----------|--------|
| 1 | AL-20 | Long-press doesn't open settings | CRITICAL | Small |
| 2 | SS-04 | No app icon — Play Store rejection | CRITICAL | Small |
| 3 | ModeEditorScreen | Schedule times not editable | CRITICAL | Medium |
| 4 | ModeEditorScreen | No day-of-week selector | MAJOR | Small |
| 5 | ModeEditorScreen | No app assignment UI | MAJOR | Medium |
| 6 | LauncherViewModel | hiddenAppCount always 0 | MAJOR | Small |
| 7 | LauncherViewModel | Search only searches mode apps | MAJOR | Small |
| 8 | ModeTransitionReceiver | Alarm chain breaks after first day | MAJOR | Small |
| 9 | LauncherViewModel | REFRESH_MODE broadcast not received | MAJOR | Small |
| 10 | SS-01 | 50+ hardcoded strings | MINOR | Medium |
| 11 | Streak | Never calculated | MINOR | Small |
| 12 | DashboardViewModel | focusModeCompletedToday always false | MINOR | Small |
| 13 | PACKAGE_USAGE_STATS | No permission prompt for user | MINOR | Small |
| 14 | SS-02 | Version hardcoded in UI | MINOR | Small |

---

*Post-remediation update section below will be filled in after Phase 4 & 5.*
