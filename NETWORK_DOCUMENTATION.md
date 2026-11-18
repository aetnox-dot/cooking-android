# 📡 Сетевая Архитектура и Документация API — Cooking App

Этот файл содержит исчерпывающее руководство по всей сетевой инфраструктуре приложения **Cooking**, включая бэкенд API (Контракт v2), интеграцию с Google Gemini AI, клиент OkHttp/Retrofit, DNS-over-HTTPS (DoH), систему авторизации/сессий, отслеживание квот и офлайн-режим.

---

## 📂 Обзор сетевых модулей и файлов

Все сетевые вызовы и обработка данных сгруппированы в двух пакетах:
- `com.aetnox.cooking.data.remote.*` — клиентская логика бэкенда, сессии, Retrofit интерфейс, DTO модели, сетевой наблюдатель.
- `com.aetnox.cooking.domain.net.*` / `com.aetnox.cooking.domain.ai.*` — прямая интеграция с Gemini AI API, DoH шифрование DNS, ИИ-инструменты ассистента.

### Карта файлов

| Файл / Класс | Назначение и функции |
| :--- | :--- |
| [`CookingApiService.kt`](app/src/main/java/com/aetnox/cooking/data/remote/CookingApiService.kt) | **Retrofit интерфейс** со всеми эндпоинтами бэкенда Контракта v2 (авторизация, квоты, оцифровка, двухфазный ИИ-поиск, облачный бэкап, перенос подписок). |
| [`BackendClient.kt`](app/src/main/java/com/aetnox/cooking/data/remote/BackendClient.kt) | **Фабрика Retrofit и OkHttp**: добавляет заголовки авторизации (`Authorization: Bearer`), версии (`X-App-Version`), фоновую переавторизацию `TokenAuthenticator` при 401 и DNS-настройки. |
| [`BackendRepository.kt`](app/src/main/java/com/aetnox/cooking/data/remote/BackendRepository.kt) | **Репозиторий высшего уровня**: выполняет сетевые запросы, оборачивает в `Result<T>`, обрабатывает машинные коды ошибок API (`error_code`), обновляет локальные остатки квот. |
| [`ApiModels.kt`](app/src/main/java/com/aetnox/cooking/data/remote/model/ApiModels.kt) | **DTO Модели данных**: сессии `AuthInitResponse`, конфигурация `AppConfigResponse`, статус и взвешенные квоты `UserStatusResponse`, бэкапы `BackupRecipeDto`, двухфазный ИИ-поиск, патчи ассистента. |
| [`GeminiApiService.kt`](app/src/main/java/com/aetnox/cooking/domain/ai/GeminiApiService.kt) | **Прямой клиент Gemini AI API**: прямое взаимодействие с `v1beta/models/gemini-2.5-flash`, даунскейл снимков до 1600px, fallback при недоступности бэкенда. |
| [`CookAssistantTools.kt`](app/src/main/java/com/aetnox/cooking/domain/ai/CookAssistantTools.kt) | **Агентный цикл ИИ-ассистента**: вызов локальных инструментов во время готовки (`get_step`, `get_ingredients`, `go_to_step`, `suggest_timer` и др.). |
| [`ExperimentalNetwork.kt`](app/src/main/java/com/aetnox/cooking/domain/net/ExperimentalNetwork.kt) | **DNS-over-HTTPS (DoH) & Настройки**: безопасный DNS (Cloudflare 1.1.1.1, Google 8.8.8.8, Quad9), утилиты проверки DoH (`DnsTester`). |
| [`NetworkObserver.kt`](app/src/main/java/com/aetnox/cooking/data/remote/NetworkObserver.kt) | **Мониторинг сети**: реализация `Flow<Boolean>` и `isOnline(context)` через `ConnectivityManager` для офлайн-режима. |
| [`AppStartupManager.kt`](app/src/main/java/com/aetnox/cooking/data/remote/AppStartupManager.kt) | **Проверка запуска**: проверкa минимальной версии приложения (`min_version_code`), режим технических работ (`MAINTENANCE`) и блокировка сетевых ИИ-запросов. |
| [`SessionManager.kt`](app/src/main/java/com/aetnox/cooking/data/remote/SessionManager.kt) | **Управление сессией**: хранение `session_token`, `accountId`, `deviceId`, уровня тарифа, месячных/суточных лимитов и кэша расценок `ai_costs`. |
| [`DeviceUtils.kt`](app/src/main/java/com/aetnox/cooking/data/remote/DeviceUtils.kt) | **Идентификация устройства**: генерация уникального хэша `ANDROID_ID` с солью (`getHardwareDeviceId`) и сведений о железе. |

