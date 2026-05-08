# EverLauncher — Project Structure Audit
Generated: 2026-04-13
Audited by: Claude Code Agent (ruflo swarm)

## Platform Coverage
- **Android**: ✅ Present — `ever-launcher-android/`
- **iOS**: ❌ Not present — iOS project was deferred (Android-only Phase 1 MVP)
- **iOS Widget**: ❌ Not present — requires iOS project

---

## Android Project: `ever-launcher-android/`

### Build & Config (7 files)
| File | Status | Notes |
|------|--------|-------|
| `settings.gradle.kts` | ✅ | Includes `:app` module |
| `build.gradle.kts` (root) | ✅ | AGP, Kotlin, KSP, Compose, Room plugins |
| `app/build.gradle.kts` | ✅ | namespace=com.everlauncher, minSdk=26, targetSdk=35 |
| `gradle/libs.versions.toml` | ✅ | Complete version catalog |
| `gradle/wrapper/gradle-wrapper.properties` | ✅ | Gradle 8.13 |
| `gradle.properties` | ✅ | useAndroidX=true |
| `app/proguard-rules.pro` | ✅ | Room, Billing, DataStore keep rules |

### Resources (3 files)
| File | Status | Notes |
|------|--------|-------|
| `app/src/main/AndroidManifest.xml` | ✅ | All permissions declared |
| `app/src/main/res/values/strings.xml` | ⚠️ | 50+ strings defined but NOT used in UI code |
| `app/src/main/res/values/themes.xml` | ✅ | AppCompat base theme |
| `app/src/main/res/mipmap-*/` | ❌ | **MISSING** — No app icon directory |

### Domain Models (7 files)
| File | Status | Notes |
|------|--------|-------|
| `domain/model/AppItem.kt` | ✅ | id, displayName, packageName, isGated, gateType, sortOrder |
| `domain/model/FocusMode.kt` | ✅ | id, name, apps≤8, schedule, theme, createdAt, sortOrder |
| `domain/model/Schedule.kt` | ✅ | int minutes-since-midnight, overnight support |
| `domain/model/GateType.kt` | ✅ | BREATHING, INTENTION, DELAY |
| `domain/model/ThemePreference.kt` | ✅ | LIGHT, DARK, AMOLED, SYSTEM |
| `domain/model/DailyAnalytics.kt` | ✅ | All metrics present |
| `domain/model/UserPreferences.kt` | ✅ | All prefs including streak |

### Data Layer — Database (8 files)
| File | Status | Notes |
|------|--------|-------|
| `data/db/EverDatabase.kt` | ✅ | Thread-safe singleton, exportSchema=false |
| `data/db/Converters.kt` | ✅ | Instant, LocalDate, DayOfWeek, List<String>, GateType, ThemePreference |
| `data/db/entities/AppEntity.kt` | ✅ | tableName="apps" |
| `data/db/entities/ModeEntity.kt` | ✅ | tableName="modes", appIds as JSON |
| `data/db/entities/AnalyticsEntity.kt` | ✅ | tableName="analytics", column name = "apps_launched" |
| `data/db/AppDao.kt` | ✅ | Full CRUD |
| `data/db/ModeDao.kt` | ✅ | Full CRUD + count() |
| `data/db/AnalyticsDao.kt` | ✅ | CRUD + 4 atomic increment queries (column names verified correct) |

### Data Layer — Preferences & Repositories (4 files)
| File | Status | Notes |
|------|--------|-------|
| `data/preferences/UserPreferencesStore.kt` | ✅ | DataStore, all keys, IO-safe |
| `data/repository/AppRepository.kt` | ✅ | Wraps AppDao |
| `data/repository/ModeRepository.kt` | ✅ | initDefaultModesIfEmpty(), loads apps by IDs in order |
| `data/repository/AnalyticsRepository.kt` | ✅ | syncUsageStats() on IO, handles SecurityException |

### Domain Layer — Use Cases (5 files)
| File | Status | Notes |
|------|--------|-------|
| `domain/usecase/GetCurrentModeUseCase.kt` | ✅ | Flow + sync, integer minuteOfDay, fallback to latest |
| `domain/usecase/CalculateFocusScoreUseCase.kt` | ✅ | Exact spec §3.2.2 formula |
| `domain/usecase/DetectInstalledAppsUseCase.kt` | ✅ | Dispatchers.IO, filters system apps |
| `domain/usecase/TrackAppLaunchUseCase.kt` | ✅ | Delegates to AnalyticsRepository |
| `domain/usecase/ScheduleModeTransitionsUseCase.kt` | ⚠️ | Schedules alarms but receiver doesn't re-schedule after each trigger |

