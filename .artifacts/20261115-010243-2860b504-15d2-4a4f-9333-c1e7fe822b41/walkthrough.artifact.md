# Walkthrough - Instant App Language Switching & Client Localization

## 1. ⚡ Мгновенное переключение языка приложения (RU / EN)
- **Устранена проблема задержки/кэширования Compose**:
  - В `AppLocaleManager.setLanguage(context, language)` при выборе языка сохраняются настройки, обновляется локаль и мгновенно вызывается `(context as? Activity)?.recreate()`.
  - В `MainActivity.kt` переопределён `attachBaseContext(newBase)`, прикрепляющий `ConfigurationContext` с выбранной локалью (`ru` или `en`) до старта Activity.
  - Внедрён лаконичный хелпер `AppLocaleManager.tr(ru, en)` для удобного двуязычного рендеринга интерфейса.

---

## 2. 🌐 Полный Каталог Локализации Ошибок (`CLIENT_LOCALIZATION_CATALOG.md`)
- **`ErrorLocalizationManager.kt`**:
  - **Таблица A (с `error_code`)**: Локализация всех кодов ошибок (`UNAUTHORIZED`, `SESSION_EXPIRED` с разделением auth vs search, `ACCOUNT_BANNED`, `RATE_LIMITED`, `MAINTENANCE`, `UPGRADE_REQUIRED`, `QUOTA_EXCEEDED`, `FEATURE_DISABLED`, `FREE_TIER_QUARANTINE`, `ASSISTANT_LOCKED`, `AI_SEARCH_LOCKED`, `PHOTO_RESTRICTED`, `PHOTO_QUOTA_EXCEEDED`, `UNSUPPORTED_IMAGE`, `IMAGE_TOO_LARGE`, `KEY_PLAN_INVALID`, `PLAN_DOWNGRADE_BLOCKED`, `CLOUD_QUOTA_EXCEEDED`, `SESSION_NOT_FOUND`, `PAYLOAD_TOO_LARGE`).
  - **Таблица B (без `error_code`)**: Маппинг фолбэк-ошибок по HTTP-кодам и тексту.
  - Автоматическая выдача русского или английского текста в зависимости от текущего языка интерфейса (`isEnglish`).

---

## 3. 🖼️ Исправление отображения квот и автообновления по фото
- **Верстка квот в `BackupScreen.kt`**:
  - Все лимиты разнесены на четкую двухстрочную верстку: заголовок и счетчик на основной строке, подстрока остатка снизу. Устранены наложения и некрасивый перенос текста `(осталось 60)`.
- **Автообновление фото в `BackendRepository.kt`**:
  - После оцифровки по фото вызов `getUserStatus(forceRefresh = true)` мгновенно обновляет остаток фото в DataStore и UI.

---

## 4. 🛑 Снятие предложения «Продолжить готовку» при перезапуске
- В `ActiveCookingManager.kt` при перезапуске приложения незаконченные сессии без идущих таймеров автоматически снимаются при холодном старте.

---

## Verification Summary

1. **Компиляция Kotlin**:
   ```bash
   ./gradlew.bat :app:compileDebugKotlin --console=plain
   ```
   **Результат**: `BUILD SUCCESSFUL`.

2. **Юнит-тесты**:
   ```bash
   ./gradlew.bat :app:testDebugUnitTest --console=plain
   ```
   **Результат**: Все **60 юнит-тестов** успешно пройдены (`60 passed, 0 skipped, 0 failed`).
