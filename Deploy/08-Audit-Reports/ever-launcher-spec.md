# EVER LAUNCHER — Complete Product & Engineering Specification

## FOR AI CODING AGENT: READ THIS FIRST

You are building "Ever Launcher" — a cross-platform minimalist launcher app for iOS and Android. This document is the single source of truth. Follow every instruction precisely. Do not skip sections. Do not make assumptions about APIs — use only the APIs and patterns documented here. If something is ambiguous, implement the safer/simpler option and leave a `// TODO: DECISION NEEDED` comment.

**This is TWO separate native projects** (not a cross-platform framework). iOS limitations make a shared codebase impossible — iOS cannot replace the home screen; it can only provide widgets. Android can fully replace the launcher. Each platform needs its own native project.

### Critical constraints you must understand before writing any code:

1. **iOS CANNOT replace the home screen.** Apple does not allow third-party launcher apps. Ever Launcher on iOS is a **WidgetKit-based widget** that sits on the user's home screen, plus a companion app for configuration. This is how Dumbify, Blank Spaces, and every other iOS "launcher" works. There is no workaround.

2. **Android CAN fully replace the home screen.** Android allows third-party launchers via the `CATEGORY_HOME` intent filter. Ever Launcher on Android is a full launcher replacement.

3. **Apple Review Guideline 2.5.8 was updated in 2024** to explicitly allow apps that "simulate multi-app widget experiences." This means our iOS widget approach is App Store compliant.

4. **iOS widgets cannot run arbitrary code on tap.** Widget taps use deep links (URL schemes) or AppIntents to open the parent app or other apps. Interactive elements are limited to `Button` and `Toggle` backed by `AppIntent`. No gesture recognizers, no scroll views, no text input.

5. **No user data leaves the device.** All analytics, preferences, and focus data are stored locally. No server, no cloud sync, no accounts. This simplifies privacy compliance and is a selling point.

---

## TABLE OF CONTENTS

