# Ever Launcher Android — Comprehensive Test Audit Report

**Audited by:** Senior Android Test Engineer  
**Project path:** `ever-launcher-android`  
**Date:** 2026-04-15  
**Existing test files:** 3 unit-test files, 0 instrumentation-test files  

---

## Executive Summary

| Category | Files | Coverage Verdict |
|----------|-------|------------------|
| Domain model unit tests | 3/3 models touched | **Partial** |
| Use-case unit tests | 1.5/5 use cases | **Poor** |
| Repository unit tests | 0/3 repositories | **Missing** |
| ViewModel unit tests | 0/4 ViewModels | **Missing** |
| Room/DB integration tests | 0 DAOs / 0 migrations | **Missing** |
| Compose UI tests | 0 screens | **Missing** |
| Edge-case / regression tests | Sparse | **Missing** |

**Bottom line:** The project is **not shippable** from a test-coverage perspective. Only `CalculateFocusScoreUseCase`, `AppItem`/`FocusMode`/`Schedule` invariants, and `Schedule.isActiveAt` have any unit-test coverage. There are **zero** tests for Repositories, ViewModels, Room database operations, DataStore, TypeConverters, and **zero** Compose UI tests.

---

## Part 1 — Existing Tests Inventory

### `AppItemValidationTest.kt`
- `valid AppItem creates successfully`
- `blank displayName throws IllegalArgumentException`
- `blank packageName throws IllegalArgumentException`
- `AppItem defaults to not gated`
- `FocusMode rejects more than 8 apps`
- `FocusMode allows exactly 8 apps`
- `FocusMode rejects blank name`
- `Schedule validates minute range`
- `Schedule detects overnight correctly`

**Verdict:** Covers basic model invariants. Does **not** test `gateOverrides`, `ThemePreference`, `createdAt`, `sortOrder`, or `Schedule.fromLocalTime`.

### `FocusScoreCalculationTest.kt`
- `perfect day yields max score`
- `zero activity gives base score plus no-gate bonus`
- `max unlocks deducts exactly 30`
- `max bypasses deducts exactly 20`
- `max overrides deducts exactly 15`
- `screen time over 3h deducts correctly`
- `screen time exactly 3h has no deduction`
- `all deductions stacked score is floored at 0`
- `score never exceeds 100`
- `phone free streak bonus capped at 15`

**Verdict:** Good coverage for `CalculateFocusScoreUseCase`. No negative-input tests (e.g., negative `unlockCount`).

### `GetCurrentModeUseCaseTest.kt`
- Tests `Schedule.isActiveAt` directly via helper `mode()` factory.
- Covers daytime, overnight, non-scheduled days, boundary inclusivity/exclusivity.
- Has two "conflict resolution" tests that replicate the algorithm inline rather than exercising `GetCurrentModeUseCase` itself.

**Verdict:** The actual `GetCurrentModeUseCase.currentModeFlow()` and `resolveCurrentMode()` are **never called**. The test file only validates `Schedule.isActiveAt` and manually duplicates conflict-resolution logic.

---

## Part 2 — Missing Tests by Layer

### 2.1 Domain / Use Cases

#### MISSING TEST: `DetectInstalledAppsUseCase`
**PRIORITY:** MUST HAVE  
**Why:** Package-manager integration is fragile across OEMs; at minimum the filtering/prefix logic must be verified.

**SUGGESTED TEST CASES:**
1. `isSystemUtilityPackage returns true for com.android.settings`
2. `isSystemUtilityPackage returns false for com.example.app`
3. `getInstalledLaunchableApps sorts alphabetically case-insensitive`
4. `getInstalledLaunchableApps marks FLAG_SYSTEM apps as isSystemApp = true`
5. `getInstalledLaunchableApps excludes packages matching systemPackagePrefixes`

**IMPLEMENTATION (sketch):**
```kotlin
class DetectInstalledAppsUseCaseTest {
    @Test
    fun `system utility prefixes are filtered`() {
        val useCase = DetectInstalledAppsUseCase(mockContext())
        // Use reflection to access private method or extract it to an
        // internal object for testability.
        val isSystem = useCase::class.java
            .getDeclaredMethod("isSystemUtilityPackage", String::class.java)
            .apply { isAccessible = true }
            .invoke(useCase, "com.android.settings") as Boolean
        assertTrue(isSystem)
    }
}
```
> **Recommended refactor:** Extract `isSystemUtilityPackage` to an `internal object AppFilter` so it is unit-testable without reflection.

