# Phone Screenshots — Capture Guide

## Requirements
- **Minimum:** 2 screenshots
- **Recommended:** 6–8 screenshots
- **Resolution:** 1080×1920 or 1080×2400 (Portrait)
- **Format:** PNG or JPG
- **Max file size:** 8 MB each

## Recommended Screenshot Sequence

### 1. Home Screen — Focus Mode
Show the main launcher with a few apps, time, and mode indicator.
*Caption idea: "Only the apps you need, right now."*

### 2. Wind Down Mode
Show the same screen but in Wind Down mode with fewer/different apps.
*Caption idea: "Automatic modes for every part of your day."*

### 3. Mindful Gate — Breathing
Show the breathing gate screen with the pulsing circle.
*Caption idea: "Take a breath before opening distracting apps."*

### 4. Mindful Gate — Intention
Show the intention prompt asking "What are you opening [App] for?"
*Caption idea: "Set your intention. Stay mindful."*

### 5. Focus Score Dashboard
Show the dashboard with the focus score ring, streak, and 7-day chart.
*Caption idea: "Track your focus. All data stays on your device."*

### 6. Settings / Dark Mode
Show the settings screen or the launcher in dark/AMOLED mode.
*Caption idea: "Clean themes that match your style."*

## How to Capture

### Option A: Android Emulator (Recommended)
1. Open Android Studio
2. Start a Pixel 7 or Pixel 8 emulator (1080×2400)
3. Set Ever Launcher as the default home app
4. Navigate to each screen
5. Use the emulator's screenshot button (camera icon) or press `Ctrl + S`
6. Screenshots save to `C:\Users\[You]\.android\avd\[Device]\...` or use the emulator extended controls to save to desktop

### Option B: Physical Device
1. Use a Pixel or Samsung phone with a 1080p or higher display
2. Enable Developer Options → Demo Mode (for clean status bar)
3. Use `adb shell screencap` for lossless PNGs:
   ```bash
   adb shell screencap -p /sdcard/screen1.png
   adb pull /sdcard/screen1.png .
   ```

## Tips for Great Screenshots
- **Clean status bar:** Use Android Demo Mode or a third-party app to show a static 9:41 time, full Wi-Fi, and full battery
- **Consistent content:** Use the same set of apps across screenshots for cohesion
- **No notifications:** Clear all notifications before capturing
- **Good contrast:** Ensure text is readable at thumbnail size
- **No personal info:** Use a test device or wipe personal messages/photos

## Upload Location
In Play Console, upload these under **Main store listing > Phone screenshots**.
