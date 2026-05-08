# Play Console Data Safety Form — Pre-Filled Answers

> Use this document as a reference when completing the Data Safety section in Google Play Console.

---

## Overview

**Does your app collect or share any of the required user data types?**
> **No**

**Is all user data your app collects encrypted in transit?**
> **No**
*(Reason: The app does not transmit any user data. All data remains on the device.)*

**Does your app provide a way for users to request deletion of their data?**
> **Yes**
*(Users can uninstall the app to permanently delete all local data. We retain no copies.)*

---

## Data Collection & Sharing

For every data type listed in Play Console, the answer is:

| Data Type | Collected? | Shared? | Purpose |
|-----------|------------|---------|---------|
| Location | No | No | — |
| Personal info (name, email, phone) | No | No | — |
| Financial info | No | No | — |
| Health & fitness | No | No | — |
| Messages | No | No | — |
| Photos & videos | No | No | — |
| Audio files | No | No | — |
| Files & docs | No | No | — |
| Calendar | No | No | — |
| Contacts | No | No | — |
| App activity (app opens, usage time) | **No** | **No** | — |
| Web browsing | No | No | — |
| App info & performance (crashes, diagnostics) | No | No | — |
| Device or other IDs | No | No | — |

**Clarification on "App activity":**
While the app reads usage stats via `UsageStatsManager` to compute the on-device Focus Score, this data is **never collected by us** — it is processed locally and stored in the app's private database. Therefore, for Play Console purposes, the answer is **No**.

---

## Sensitive Permissions

### PACKAGE_USAGE_STATS
- **Declared in manifest?** Yes
- **Justification:** App functionality — required to calculate the user's personal Focus Score (screen time, unlock count) entirely on-device.

### SCHEDULE_EXACT_ALARM
- **Declared in manifest?** Yes
- **Justification:** App functionality — required to switch focus modes at the exact scheduled time.

### QUERY_ALL_PACKAGES
- **Declared in manifest?** No (removed)
- **Replacement:** Targeted `<queries>` intent filters for `CATEGORY_LAUNCHER` and `CATEGORY_HOME` only.

---

## Security Practices

- **Data storage:** Exclusive to device private internal storage (Room + DataStore)
- **Cloud backup:** Disabled (`android:allowBackup="false"`)
- **Network communication:** None initiated by the app
- **Third-party SDKs:** None (no analytics, ads, or crash reporting)
- **Encryption at rest:** Governed by Android device encryption (the app does not manage keys)

---

## Review Statement

"Ever Launcher does not collect, transmit, store, sell, or share any personal information. All user data, including usage statistics and focus scores, is processed and retained exclusively on the user's device. The app contains no third-party analytics, advertising, or crash-reporting SDKs."