---

#### MISSING TEST: `GetCurrentModeUseCase` (real use-case integration)
**PRIORITY:** MUST HAVE  
**Why:** Current tests never instantiate the use case with mocked collaborators.

**SUGGESTED TEST CASES:**
1. `currentModeFlow emits override mode when prefs contain currentModeOverrideId`
2. `currentModeFlow falls back to schedule resolution when override ID is null`
3. `currentModeFlow emits null when repository returns empty list`
4. `resolveCurrentMode returns most recently created mode when no schedule is active`
5. `resolveCurrentMode returns latest-created mode when multiple schedules overlap`

**IMPLEMENTATION (sketch):**
```kotlin
@RunWith(MockitoJUnitRunner::class)
class GetCurrentModeUseCaseIntegrationTest {
    @Mock lateinit var modeRepo: ModeRepository
    @Mock lateinit var prefsStore: UserPreferencesStore

    @Test
    fun `flow emits override mode when user has manually overridden`() = runTest {
        val overrideMode = FocusMode(name = "Override", schedule = Schedule(0, 1))
        whenever(modeRepo.getAllModesFlow()).thenReturn(flowOf(listOf(overrideMode)))
        whenever(prefsStore.userPreferencesFlow)
            .thenReturn(flowOf(UserPreferences(currentModeOverrideId = overrideMode.id)))

        val useCase = GetCurrentModeUseCase(modeRepo, prefsStore)
        assertEquals(overrideMode, useCase.currentModeFlow().first())
    }
}
```

---

#### MISSING TEST: `ScheduleModeTransitionsUseCase.findNextTriggerTime`
**PRIORITY:** MUST HAVE  
**Why:** Alarm scheduling is mission-critical; missing an alarm means the launcher stays in the wrong mode overnight.

**SUGGESTED TEST CASES:**
1. `findNextTriggerTime returns today when mode start is in the future`
2. `findNextTriggerTime returns tomorrow when mode start has already passed`
3. `findNextTriggerTime skips non-active days`
4. `findNextTriggerTime returns null when no active day exists in next 7 days`
5. `findNextTriggerTime handles overnight schedules correctly` (start=21:00, end=07:00)

**IMPLEMENTATION (sketch):**
```kotlin
class ScheduleModeTransitionsUseCaseTest {
    // Extract findNextTriggerTime to an internal function or test via reflection.
    @Test
    fun `next trigger tomorrow when start already passed today`() {
        val mode = FocusMode(name = "Work", schedule = Schedule(540, 1020)) // 9-17
        val from = LocalDateTime.of(2024, 1, 1, 18, 0) // Monday 18:00
        val next = invokeFindNextTriggerTime(mode, from)
        assertEquals(LocalDateTime.of(2024, 1, 2, 9, 0), next)
    }
}
```
> **Recommended refactor:** Make `findNextTriggerTime` an `internal` top-level function or move it to a `ScheduleCalculator` object.

---

#### MISSING TEST: `TrackAppLaunchUseCase`
**PRIORITY:** SHOULD HAVE  
**Why:** Thin wrapper, but verifying delegation prevents silent breakages when analytics requirements change.

**SUGGESTED TEST CASES:**
1. `trackLaunch delegates to incrementAppsLaunched`
2. `trackGatedBypass delegates to incrementGatedBypasses`
3. `trackModeOverride delegates to incrementModeOverrides`

**IMPLEMENTATION (sketch):**
```kotlin
class TrackAppLaunchUseCaseTest {
    @Mock lateinit var repo: AnalyticsRepository
    @Test
    fun `trackLaunch delegates correctly`() = runTest {
        TrackAppLaunchUseCase(repo).trackLaunch()
        verify(repo).incrementAppsLaunched()
    }
}
```

---

### 2.2 Repositories

#### MISSING TEST: `AppRepository`
**PRIORITY:** MUST HAVE  
**Why:** Repository is the data-source abstraction; if mapping between `AppEntity` and `AppItem` drifts, the UI breaks.

**SUGGESTED TEST CASES:**
1. `getAllAppsFlow maps entities to domain items`
2. `getAppById returns mapped domain item`
3. `getAppById returns null when DAO returns null`
4. `replaceAll clears table and inserts new list`
5. `insertApp converts domain to entity before passing to DAO`

