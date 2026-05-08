# `<queries>` Intent Block — Justification

## Play Console Context
Google Play restricts the use of `QUERY_ALL_PACKAGES` to apps that can demonstrate a core functional need to see all installed apps. Ever Launcher previously declared this permission but has removed it in favor of targeted `<queries>` intent filters.

## Answer (Copy-Paste Ready)

Ever Launcher is an Android launcher replacement. Its primary function is to display installed apps that the user can launch from the home screen. We have removed `QUERY_ALL_PACKAGES` and now use targeted `<queries>` intent filters to query only apps that declare `Intent.ACTION_MAIN` + `Intent.CATEGORY_LAUNCHER`.

### Why app querying is necessary
- A launcher app must know which apps are available to launch. Without this visibility, the app list would be empty or incomplete.
- We do not collect, transmit, or log the list of installed apps. The query results are used only to populate the launcher UI in real time.

### Scope limitation
Our `<queries>` block is strictly limited to:
```xml
<queries>
    <intent>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent>
    <intent>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.HOME" />
    </intent>
</queries>
```

This means Ever Launcher can only see:
1. Apps that are launchable from a home screen (`CATEGORY_LAUNCHER`)
2. Other launcher apps (`CATEGORY_HOME`) — needed only for the "Set as default" onboarding check

### Data handling
- No list of installed apps is transmitted off-device.
- No analytics or logging of package names occurs.
- The query is performed locally by `PackageManager.queryIntentActivities()`.

---

## Supporting Evidence
- `AndroidManifest.xml` — shows `<queries>` block and absence of `QUERY_ALL_PACKAGES`
- `DetectInstalledAppsUseCase.kt` — shows filtered query for `CATEGORY_LAUNCHER` only
- `privacy_policy.md` — Section 3.2 documents this limited query behavior