---

## 🌐 1. Эндпоинты Бэкенда (`CookingApiService.kt`)

Базовый URL: `https://cooking.aetnox.workers.dev/api/v1/`.
Все авторизованные запросы автоматически снабжаются заголовком `Authorization: Bearer <session_token>` и `X-App-Version: <versionCode>`.

### Конфигурация, Авторизация и Подписка

1. `GET /api/v1/app/config`
   - **Метод**: `getAppConfig()`
   - **Доступ**: Без авторизации
   - **Описание**: Получение глобальной конфигурации при старте: минимальная версия приложения (`min_version_code`), флаг техработ (`maintenance_active`), расценки действий `ai_costs` и переключатели функций `features`.

2. `POST /api/v1/auth/init`
   - **Метод**: `initAuth(@Body request: AuthInitRequest)`
   - **Доступ**: Без авторизации
   - **Описание**: Инициализация/регистрация сессии по уникальному хэшу устройства (`device_id`). Возвращает `session_token`, `account_id`, уровень тарифа и срок подписки.

3. `POST /api/v1/auth/logout`
   - **Метод**: `logout()`
   - **Доступ**: Авторизованный
   - **Описание**: Инвалидация текущего токена сессии.

4. `POST /api/v1/auth/transfer/create`
   - **Метод**: `createTransferCode()`
   - **Доступ**: Авторизованный
   - **Описание**: Генерация 6-значного одноразового кода для переноса подписки на новое устройство (действителен 10 минут, кулдаун 7 дней, макс. 2 переноса).

5. `POST /api/v1/auth/transfer/claim`
   - **Метод**: `claimTransferCode(@Body body: TransferClaimRequest)`
   - **Доступ**: Авторизованный
   - **Описание**: Активация переноса подписки по 6-значному коду на устройстве-приёмнике. Устройство-донор отправляется в карантин на 30 дней.

6. `GET /api/v1/user/status`
   - **Метод**: `getUserStatus()`
   - **Доступ**: Авторизованный
   - **Описание**: Запрос текущего статуса пользователя: оставшиеся месячные квоты (ИИ-запросы, фото, облачные рецепты), расценки `ai_costs`, статус карантина и срок подписки.

7. `POST /api/v1/user/activate-key`
   - **Метод**: `activateKey(@Body body: ActivateKeyRequest)`
   - **Доступ**: Авторизованный
   - **Описание**: Активация ключа подписки (напр. `RECIPE-XXXX-XXXX`). Нормализует регистр и разделители.

### ИИ-Функции: Оцифровка, Ассистент и Поиск

8. `POST /api/v1/recipe/parse`
   - **Метод**: `parseRecipe(@Body request: RecipeParseRequest)`
   - **Доступ**: Авторизованный
   - **Описание**: Структурирование рецепта из текста (до 4000 символов) или фотографии в Base64. Списывает `parse` (1) или `parse_photo` (2) ИИ-запроса и фото-квоту.

9. `POST /api/v1/assistant/step`
   - **Метод**: `assistantStep(@Body request: AssistantStepRequest)`
   - **Доступ**: Авторизованный
   - **Описание**: Итерация кухонного ассистента в режиме готовки с поддержкой «разбора полётов» (`allow_recipe_updates = true`). Возвращает ответ или предложенный патч `proposed_update`.