**IMPLEMENTATION (sketch):**
```kotlin
@RunWith(MockitoJUnitRunner::class)
class AppRepositoryTest {
    @Mock lateinit var appDao: AppDao
    private lateinit var repo: AppRepository

    @Before fun setup() { repo = AppRepository(appDao) }

    @Test
    fun `getAllAppsFlow emits mapped domain list`() = runTest {
        val entity = AppEntity("1", "Maps", "com.google.maps")
        whenever(appDao.getAllFlow()).thenReturn(flowOf(listOf(entity)))

        repo.getAllAppsFlow().test {
            assertEquals(listOf(AppItem("1", "Maps", "com.google.maps")), awaitItem())
            awaitComplete()
        }
    }
}
```

---

#### MISSING TEST: `ModeRepository`
**PRIORITY:** MUST HAVE  
**Why:** Contains complex bidirectional mapping (`gateOverrides` pipe-string, `activeDays` comma-string, appId ordering).

**SUGGESTED TEST CASES:**
1. `getModeById loads apps in correct order from appIds`
2. `toDomain parses gateOverrides pipe-delimited string correctly`
3. `toDomain handles blank gateOverrides as emptyMap`
4. `toDomain falls back to all days when activeDays string is empty`
5. `toEntity serializes gateOverrides and activeDays correctly`
6. `initDefaultModesIfEmpty inserts defaults only when count == 0`
7. `updateModeGateOverrides updates entity gateOverrides string`
8. `getAllModes preserves sortOrder from query`

**IMPLEMENTATION (sketch):**
```kotlin
class ModeRepositoryTest {
    @Mock lateinit var modeDao: ModeDao
    @Mock lateinit var appDao: AppDao

    @Test
    fun `gateOverrides roundtrip`() = runTest {
        val repo = ModeRepository(modeDao, appDao)
        val mode = FocusMode(
            name = "Test",
            schedule = Schedule(0, 1),
            gateOverrides = mapOf("pkg1" to "BREATHING", "pkg2" to "NONE")
        )
        whenever(modeDao.getById(any())).thenReturn(mode.toEntity())
        assertEquals(mode.gateOverrides, repo.getModeById(mode.id)!!.gateOverrides)
    }
}
```
> **Recommended refactor:** Extract `ModeEntity.toDomain(...)` and `FocusMode.toEntity()` to `internal` extension functions so they can be unit-tested in isolation without Room.

---

#### MISSING TEST: `AnalyticsRepository`
**PRIORITY:** MUST HAVE  
**Why:** Analytics data drives the focus score and streaks. Any bug in DAO-to-domain mapping corrupts user-visible metrics.

**SUGGESTED TEST CASES:**
1. `getTodayAnalytics returns default DailyAnalytics when no row exists`
2. `ensureTodayExists inserts empty entity when missing`
3. `ensureTodayExists does nothing when row already exists`
4. `incrementAppsLaunched calls ensureTodayExists then DAO increment`
5. `updateFocusScore upserts score for today`
6. `syncUsageStats handles SecurityException silently`

**IMPLEMENTATION (sketch):**
```kotlin
class AnalyticsRepositoryTest {
    @Mock lateinit var analyticsDao: AnalyticsDao
    @Mock lateinit var context: Context

    @Test
    fun `getTodayAnalytics returns default when missing`() = runTest {
        whenever(analyticsDao.getByDate(any())).thenReturn(null)
        val repo = AnalyticsRepository(analyticsDao, context)
        val today = LocalDate.now()
        assertEquals(DailyAnalytics(date = today), repo.getTodayAnalytics())
    }
}
```

---

### 2.3 Database / Room Integration Tests

#### MISSING TEST: `EverDatabase` + DAO integration tests
**PRIORITY:** MUST HAVE  
**Why:** Schema evolution, SQL query correctness, and TypeConverter integration must be validated on a real (in-memory) Room database.

**SUGGESTED TEST CASES:**
1. `AppDao CRUD round-trip` — insert, getById, update, delete
2. `ModeDao getAll returns modes ordered by sort_order ASC, created_at ASC`
3. `ModeDao count returns correct number`
4. `AnalyticsDao getLast7Days returns at most 7 rows ordered DESC`
5. `AnalyticsDao incrementUnlocks increments correct column`
6. `Database schema version is 2`
7. `Destructive migration works when schema changes` (if applicable)

