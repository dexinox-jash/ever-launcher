# Tablet Screenshots — Capture Guide

## Requirements
- **7-inch tablets:** Minimum 1 screenshot
- **10-inch tablets:** Minimum 1 screenshot
- **Resolution:** Any standard tablet resolution (e.g., 1600×2560, 1200×1920)
- **Format:** PNG or JPG
- **Max file size:** 8 MB each

## Recommended Screenshots

### 7-inch Tablet
Capture the launcher home screen in landscape or portrait. The app will fill the screen but may have extra whitespace on the sides — that's acceptable.

### 10-inch Tablet
Same as above. If possible, capture the Settings screen or Dashboard to show that the UI scales correctly.

## How to Capture

### Android Emulator
1. Create a tablet AVD:
   - **7-inch:** Nexus 7 (1200×1920)
   - **10-inch:** Pixel Tablet (1600×2560)
2. Install the release APK:
   ```bash
   adb install Deploy/01-Build-Artifacts/app-release.apk
   ```
3. Set Ever Launcher as default home
4. Capture screenshots using `adb shell screencap` or the emulator controls

## Note on Tablet Optimization
The current release is functional on tablets but not specifically optimized for large screens. The screenshots should reflect the actual user experience — do not misrepresent the layout.

## Upload Location
In Play Console, upload under **Main store listing > Tablet (7-inch) screenshots** and **Tablet (10-inch) screenshots**.