10. `POST /api/v1/recipes/ai-search`
    - **Метод**: `aiSearch(@Body request: AiSearchRequest)`
    - **Доступ**: Авторизованный
    - **Описание**: Одиночный ИИ-поиск по диалогу. Для библиотеки передаётся `local_recipes` (до 240 элементов, со `status` и `cloudId`).

11. `POST /api/v1/recipes/ai-search/plan`
    - **Метод**: `aiSearchPlan(@Body request: AiSearchPlanRequest)`
    - **Доступ**: Авторизованный
    - **Описание**: Шаг 1 двухфазного ИИ-поиска v2. Анализирует диалог и список скачанных `cloud_ids`, возвращает план поиска (`keywords`, `ingredients`, `min_minutes`, `max_minutes`) и одноразовый `search_token` (живёт 10 мин). Квоту не списывает (`search_plan = 0`).

12. `POST /api/v1/recipes/ai-search/finish`
    - **Метод**: `aiSearchFinish(@Body request: AiSearchFinishRequest)`
    - **Доступ**: Авторизованный
    - **Описание**: Шаг 3 двухфазного ИИ-поиска v2. Принимает отобранные клиентом локальные рецепты `local_matches` (до 30 штук), возвращает итоговый ответ и список результатов. Списывает `search_finish` (1 ИИ-запрос). Авторетрай при `SEARCH_SESSION_NOT_FOUND` (404) или `SEARCH_SESSION_EXPIRED` (410); старые коды `SESSION_NOT_FOUND`/`SESSION_EXPIRED` принимаются как фолбэк.

### Облачные Рецепты и Публичный Каталог

13. `POST /api/v1/recipes/upload`
    - **Метод**: `uploadRecipe(@Body request: RecipeUploadRequest)`
    - **Доступ**: Авторизованный
    - **Описание**: Выгрузка/обновление рецепта в облако. Сервер проводит санитайз payload (вырезает личные поля) и возвращает 10-значный `access_code` для приватных рецептов.

14. `POST /api/v1/recipes/visibility`
    - **Метод**: `changeRecipeVisibility(@Body request: RecipeVisibilityRequest)`
    - **Доступ**: Авторизованный
    - **Описание**: Изменение публичности рецепта (`is_public: true/false`).

15. `GET /api/v1/recipes/{id}`
    - **Метод**: `getCloudRecipe(@Path("id") recipeId: String)`
    - **Доступ**: Без авторизации (для публичных) / Авторизованный (для своих приватных)
    - **Описание**: Скачивание контента рецепта из облака по ID.

16. `GET /api/v1/recipes`
    - **Метод**: `listMyRecipes()`
    - **Доступ**: Авторизованный
    - **Описание**: Получение списка всех выгруженных пользователем облачных рецептов.

17. `DELETE /api/v1/recipes/{recipeId}`
    - **Метод**: `deleteCloudRecipe(@Path("recipeId") recipeId: String)`
    - **Доступ**: Авторизованный
    - **Описание**: Удаление выгруженного рецепта из облачного хранилища.

18. `GET /api/v1/recipes/public`
    - **Метод**: `listPublicRecipes(@Query("page") page: Int, @Query("limit") limit: Int)`
    - **Доступ**: Без авторизации
    - **Описание**: Публичная лента открытых рецептов сообщества (постранично).

19. `GET /api/v1/recipes/search`
    - **Метод**: `searchCloudRecipes(@Query("q") query: String, @Query("page") page: Int, @Query("relaxed") relaxed: Int?)`
    - **Доступ**: Авторизованный
    - **Описание**: Полнотекстовый FTS-поиск среди публичных облачных рецептов по названию и тегам (от 2 символов).

20. `GET /api/v1/recipes/access/{code}`
    - **Метод**: `getRecipeByAccessCode(@Path("code") code: String)`
    - **Доступ**: Без авторизации
    - **Описание**: Скачивание рецепта по 10-значному коду доступа.

