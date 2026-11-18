# Task Management

- [x] Analyze project for code quality, warnings, edge cases, and small bugs
- [x] Fix identified errors and code quality issues
- [x] Verify changes with compilation and unit tests
- [x] Investigate and fix Recipe List Screen lagging issue
- [x] Add App Version Display to Settings / Experiments
- [x] Active Cooking Session Reset on App Restart & Vitamin AI Renaming
- [x] Fix Photo Quota / Photo Scanning Layout & Functionality Issue
- [x] Add App Language Switching (RU / EN) & Localization Catalog
- [x] Fix Instant App Language Switching Recomposition & attachBaseContext
    - [x] Implement `attachBaseContext` in `MainActivity.kt` with custom locale Configuration
    - [x] Implement `Activity.recreate()` in `AppLocaleManager.setLanguage`
    - [x] Add `AppLocaleManager.tr(ru, en)` bilingual string resolver
    - [x] Integrate string translation across BackupScreen tabs and headers
    - [x] Verify build and unit tests (60/60 passing)
- [x] Finalize walkthrough and notify user