**IMPLEMENTATION (sketch):**
```kotlin
@RunWith(AndroidJUnit4::class)
class EverDatabaseTest {
    private lateinit var db: EverDatabase
    private lateinit var appDao: AppDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EverDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        appDao = db.appDao()
    }

    @After
    fun closeDb() = db.close()

    @Test
    fun `appDao insert and retrieve by id`() = runTest {
        val app = AppEntity("1", "Calendar", "com.google.calendar")
        appDao.insert(app)
        assertEquals(app, appDao.getById("1"))
    }
}
```

---

#### MISSING TEST: `Converters`
**PRIORITY:** MUST HAVE  
**Why:** TypeConverters are the serialization layer. Bugs here corrupt schedules, app lists, and preferences silently.

**SUGGESTED TEST CASES:**
1. `instantToLong and longToInstant roundtrip including null`
2. `localDateToLong and longToLocalDate roundtrip including null`
3. `daysToString and stringToDays roundtrip with full week and singleton set`
4. `daysToString returns null for null input`
5. `stringToDays returns emptySet for empty string`
6. `stringListToJson and jsonToStringList roundtrip`
7. `jsonToStringList returns emptyList for invalid JSON`
8. `jsonToStringList returns emptyList for null`
9. `gateTypeToString and stringToGateType roundtrip including null`
10. `stringToGateType returns null for unknown value`
11. `themeToString and stringToTheme roundtrip`
12. `stringToTheme defaults to SYSTEM for unknown value`

**IMPLEMENTATION (sketch):**
```kotlin
class ConvertersTest {
    private val converters = Converters()

    @Test
    fun `instant roundtrip`() {
        val now = Instant.now()
        assertEquals(now, converters.longToInstant(converters.instantToLong(now)))
    }

    @Test
    fun `jsonToStringList returns emptyList for garbage input`() {
        assertEquals(emptyList<String>(), converters.jsonToStringList("not json"))
    }
}
```

---

### 2.4 ViewModels

#### MISSING TEST: `LauncherViewModel`
**PRIORITY:** MUST HAVE  
**Why:** Core user-facing ViewModel. Contains gate-override logic, search filtering, and hidden-app counting.

**SUGGESTED TEST CASES:**
1. `applyGateOverrides disables gate for NONE override`
2. `applyGateOverrides changes gate type when override is valid GateType`
3. `applyGateOverrides leaves app unchanged when package not in overrides`
4. `applyGateOverrides ignores invalid GateType names`
5. `onSearchQueryChange filters allInstalledApps by display name case-insensitive`
6. `onSearchQueryChange returns empty list for blank query`
7. `hideSearch clears query and results`
8. `uiState hiddenAppCount is calculated correctly when mode changes`

**IMPLEMENTATION (sketch):**
```kotlin
@RunWith(AndroidJUnit4::class)
class LauncherViewModelTest {
    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `applyGateOverrides NONE disables gate`() {
        val vm = LauncherViewModel(ApplicationProvider.getApplicationContext())
        val app = AppItem("1", "Maps", "com.maps", isGated = true, gateType = GateType.DELAY)
        val mode = FocusMode(name = "Test", schedule = Schedule(0, 1),
            gateOverrides = mapOf("com.maps" to "NONE"))

        val result = vm.javaClass.getDeclaredMethod("applyGateOverrides", List::class.java, FocusMode::class.java)
            .apply { isAccessible = true }
            .invoke(vm, listOf(app), mode) as List<AppItem>

        assertFalse(result.first().isGated)
        assertNull(result.first().gateType)
    }
}
```
> **Recommended refactor:** Make `applyGateOverrides` an `internal` extension function or move it to a pure function inside the domain layer.

---

#### MISSING TEST: `DashboardViewModel`
**PRIORITY:** MUST HAVE  
**Why:** Focus score and streak logic directly affect user motivation/retention.

**SUGGESTED TEST CASES:**
1. `isFocusModeCompletedToday returns true when a non-overnight mode ended earlier today`
2. `isFocusModeCompletedToday returns false when only overnight modes exist`
3. `calculateStreak counts consecutive days with score >= 60`
4. `calculateStreak includes today if score >= 60`
5. `calculateStreak breaks on first day with score < 60`
6. `calculateStreak best is max of current streak and saved best`
7. `switchMode sets override and increments analytics`

