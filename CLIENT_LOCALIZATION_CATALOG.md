# КАТАЛОГ ЛОКАЛИЗАЦИИ КЛИЕНТА (Cookin API)

> С 15.11.2026 все пользовательские тексты сервера приходят **на английском**.
> Клиент локализует интерфейс по `error_code`; английский `error` — фолбэк для показа «как есть».
> С 17.11.2026 сервер возвращает явный `error_code` на **любой** ошибке (валидация, 404, IDOR, 502 от ИИ, 500) — сопоставление текстов регулярками не требуется.
> Валюта квоты: EN «AI vitamin(s)», RU «витамины ИИ» (ранее «ИИ-запросы»). Поля API не менялись.
> Пользовательские ответы ИИ (ассистент `content`, поиск `reply`) — **на языке запроса пользователя**.

## Как локализовать

1. **Есть `error_code`** → клиент показывает свою локализованную строку по коду (таблица A). Код — ключ локализации, текст сервера — фолбэк.
2. **Нет `error_code`** (только от серверов старше 17.11.2026) → таблица B: мапить по паре «эндпоинт + HTTP-код», текст — фолбэк.
3. **Динамические данные** (цены, остатки) — числа из полей, строку строит клиент сам (правила плюрализации — в конце).

---

## Таблица A. Ошибки с `error_code`

