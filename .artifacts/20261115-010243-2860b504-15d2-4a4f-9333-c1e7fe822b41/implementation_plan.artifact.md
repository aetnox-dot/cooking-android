# Implementation Plan - Fix App Language Switching in Jetpack Compose

## Problem Statement
Language switching was saving preference but was not immediately recreating the Activity context or updating Compose UI strings across screens.

## Proposed Changes

### 1. Locale Context & Activity Recreation Lifecycle

#### [AppLocaleManager.kt](file:///D:/Android_Studio_project/cooking/app/src/main/java/com/aetnox/cooking/ui/util/AppLocaleManager.kt)
- Add `tr(ru: String, en: String)` helper method for bilingual UI strings.
- In `setLanguage(context, language)`, save language to SharedPreferences, update `currentLanguage.value`, apply configuration context, and call `(context as? Activity)?.recreate()` to trigger immediate Activity recreation with the new locale.

#### [MainActivity.kt](file:///D:/Android_Studio_project/cooking/app/src/main/java/com/aetnox/cooking/MainActivity.kt)
- Override `attachBaseContext(newBase: Context)` to automatically attach a configuration context matching the saved `app_language` preference ("ru" or "en") before Activity inflation.

---

### 2. UI String Localization Integration

#### [BackupScreen.kt](file:///D:/Android_Studio_project/cooking/app/src/main/java/com/aetnox/cooking/ui/screen/BackupScreen.kt)
- Use `AppLocaleManager.tr(...)` for top app bar titles, tab names ("👤 Account" / "👤 Аккаунт", "🎨 Appearance" / "🎨 Оформление", etc.), quota section headers, and action button labels.

#### [RecipeListScreen.kt](file:///D:/Android_Studio_project/cooking/app/src/main/java/com/aetnox/cooking/ui/screen/RecipeListScreen.kt)
- Use `AppLocaleManager.tr(...)` for tab headers ("📖 My Cookbook" / "📖 Моя книга", "🌐 Cloud Catalog" / "🌐 Облачный каталог"), search placeholders, and filter titles.

---

## Verification Plan

### Automated Tests
- Run Gradle unit tests to ensure all tests pass:
  ```bash
  ./gradlew.bat :app:testDebugUnitTest --console=plain
  ```
- Compile Kotlin:
  ```bash
  ./gradlew.bat :app:compileDebugKotlin --console=plain
  ```

### Manual Verification
- Test clicking "🇬🇧 English" in Settings: Activity recreates instantly and all UI text + error messages switch to English.
- Test clicking "🇷🇺 Русский" in Settings: Activity recreates instantly and all UI text + error messages switch back to Russian.