### Receivers & Services (2 files)
| File | Status | Notes |
|------|--------|-------|
| `receiver/ModeTransitionReceiver.kt` | ⚠️ | BOOT_COMPLETED reschedules OK; MODE_TRANSITION sends broadcast but does NOT re-schedule next alarm |
| `service/UsageTrackingService.kt` | ✅ | Helper (not Android Service), Dispatchers.IO, safe |

### UI — Launcher (4 files)
| File | Status | Notes |
|------|--------|-------|
| `ui/launcher/LauncherScreen.kt` | ❌ | **MISSING long-press handler** for settings; swipe-up only |
| `ui/launcher/LauncherViewModel.kt` | ❌ | `hiddenAppCount` always 0; search only searches mode apps not all installed; no REFRESH_MODE broadcast listener |
| `ui/launcher/AppListItem.kt` | ✅ | Text-only, no ripple, 14dp padding, 0.15 alpha divider |
| `ui/launcher/ModeIndicator.kt` | ✅ | Uppercase mode name, labelSmall monospace |

### UI — Gates (3 files)
| File | Status | Notes |
|------|--------|-------|
| `ui/gates/BreathingGateScreen.kt` | ✅ | 5s auto-dismiss, sinusoidal 4s cycle, no skip |
| `ui/gates/IntentionGateScreen.kt` | ✅ | 5-char minimum, input not stored |
| `ui/gates/DelayGateScreen.kt` | ✅ | 10s countdown, no skip |

### UI — Onboarding (4 files)
| File | Status | Notes |
|------|--------|-------|
| `ui/onboarding/OnboardingViewModel.kt` | ✅ | 3 steps: WELCOME→APP_PICKER→MODE_SETUP |
| `ui/onboarding/WelcomeScreen.kt` | ⚠️ | Hardcoded strings |
| `ui/onboarding/AppPickerScreen.kt` | ⚠️ | Hardcoded strings |
| `ui/onboarding/ModeSetupScreen.kt` | ⚠️ | Hardcoded strings |

### UI — Settings (3 files)
| File | Status | Notes |
|------|--------|-------|
| `ui/settings/SettingsScreen.kt` | ⚠️ | All strings hardcoded; has Privacy Policy, Terms, Restore Purchases |
| `ui/settings/SettingsViewModel.kt` | ✅ | Clean, proper StateFlow usage |
| `ui/settings/ModeEditorScreen.kt` | ❌ | **Schedule times NOT editable** (Text only, no Slider/Picker); **No day-of-week selector**; **No app assignment UI** |
| `ui/settings/GateConfigScreen.kt` | ⚠️ | Per-app gate only; spec says also per-mode |

### UI — Dashboard (4 files)
| File | Status | Notes |
|------|--------|-------|
| `ui/dashboard/DashboardViewModel.kt` | ⚠️ | `focusModeCompletedToday` always false; streak never auto-calculated |
| `ui/dashboard/DashboardScreen.kt` | ✅ | Score ring, streak, weekly chart, mode override |
| `ui/dashboard/FocusScoreRing.kt` | ✅ | 800ms FastOutSlowIn, 270° arc, Canvas |
| `ui/dashboard/WeeklyChart.kt` | ✅ | 7-day bars, Canvas, proportional height |

### UI — Theme (3 files)
| File | Status | Notes |
|------|--------|-------|
| `ui/theme/Color.kt` | ✅ | All 21 tokens for Light/Dark/AMOLED |
| `ui/theme/Type.kt` | ✅ | bodyLarge 18sp medium, labelSmall 10sp mono 2sp, headlineMedium 22sp serif, displayLarge 36sp serif |
| `ui/theme/Theme.kt` | ✅ | 3 ColorScheme instances, LocalThemePreference |

### Billing (1 file)
| File | Status | Notes |
|------|--------|-------|
| `billing/BillingManager.kt` | ✅ | Billing 7.x, INAPP product, acknowledge, restore |

### Entry Points (3 files)
| File | Status | Notes |
|------|--------|-------|
| `EverLauncherApp.kt` | ✅ | Application subclass, lazy DB init |
| `MainActivity.kt` | ✅ | Edge-to-edge, onboarding gate, back=no-op, onNewIntent |
| `SettingsActivity.kt` | ✅ | NavHost 4 destinations, BillingManager init |

### Tests (3 files, 26 total test cases)
| File | Tests | Pass |
|------|-------|------|
| `AppItemValidationTest.kt` | 9 | 9 ✅ |
| `FocusScoreCalculationTest.kt` | 10 | 10 ✅ |
| `GetCurrentModeUseCaseTest.kt` | 7 | 7 ✅ |

---

## Summary Counts
- **Total source files**: 52 (49 main + 3 test)
- **Files with issues**: 9
- **Critical issues**: 3
- **Major issues**: 6
- **Minor issues**: 6
