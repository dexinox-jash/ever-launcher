# Ever Launcher — Build Instructions

## Requirements

- Android Studio Ladybug (2024.2.1) or newer
- JDK 17 (bundled with Android Studio is fine)
- Android SDK 35 installed
- Windows, macOS, or Linux

## Quick Start

```bash
# 1. Open the project in Android Studio
#    File > Open > select the ever-launcher-android/ folder

# 2. Sync Gradle
#    Android Studio will prompt you; click "Sync Now"

# 3. Build debug APK
./gradlew assembleDebug

# 4. Build release APK + AAB (requires signing config)
./gradlew assembleRelease bundleRelease

# 5. Run tests
./gradlew test

# 6. Run lint
./gradlew lint
```

## Project Structure

```
ever-launcher-android/
├── app/
│   ├── build.gradle.kts          # App-level build config
│   ├── proguard-rules.pro        # R8 keep rules
│   ├── signing.properties        # Release signing credentials (DO NOT COMMIT)
│   ├── release.keystore          # Release keystore (DO NOT COMMIT)
│   └── src/main/java/...         # Kotlin source
├── gradle/
│   └── libs.versions.toml        # Dependency catalog
├── build.gradle.kts              # Root build config
└── settings.gradle.kts           # Project settings
```

## Signing Configuration

The release build reads signing credentials from `app/signing.properties`:

```properties
storeFile=release.keystore
storePassword=YOUR_STORE_PASSWORD
keyAlias=everlauncher
keyPassword=YOUR_KEY_PASSWORD
```

If `signing.properties` is missing, the build falls back to environment variables:
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`
- `RELEASE_STORE_PASSWORD`

### Generating a new keystore

```bash
keytool -genkey -v \
  -keystore release.keystore \
  -alias everlauncher \
  -keyalg RSA -keysize 2048 -validity 10000
```

**Important:**
- Back up your keystore and passwords in a password manager.
- Losing the keystore means you can never update the app on Google Play.
- Never commit the keystore or `signing.properties` to git.

## Verification Checklist

Before any release, run:

```bash
./gradlew clean
./gradlew test
./gradlew lint
./gradlew assembleRelease bundleRelease
```

All must pass with:
- `BUILD SUCCESSFUL`
- Lint: 0 errors
- Tests: all passing

## Troubleshooting

| Issue | Solution |
|-------|----------|
| `Unresolved reference: ResolveInfoFlags` | Update Android SDK to API 33+ |
| Lint error on `registerReceiver` | Already suppressed; ensure you are on the latest branch |
| `release.keystore` not found | Copy it into `app/` or update `storeFile` path in `signing.properties` |
| KSP compilation errors | File > Invalidate Caches > Invalidate and Restart |
| Tests fail on CI | Ensure `signing.properties` exists or env vars are set |
