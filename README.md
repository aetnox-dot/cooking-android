# 🍳 Cooking — Smart Culinary Assistant & Recipe Book

[![Latest Release](https://img.shields.io/badge/Release-v1.0.0-success.svg?style=flat&logo=github)](https://github.com/aetnox-dot/cooking-android/releases)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2026.02.01-4285F4.svg?style=flat&logo=android)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material%203-Latest-006C4C.svg?style=flat&logo=materialdesign)](https://m3.material.io)
[![Nothing OS Design](https://img.shields.io/badge/Design-Nothing%20OS%20Style-D71921.svg?style=flat)](https://nothing.tech)

**Cooking** is a modern Android application written in **Kotlin** and **Jetpack Compose** for organizing, creating, searching, and cooking recipes. It features an interactive AI sous-chef, automatic device manufacturer detection (Nothing Phone / Nothing OS styling), live dynamic serving recalculations, step-by-step cooking timers, and JSON backups.

---

## 📥 Download

Get the latest APK directly from the [GitHub Releases Page](https://github.com/aetnox-dot/cooking-android/releases).

---

## 🌟 Key Features

### 🤖 Interactive AI Sous-Chef & Smart Search
- **AI Cooking Assistant**: Real-time culinary Q&A during cook mode, ingredient substitution suggestions, baking timer recommendations, and live recipe patch application.
- **Two-Phase Cloud Search**: AI-powered cloud recipe search with quota management and local catalog filtering.
- **Smart AI Editor**: Parse raw recipe text into structured sections and steps automatically using AI.

### ⏱️ Chef Mode & Cooking Timers
- **Dynamic Servings Scaler**: Automatically recalculates all ingredient weights and units as servings change.
- **Interactive Timers**: Built-in notifications, sound alerts, and background timer tracking.
- **ExoPlayer Video Viewer**: Seamless step-by-step video instructions using Media3 ExoPlayer.

### 🎨 Personalization & Nothing OS Design
- **Nothing OS Style Integration**: Pitch-black dark mode (`#000000` / `#141414`), signature Nothing Red `#D71921` accent, NDot typography, and springy Haptic Feedback.
- **Nothing Device Auto-Detection**: Devices running Nothing OS automatically launch in Nothing style by default.
- **10 Dynamic App Themes**: System (Material You), Nothing (Dot Style), Herbal Kitchen, Light Bakery, Morning Garden, Deep Ocean, Night Chef, Chocolate & Caramel, Neon Kitchen, and Custom Accent Theme.
- **4 Custom App Icons**: Full Color Brand, Neon, Night Chef, and Nothing Dot Style.

### 💾 Data Safety & Export
- **One-Click JSON Backup**: Transactional import and export of all recipe data.
- **Markdown Export**: Instant copy of any recipe to clipboard formatted in clean Markdown.
- **Bilingual Interface**: Full support for English (🇬🇧) and Russian (🇷🇺) with proper pluralization rules.

---

## 🛠️ Tech Stack & Architecture

| Layer | Technology / Library |
| :--- | :--- |
| **Language** | Kotlin 2.2.10 |
| **UI Framework** | Jetpack Compose (BOM 2026.02.01), Material 3, Navigation Compose |
| **Architecture** | Clean Architecture + MVVM + StateFlow / Coroutines |
| **Database** | Room 2.8.5 (KSP, Flow, Coroutines) |
| **Networking & Serialization** | Retrofit 2.11.0, OkHttp 4.12.0, Kotlinx Serialization, DNS-over-HTTPS (DoH) |
| **Media & Images** | Coil Compose 2.7.0, Media3 ExoPlayer 1.5.1 |
| **Preferences** | Jetpack DataStore Preferences, SharedPreferences |

---

## 📱 Build & Setup

### Requirements
- **Android Studio**: 2026.1+ (Ladybug / Jellyfish or newer)
- **JDK**: Java 17+ (JBR)
- **Min SDK**: API 30 (Android 11)
- **Target SDK**: API 36 (Android 15)

### Command Line Build (Git Bash / PowerShell)

```bash
# Set JAVA_HOME (if needed)
export JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"

# Kotlin Compile Check
./gradlew.bat :app:compileDebugKotlin --console=plain

# Unit Tests Execution
./gradlew.bat :app:testDebugUnitTest --console=plain

# Assemble Debug APK
./gradlew.bat :app:assembleDebug --console=plain
```

---
