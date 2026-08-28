# Implementation Plan: Pure Native Android (Kotlin & Jetpack Compose) Spotify Ad Muter

Transform into a **100% Pure Native Android** application built with Kotlin 2.0, Jetpack Compose, Material 3, and targeting Android 16 (API 36) for maximum battery efficiency, zero VM overhead, and sub-millisecond ad detection.

## Architecture Highlights

```
+-------------------------------------------------------------+
|              JETPACK COMPOSE & MATERIAL 3 UI                |
|  - Dashboard: Master Glow Switch, Live Status, Audio Waves  |
|  - Real-time Stats: Total Ads Muted, Total Silence Saved    |
|  - Interactive Setup Wizard: 1-tap Permissions Direct Jumps |
|  - Activity Feed & Fine-Tuning Settings                     |
+------------------------------+------------------------------+
                               | StateFlow / Coroutines
+------------------------------v------------------------------+
|            NATIVE KOTLIN CORE (BATTERY-OPTIMIZED)           |
|  1. SpotifyNotificationListenerService                      |
|     - Fast event hook (only triggers on Spotify updates)    |
|     - Multi-heuristic ad recognition                        |
|  2. SpotifyBroadcastReceiver                                |
|     - Passive broadcast receiver                            |
|  3. AdMuterForegroundService (Target SDK 36 / SpecialUse)   |
|     - Lightweight foreground notification                   |
|  4. Volume Controller Engine (AudioManager)                 |
|     - Direct hardware audio stream mute & restore           |
|     - Transient 500ms WakeLocks during transitions          |
+-------------------------------------------------------------+
```

---

## Technical Stack & Versions

- **Target SDK**: Android 16 (API 36 - `compileSdk = 36`, `targetSdk = 36`, `minSdk = 26`)
- **Language**: Kotlin 2.0.20 + Kotlin Coroutines
- **UI Toolkit**: Jetpack Compose + Material 3 (Compose BOM `2026.08.00`, Material 3 `1.4.0`)
- **Android Gradle Plugin**: `8.5.2` (or `8.6.0`)
- **Architecture**: MVVM with `StateFlow`, `ViewModel`, and Service Event Bus