**IMPLEMENTATION (sketch):**
```kotlin
class DashboardViewModelTest {
    @Test
    fun `calculateStreak counts backwards from yesterday`() {
        val vm = DashboardViewModel(ApplicationProvider.getApplicationContext())
        val days = listOf(
            DailyAnalytics(LocalDate.now().minusDays(2), focusScore = 70),
            DailyAnalytics(LocalDate.now().minusDays(1), focusScore = 70),
            DailyAnalytics(LocalDate.now(), focusScore = 70)
        )
        val method = vm.javaClass.getDeclaredMethod("calculateStreak", List::class.java, Int::class.java)
            .apply { isAccessible = true }
        val (streak, best) = method.invoke(vm, days, 0) as Pair<Int, Int>
        assertEquals(3, streak)
        assertEquals(3, best)
    }
}
```
> **Recommended refactor:** Extract `calculateStreak` and `isFocusModeCompletedToday` to a pure `internal object DashboardCalculator`.

---

#### MISSING TEST: `OnboardingViewModel`
**PRIORITY:** SHOULD HAVE  
**Why:** Onboarding is a one-time critical path; bugs here block user activation.

**SUGGESTED TEST CASES:**
1. `nextStep advances from WELCOME -> APP_PICKER -> MODE_SETUP -> SET_DEFAULT`
2. `toggleApp adds and removes appId from selected set`
3. `moveModeUp swaps modes and preserves list size`
4. `moveModeDown does nothing at last index`
5. `completeOnboarding inserts selected apps and seeds modes with sortOrder`
6. `completeOnboarding caps apps per mode to MAX_APPS`

**IMPLEMENTATION (sketch):**
```kotlin
@RunWith(AndroidJUnit4::class)
class OnboardingViewModelTest {
    @Test
    fun `nextStep advances through onboarding flow`() {
        val vm = OnboardingViewModel(ApplicationProvider.getApplicationContext())
        assertEquals(OnboardingStep.WELCOME, vm.uiState.value.step)
        vm.nextStep()
        assertEquals(OnboardingStep.APP_PICKER, vm.uiState.value.step)
    }
}
```

---

#### MISSING TEST: `SettingsViewModel`
**PRIORITY:** SHOULD HAVE  
**Why:** Thin ViewModel, but verifying that each setter delegates to `UserPreferencesStore` prevents regressions.

**SUGGESTED TEST CASES:**
1. `setTheme delegates to prefsStore`
2. `setFontChoice delegates to prefsStore`
3. `deleteMode delegates to modeRepository`
4. `saveMode updates existing mode or inserts new mode`

---

### 2.5 Preferences / DataStore

#### MISSING TEST: `UserPreferencesStore`
**PRIORITY:** SHOULD HAVE  
**Why:** Default values and enum parsing fallback logic must be verified.

**SUGGESTED TEST CASES:**
1. `userPreferencesFlow emits defaults when DataStore is empty`
2. `userPreferencesFlow recovers from IOException by emitting emptyPreferences`
3. `invalid enum strings in DataStore fall back to SYSTEM / SYSTEM / MEDIUM`
4. `setModeOverride with null removes key from DataStore`
5. `updateStreak writes both current and best streak`

**IMPLEMENTATION (sketch):**
```kotlin
@RunWith(AndroidJUnit4::class)
class UserPreferencesStoreTest {
    private lateinit var store: UserPreferencesStore

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Use a test-specific DataStore file and clean it up before each test.
        store = UserPreferencesStore(context)
    }

    @Test
    fun `flow emits defaults for empty store`() = runTest {
        val prefs = store.userPreferencesFlow.first()
        assertEquals(ThemePreference.SYSTEM, prefs.globalTheme)
        assertEquals(FontSize.MEDIUM, prefs.fontSize)
        assertFalse(prefs.hasCompletedOnboarding)
    }
}
```

---

### 2.6 Compose UI Tests (ui-test-junit4)

**Current state:** `androidTest/` directory is completely empty. The `build.gradle.kts` already includes `compose-ui-test-junit4` and `androidx-test-junit`, so the infrastructure is present.

#### MISSING TEST: `WelcomeScreen`
**PRIORITY:** SHOULD HAVE

**SUGGESTED TEST CASES:**
1. `welcome screen displays app name and tagline`
2. `clicking Get Started invokes onGetStarted`

