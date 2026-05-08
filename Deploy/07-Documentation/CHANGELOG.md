# Ever Launcher Changelog

## 1.0.0 — Initial Release

**Release Date:** April 15, 2026

### What's New
- **Minimalist Android Launcher** — replaces your home screen with a calm, time-aware app list
- **Focus Modes** — three default modes (Focus, Personal, Wind Down) that activate automatically based on time of day
- **Mindful Gates** — add intentional friction (breathing pause, intention prompt, or delay) before opening distracting apps
- **On-Device Focus Score** — track daily screen time, unlock count, and focus streaks; all data stays local
- **Custom Themes** — system, light, dark, and AMOLED modes with customizable fonts
- **Fully Offline** — no analytics, no ads, no backend, no data collection

### Permissions Used
- `PACKAGE_USAGE_STATS` — for calculating your personal focus score (on-device only)
- `SCHEDULE_EXACT_ALARM` — for switching focus modes on time
- `RECEIVE_BOOT_COMPLETED` — for rescheduling mode alarms after reboot

### Technical Highlights
- Built with Jetpack Compose + Room + Kotlin Coroutines
- Target SDK 35, min SDK 26 (Android 8.0+)
- ProGuard/R8 optimized release build
- Comprehensive unit test coverage for core algorithms
- Full accessibility support with visible navigation controls

### Known Limitations
- Compose UI integration tests are not included in this release (all core logic is unit-tested)
- Tablet layout is functional but not specifically optimized for large screens

---

## Future Roadmap (Not Committed)

- Tablet-optimized layout
- Widget support
- Additional gate types
- Export/import of focus data
