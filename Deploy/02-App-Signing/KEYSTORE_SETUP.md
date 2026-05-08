# Release Keystore Setup

## What You Have

A release keystore has been generated for you:

- **File:** `release.keystore`
- **Alias:** `everlauncher`
- **Algorithm:** RSA, 2048-bit
- **Validity:** 10,000 days
- **Default password:** `everlauncher` (for both store and key)

## ⚠️ CRITICAL SECURITY WARNING

**You MUST change the default password before publishing.**

If someone obtains this keystore file and knows the password, they can sign malicious updates to your app that Google Play will accept as legitimate.

## How to Change the Password

### Option 1: Change password on existing keystore
```bash
keytool -storepasswd -keystore release.keystore
keytool -keypasswd -alias everlauncher -keystore release.keystore
```

### Option 2: Generate a brand new keystore (Recommended)
```bash
keytool -genkey -v \
  -keystore release.keystore \
  -alias everlauncher \
  -keyalg RSA -keysize 2048 -validity 10000
```

Then:
1. Replace `Deploy/02-App-Signing/release.keystore` with your new keystore
2. Replace `ever-launcher-android/app/release.keystore` with the same file
3. Update `ever-launcher-android/app/signing.properties` with your new password

## Update signing.properties

```properties
storeFile=release.keystore
storePassword=YOUR_NEW_STRONG_PASSWORD
keyAlias=everlauncher
keyPassword=YOUR_NEW_STRONG_PASSWORD
```

## Backup Requirements

- Store the keystore file in a password manager (1Password, Bitwarden, etc.) or encrypted USB drive
- Store the password separately from the keystore file
- Give a backup copy to a trusted partner or lawyer in case of emergency
- **Never** commit the keystore or `signing.properties` to git

## Why This Matters

Google Play requires that all app updates be signed with the same key. If you lose the keystore or forget the password, you will **never** be able to update Ever Launcher on Google Play again. You would have to publish a new app with a new package name and lose all your users, ratings, and reviews.

## Verification

After changing the password, verify the build still signs correctly:

```bash
cd ever-launcher-android
./gradlew assembleRelease
```

You should see:
```
> Task :app:validateSigningRelease UP-TO-DATE
> Task :app:signReleaseBundle
```

And the APK/AAB should be in `app/build/outputs/`.