| `error_code` | HTTP | Текст сервера (EN) | Рекомендуемый RU |
|---|---|---|---|
| `UNAUTHORIZED` | 401 | `Authorization required` | Требуется авторизация |
| `UNAUTHORIZED` | 401 | `Session not found or expired` | Сессия не найдена или истекла |
| `SESSION_EXPIRED` | 401 (auth) | `Session expired due to inactivity. Please sign in again.` | Сессия истекла по неактивности. Войдите заново |
| `ACCOUNT_BANNED` | 403 | `Account banned by administrator` | Аккаунт заблокирован администратором |
| `ACCOUNT_BANNED` | 403 (auth/init) | `This device is banned` | Устройство заблокировано |
| `RATE_LIMITED` | 429 | `Too many requests. Please try again later.` | Слишком много запросов. Попробуйте позже |
| `RATE_LIMITED` | 429 | `Too many attempts. Please try again in an hour.` | Слишком много попыток. Попробуйте через час |
| `RATE_LIMITED` | 429 | `Too many activation attempts. Please try again in an hour.` | Слишком много попыток активации. Попробуйте через час |
| `RATE_LIMITED` | 429 | `Too often. Backup is available a few times per hour.` | Слишком часто. Бэкап доступен несколько раз в час |
| `RATE_LIMITED` | 429 | `Too many search requests. Please try again in a minute.` | Слишком много запросов поиска. Попробуйте через минуту |
| `RATE_LIMITED` | 429 | `Too many searches in a row. Please try again later.` | Слишком много поисков подряд. Попробуйте позже |
| `RATE_LIMITED` | 429 | `Too many feedback messages. Please try again later.` | Слишком много отзывов. Попробуйте позже |
| `MAINTENANCE` | 503 | `message` = дефолт `Under maintenance. Please try again later.` (или текст админа, любой язык) | Ведутся технические работы. Попробуйте позже |
| `UPGRADE_REQUIRED` | 426 | `This app version is no longer supported. Please update the app.` (+ `update_url`) | Эта версия приложения больше не поддерживается. Обновите приложение |
| `QUOTA_EXCEEDED` | 403 | `Not enough quota: this action costs {N} AI vitamin(s), you have {M} AI vitamins left` (варианты: `this search costs … (library of X recipes) …`, `search planning costs …`) | Недостаточно лимита: это действие стоит {N} витамин(а/ов) ИИ, у вас осталось {M} витамин(а/ов) ИИ |
| `FEATURE_DISABLED` | 403 | `This feature is temporarily disabled` / `AI search is temporarily disabled` | Функция временно отключена / ИИ-поиск временно отключён |
| `FREE_TIER_QUARANTINE` | 403 | `Free AI features are temporarily locked. Activate a subscription key.` / `Assistant features are unavailable while quarantined` / `AI features are unavailable while quarantined` | Бесплатные функции ИИ временно заблокированы. Активируйте ключ подписки / Функции ассистента недоступны в карантине / Функции ИИ недоступны в карантине |
| `ASSISTANT_LOCKED` | 403 | `Assistant is not available on your current plan` | Ассистент недоступен в текущем тарифе |
| `AI_SEARCH_LOCKED` | 403 | `AI search is not available on your current plan` | ИИ-поиск недоступен в текущем тарифе |
| `PHOTO_RESTRICTED` | 403 | `Photo digitization is not included in your current plan` | Оцифровка по фото не входит в ваш текущий тариф |
| `PHOTO_QUOTA_EXCEEDED` | 403 | `Monthly photo digitization limit reached` | Месячный лимит оцифровки по фото исчерпан |
| `UNSUPPORTED_IMAGE` | 415 | `Supported formats: JPEG, PNG, WebP, HEIC` | Поддерживаются JPEG, PNG, WebP и HEIC |
| `IMAGE_TOO_LARGE` | 413 | `Photo is too large (max ~5 MB)` | Фото слишком большое (максимум ~5 МБ) |
| `PAYLOAD_TOO_LARGE` | 413 | `Request body too large (max ~12 MB)` | Тело запроса слишком большое (максимум ~12 МБ) |
| `KEY_PLAN_INVALID` | 409 | `The key is misconfigured. Contact support — the key was not used.` | Ключ настроен неверно. Обратитесь в поддержку — ключ не использован |
| `PLAN_DOWNGRADE_BLOCKED` | 409 | `You already have a higher plan active. The key was not used — activate it after your subscription ends.` | У вас уже активен более высокий тариф. Ключ не использован — активируйте его после окончания подписки |
| `CLOUD_QUOTA_EXCEEDED` | 403 | `Backups are not available on your plan` | Бэкапы недоступны на вашем тарифе |
| `CLOUD_QUOTA_EXCEEDED` | 403 | `Not enough cloud storage: {X} of {Y} used (cloud recipes + backup)` | Недостаточно места в облачном хранении: занято {X} из {Y} (облачные рецепты + бэкап) |
| `CLOUD_QUOTA_EXCEEDED` | 403 | `Cloud recipe limit reached or unavailable on your plan` | Лимит облачных рецептов исчерпан или недоступен на вашем тарифе |
| `INVALID_BACKUP` | 400 | `recipes must be an array` / `recipes array is empty` / `Max 500 recipes per backup` / `Recipe #{i}: …` (строгая валидация, текст содержит причину) | Текст содержит причину — можно показывать как есть (EN) или общую строку «Бэкап не прошёл проверку» |
| `BACKUP_TOO_LARGE` | 413 | `Backup is too large (max ~1 MB)` | Бэкап слишком большой (максимум ~1 МБ) |
| `SEARCH_SESSION_NOT_FOUND` | 404 | `Search session not found` | Сессия поиска не найдена. Начните поиск заново |
| `SEARCH_SESSION_EXPIRED` | 410 | `Search session expired, please start a new search` | Сессия поиска истекла. Начните поиск заново |
| `AI_UNAVAILABLE` | 502 | `The AI service is temporarily unavailable. Please try again later.` | Сервис ИИ временно недоступен. Попробуйте позже |
| `AI_OVERLOADED` | 502 | `All available AI models are overloaded or returned errors. Please try again in 1-2 minutes.` | Все доступные модели ИИ перегружены или вернули ошибку. Попробуйте через 1-2 минуты |
| `AI_PARSE_FAILED` | 500 | `Failed to parse the recipe format` | Не удалось разобрать формат рецепта. Попробуйте позже |
| `INVALID_RECIPE` | 422 | `The AI returned an incomplete recipe structure` / `Invalid recipe structure` | ИИ вернул неполную структуру рецепта. Попробуйте переформулировать |
| `INVALID_JSON` | 400 | `Invalid JSON in request body` / `payload_json is not valid JSON` | Некорректный формат запроса |
| `ENDPOINT_NOT_FOUND` | 404 | `Endpoint Not Found` | Запрашиваемый эндпоинт не найден |
| `INTERNAL_ERROR` | 500 | `Internal server error` | Внутренняя ошибка сервера |
| `VALIDATION_ERROR` | 400 | `device_id is required` / `search_token is required` / `No messages provided` / `Invalid message history` / `recipe_id and is_public (true/false) are required` / `Message exceeds 2000 characters` и т.п. | Некорректные данные запроса (конкретное поле — из текста) |
| `EMPTY_REQUEST` | 400 | `Provide text or a photo` | Укажите текст или прикрепите фото |
| `TEXT_TOO_LONG` | 400 | `Text exceeds 4000 characters` | Текст превышает 4000 символов |
| `EMPTY_FEEDBACK` | 400 | `Feedback message is empty` | Сообщение отзыва не может быть пустым |
| `QUERY_TOO_SHORT` | 400 | `At least 2 characters required for search` | Минимум 2 символа для поиска |
| `KEY_REQUIRED` | 400 | `Enter a key` | Введите ключ |
| `INVALID_KEY` | 400 | `Invalid or already used key` | Неверный или уже использованный ключ |
| `KEY_ALREADY_ACTIVATED` | 409 | `The key has already been activated` | Ключ уже был активирован |
| `TRANSFER_NO_SUBSCRIPTION` | 400 | `No active paid subscription on this device` | На устройстве нет активной платной подписки |
| `INVALID_TRANSFER_CODE` | 400 | `Invalid, expired, or already used code` | Неверный, истекший или уже использованный код переноса |
| `TRANSFER_SOURCE_NOT_FOUND` | 400 | `Source account not found` | Исходный аккаунт не найден |
| `TRANSFER_SAME_DEVICE` | 400 | `Cannot transfer the subscription to the same device` | Нельзя перенести подписку на то же устройство |
| `TRANSFER_SOURCE_EXPIRED` | 400 | `The subscription on the source device is no longer active` | Подписка на исходном устройстве уже неактивна |
| `TRANSFER_COOLDOWN` | 429 | `Transfer is temporarily blocked by a 7-day cooldown` | Перенос временно заблокирован (кулдаун 7 дней) |
| `TRANSFER_LIMIT_REACHED` | 403 | `Transfer limit reached (max 2)` | Исчерпан лимит переносов подписки (максимум 2) |
| `RECIPE_TOO_LARGE` | 413 | `Recipe is too large` | Рецепт слишком большой |
| `CLOUD_RECIPE_NOT_FOUND` | 404 | `Recipe not found` / `Recipe not found in the cloud` / `No recipe found for this code` | Рецепт не найден в облаке |
| `INVALID_ACCESS_CODE` | 404 | `Invalid access code` | Неверный код доступа к рецепту |
| `ACCESS_RESTRICTED` | 403 | `Access restricted to the author` | Доступ ограничен автором рецепта |
| `FORBIDDEN` | 403 | `You cannot modify another user's recipe` | Нет доступа к изменению чужого рецепта |

