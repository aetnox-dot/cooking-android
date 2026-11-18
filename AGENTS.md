# Cooking — Память проекта

Кулинарное приложение: Kotlin, Jetpack Compose, Room (KSP), Navigation, Coil, Media3, OkHttp, Retrofit.
Пакет: `com.aetnox.cooking` (`data` / `domain` / `ui`). Пользователь: Aetnox (ru).

> ⚠️ **КРИТИЧЕСКОЕ ПРАВИЛО ДЛЯ ИИ**: ОЧЕНЬ ВНИМАТЕЛЬНО ВЧИТЫВАТЬСЯ В ЗАПРОС ПОЛЬЗОВАТЕЛЯ И ВЫПОЛНЯТЬ ВСЕ ПУНКТЫ И ТОНКОСТИ БЕЗ ИСКЛЮЧЕНИЯ! Тщательно сверять изменения по чек-листу требований, не пропуская нюансы и краевые случаи.

## Лечение серой кнопки Run в IDE

- **File → Sync Project with Gradle Files** (решает 90% случаев после консольных сборок или редактирования gradle файлов).
- **File → Invalidate Caches → Invalidate and Restart** (если синхронизация не помогла).
- **Ошибка `Activity class {...MainActivityAliasDefault} does not exist` при смене иконки**: В выпадающем меню рядом с кнопкой Run выберите **Edit Configurations...** → смените **Launch** с `Default Activity` на `Specified Activity` (`com.aetnox.cooking.MainActivity`).

## Консольная сборка (Git Bash)

```bash
export JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
./gradlew.bat :app:compileDebugKotlin --console=plain   # быстрая проверка кода
./gradlew.bat :app:testDebugUnitTest --console=plain   # юнит-тесты
./gradlew.bat :app:assembleDebug --console=plain       # debug APK
./gradlew.bat :app:assembleRelease --console=plain     # release APK (R8, unsigned)
```

## Ключевые договорённости и архитектура

- **Окончания строк**: CRLF. Сверять текст при замене посимвольно.
- **Безопасность и БД**: Снапшот БД перед деструктивной миграцией, транзакционный импорт бэкапов, `allowBackup="false"`, R8 keep-правила в `rules.keep`.
- **Сеть и бэкенд API (`v1`, Worker `a6f13107+`)**:
  - Точка сетевых вызовов: `BackendRepository` / `BackendClient`. Справочник: [`NETWORK_DOCUMENTATION.md`](NETWORK_DOCUMENTATION.md).
  - **Взвешенные квоты (`ai_costs`)**: Динамические расценки, списание `cost` в ответах ИИ.
  - **Бэкапы (`POST/GET /recipes/backup`)**: Строгая валидация DTO, проверка лимита 500 рецептов, восстановление 1:1.
  - **Двухфазный ИИ-поиск**: `plan` (`cloud_ids`) → локальный фильтр (до 30 matches) → `finish`. Авторетрай при `SEARCH_SESSION_EXPIRED` (410) / `SEARCH_SESSION_NOT_FOUND` (404); старые `SESSION_EXPIRED`/`SESSION_NOT_FOUND` — фолбэк. В фолбэке передаются `status` и `cloudId` (кап 240).
  - **Су-шеф «Разбор полётов»**: `allow_recipe_updates = true`, diff-карточка `ProposedUpdateCard`, применение патча к Room и best-effort синк в облако. Поддержка `summary` при пустом `content`.
  - **Облачные рецепты**: Санитайзер вырезает личные поля на сервере. При скачивании клиент задаёт дефолты (`status="PLANNED"`, `isFavorite=false`, `rating=0`, `postCookNote=""`).
- **Эксперименты**: DoH (Cloudflare/Google/Quad9/свой URL), ИИ-ассистент при готовке (`CookAssistantTools`), thinking (OFF / 512 / 2048 токенов).
- **Темы**: 9 тем в `ui/theme/` (включая `AppTheme.NOTHING`).

## Дизайн-система Nothing OS (Стиль Nothing)

- **Палитра (Dark Mode):** Фон `#000000`, поверхности карточек `#141414` / `#1B1B1D`, рамки `rgba(255,255,255,0.12)` (`1dp`), акцентный `Nothing Red` `#D71921` (использовать ТОЛЬКО точечно <= 5-10%).
- **Палитра (Light Mode):** Фон `#F2F2F2`, карточки `#FFFFFF`, рамки `rgba(0,0,0,0.1)`.
- **Типографика:** `NDot` шрифт — ТОЛЬКО крупные заголовки (`24-32sp`), цифры/таймеры, системные бейджи (`10-12sp`), всегда в **ALL CAPS** с трекингом `0.06em-0.1em`. `NType82` (Monospace / Grotesque) — для основного текста и табличных моноширинных цифр.
- **Геометрия:** Карточки `24dp`-`28dp`, вложенные блоки `16dp`, кнопки/переключатели — строго капсульные (`CircleShape`). Тени полностью **ЗАПРЕЩЕНЫ** (`elevation = 0dp`).
- **Иконки & Интерактивность:** Линейные монохромные иконки (`1.5-2dp`). Механические, пружинистые нажатия (`scale: 0.97`, `120-150ms`) с `Haptic Feedback`.