### Облачный Бэкап и Обратная Связь

21. `POST /api/v1/recipes/backup`
    - **Метод**: `uploadCloudBackup(@Body request: CloudBackupUploadRequest)`
    - **Доступ**: Авторизованный
    - **Описание**: Выгрузка полного снапшота локальной библиотеки `RecipeEntity` на сервер (включая личные оценки, заметки, статусы). Ограничение: до 500 рецептов, строгая валидация разрешённых полей DTO, лимит 6 выгрузок/час.

22. `GET /api/v1/recipes/backup`
    - **Метод**: `getCloudBackup()`
    - **Доступ**: Авторизованный
    - **Описание**: Скачивание последнего сохранённого облачного бэкапа пользователя.

23. `POST /api/v1/feedback`
    - **Метод**: `sendFeedback(@Body body: FeedbackRequest)`
    - **Доступ**: Авторизованный
    - **Описание**: Отправка сообщения или отзыва разработчикам (до 2000 символов, лимит 5 сообщений/час).

---

## 🤖 2. Прямая Интеграция с Gemini AI (`GeminiApiService.kt`)

Если бэкенд недоступен или требуется автономная работа с собственным ключом API, вызовы выполняются напрямую к Google AI Studio.

- **Эндпоинт**: `POST https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent`
- **Заголовок**: `x-goog-api-key: <API_KEY>`
- **Модель по умолчанию**: `gemini-2.5-flash`
- **Сжатие изображений**: `readImageBase64` автоматически уменьшает снимки кулинарных книг до макс. 1600px по широкой стороне и сжимает JPEG до 80% качества.
- **Инструменты шефа**: `CookAssistantTools.kt` реализует локальный агентный цикл с инструментами: `get_step`, `get_all_steps`, `get_ingredients`, `get_notes`, `get_chef_sheet`, `get_nutrition`, `go_to_step`, `suggest_timer`.

---

## 🔒 3. DNS-over-HTTPS и Сетевая Безопасность (`ExperimentalNetwork.kt`)

Для защиты от блокировок и спуфинга DNS приложение поддерживает нативный DoH через OkHttp DNS resolver.

- **Поддерживаемые провайдеры (`DnsMode`)**:
  - `OFF` — Стандартный системный DNS Android
  - `CLOUDFLARE` — `https://1.1.1.1/dns-query` (Bootstrap IP: `1.1.1.1`, `1.0.0.1`)
  - `GOOGLE` — `https://8.8.8.8/dns-query` (Bootstrap IP: `8.8.8.8`, `8.8.4.4`)
  - `QUAD9` — `https://9.9.9.9/dns-query` (Bootstrap IP: `9.9.9.9`, `149.112.112.112`)
  - `CUSTOM` — Кастомный HTTPS URL шлюза DoH
- **Диагностика**: `DnsTester.testResolution(context)` выполняет тестовый резолв `generativelanguage.googleapis.com` и замеряет задержку.

---

## 📡 4. Офлайн-режим, Квоты и Обработка Ошибок

- **Автоматическая авторизация (401)**: `TokenAuthenticator` в `BackendClient.kt` при 401 прозрачно вызывает `/auth/init`, сохраняет новый `session_token` и повторяет упавший запрос.
- **Глобальные статусы**:
  - `503 MAINTENANCE` — сервер на обслуживании (`AppStartupManager` блокирует сетевые вызовы).
  - `426 UPGRADE_REQUIRED` — приложение требует обновления до `min_version_code`.
- **Взвешенные квоты (`ai_costs`)**: сервер возвращает стоимость списания `cost` и новый остаток `remaining_month` в ответах платных ИИ-эндпоинтов, клиент мгновенно обновляет локальное состояние в `SessionManager`.
- **Офлайн-режим**: Все локальные операции (просмотр, редактирование, калькулятор порций, таймеры шагов, экспорт/импорт в JSON) работают 100% автономно при отсутствии интернет-соединения (`NetworkObserver`).