⚠️ `SESSION_EXPIRED` с 17.11.2026 однозначен: только **auth** (истекла сессия входа → re-init). Сессии поиска — `SEARCH_SESSION_EXPIRED`/`SEARCH_SESSION_NOT_FOUND`. Для обратной совместимости клиент принимает и старые `SESSION_EXPIRED`(410)/`SESSION_NOT_FOUND`(404) от поискового finish (различать по HTTP: 401 — авторизация, 404/410 — поиск).

⚠️ `UNAUTHORIZED` тоже двусмысленный: нет заголовка или сессия не найдена — в обоих случаях нужен новый `auth/init`.

## Таблица B. УСТАРЕЛО (серверы до 17.11.2026): ошибки без `error_code`

Современный сервер шлёт на все эти случаи коды из таблицы A — таблица B нужна только как фолбэк для старых версий сервера. Маппинг: «эндпоинт + HTTP-код» → тот же RU, что в таблице A.

| Эндпоинт | HTTP | Текст сервера (EN) | Код в новой версии |
|---|---|---|---|
| все | 400 | `Invalid JSON in request body` | `INVALID_JSON` |
| все | 500 | `Internal server error` | `INTERNAL_ERROR` |
| все | 404 | `Endpoint Not Found` | `ENDPOINT_NOT_FOUND` |
| `POST /auth/init` | 400 | `device_id is required` / `device_id is too long (max 128 characters)` | `VALIDATION_ERROR` |
| `POST /auth/transfer/create` | 400 | `No active paid subscription on this device` | `TRANSFER_NO_SUBSCRIPTION` |
| `POST /auth/transfer/claim` | 400 | `Transfer code is required` | `VALIDATION_ERROR` |
| `POST /auth/transfer/claim` | 400 | `Invalid, expired, or already used code` | `INVALID_TRANSFER_CODE` |
| `POST /auth/transfer/claim` | 400 | `Source account not found` | `TRANSFER_SOURCE_NOT_FOUND` |
| `POST /auth/transfer/claim` | 400 | `Cannot transfer the subscription to the same device` | `TRANSFER_SAME_DEVICE` |
| `POST /auth/transfer/claim` | 400 | `The subscription on the source device is no longer active` | `TRANSFER_SOURCE_EXPIRED` |
| `POST /auth/transfer/claim` | 429 | `Transfer is temporarily blocked by a 7-day cooldown` | `TRANSFER_COOLDOWN` |
| `POST /auth/transfer/claim` | 403 | `Transfer limit reached (max 2)` | `TRANSFER_LIMIT_REACHED` |
| `POST /user/activate-key` | 400 | `Enter a key` | `KEY_REQUIRED` |
| `POST /user/activate-key` | 400 | `Invalid or already used key` | `INVALID_KEY` |
| `POST /user/activate-key` | 409 | `The key has already been activated` | `KEY_ALREADY_ACTIVATED` |
| `POST /recipe/parse` | 400 | `Provide text or a photo` | `EMPTY_REQUEST` |
| `POST /recipe/parse` | 400 | `Text exceeds 4000 characters` | `TEXT_TOO_LONG` |
| `POST /recipe/parse` | 500 | `Failed to parse the recipe format` | `AI_PARSE_FAILED` |
| `POST /recipe/parse` | 422 | `The AI returned an incomplete recipe structure` | `INVALID_RECIPE` |
| `POST /assistant/step` | 400 | `No messages provided` / `Invalid message history` | `VALIDATION_ERROR` |
| `POST /recipes/upload` | 400/413 | `Invalid recipe data` / `Recipe is too large` / `payload_json is not valid JSON` | `VALIDATION_ERROR` / `RECIPE_TOO_LARGE` / `INVALID_JSON` |
| `POST /recipes/upload` | 422 | `Invalid recipe structure` | `INVALID_RECIPE` |
| `POST /recipes/upload` | 403 | `You cannot modify another user's recipe` | `FORBIDDEN` |
| `POST /recipes/visibility` | 400 | `recipe_id and is_public (true/false) are required` | `VALIDATION_ERROR` |
| `POST /recipes/visibility`, `DELETE /recipes/{id}` | 404 | `Recipe not found in the cloud` | `CLOUD_RECIPE_NOT_FOUND` |
| `GET /recipes/{id}` | 404/403 | `Recipe not found` / `Access restricted to the author` | `CLOUD_RECIPE_NOT_FOUND` / `ACCESS_RESTRICTED` |
| `GET /recipes/search` | 400 | `At least 2 characters required for search` | `QUERY_TOO_SHORT` |
| `GET /recipes/access/{code}` | 404 | `Invalid access code` / `No recipe found for this code` | `INVALID_ACCESS_CODE` / `CLOUD_RECIPE_NOT_FOUND` |
| `POST /feedback` | 400 | `Feedback message is empty` / `Message exceeds 2000 characters` | `EMPTY_FEEDBACK` / `VALIDATION_ERROR` |
| `POST /recipes/backup` | 413 | `Backup is too large (max ~1 MB)` | `BACKUP_TOO_LARGE` |
| `finish` (двухфазный поиск) | 404/410 | `Search session not found` / `Search session expired…` | `SEARCH_SESSION_NOT_FOUND` / `SEARCH_SESSION_EXPIRED` (раньше `SESSION_NOT_FOUND`/`SESSION_EXPIRED`) |
| ИИ-эндпоинты | 502 | `The AI service is temporarily unavailable. Please try again later.` | `AI_UNAVAILABLE` |
| ИИ-эндпоинты | 502 | `All available AI models are overloaded or returned errors. Please try again in 1-2 minutes.` | `AI_OVERLOADED` |