1. [Product Overview](#1-product-overview)
2. [Target Users & Goals](#2-target-users--goals)
3. [Feature Specification](#3-feature-specification)
4. [iOS Project — Complete Specification](#4-ios-project)
5. [Android Project — Complete Specification](#5-android-project)
6. [Data Models (Shared Logic)](#6-data-models)
7. [UI/UX Design Specification](#7-uiux-design-specification)
8. [Screen-by-Screen Specification](#8-screen-by-screen-specification)
9. [Performance Requirements](#9-performance-requirements)
10. [Testing Requirements](#10-testing-requirements)
11. [App Store & Play Store Submission](#11-store-submission)
12. [Known Pitfalls & Anti-Patterns](#12-known-pitfalls)

---

## 1. PRODUCT OVERVIEW

### What is Ever Launcher?

Ever Launcher is a minimalist phone launcher that helps users focus by showing only the apps they need, when they need them. It goes beyond a static text list (which is all competitors offer) by introducing **context-aware modes** that automatically adjust which apps are visible based on time of day.

### Why does it exist?

The existing minimalist launcher market on iOS has a clear gap:
- **Dumbify** (3.0★, 202 ratings, $4.99): Routes through Apple Shortcuts causing visible delay. Requires URL scheme knowledge for custom apps. Only one widget size. No focus features.
- **Blank Spaces** (4.5★, 50K+ members, subscription $24/yr): Better execution but uses an expensive subscription model. Has app locking and breathing exercises but no time-based context switching.
- **Dumb Phone** ($10 bundle): Has app blocking and mindful pauses but no context awareness.
- **Minimalist Phone** ($20/yr or $60 lifetime): Android-first, recently on iOS. Smart folders by category.

**Nobody offers context-aware mode switching + one-time pricing + cross-platform.**

### Business Model

- **Free tier**: Basic launcher with 1 mode, up to 5 apps visible
- **Paid tier**: $7.99 one-time purchase (non-consumable IAP). Unlimited apps, unlimited modes, focus score, mindful gates, streaks, all themes
- **No subscription. No ads. No data collection.**

### Revenue justification

- Undercuts Blank Spaces ($24/yr) and Minimalist Phone ($60 lifetime) significantly
- One-time purchase removes friction and is the #1 praise point in Dumbify reviews
- Free tier drives organic downloads; upgrade wall at mode creation is the conversion point

---

## 2. TARGET USERS & GOALS

### Primary users

| Segment | Need | Feature they care about most |
|---------|------|------------------------------|
| Students (18-25) | Stop TikTok/Instagram during study | Mindful gates + focus mode scheduling |
| Knowledge workers (25-45) | Deep focus during work hours | Context modes that auto-switch by time |
| Digital minimalists (all ages) | Intentional phone use as lifestyle | Clean aesthetic + focus streaks |

### Core user goal

"I want my phone to help me focus on my life, not steal my attention. I want to see only productive tools when I'm working, and only personal essentials when I'm relaxing."

### Success metrics for the app

- Setup completion in under 90 seconds (measured by time between first app open and widget placed)
- Daily active usage (widget is always visible, so "usage" = app opens for settings + focus score views)
- Focus streak retention at 7 days, 30 days
- App Store rating target: 4.5+ stars

---

## 3. FEATURE SPECIFICATION

### 3.1 Core Features (Phase 1 — MVP)

#### 3.1.1 App Launcher Widget

**What**: A full-screen widget (iOS) or home screen (Android) displaying a vertical list of app names in clean typography. Tapping an app name opens that app.

**iOS implementation**:
- WidgetKit widget using `AppIntentConfiguration`
- Widget family: `.systemLarge` (full-width, takes ~4 app rows on home screen) and `.systemExtraLarge` on iPad
- Each app name is rendered as a `Link` with a deep link URL scheme (e.g., `tel://` for Phone, `message://` for Messages)
- For apps without public URL schemes: use `AppIntent` with `openAppWhenRun = true` that triggers an Apple Shortcut via `INShortcut`
- The widget must support `containerBackground` modifier for iOS 17+ to remove default widget padding
- Widget must provide a `TimelineProvider` that generates entries based on current mode

**Android implementation**:
- Activity with `CATEGORY_HOME` and `CATEGORY_DEFAULT` intent filters in `AndroidManifest.xml`
- `LauncherApps` system service to query installed apps
- `PackageManager` to resolve launch intents
- Jetpack Compose `LazyColumn` for the app list
- Direct `startActivity(launchIntent)` — no routing through any intermediate app

**App list rendering rules**:
- Font: System default serif for display, system sans-serif for body (platform-specific fonts look most native)
- Each app name is a single line of text, left-aligned
- Vertical padding between items: 14dp (Android) / 14pt (iOS)
- Maximum apps per mode: 8 (this is a design decision to prevent clutter, not a technical limit)
- If fewer than 8 apps, remaining space is empty (intentional calm)

#### 3.1.2 Context Modes

**What**: Named groups of apps that automatically activate based on time-of-day rules. Each mode has a name, a list of apps (max 8), a schedule (start time + end time), and a theme preference (light/dark/system).

**Default modes created during onboarding**:

| Mode | Default Schedule | Default Apps | Theme |
|------|-----------------|--------------|-------|
| Focus | 9:00 AM – 5:00 PM, Mon-Fri | Calendar, Mail, Notes, Slack, Files | Dark |
| Personal | 5:00 PM – 9:00 PM, Mon-Fri + All day Sat-Sun | Phone, Messages, Camera, Maps, Music | System |
| Wind Down | 9:00 PM – 7:00 AM, Every day | Clock, Phone, Kindle/Books, Meditation | Dark (warm) |

**Schedule conflict resolution**: If two modes overlap in schedule, the mode created most recently takes priority. The user should be warned during mode creation if there's a conflict.

**Manual override**: User can always manually switch modes from the companion app regardless of schedule. Manual override persists until the next scheduled mode transition.

**iOS implementation**:
- Store modes in `UserDefaults` via App Group (shared between app and widget extension)
- `TimelineProvider` generates timeline entries at each mode transition time
- Widget reads current time, determines active mode, renders that mode's app list
- Use `WidgetCenter.shared.reloadAllTimelines()` when user changes mode config

**Android implementation**:
- Store modes in Room database
- Use `AlarmManager` with `setExactAndAllowWhileIdle()` for mode transitions
- `BroadcastReceiver` triggers UI update on mode change
- Fallback: check current time on every `onResume()` of launcher activity

#### 3.1.3 One-Tap Setup

**What**: An onboarding flow that auto-detects installed apps and lets users assign them to modes by tapping. No URL schemes, no technical knowledge required.

**Flow (4 screens maximum)**:

1. **Welcome screen**: App name, one-line value prop ("Your phone, focused."), "Get Started" button
2. **Pick your apps**: Grid/list of all installed apps (auto-detected). User taps to select which apps they want accessible. Selected apps get a checkmark. Pre-select the most common productivity + essential apps by default.
3. **Set your modes**: Show the 3 default modes with their schedules. User can edit names, times, and drag apps between modes. "Looks good" button to confirm.
4. **Add the widget** (iOS only): Step-by-step visual instruction to add the widget to home screen. On Android, prompt to set Ever Launcher as default launcher.

**iOS app detection**:
- iOS does NOT provide an API to list all installed apps. This is a known platform limitation.
- Instead, maintain a curated list of ~100 most common apps with their URL schemes (see Section 6.3 for the list)
- Use `UIApplication.shared.canOpenURL()` to check which are installed (requires `LSApplicationQueriesSchemes` in Info.plist — Apple allows up to 50 schemes to be queried)
- For apps beyond the curated list: provide "Add Custom App" with a text field for the app name and URL scheme, plus a link to a help page explaining how to find URL schemes
- **This is the same approach Dumbify and Blank Spaces use. There is no better alternative on iOS.**

**Android app detection**:
- `packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)` returns all launchable apps
- This gives package name, app label, and icon for every installed app
- Filter out system apps the user doesn't need (system settings, package installer, etc.)

#### 3.1.4 Theming

**What**: Light mode, dark mode, AMOLED black mode, and system-follow. User picks per-mode or globally.

**Color tokens** (define these as named constants/CSS variables):

| Token | Light | Dark | AMOLED |
|-------|-------|------|--------|
| `background` | #F5F5F0 | #1A1A1C | #000000 |
| `text-primary` | #111111 | #F0F0F0 | #E8E8E8 |
| `text-secondary` | #666666 | #888892 | #777780 |
| `accent` | #2D5F2D | #C8FF00 | #C8FF00 |
| `surface` | #FFFFFF | #242428 | #0A0A0A |
| `border` | #E0E0DC | #2A2A2F | #1A1A1A |
| `destructive` | #CC3333 | #FF4D4D | #FF4D4D |

**Typography**:
- iOS: SF Pro Display (system font, automatically available) for headings, SF Pro Text for body
- Android: Use `fontFamily = FontFamily.Serif` for headings (maps to Noto Serif), `FontFamily.Default` for body (maps to Roboto)
- App name list items: 18sp/pt, medium weight (500)
- Mode label: 10sp/pt, monospaced, uppercase, letter-spacing 2px, accent color
- Section headings in settings: 22sp/pt, serif, regular weight

#### 3.1.5 Basic Analytics (On-Device Only)

**What**: Track daily phone unlocks, total screen time (read from system APIs where available), and which apps were launched through Ever Launcher.

**iOS implementation**:
- `DeviceActivityMonitor` (Screen Time API, requires `FamilyControls` entitlement) — NOTE: This requires the user to grant Screen Time access, which is optional. If not granted, show only Ever Launcher-specific data (app launches from widget).
- Track widget taps via shared `UserDefaults` counter incremented by each `AppIntent.perform()`
- Store daily counts in a local SQLite database (use SwiftData or Core Data)

**Android implementation**:
- `UsageStatsManager` to read app usage (requires `PACKAGE_USAGE_STATS` permission, user must grant in Settings)
- Track launches from the launcher directly in Room database
- Daily summary: total unlocks (from `UsageEvents.Event.KEYGUARD_HIDDEN`), apps launched, time per app

**Privacy**: All data stays on device. No network calls. No analytics SDKs. No crash reporting SDKs. The app's network permission should be `none` (iOS) or not requested (Android). This is both a feature and a compliance advantage.

---

### 3.2 Focus Features (Phase 2)

#### 3.2.1 Mindful Gates

**What**: A configurable friction layer before opening specific apps. When user taps a "gated" app, they see a brief intervention before the app opens.

**Gate types**:

1. **Breathing pause** (default): A 5-second screen showing "Breathe in... Breathe out..." with a pulsing circle animation. App opens automatically after 5 seconds.
2. **Intention prompt**: A screen asking "What are you opening [App Name] for?" with a text input. User must type at least 5 characters and tap "Continue" to proceed. Input is not stored.
3. **Simple delay**: A 10-second countdown timer with the message "Opening [App Name] in 10s". No way to skip.

**Implementation on iOS**:
- When a gated app is tapped in the widget, the deep link first opens the Ever Launcher companion app (not the target app)
- The companion app shows the gate screen
- After the gate is passed, the companion app opens the target app via `UIApplication.shared.open(url)`
- This adds ~0.5s of overhead vs. non-gated apps, which is acceptable

**Implementation on Android**:
- Intercept the launch intent in the launcher activity
- Show the gate as a full-screen composable overlay
- After gate is passed, call `startActivity(targetAppIntent)`

**User configuration**:
- In settings, each app has a toggle: "Add mindful gate"
- Below the toggle, a picker for gate type (breathing / intention / delay)
- Apps can be gated in some modes and not others (e.g., Instagram gated during Focus mode but not during Personal mode)

#### 3.2.2 Focus Score

**What**: A daily score from 0-100 representing how focused the user was, based on objective metrics.

**Scoring formula**:

```
Base score = 100

Deductions:
- Each phone unlock: -1 point (max -30)
- Each gated app bypass: -2 points (max -20)  
- Each mode override (manual switch): -5 points (max -15)
- Screen time over 3 hours: -1 per additional 15 minutes (max -20)

Bonuses:
+ Each 30-minute streak without phone use: +3 (max +15)
+ Completing all focus mode hours: +10
+ No gated app opened: +5

Final score = max(0, min(100, base + bonuses + deductions))
```

**Display**: A simple arc/ring chart on the companion app's home screen showing today's score. Below it, a 7-day bar chart showing daily scores. No gamification beyond this — keep it informational, not judgmental.

#### 3.2.3 Focus Streaks

**What**: Track consecutive days where the user achieved a focus score >= 60.

**Rules**:
- A "focused day" = focus score >= 60
- Streak resets to 0 if score drops below 60 for a day
- Personal best streak is stored and displayed
- No notifications about streaks (the app should reduce phone interaction, not increase it)

---

### 3.3 Polish Features (Phase 3)

#### 3.3.1 Utility Widgets (iOS) / Home Screen Sections (Android)

- **Clock widget**: Current time in large typography, date below. Small widget size.
- **Next event widget**: Shows next calendar event name and time (requires `EventKit` / Calendar permission). Small widget size.
- These are separate WidgetKit widgets (iOS) or home screen sections (Android), not part of the launcher list.

#### 3.3.2 Custom Fonts

- Offer 4 font options for the app list: System Default, Serif, Monospace, Rounded
- iOS: SF Pro, New York (serif), SF Mono, SF Pro Rounded
- Android: Roboto, Noto Serif, JetBrains Mono (bundled), Product Sans fallback to Roboto

#### 3.3.3 Wallpaper-Matched Backgrounds (iOS Only)

- Same trick Blank Spaces uses: user takes a screenshot of their blank home screen wallpaper, imports it into the app, and the widget uses a cropped section as its background to appear transparent
- Implement with `Image` in widget view, cropped to widget position on screen
- Provide 6 built-in solid-color wallpapers (3 light, 3 dark) that match the widget backgrounds perfectly

---

## 4. iOS PROJECT

### 4.1 Project Setup

```
EverLauncher/
├── EverLauncher.xcodeproj
├── EverLauncher/                    # Main app target
│   ├── App/
│   │   ├── EverLauncherApp.swift    # @main entry point
│   │   └── ContentView.swift        # Root navigation
│   ├── Views/
│   │   ├── Onboarding/
│   │   │   ├── WelcomeView.swift
│   │   │   ├── AppPickerView.swift
│   │   │   ├── ModeSetupView.swift
│   │   │   └── WidgetInstructionView.swift
│   │   ├── Home/
│   │   │   ├── DashboardView.swift   # Focus score + streak display
│   │   │   └── ModeEditorView.swift
│   │   ├── Settings/
│   │   │   ├── SettingsView.swift
│   │   │   ├── ThemePickerView.swift
│   │   │   ├── GateConfigView.swift
│   │   │   └── AppManagementView.swift
│   │   └── Gates/
│   │       ├── BreathingGateView.swift
│   │       ├── IntentionGateView.swift
│   │       └── DelayGateView.swift
│   ├── Models/
│   │   ├── AppItem.swift             # App data model
│   │   ├── FocusMode.swift           # Mode data model
│   │   ├── FocusScore.swift          # Score calculation
│   │   └── UserPreferences.swift     # Settings storage
│   ├── Services/
│   │   ├── AppDetectionService.swift  # canOpenURL checks
│   │   ├── ModeScheduler.swift        # Time-based mode resolution
│   │   ├── AnalyticsStore.swift       # On-device analytics (Core Data)
│   │   └── StoreKitManager.swift      # IAP handling
│   ├── Utilities/
│   │   ├── Constants.swift
│   │   ├── ColorTokens.swift
│   │   └── TypographyTokens.swift
│   └── Resources/
│       ├── Assets.xcassets
│       ├── Info.plist
│       └── EverLauncher.entitlements
├── EverLauncherWidget/              # Widget extension target
│   ├── EverLauncherWidget.swift      # Widget definition
│   ├── EverLauncherWidgetBundle.swift
│   ├── TimelineProvider.swift        # Timeline entries
│   ├── WidgetEntryView.swift         # Widget SwiftUI view
│   ├── AppLaunchIntent.swift         # AppIntent for launching apps
│   └── Info.plist
├── Shared/                          # App Group shared code
│   ├── SharedDefaults.swift          # UserDefaults wrapper
│   ├── AppDatabase.swift             # Shared Core Data stack
│   └── Models/                       # Models used by both targets
│       ├── SharedAppItem.swift
│       └── SharedFocusMode.swift
└── Tests/
    ├── EverLauncherTests/
    └── EverLauncherUITests/
```

### 4.2 Xcode Configuration Requirements

- **Deployment target**: iOS 17.0 minimum (required for interactive WidgetKit features and AppIntents)
- **App Group**: `group.com.everlauncher.shared` — MUST be configured in both the app target and widget extension target. This is how data is shared between the app and widget.
- **Capabilities to enable**:
  - App Groups (for shared UserDefaults and Core Data between app and widget)
  - In-App Purchase (for the $7.99 unlock)
- **Info.plist — LSApplicationQueriesSchemes**: List of URL schemes the app can check. Add the top 50 most common app URL schemes (see Section 6.3). Apple enforces a limit of 50 entries.
- **No network entitlement needed** — the app makes zero network calls (except StoreKit for IAP, which uses system frameworks)

### 4.3 Widget Technical Details

#### Widget Definition

```swift
// EverLauncherWidget.swift
import WidgetKit
import SwiftUI

struct EverLauncherWidget: Widget {
    let kind: String = "EverLauncherWidget"
    
    var body: some WidgetConfiguration {
        // Use StaticConfiguration (not AppIntentConfiguration) because
        // the widget content is determined by time-based mode, not user 
        // widget-edit configuration
        StaticConfiguration(
            kind: kind,
            provider: ModeTimelineProvider()
        ) { entry in
            LauncherWidgetView(entry: entry)
                .containerBackground(for: .widget) {
                    Color(entry.mode.backgroundColor)
                }
        }
        .configurationDisplayName("Ever Launcher")
        .description("Your focused app launcher")
        .supportedFamilies([.systemLarge])
    }
}
```

#### Timeline Provider

```swift
// TimelineProvider.swift
struct ModeTimelineProvider: TimelineProvider {
    typealias Entry = ModeEntry
    
    func placeholder(in context: Context) -> ModeEntry {
        ModeEntry(date: Date(), mode: FocusMode.default)
    }
    
    func getSnapshot(in context: Context, completion: @escaping (ModeEntry) -> Void) {
        let currentMode = ModeScheduler.shared.currentMode()
        completion(ModeEntry(date: Date(), mode: currentMode))
    }
    
    func getTimeline(in context: Context, completion: @escaping (Timeline<ModeEntry>) -> Void) {
        let now = Date()
        let scheduler = ModeScheduler.shared
        var entries: [ModeEntry] = []
        
        // Generate entries for the next 24 hours at each mode transition
        let transitions = scheduler.upcomingTransitions(from: now, hours: 24)
        
        for transition in transitions {
            entries.append(ModeEntry(
                date: transition.date,
                mode: transition.mode
            ))
        }
        
        // If no transitions, just show current mode
        if entries.isEmpty {
            entries.append(ModeEntry(date: now, mode: scheduler.currentMode()))
        }
        
        let timeline = Timeline(entries: entries, policy: .atEnd)
        completion(timeline)
    }
}
```

#### App Launch via Deep Links

For each app in the widget, use a `Link` with the app's URL scheme:

```swift
// WidgetEntryView.swift
struct LauncherWidgetView: View {
    let entry: ModeEntry
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Mode label
            Text(entry.mode.name.uppercased())
                .font(.system(size: 10, weight: .medium, design: .monospaced))
                .foregroundColor(Color.accentColor)
                .kerning(2)
                .padding(.bottom, 12)
            
            // App list
            ForEach(entry.mode.apps) { app in
                if let url = URL(string: app.urlScheme) {
                    Link(destination: url) {
                        Text(app.displayName)
                            .font(.system(size: 18, weight: .medium))
                            .foregroundColor(Color.primary)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.vertical, 12)
                    }
                }
                
                if app.id != entry.mode.apps.last?.id {
                    Divider()
                        .opacity(0.15)
                }
            }
            
            Spacer()
            
            // Hidden apps indicator
            if entry.mode.hiddenCount > 0 {
                Text("\(entry.mode.hiddenCount) apps hidden during \(entry.mode.name.lowercased())")
                    .font(.system(size: 10, weight: .medium, design: .monospaced))
                    .foregroundColor(Color.secondary.opacity(0.5))
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 10)
                    .background(Color.secondary.opacity(0.05))
                    .cornerRadius(10)
            }
        }
        .padding(.horizontal, 4)
    }
}
```

#### Handling Gated Apps (iOS)

For apps with mindful gates, the widget link opens the Ever Launcher app with a special URL:

```swift
// Instead of linking directly to the app's URL scheme:
// Link(destination: URL(string: "instagram://")!)

// Link to our own app with a gate parameter:
// Link(destination: URL(string: "everlauncher://gate?app=instagram&scheme=instagram://")!)

// In the main app, handle this URL:
// EverLauncherApp.swift
.onOpenURL { url in
    if url.scheme == "everlauncher", url.host == "gate" {
        let params = URLComponents(url: url, resolvingAgainstBaseURL: false)?.queryItems
        let appScheme = params?.first(where: { $0.name == "scheme" })?.value
        let appName = params?.first(where: { $0.name == "app" })?.value
        // Show gate view, then open target app after gate is passed
        gateViewModel.presentGate(appName: appName ?? "", targetScheme: appScheme ?? "")
    }
}
```

### 4.4 In-App Purchase (StoreKit 2)

```swift
// StoreKitManager.swift
import StoreKit

@MainActor
class StoreKitManager: ObservableObject {
    @Published var isPro: Bool = false
    
    private let productID = "com.everlauncher.pro"
    
    init() {
        Task { await checkEntitlement() }
        // Listen for transaction updates (e.g., family sharing, refunds)
        listenForTransactions()
    }
    
    func checkEntitlement() async {
        for await result in Transaction.currentEntitlements {
            if case .verified(let transaction) = result,
               transaction.productID == productID {
                isPro = true
                return
            }
        }
        isPro = false
    }
    
    func purchase() async throws {
        guard let product = try await Product.products(for: [productID]).first else {
            throw StoreError.productNotFound
        }
        let result = try await product.purchase()
        switch result {
        case .success(let verification):
            if case .verified(_) = verification {
                isPro = true
            }
        case .pending:
            break // Ask-to-Buy or SCA
        case .userCancelled:
            break
        @unknown default:
            break
        }
    }
    
    func restorePurchases() async {
        try? await AppStore.sync()
        await checkEntitlement()
    }
    
    private func listenForTransactions() {
        Task.detached {
            for await result in Transaction.updates {
                if case .verified(let transaction) = result {
                    await transaction.finish()
                    await self.checkEntitlement()
                }
            }
        }
    }
}
```

**Apple Review requirement**: You MUST include a "Restore Purchases" button visible in the settings screen. Without this, the app will be rejected.

---

## 5. ANDROID PROJECT

### 5.1 Project Setup

```
ever-launcher-android/
├── app/
│   ├── build.gradle.kts
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/everlauncher/
│   │   │   │   ├── EverLauncherApp.kt           # Application class
│   │   │   │   ├── MainActivity.kt               # Launcher activity
│   │   │   │   ├── ui/
│   │   │   │   │   ├── theme/
│   │   │   │   │   │   ├── Theme.kt
│   │   │   │   │   │   ├── Color.kt
│   │   │   │   │   │   └── Type.kt
│   │   │   │   │   ├── launcher/
│   │   │   │   │   │   ├── LauncherScreen.kt      # Main launcher composable
│   │   │   │   │   │   ├── AppListItem.kt
│   │   │   │   │   │   └── ModeIndicator.kt
│   │   │   │   │   ├── onboarding/
│   │   │   │   │   │   ├── WelcomeScreen.kt
│   │   │   │   │   │   ├── AppPickerScreen.kt
│   │   │   │   │   │   └── ModeSetupScreen.kt
│   │   │   │   │   ├── settings/
│   │   │   │   │   │   ├── SettingsScreen.kt
│   │   │   │   │   │   ├── ThemePickerScreen.kt
│   │   │   │   │   │   ├── ModeEditorScreen.kt
│   │   │   │   │   │   └── GateConfigScreen.kt
│   │   │   │   │   ├── gates/
│   │   │   │   │   │   ├── BreathingGateScreen.kt
│   │   │   │   │   │   ├── IntentionGateScreen.kt
│   │   │   │   │   │   └── DelayGateScreen.kt
│   │   │   │   │   └── dashboard/
│   │   │   │   │       ├── DashboardScreen.kt
│   │   │   │   │       ├── FocusScoreRing.kt
│   │   │   │   │       └── WeeklyChart.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── db/
│   │   │   │   │   │   ├── EverDatabase.kt        # Room database
│   │   │   │   │   │   ├── AppDao.kt
│   │   │   │   │   │   ├── ModeDao.kt
│   │   │   │   │   │   └── AnalyticsDao.kt
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── AppRepository.kt
│   │   │   │   │   │   ├── ModeRepository.kt
│   │   │   │   │   │   └── AnalyticsRepository.kt
│   │   │   │   │   └── preferences/
│   │   │   │   │       └── UserPreferences.kt      # DataStore
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── AppItem.kt
│   │   │   │   │   │   ├── FocusMode.kt
│   │   │   │   │   │   └── FocusScore.kt
│   │   │   │   │   └── usecase/
│   │   │   │   │       ├── GetCurrentModeUseCase.kt
│   │   │   │   │       ├── CalculateFocusScoreUseCase.kt
│   │   │   │   │       └── DetectInstalledAppsUseCase.kt
│   │   │   │   ├── receiver/
│   │   │   │   │   └── ModeTransitionReceiver.kt    # AlarmManager BroadcastReceiver
│   │   │   │   └── service/
│   │   │   │       └── UsageTrackingService.kt      # Foreground service for usage stats
│   │   │   └── res/
│   │   │       ├── values/
│   │   │       │   ├── strings.xml
│   │   │       │   ├── colors.xml
│   │   │       │   └── themes.xml
│   │   │       └── ...
│   │   └── test/
│   └── ...
├── gradle/
├── build.gradle.kts                 # Project-level
└── settings.gradle.kts
```

### 5.2 AndroidManifest.xml — Critical Configuration

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.everlauncher">

    <!-- Permissions -->
    <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />
    <!-- Required to list installed apps on Android 11+. 
         Google Play requires a declaration explaining why. 
         Reason: "This app is a launcher and needs to display all 
         installed apps for the user to organize." -->
    
    <uses-permission android:name="android.permission.PACKAGE_USAGE_STATS"
        tools:ignore="ProtectedPermissions" />
    <!-- Optional: for focus score screen time data. 
         Requires user to grant in system Settings. -->
    
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <!-- To re-register AlarmManager alarms after device reboot -->
    
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
    <!-- For mode transition scheduling -->
    
    <uses-permission android:name="com.android.vending.BILLING" />
    <!-- For Google Play Billing (IAP) -->

    <application
        android:name=".EverLauncherApp"
        android:label="@string/app_name"
        android:icon="@mipmap/ic_launcher"
        android:theme="@style/Theme.EverLauncher"
        android:supportsRtl="true">

        <!-- Main launcher activity -->
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:launchMode="singleTask"
            android:stateNotNeeded="true"
            android:clearTaskOnLaunch="true"
            android:screenOrientation="nosensor"
            android:windowSoftInputMode="adjustPan">
            
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.HOME" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Settings/Dashboard activity (separate from launcher) -->
        <activity
            android:name=".SettingsActivity"
            android:exported="false"
            android:parentActivityName=".MainActivity" />

        <!-- Boot receiver to re-schedule mode transitions -->
        <receiver
            android:name=".receiver.ModeTransitionReceiver"
            android:exported="false">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="com.everlauncher.MODE_TRANSITION" />
            </intent-filter>
        </receiver>
    </application>
</manifest>
```

### 5.3 Key Android Implementation Notes

**Handling the back button**: As a launcher, pressing the Home button should always return to Ever Launcher. The back button on the launcher screen should do nothing (standard launcher behavior). Override `onBackPressed()` to be a no-op in the launcher activity.

**Wallpaper**: Use `WallpaperManager` to get the user's current wallpaper and display it behind the app list with a semi-transparent overlay.

**App drawer**: Ever Launcher does NOT show a traditional app drawer. All installed apps are accessible through the system's App Library or by swiping to a secondary screen. The main screen only shows the current mode's apps. This is intentional — it creates friction for non-essential apps.

**Google Play Billing**: Use Google Play Billing Library 7.x. Implementation pattern is similar to StoreKit 2 — query products, launch purchase flow, verify purchase via `BillingClient.queryPurchasesAsync()`.

### 5.4 Gradle Dependencies

```kotlin
// app/build.gradle.kts
dependencies {
    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    
    // Google Play Billing
    implementation("com.android.billingclient:billing-ktx:7.1.1")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

---

## 6. DATA MODELS

### 6.1 AppItem

```
AppItem {
    id: UUID/String (unique identifier)
    displayName: String (user-facing name, e.g., "Messages")
    packageName: String? (Android only, e.g., "com.google.android.apps.messaging")
    urlScheme: String? (iOS only, e.g., "messages://")
    isSystemApp: Boolean
    isGated: Boolean (default false)
    gateType: GateType? (breathing | intention | delay)
    iconData: Data? (Android can extract app icons; iOS cannot)
    sortOrder: Int (position in list within a mode)
}
```

### 6.2 FocusMode

```
FocusMode {
    id: UUID/String
    name: String (e.g., "Focus", "Personal", "Wind Down")
    apps: [AppItem] (ordered list, max 8)
    schedule: Schedule {
        startTime: Time (HH:mm, 24-hour)
        endTime: Time (HH:mm, 24-hour)
        activeDays: [DayOfWeek] (mon, tue, wed, thu, fri, sat, sun)
    }
    theme: ThemePreference (light | dark | amoled | system)
    isActive: Boolean (computed from current time + schedule)
    createdAt: Date
    sortOrder: Int
}
```

### 6.3 Curated iOS URL Schemes (Top 50)

This list is used for app detection on iOS via `canOpenURL()`. Each scheme must also be listed in `LSApplicationQueriesSchemes` in Info.plist.

```
CRITICAL: These URL schemes are verified as of 2025. URL schemes can change 
between app versions. The app should handle failures gracefully — if a URL 
scheme fails to open, show a toast/alert saying "Unable to open [App]. 
The app may need to be reinstalled."

Communication:
- tel:// → Phone
- sms:// → Messages
- mailto:// → Mail
- facetime:// → FaceTime
- whatsapp:// → WhatsApp
- tg:// → Telegram
- fb-messenger:// → Messenger
- signal:// → Signal
- viber:// → Viber

Social (these are "distraction" apps — default gated):
- instagram:// → Instagram
- twitter:// → X (Twitter)
- fb:// → Facebook
- snapchat:// → Snapchat
- linkedin:// → LinkedIn
- reddit:// → Reddit
- tiktok:// → TikTok
- pinterest:// → Pinterest

Productivity:
- calshow:// → Calendar
- mobilenotes:// → Notes
- shortcuts:// → Shortcuts
- x-apple-reminder:// → Reminders
- notion:// → Notion
- trello:// → Trello
- slack:// → Slack
- googlegmail:// → Gmail
- ms-outlook:// → Outlook
- todoist:// → Todoist

Media:
- music:// → Apple Music
- spotify:// → Spotify
- youtube:// → YouTube (default gated)
- vlc:// → VLC
- pcast:// → Podcasts

Utilities:
- maps:// → Apple Maps
- googlemaps:// → Google Maps
- waze:// → Waze
- camera:// → Camera (NOTE: This may not work on all iOS versions. Fallback: use Shortcuts-based launch)
- photos-redirect:// → Photos
- x-apple-coredata-settings:// → Settings (NOTE: Limited — opens Settings root only)

Finance:
- venmo:// → Venmo
- paypal:// → PayPal
- cashapp:// → Cash App

Health & Fitness:
- x-apple-health:// → Health
- strava:// → Strava
- headspace:// → Headspace
- calm:// → Calm

Shopping:
- amazon:// → Amazon
- target:// → Target

Other:
- kindle:// → Kindle
- chatgpt:// → ChatGPT
- uber:// → Uber
- lyft:// → Lyft
```

### 6.4 DailyAnalytics

```
DailyAnalytics {
    date: Date (day only, no time)
    unlockCount: Int
    totalScreenTimeMinutes: Int
    appsLaunchedFromLauncher: Int
    gatedAppBypasses: Int
    modeOverrides: Int
    longestPhoneFreeStreakMinutes: Int
    focusScore: Int (0-100, calculated)
    focusModeCompletionPercent: Float (0-1)
}
```

### 6.5 UserPreferences

```
UserPreferences {
    hasCompletedOnboarding: Boolean (default false)
    isPro: Boolean (default false)
    globalTheme: ThemePreference (default system)
    fontChoice: FontChoice (default system)
    fontSize: FontSize (small | medium | large, default medium)
    showHiddenAppCount: Boolean (default true)
    currentModeOverride: UUID? (nil = follow schedule)
    currentModeOverrideExpiry: Date? (when manual override expires)
    currentStreak: Int (default 0)
    bestStreak: Int (default 0)
}
```

---

## 7. UI/UX DESIGN SPECIFICATION

### 7.1 Design Principles

1. **Calm over clever**: Every screen should feel like a deep breath. No bouncing animations, no attention-grabbing colors, no dopamine tricks. The app exists to reduce stimulation.

2. **Typography-first**: The UI is almost entirely text. No app icons (that's the whole point). Beautiful type is the product's identity.

3. **Instant comprehension**: Every screen should be understood in under 2 seconds. If a screen needs explanation, it's too complex.

4. **Respect the user's time**: No splash screens. No onboarding carousels beyond the 4-step setup. No "what's new" popups. No rating prompts.

### 7.2 Animation Guidelines

- **Transitions between screens**: 250ms ease-out slide or fade. No spring physics, no bounce.
- **Mode label in widget**: No animation. Static text that updates when mode changes.
- **Breathing gate circle**: Slow sinusoidal scale animation, 4-second cycle (2s expand, 2s contract). Use `opacity` and `scale` only — no blur or particle effects.
- **Focus score ring**: Draw animation on appear, 800ms ease-out. Fill from 0° to score°.
- **No micro-interactions on tap**: Tapping an app name should feel instant and invisible. No highlight animation, no ripple. The app should disappear from consciousness.

### 7.3 Spacing System

Use an 8px base grid:
- `xs`: 4px
- `sm`: 8px
- `md`: 16px
- `lg`: 24px
- `xl`: 32px
- `xxl`: 48px

### 7.4 Icon

The app icon should be:
- A simple "E" letterform in the accent color (#C8FF00) on a near-black background (#0A0A0B)
- The "E" should use a serif typeface at heavy weight
- Rounded corners per platform guidelines (iOS: superellipse, Android: squircle or adaptive icon)
- No gradients, no shadows, no additional elements

---

## 8. SCREEN-BY-SCREEN SPECIFICATION

### 8.1 Widget (iOS) / Launcher Home (Android)

**Layout**:
```
┌─────────────────────────────┐
│ FOCUS · 2h 14m remaining    │ ← Mode label (mono, 10pt, accent color)
│ ─────────────────────       │ ← Progress bar (optional, shows time remaining in mode)
│                             │
│ Calendar                    │ ← App names (serif, 18pt, primary color)
│ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─  │ ← Subtle divider (0.15 opacity)
│ Notes                       │
│ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─  │
│ Slack                       │
│ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─  │
│ Mail                        │
│ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─  │
│ Files                       │
│                             │
│                             │
│   3 apps hidden · focus     │ ← Hidden count (mono, 10pt, dim)
└─────────────────────────────┘
```

**Android-only additions**:
- Show current time at the top in large serif font (36sp)
- Show current date below time (14sp, secondary color)
- Long-press anywhere opens settings (SettingsActivity)
- Swipe up from bottom opens minimal search bar (searches only among installed apps, not web)

### 8.2 Dashboard (Companion App Home)

```
┌─────────────────────────────┐
│                             │
│  Ever Launcher      ⚙️      │ ← App name + settings icon
│                             │
│      ┌───────────┐          │
│      │    78     │          │ ← Focus score ring (large, centered)
│      │  Today's  │          │
│      │   Score   │          │
│      └───────────┘          │
│                             │
│  🔥 12 day streak           │ ← Streak counter (if > 0)
│     Best: 23 days           │
│                             │
│  ┌─┬─┬─┬─┬─┬─┬─┐          │
│  │ │█│█│ │█│█│█│          │ ← 7-day bar chart
│  │M│T│W│T│F│S│S│          │
│  └─┴─┴─┴─┴─┴─┴─┘          │
│                             │
│  Current Mode: Focus        │
│  Switch to: Personal ▸      │ ← Manual override
│                             │
│  ┌─────────────────────┐    │
│  │ Edit Modes           │    │ ← Button
│  └─────────────────────┘    │
│  ┌─────────────────────┐    │
│  │ Manage Apps          │    │ ← Button
│  └─────────────────────┘    │
│                             │
└─────────────────────────────┘
```

### 8.3 Settings Screen

```
┌─────────────────────────────┐
│ ← Settings                  │
│                             │
│ APPEARANCE                  │ ← Section header (mono, 11pt, accent)
│ Theme           System ▸    │
│ Font            Default ▸   │
│ Font Size       Medium ▸    │
│                             │
│ LAUNCHER                    │
│ Show hidden app count  [✓]  │
│ Show mode timer        [✓]  │
│ Show time on home      [ ]  │ ← Android only
│                             │
│ FOCUS                       │
│ Mindful Gates         ▸     │ ← Opens gate config per app
│ Focus Score Target    60 ▸  │
│                             │
│ PURCHASE                    │
│ ┌─────────────────────┐    │
│ │ Upgrade to Pro $7.99 │    │ ← Only if not pro
│ └─────────────────────┘    │
│ Restore Purchases           │ ← REQUIRED by Apple
│                             │
│ ABOUT                       │
│ Privacy Policy        ▸     │ ← REQUIRED — opens in-app web view or Safari
│ Terms of Use          ▸     │ ← REQUIRED for IAP apps
│ Version 1.0.0               │
│                             │
└─────────────────────────────┘
```

---

## 9. PERFORMANCE REQUIREMENTS

### 9.1 Benchmarks

| Metric | Target | How to measure |
|--------|--------|----------------|
| Cold launch to interactive (Android) | < 800ms | `adb shell am start -W` |
| Widget render time (iOS) | < 100ms | Instruments → WidgetKit timeline |
| App launch from widget tap | < 300ms | Manual stopwatch (perception test) |
| Memory usage (idle) | < 30MB | Xcode Memory Gauge / Android Profiler |
| Battery impact | < 1% per day | 24-hour battery drain test |
| App size (installed) | < 15MB | App Store Connect / Play Console |

### 9.2 Performance rules

- **No network calls** in the app (except StoreKit/Play Billing system calls). This means zero latency from network operations.
- **No image assets** beyond the app icon. The UI is entirely text and vector shapes. This keeps the binary tiny.
- **Widget timeline entries**: Generate at most 24 entries (one per hour). WidgetKit has a daily budget for timeline reloads — excessive reloads cause the widget to freeze.
- **Room/Core Data queries**: All database queries must be off the main thread. Use `Dispatchers.IO` (Kotlin) or `perform` / `@MainActor` (Swift).
- **No third-party dependencies** beyond platform SDKs, Room, and Play Billing. Every dependency is a risk for size, crashes, and supply chain attacks.

---

## 10. TESTING REQUIREMENTS

### 10.1 Unit Tests (Required before any submission)

**Models**:
- `FocusScore` calculation produces correct scores for all edge cases (0 unlocks, max unlocks, mixed bonuses/deductions)
- `ModeScheduler` correctly resolves the active mode for any given time + day combination
- `ModeScheduler` handles overnight modes correctly (e.g., Wind Down 9 PM - 7 AM)
- `ModeScheduler` handles schedule conflicts (latest-created mode wins)
- `AppItem` URL scheme validation (reject empty strings, invalid URLs)

**Repositories**:
- CRUD operations for modes and apps
- Deleting a mode doesn't delete the apps (apps can belong to multiple modes)
- Focus score history queries return correct date ranges

### 10.2 UI Tests

**Onboarding flow**:
- Complete flow from welcome to widget instruction in < 90 seconds
- Verify all 3 default modes are created with correct apps
- Verify tapping "Skip" at any step still creates a usable default config

**Settings**:
- Theme changes apply immediately (no restart needed)
- Font size changes reflect in the widget within 5 seconds (after timeline reload)
- Toggling a gate on an app persists across app restarts

**Widget (iOS manual test — automated WidgetKit testing is limited)**:
- Widget displays correct mode based on current time
- Tapping an app name opens the correct app
- Tapping a gated app shows the gate screen before opening
- Widget updates when user changes modes in the companion app

### 10.3 Edge Cases to Test

- What happens when a user's phone clock is wrong (timezone mismatch)?
  - Answer: Use system time only. Do not try to correct.
- What happens when no mode covers the current time?
  - Answer: Show the last active mode. Never show a blank widget.
- What happens when the user deletes an app that's in a mode?
  - Android: `PackageManager` intent resolution will fail. Handle gracefully — show the app name grayed out with "(not installed)" and remove from mode on next app open.
  - iOS: `canOpenURL` will return false. Same handling.
- What happens when the user has 0 apps in a mode?
  - Show an empty state: "No apps in this mode. Tap to configure." with a link to open the companion app.
- What happens during a phone restart?
  - Android: `BOOT_COMPLETED` receiver re-registers AlarmManager alarms.
  - iOS: WidgetKit timelines persist through restarts. No special handling needed.

---

## 11. STORE SUBMISSION

### 11.1 Apple App Store Requirements

**App Store Connect Configuration**:
- **Category**: Productivity
- **Age Rating**: 4+ (no objectionable content)
- **Price**: Free (with IAP)
- **In-App Purchase**: "Ever Launcher Pro" — Non-consumable — $7.99
- **Privacy Nutrition Label**:
  - Data Not Collected ✓ (all data stays on device)
  - No tracking ✓
  - No third-party analytics ✓
- **Screenshots needed**: 6.7" (iPhone 15 Pro Max), 6.1" (iPhone 15 Pro), 5.5" (iPhone 8 Plus — if supporting older), and iPad Pro 12.9" if universal
- **App Review Notes**: Include text: "This app is a minimalist launcher that uses a home screen widget to display a text-based list of apps. The widget uses URL schemes and deep links to open apps directly. No special hardware or test account is needed to review."

**Required legal pages** (host these as simple static web pages):
- Privacy Policy URL (in App Store Connect AND in-app Settings)
- Terms of Use URL (in-app Settings, required for apps with IAP)

**Screenshot content to prepare** (each screenshot should show one clear feature):
1. Widget on home screen — Focus mode, dark theme
2. Widget on home screen — Personal mode, light theme
3. Onboarding — app picker screen
4. Dashboard — focus score + streak
5. Settings — mode editor
6. Mindful gate — breathing screen

### 11.2 Google Play Store Requirements

**Play Console Configuration**:
- **Category**: Productivity → Personalization
- **Content Rating**: IARC questionnaire (will likely result in "Everyone")
- **Price**: Free (with IAP)
- **In-App Product**: "Ever Launcher Pro" — One-time — $7.99
- **Data Safety Form**:
  - Data collected: None
  - Data shared: None
  - Security practices: Data encrypted in transit (N/A — no network), Data can be deleted (yes — uninstall removes all data)
- **QUERY_ALL_PACKAGES declaration**: Required. In Play Console → Policy → Permissions declaration, explain: "This app is a home screen launcher and requires QUERY_ALL_PACKAGES to display installed apps for the user to organize their home screen."
- **Screenshots**: Phone (at least 2), 7" tablet, 10" tablet
- **Feature Graphic**: 1024x500 banner image
- **App signing**: Use Google Play App Signing (let Google manage the signing key)

### 11.3 Both Platforms — Pre-Submission Checklist

- [ ] App launches without crash on oldest supported OS version
- [ ] App launches without crash on latest OS version
- [ ] All text is localized (at minimum: English). Hardcoded strings are never acceptable.
- [ ] Privacy Policy URL loads and is accurate
- [ ] Terms of Use URL loads and is accurate
- [ ] "Restore Purchases" button works (iOS)
- [ ] IAP purchase flow works in sandbox/test environment
- [ ] No placeholder text anywhere ("Lorem ipsum", "TODO", "Test")
- [ ] App icon displays correctly at all sizes
- [ ] No crashes in Crashlytics/TestFlight over 48-hour test period
- [ ] Widget/launcher works after phone restart
- [ ] Widget/launcher works after OS update
- [ ] All screenshots are actual captures from the app (not mockups)
- [ ] App description does not mention competing apps by name
- [ ] App description does not make unverifiable claims ("#1 launcher")

---

## 12. KNOWN PITFALLS & ANTI-PATTERNS

### 12.1 iOS-Specific Pitfalls

1. **Do NOT use `Shortcuts` app as a routing layer.** This is Dumbify's fatal flaw. The visible flash through the Shortcuts app breaks the calm experience. Use `Link` with URL schemes or `AppIntent` with `openAppWhenRun = true`.

2. **Do NOT exceed 50 `LSApplicationQueriesSchemes`.** Apple silently ignores schemes beyond the limit. Prioritize the 50 most common apps.

3. **Do NOT call `WidgetCenter.shared.reloadAllTimelines()` more than a few times per hour.** WidgetKit has an internal budget. Excessive calls cause the widget to stop updating entirely.

4. **Do NOT use `UIApplication.shared` in the widget extension.** The widget extension runs in its own process without access to `UIApplication`. Use `WidgetCenter` APIs only.

5. **Do NOT forget `containerBackground` modifier on iOS 17+.** Without it, the widget renders with the default system background color, breaking the seamless aesthetic.

6. **Widget timeline entries MUST have distinct dates.** Two entries with the same date causes undefined behavior. Always use unique timestamps.

7. **App Group must be configured in BOTH targets.** A common mistake is adding it to the app but not the widget extension. Both must have the same App Group in their entitlements.

### 12.2 Android-Specific Pitfalls

1. **Do NOT use `FLAG_ACTIVITY_NEW_TASK` without `FLAG_ACTIVITY_RESET_TASK_IF_NEEDED`.** When launching apps from a launcher, you need both flags or the target app may not come to the foreground correctly.

2. **Do NOT query `PackageManager` on the main thread.** `queryIntentActivities()` can take 200ms+ on devices with many apps. Always call from a coroutine on `Dispatchers.IO`.

3. **Do NOT forget to handle the `QUERY_ALL_PACKAGES` Play Store declaration.** Apps using this permission without a valid declaration are rejected. Launcher apps are an explicitly approved use case.

4. **Do NOT use `AlarmManager.setExact()` on Android 12+ without the `SCHEDULE_EXACT_ALARM` permission.** Use `setExactAndAllowWhileIdle()` and handle the case where the user revokes the permission.

5. **Handle `onNewIntent()` in the launcher activity.** When the user presses Home while already on the home screen, `onNewIntent()` is called instead of `onCreate()`. The activity must handle this to refresh the current mode.

6. **Respect "Do Not Disturb" mode.** Never play sounds, vibrate, or show notifications from a launcher app.

### 12.3 Cross-Platform Anti-Patterns

1. **Never store timestamps in local time.** Always store UTC. Convert to local time only for display. Mode schedules use `HH:mm` in the user's local timezone — store the timezone alongside.

2. **Never use floating-point for time comparisons.** Use integer minutes-since-midnight (0-1439) for schedule comparisons.

3. **Never show more than 8 apps.** Research shows that choice overload kicks in above ~7 items. The limit is a feature, not a constraint. Enforce it in the data model.

4. **Never add "motivational quotes" or "daily tips."** This is a focus tool, not a wellness app. Every pixel of screen real estate on the launcher should serve the user's intent, not the app's engagement metrics.

5. **Never send push notifications.** A launcher app that creates notifications is antithetical to its purpose. The only notification allowed is a local notification when a mode transition occurs, and even that should be off by default.

---

## APPENDIX A: BUILD & RUN INSTRUCTIONS

### iOS

```bash
# Prerequisites
# - Xcode 15.4+ installed
# - Apple Developer account (for provisioning profiles and App Groups)

# 1. Open the project
open EverLauncher.xcodeproj

# 2. Select the EverLauncher scheme
# 3. Configure signing:
#    - Team: Your Apple Developer team
#    - Bundle ID: com.everlauncher.app
#    - Widget Bundle ID: com.everlauncher.app.widget
#    - App Group: group.com.everlauncher.shared (MUST be in both targets)

# 4. Build and run on device (widgets don't work in Simulator reliably)
# Cmd+R to run

# 5. To test the widget:
#    - Long-press home screen → + button → search "Ever Launcher"
#    - Add the large widget
```

### Android

```bash
# Prerequisites
# - Android Studio Ladybug+ installed
# - JDK 17

# 1. Open the project in Android Studio
# 2. Let Gradle sync complete

# 3. Build
./gradlew assembleDebug

# 4. Install on device
./gradlew installDebug

# 5. Set as default launcher:
#    - Settings → Apps → Default apps → Home app → Ever Launcher
```

---

## APPENDIX B: WHAT THIS SPEC INTENTIONALLY OMITS

The following features were considered and deliberately excluded from v1 to keep scope manageable. They can be added in future versions:

1. **Cloud sync of preferences** — Adds server costs, account system, privacy complexity. Not worth it for v1.
2. **Apple Watch companion** — Nice-to-have, but doubles iOS development effort.
3. **Location-based mode switching** — Requires location permission, which conflicts with the "no data collection" promise. Can be added as opt-in later.
4. **Calendar integration for auto-focus** — Requires calendar permission. Add in v2 if users request it.
5. **Family/parental controls** — Entire separate product category. Not v1.
6. **Android widgets** (Glance/AppWidgetProvider) — The launcher IS the home screen on Android, so a widget is redundant.
7. **Siri/Google Assistant integration** — Low priority for a visual launcher.

---

## APPENDIX C: DEVELOPER NOTES ON iOS "LAUNCHER" REALITY

Be transparent with yourself about what iOS allows. An iOS "launcher" is marketing language — technically you are building:

1. A **WidgetKit widget** that displays a list of text links
2. A **companion app** for configuration and focus metrics
3. A set of **deep links** that open other apps when tapped

This is exactly what every competitor does (Dumbify, Blank Spaces, Minimalist Phone). There is no secret API, no private entitlement, no workaround that gives you true launcher replacement on iOS. Apple intentionally restricts this.

The value you provide is not in the technical mechanism (which is commoditized) but in:
- **The UX**: frictionless setup, context-aware modes, beautiful typography
- **The philosophy**: focus-first design, active interventions (gates), attention metrics
- **The business model**: fair one-time pricing vs. competitors' subscriptions

Build the best version of what's possible, not a fantasy of what isn't.

---

END OF SPECIFICATION
