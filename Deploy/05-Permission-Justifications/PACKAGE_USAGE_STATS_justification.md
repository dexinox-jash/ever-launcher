# PACKAGE_USAGE_STATS — Permission Justification

## Play Console Question
> Why does your app need the `PACKAGE_USAGE_STATS` permission?

## Answer (Copy-Paste Ready)

Ever Launcher is an Android home screen replacement launcher that helps users build healthier digital habits through focus modes and on-device analytics. The `PACKAGE_USAGE_STATS` permission is required solely to calculate the user's personal Focus Score — specifically, daily screen time and unlock count.

### Core functionality that depends on this permission
1. **Focus Score Dashboard** — Displays today's screen time (in minutes) and number of device unlocks.
2. **Focus Streak Tracking** — Calculates consecutive days with a Focus Score ≥ 60 to motivate healthy usage patterns.
3. **Longest Phone-Free Streak** — Shows the longest continuous period without unlocking the device.

### Why this permission is necessary
- Android's `UsageStatsManager` is the only official API that provides aggregated, historical screen-interactive and keyguard-hidden events needed to compute these metrics accurately.
- There is no alternative permission or API that provides unlock count or precise screen-on duration.

### Data handling
- **All data is processed and stored exclusively on the user's device.**
- No usage data is transmitted to our servers, shared with third parties, or backed up to the cloud.
- The app contains no analytics SDK, crash reporting SDK, or advertising SDK.
- Users can revoke this permission at any time via Android Settings → Apps → Special App Access → Usage Access → Ever Launcher.

### User disclosure
The app prominently displays an in-app banner on the Dashboard explaining that Usage Access is needed for accurate Focus Score calculation, with a direct deep-link to system Settings.

---

## Supporting Evidence (Keep Handy)
- `AnalyticsRepository.kt` — shows on-device storage only (Room database)
- `UsageTrackingService.kt` — shows no network calls, no external transmission
- `AndroidManifest.xml` — shows the permission declaration
- `privacy_policy.md` — Section 3.1 documents this permission and its on-device-only use