## Поля ответов, которые клиент локализует/рендерит сам

| Поле | Где | Что с ним делать |
|---|---|---|
| `error` | все ошибки | Фолбэк-показ; при наличии `error_code` — своя строка по таблице A |
| `message` | 503 `MAINTENANCE`, 426 `UPGRADE_REQUIRED` | Фолбэк-показ; `message` при MAINTENANCE может быть произвольным текстом админа (любой язык) |
| `update_url` | 426 | Куда вести кнопку «Обновить» (не переводится) |
| `plan_name` | `/user/status` и др. | Задаётся админом в панели (любой язык); для локализации использовать `plan_level` |
| `ai_costs.*` | `/app/config`, `/user/status` | Числа; клиент строит строки расценок («Оцифровка — 2 витамина ИИ») |
| `cost`, `remaining_month` | ответы ИИ-эндпоинтов | Числа витаминов; строку строит клиент с плюрализацией |
| `quotas.ai_requests / photos / cloud` (`used_*`, `monthly_limit`, `remaining`) | `/user/status` | Числа; подписи («ИИ-лимит», «Фото», «Облако») — локализация клиента |
| `plan_level` | `/user/status` | 0 = «Карантин» — название тарифа клиент локализует по уровню |

## Плюрализация витаминов

- **EN**: `1 AI vitamin`, иначе `N AI vitamins`.
- **RU**: 1, 21, 31… → «витамин ИИ»; 2–4, 22–24… → «витамина ИИ»; 0, 5–20, 11–14, 25–30… → «витаминов ИИ».
- Сервер (EN) уже даёт правильную форму в `error`; в собственных строках клиент использует эти правила.

## Что НЕ локализует клиент

- Тех-детали в `ai_request_logs`, `ai_live_requests` (только админ-панель).
- Тексты админ-API `/api/v1/admin/*` — остаются на русском (панель — русскоязычный UI владельца).
- `device_info`, `account_id`, `recipe_id` — идентификаторы.