**IMPLEMENTATION (sketch):**
```kotlin
@RunWith(AndroidJUnit4::class)
class WelcomeScreenTest {
    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun getStartedClickFires() {
        var clicked = false
        composeTestRule.setContent {
            WelcomeScreen(onGetStarted = { clicked = true })
        }
        composeTestRule.onNodeWithText("Get Started").performClick()
        assertTrue(clicked)
    }
}
```

---

#### MISSING TEST: `AppPickerScreen`
**PRIORITY:** SHOULD HAVE

**SUGGESTED TEST CASES:**
1. `loading state shows CircularProgressIndicator`
2. `app list displays display names`
3. `tapping an app invokes onToggleApp with correct id`
4. `selected apps show check icon`
5. `Next button is visible and clickable`

---

#### MISSING TEST: `LauncherScreen`
**PRIORITY:** MUST HAVE

**SUGGESTED TEST CASES:**
1. `time and date texts are displayed`
2. `mode indicator shows current mode name`
3. `empty app list shows no-apps message`
4. `app list items are displayed with correct names`
5. `swipe-up gesture reveals search overlay`
6. `search query filters app list`
7. `hidden app count chip is shown when showHiddenCount is true`

**IMPLEMENTATION (sketch):**
```kotlin
@RunWith(AndroidJUnit4::class)
class LauncherScreenTest {
    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun launcherDisplaysAppsAndMode() {
        val state = LauncherUiState(
            currentTime = "09:41",
            currentDate = "Monday, Jan 1",
            currentMode = FocusMode(name = "Focus", schedule = Schedule(540, 1020)),
            visibleApps = listOf(AppItem("1", "Calendar", "com.calendar")),
            hiddenAppCount = 5,
            showHiddenCount = true
        )
        composeTestRule.setContent {
            LauncherScreen(state = state, onOpenSettings = {})
        }
        composeTestRule.onNodeWithText("Focus").assertIsDisplayed()
        composeTestRule.onNodeWithText("Calendar").assertIsDisplayed()
        composeTestRule.onNodeWithText("5 apps hidden").assertIsDisplayed()
    }
}
```
> **Note:** `LauncherScreen` takes a `viewModel` parameter with a default. For testability, consider adding an overload that accepts a raw `LauncherUiState` and callback lambdas, or provide a test ViewModel factory.

---

#### MISSING TEST: `DashboardScreen`
**PRIORITY:** SHOULD HAVE

**SUGGESTED TEST CASES:**
1. `focus score ring displays today's score`
2. `usage access banner shown when permission is denied`
3. `streak text shown when currentStreak > 0`
4. `mode switch buttons call vm::switchMode`
5. `follow schedule button is visible when override is active`

---

#### MISSING TEST: `Gate Screens`
**PRIORITY:** SHOULD HAVE

**SUGGESTED TEST CASES:**
1. `BreathingGateScreen displays app name and auto-fires onGatePassed after 5s`
2. `IntentionGateScreen requires at least 5 characters before enabling continue`
3. `IntentionGateScreen dismisses on cancel`
4. `DelayGateScreen shows countdown and fires onGatePassed after 10s`

---

#### MISSING TEST: `SettingsScreen`
**PRIORITY:** NICE TO HAVE

**SUGGESTED TEST CASES:**
1. `theme row cycles through SYSTEM -> LIGHT -> DARK -> AMOLED`
2. `toggle rows call their callbacks with new checked state`
3. `version name is displayed`

---

### 2.7 Edge Cases & Regression Tests

| # | MISSING TEST | Priority | Scenario |
|---|--------------|----------|----------|
| 1 | `Schedule.isActiveAt(1439, SUNDAY)` | MUST HAVE | Exact boundary at end of day |
| 2 | `Schedule` with `startMinutes == endMinutes` | MUST HAVE | Zero-duration schedule behavior is undefined |
| 3 | `FocusMode` with exactly 0 apps | SHOULD HAVE | Empty mode validation |
| 4 | `ModeRepository` with malformed `gateOverrides` | MUST HAVE | `"pkg1|pkg2"` or missing colon should not crash |
| 5 | `ModeRepository` with malformed `activeDays` | MUST HAVE | `"foo,bar"` should fallback to all days |
| 6 | `AppRepository.getAppsByIds` with empty id list | SHOULD HAVE | Should return empty list, not crash DAO |
| 7 | `AnalyticsRepository` at day boundary (23:59 -> 00:00) | MUST HAVE | `LocalDate.now()` must not cache stale date across suspend boundaries |
| 8 | `LauncherViewModel` search with special characters | SHOULD HAVE | Query `"` should not crash regex/filter |
| 9 | `DashboardViewModel.calculateStreak` with gaps | MUST HAVE | `score >= 60` on today and day-2 but `< 60` on day-1 should yield streak = 1 |
| 10 | `Converters.jsonToStringList` with nested arrays | NICE TO HAVE | `[["a"]]` should safely return emptyList |
| 11 | `EverDatabase.getInstance` singleton behavior | SHOULD HAVE | Second call must return same instance |
| 12 | `ModeTransitionReceiver` `onReceive` with unknown action | SHOULD HAVE | Should finish pending result without crash |

---

## Part 3 — Test Architecture & Convention Issues

### 3.1 Naming Conventions
- **Good:** Existing tests use backtick names (e.g., `` `perfect day yields max score` ``).
- **Bad:** Test classes are placed directly in `com.everlauncher` rather than mirroring the package structure of the classes under test.
  - `AppItemValidationTest` should be in `com.everlauncher.domain.model`
  - `FocusScoreCalculationTest` should be in `com.everlauncher.domain.usecase`
  - `GetCurrentModeUseCaseTest` should be in `com.everlauncher.domain.usecase`

### 3.2 Missing Test Infrastructure
- **No `InstantTaskExecutorRule`** in ViewModel tests (because there are no ViewModel tests yet). Required for `LiveData`-style state assertions, though `StateFlow` can be tested with `kotlinx-coroutines-test`.
- **No `MainDispatcherRule`** to replace `Dispatchers.Main` in coroutine tests.
- **No Mockito / MockK** dependency in `build.gradle.kts`. Add `testImplementation(libs.mockk)` or `testImplementation("org.mockito:mockito-core:5.x")` and `mockito-inline` for mocking `final` Kotlin classes.
- **No Turbine** dependency. Highly recommended for `Flow` testing: `testImplementation("app.cash.turbine:turbine:1.1.0")`.
- **No `robolectric`** dependency. Many Android-framework-touching classes (`LauncherViewModel`, `DetectInstalledAppsUseCase`) are difficult to unit-test without Robolectric. Suggest adding `testImplementation("org.robolectric:robolectric:4.12")`.

### 3.3 Testability Debt in Production Code
Several classes are hard to test because they instantiate dependencies internally:

| Class | Problem | Fix |
|-------|---------|-----|
| `LauncherViewModel` | Instantiates `EverDatabase`, `UserPreferencesStore`, repositories, and use cases inside `init` | Accept dependencies via constructor (use a Factory + Hilt/Koin, or at minimum an `internal` constructor for tests) |
| `DashboardViewModel` | Same as above | Same fix |
| `OnboardingViewModel` | Same as above | Same fix |
| `SettingsViewModel` | Same as above | Same fix |
| `DetectInstalledAppsUseCase` | Requires real `Context` to test private `isSystemUtilityPackage` | Extract `AppFilter` object with internal visibility |
| `ScheduleModeTransitionsUseCase` | `findNextTriggerTime` is private and uses `LocalDateTime.now()` internally | Make it `internal` and accept a `from: LocalDateTime` parameter |
| `GetCurrentModeUseCase` | `resolveBySchedule` uses `LocalDateTime.now()` | Inject a `() -> LocalDateTime` clock function |

---

## Part 4 — Recommended Build.gradle Additions

Add the following to `app/build.gradle.kts` inside `dependencies { ... }`:

```kotlin
// Unit-test mocking
 testImplementation("io.mockk:mockk:1.13.11")
 testImplementation("org.mockito:mockito-core:5.12.0")
 testImplementation("org.mockito:mockito-inline:5.2.0")

// Coroutine test helpers
 testImplementation("app.cash.turbine:turbine:1.1.0")
 testImplementation("androidx.arch.core:core-testing:2.2.0")

// Robolectric for Android-unit tests without emulator
 testImplementation("org.robolectric:robolectric:4.12.1")

// Room testing
 testImplementation("androidx.room:room-testing:2.6.1")

// Compose UI tests already present:
// androidTestImplementation(platform(libs.compose.bom))
// androidTestImplementation(libs.androidx.test.junit)
// androidTestImplementation(libs.compose.ui.test.junit4)
```

---

## Part 5 — Shippable Test-Coverage Roadmap

### Phase 1 — MUST HAVE (Block Release)
1. `ConvertersTest` — full round-trip coverage.
2. `AppDaoTest`, `ModeDaoTest`, `AnalyticsDaoTest` — in-memory Room integration tests.
3. `AppRepositoryTest`, `ModeRepositoryTest`, `AnalyticsRepositoryTest` — mocked DAO tests.
4. `GetCurrentModeUseCaseIntegrationTest` — real use-case with mocked repositories.
5. `ScheduleModeTransitionsUseCaseTest` — alarm-schedule logic.
6. `LauncherViewModel` gate-override and search-filter tests (refactor to testable first).
7. `DashboardViewModel` streak and focus-completion tests (refactor to testable first).

### Phase 2 — SHOULD HAVE (Shippable with minor risk)
1. `DetectInstalledAppsUseCase` prefix-filter tests.
2. `TrackAppLaunchUseCase` delegation tests.
3. `OnboardingViewModel` flow and completion tests.
4. `UserPreferencesStore` default-value tests.
5. Compose UI tests for `WelcomeScreen`, `AppPickerScreen`, `LauncherScreen`.
6. Compose UI tests for gate screens (`BreathingGateScreen`, `IntentionGateScreen`, `DelayGateScreen`).

### Phase 3 — NICE TO HAVE (Post-release backlog)
1. Full `DashboardScreen` Compose UI tests.
2. `SettingsScreen` Compose UI tests.
3. `MainActivity` / navigation flow end-to-end tests.
4. `ModeTransitionReceiver` instrumentation tests.
5. Property-based tests for `CalculateFocusScoreUseCase` (e.g., with Kotest property testing).

---

## Part 6 — Quick Reference: Untested Source Files

**Completely untested files (100% gap):**
- `data/db/Converters.kt`
- `data/db/EverDatabase.kt`
- `data/db/AppDao.kt`
- `data/db/ModeDao.kt`
- `data/db/AnalyticsDao.kt`
- `data/db/entities/AppEntity.kt`
- `data/db/entities/ModeEntity.kt`
- `data/db/entities/AnalyticsEntity.kt`
- `data/repository/AppRepository.kt`
- `data/repository/ModeRepository.kt`
- `data/repository/AnalyticsRepository.kt`
- `data/preferences/UserPreferencesStore.kt`
- `domain/usecase/DetectInstalledAppsUseCase.kt`
- `domain/usecase/ScheduleModeTransitionsUseCase.kt`
- `domain/usecase/TrackAppLaunchUseCase.kt`
- `ui/launcher/LauncherViewModel.kt`
- `ui/launcher/LauncherScreen.kt`
- `ui/launcher/AppListItem.kt`
- `ui/launcher/ModeIndicator.kt`
- `ui/dashboard/DashboardViewModel.kt`
- `ui/dashboard/DashboardScreen.kt`
- `ui/dashboard/FocusScoreRing.kt`
- `ui/dashboard/WeeklyChart.kt`
- `ui/onboarding/OnboardingViewModel.kt`
- `ui/onboarding/WelcomeScreen.kt`
- `ui/onboarding/AppPickerScreen.kt`
- `ui/onboarding/ModeSetupScreen.kt`
- `ui/onboarding/SetDefaultLauncherScreen.kt`
- `ui/settings/SettingsViewModel.kt`
- `ui/settings/SettingsScreen.kt`
- `ui/settings/GateConfigScreen.kt`
- `ui/settings/ModeEditorScreen.kt`
- `ui/gates/BreathingGateScreen.kt`
- `ui/gates/DelayGateScreen.kt`
- `ui/gates/IntentionGateScreen.kt`
- `receiver/ModeTransitionReceiver.kt`
- `service/UsageTrackingService.kt`
- `MainActivity.kt`
- `SettingsActivity.kt`
- `EverLauncherApp.kt`

**Partially tested files:**
- `domain/model/AppItem.kt` — init block only
- `domain/model/FocusMode.kt` — init block only
- `domain/model/Schedule.kt` — `isActiveAt` only
- `domain/usecase/CalculateFocusScoreUseCase.kt` — well covered
- `domain/usecase/GetCurrentModeUseCase.kt` — only via inline copy of logic in tests

---

*End of Audit Report*
