package com.aetnox.cooking.data.remote

import com.aetnox.cooking.data.remote.model.ApiErrorCodes

object ErrorLocalizationManager {

    /**
     * Преобразует HTTP-код, error_code сервера и текст ошибки в локализованную строку
     * в полном соответствии с CLIENT_LOCALIZATION_CATALOG.md (Таблица A и Таблица B).
     */
    fun getLocalizedErrorMessage(
        httpCode: Int,
        errorCode: String?,
        serverErrorText: String?,
        endpoint: String? = null,
        isEnglish: Boolean = false,
    ): String {
        // --- ТАБЛИЦА A: Ошибки с error_code ---
        // Сервер с 17.11.2026 гарантирует error_code на ЛЮБОЙ ошибке,
        // поэтому таблица B (сопоставление текстов) — только фолбэк
        // для ответов старых версий сервера.
        if (!errorCode.isNullOrBlank()) {
            when (errorCode) {
                ApiErrorCodes.UNAUTHORIZED -> {
                    return if (isEnglish) "Authorization required / Session not found or expired"
                    else "Требуется авторизация / Сессия не найдена или истекла"
                }

                // Устаревший код: с 17.11.2026 сервер шлёт SEARCH_SESSION_EXPIRED (410).
                // Эвристика по эндпоинту оставлена для ответов старых серверов.
                ApiErrorCodes.SESSION_EXPIRED -> {
                    val isSearchEndpoint = (endpoint?.contains("search", ignoreCase = true) == true) ||
                            (serverErrorText?.contains("search", ignoreCase = true) == true)
                    return if (isSearchEndpoint) {
                        if (isEnglish) "Search session expired, please start a new search"
                        else "Сессия поиска истекла. Начните поиск заново"
                    } else {
                        if (isEnglish) "Session expired due to inactivity. Please sign in again."
                        else "Сессия авторизации истекла. Войдите снова"
                    }
                }

                ApiErrorCodes.SESSION_NOT_FOUND -> {
                    return if (isEnglish) "Search session not found"
                    else "Сессия поиска не найдена. Начните поиск заново"
                }

                ApiErrorCodes.ACCOUNT_BANNED -> {
                    return if (isEnglish) "Account banned by administrator / This device is banned"
                    else "Аккаунт или устройство заблокировано администратором"
                }

                ApiErrorCodes.RATE_LIMITED -> {
                    return if (isEnglish) "Too many requests. Please try again later."
                    else "Слишком много запросов. Попробуйте снова позже."
                }

                ApiErrorCodes.MAINTENANCE -> {
                    if (!serverErrorText.isNullOrBlank() && serverErrorText != "MAINTENANCE") {
                        return serverErrorText
                    }
                    return if (isEnglish) "Under maintenance. Please try again later."
                    else "Ведутся плановые технические работы. Пожалуйста, попробуйте позже."
                }

                ApiErrorCodes.UPGRADE_REQUIRED -> {
                    return if (isEnglish) "This app version is no longer supported. Please update the app."
                    else "Версия приложения больше не поддерживается. Пожалуйста, обновите приложение."
                }

                ApiErrorCodes.QUOTA_EXCEEDED -> {
                    if (!serverErrorText.isNullOrBlank() && serverErrorText.contains("vitamin", ignoreCase = true)) {
                        return serverErrorText
                    }
                    return if (isEnglish) "Not enough AI vitamins quota."
                    else "Недостаточно лимита витаминов ИИ."
                }

                ApiErrorCodes.FEATURE_DISABLED -> {
                    if (!serverErrorText.isNullOrBlank()) return serverErrorText
                    return if (isEnglish) "This feature is temporarily disabled"
                    else "Данная функция временно отключена"
                }

                ApiErrorCodes.FREE_TIER_QUARANTINE -> {
                    if (!serverErrorText.isNullOrBlank()) return serverErrorText
                    return if (isEnglish) "Free AI features are temporarily locked. Please activate a license key."
                    else "Бесплатные ИИ-функции заблокированы. Активируйте ключ доступа."
                }

                ApiErrorCodes.ASSISTANT_LOCKED -> {
                    return if (isEnglish) "Assistant is not available on your current plan"
                    else "Ассистент недоступен в вашем текущем тарифе"
                }

                ApiErrorCodes.AI_SEARCH_LOCKED -> {
                    return if (isEnglish) "AI search is not available on your current plan"
                    else "ИИ-поиск недоступен в вашем текущем тарифе"
                }

                ApiErrorCodes.PHOTO_RESTRICTED -> {
                    return if (isEnglish) "Photo digitization is not included in your current plan"
                    else "Распознавание по фото не входит в ваш текущий тариф"
                }

                ApiErrorCodes.PHOTO_QUOTA_EXCEEDED -> {
                    return if (isEnglish) "Monthly photo digitization limit reached"
                    else "Месячный лимит фото-оцифровок исчерпан"
                }

                ApiErrorCodes.UNSUPPORTED_IMAGE -> {
                    return if (isEnglish) "Supported image formats: JPEG, PNG, WebP, HEIC"
                    else "Неподдерживаемый формат изображения. Поддерживаются: JPEG, PNG, WebP, HEIC"
                }

                ApiErrorCodes.IMAGE_TOO_LARGE -> {
                    return if (isEnglish) "Photo is too large (max ~5 MB)"
                    else "Файл изображения слишком большой (максимум ~5 МБ)"
                }

                ApiErrorCodes.KEY_PLAN_INVALID -> {
                    return if (isEnglish) "The key is misconfigured. Contact support — the key was not used."
                    else "Ключ выпущен на несуществующий тариф. Обратитесь в поддержку."
                }

                ApiErrorCodes.PLAN_DOWNGRADE_BLOCKED -> {
                    return if (isEnglish) "You already have a higher plan active."
                    else "У вас уже активен более высокий тарифный план."
                }

                ApiErrorCodes.CLOUD_QUOTA_EXCEEDED -> {
                    return if (isEnglish) "Not enough cloud storage: cloud recipe limit reached."
                    else "Лимит облачных рецептов исчерпан. Обновите тариф для увеличения емкости."
                }

                ApiErrorCodes.PAYLOAD_TOO_LARGE -> {
                    return if (isEnglish) "Request body too large (max ~12 MB)"
                    else "Превышен размер передаваемых данных (макс. 12 МБ)"
                }

                // --- Коды, добавленные сервером 17.11.2026 (гарантия error_code на любой ошибке) ---

                ApiErrorCodes.SEARCH_SESSION_NOT_FOUND -> {
                    return if (isEnglish) "Search session not found"
                    else "Сессия поиска не найдена. Начните поиск заново"
                }

                ApiErrorCodes.SEARCH_SESSION_EXPIRED -> {
                    return if (isEnglish) "Search session expired, please start a new search"
                    else "Сессия поиска истекла. Начните поиск заново"
                }

                ApiErrorCodes.AI_UNAVAILABLE -> {
                    return if (isEnglish) "The AI service is temporarily unavailable. Please try again later."
                    else "Сервис ИИ временно недоступен. Попробуйте снова через 1-2 минуты."
                }

                ApiErrorCodes.AI_OVERLOADED -> {
                    return if (isEnglish) "All available AI models are overloaded. Please try again in 1-2 minutes."
                    else "Все модели ИИ перегружены. Попробуйте снова через 1-2 минуты."
                }

                ApiErrorCodes.AI_PARSE_FAILED -> {
                    return if (isEnglish) "Failed to parse the recipe format. Please try again later."
                    else "Не удалось разобрать формат рецепта. Попробуйте позже."
                }

                ApiErrorCodes.INVALID_RECIPE -> {
                    return if (isEnglish) "The AI returned an incomplete recipe structure. Try rephrasing the request."
                    else "ИИ вернул неполную структуру рецепта. Попробуйте переформулировать запрос."
                }

                ApiErrorCodes.INVALID_JSON -> {
                    return if (isEnglish) "Invalid JSON in request body"
                    else "Некорректный формат запроса"
                }

                ApiErrorCodes.ENDPOINT_NOT_FOUND -> {
                    return if (isEnglish) "Endpoint Not Found"
                    else "Запрашиваемый эндпоинт не найден"
                }

                ApiErrorCodes.INTERNAL_ERROR -> {
                    return if (isEnglish) "Internal server error"
                    else "Внутренняя ошибка сервера"
                }

                ApiErrorCodes.VALIDATION_ERROR -> {
                    return if (isEnglish) "Invalid request data"
                    else "Некорректные данные запроса"
                }

                ApiErrorCodes.EMPTY_REQUEST -> {
                    return if (isEnglish) "Provide text or a photo"
                    else "Укажите текст или прикрепите фото"
                }

                ApiErrorCodes.TEXT_TOO_LONG -> {
                    return if (isEnglish) "Text exceeds 4000 characters"
                    else "Текст превышает 4000 символов"
                }

                ApiErrorCodes.EMPTY_FEEDBACK -> {
                    return if (isEnglish) "Feedback message is empty"
                    else "Сообщение отзыва не может быть пустым"
                }

                ApiErrorCodes.QUERY_TOO_SHORT -> {
                    return if (isEnglish) "At least 2 characters required for search"
                    else "Минимум 2 символа для поиска"
                }

                ApiErrorCodes.KEY_REQUIRED -> {
                    return if (isEnglish) "Enter a key"
                    else "Введите ключ"
                }

                ApiErrorCodes.INVALID_KEY -> {
                    return if (isEnglish) "Invalid or already used key"
                    else "Неверный или уже использованный ключ"
                }

                ApiErrorCodes.KEY_ALREADY_ACTIVATED -> {
                    return if (isEnglish) "The key has already been activated"
                    else "Ключ уже был активирован"
                }

                ApiErrorCodes.TRANSFER_NO_SUBSCRIPTION -> {
                    return if (isEnglish) "No active paid subscription on this device"
                    else "На устройстве нет активной платной подписки"
                }

                ApiErrorCodes.INVALID_TRANSFER_CODE -> {
                    return if (isEnglish) "Invalid, expired, or already used transfer code"
                    else "Некорректный, истекший или уже использованный код переноса"
                }

                ApiErrorCodes.TRANSFER_SOURCE_NOT_FOUND -> {
                    return if (isEnglish) "Source account not found"
                    else "Исходный аккаунт не найден"
                }

                ApiErrorCodes.TRANSFER_SAME_DEVICE -> {
                    return if (isEnglish) "Cannot transfer the subscription to the same device"
                    else "Нельзя перенести подписку на то же устройство"
                }

                ApiErrorCodes.TRANSFER_SOURCE_EXPIRED -> {
                    return if (isEnglish) "The subscription on the source device is no longer active"
                    else "Подписка на исходном устройстве уже неактивна"
                }

                ApiErrorCodes.TRANSFER_COOLDOWN -> {
                    return if (isEnglish) "Transfer is temporarily blocked by a 7-day cooldown"
                    else "Перенос временно заблокирован (кулдаун 7 дней)"
                }

                ApiErrorCodes.TRANSFER_LIMIT_REACHED -> {
                    return if (isEnglish) "Transfer limit reached (max 2)"
                    else "Исчерпан лимит переносов подписки (максимум 2)"
                }

                ApiErrorCodes.RECIPE_TOO_LARGE -> {
                    return if (isEnglish) "Recipe is too large"
                    else "Рецепт слишком большой"
                }

                ApiErrorCodes.CLOUD_RECIPE_NOT_FOUND -> {
                    return if (isEnglish) "Recipe not found in the cloud"
                    else "Рецепт не найден в облаке"
                }

                ApiErrorCodes.INVALID_ACCESS_CODE -> {
                    return if (isEnglish) "Invalid access code"
                    else "Неверный код доступа к рецепту"
                }

                ApiErrorCodes.ACCESS_RESTRICTED -> {
                    return if (isEnglish) "Access restricted to the author"
                    else "Доступ ограничен автором рецепта"
                }

                ApiErrorCodes.FORBIDDEN -> {
                    return if (isEnglish) "You cannot modify another user's recipe"
                    else "Нет доступа к изменению чужого рецепта"
                }

                ApiErrorCodes.BACKUP_TOO_LARGE -> {
                    return if (isEnglish) "Backup is too large (max ~1 MB)"
                    else "Файл бэкапа слишком большой (макс. ~1 МБ)"
                }
            }
        }

        // --- ТАБЛИЦА B (устарела, только для ответов серверов до 17.11.2026):
        // ошибки без error_code — маппинг по тексту / HTTP коду ---
        val text = serverErrorText.orEmpty()

        if (httpCode == 502 || text.contains("temporarily unavailable", ignoreCase = true) || text.contains("overloaded", ignoreCase = true)) {
            return if (isEnglish) "The AI service is temporarily unavailable. Please try again later."
            else "Сервис ИИ временно недоступен. Попробуйте снова через 1-2 минуты."
        }

        if (httpCode == 400 && text.contains("Invalid JSON", ignoreCase = true)) {
            return if (isEnglish) "Invalid JSON in request body"
            else "Некорректный формат запроса"
        }

        if (httpCode == 500 || text.contains("Internal server error", ignoreCase = true)) {
            return if (isEnglish) "Internal server error"
            else "Внутренняя ошибка сервера"
        }

        if (httpCode == 404 && text.contains("Endpoint Not Found", ignoreCase = true)) {
            return if (isEnglish) "Endpoint Not Found"
            else "Запрашиваемый эндпоинт не найден"
        }

        if (text.contains("Transfer code", ignoreCase = true) || text.contains("transfer", ignoreCase = true)) {
            return if (isEnglish) "Invalid, expired, or already used transfer code"
            else "Некорректный, истекший или уже использованный код переноса"
        }

        if (text.contains("key", ignoreCase = true) || text.contains("Key", ignoreCase = true)) {
            return if (isEnglish) "Invalid or already used key"
            else "Неверный или уже использованный ключ"
        }

        if (text.contains("Provide text or a photo", ignoreCase = true)) {
            return if (isEnglish) "Provide text or a photo"
            else "Укажите текст или прикрепите фото"
        }

        if (text.contains("Recipe not found", ignoreCase = true)) {
            return if (isEnglish) "Recipe not found in the cloud"
            else "Рецепт не найден в облаке"
        }

        if (text.contains("access code", ignoreCase = true)) {
            return if (isEnglish) "Invalid access code"
            else "Неверный код доступа к рецепту"
        }

        if (text.contains("Feedback message", ignoreCase = true)) {
            return if (isEnglish) "Feedback message is empty"
            else "Сообщение отзыва не может быть пустым"
        }

        if (text.contains("Backup is too large", ignoreCase = true)) {
            return if (isEnglish) "Backup is too large (max ~1 MB)"
            else "Файл бэкапа слишком большой (макс. ~1 МБ)"
        }

        if (text.isNotBlank()) return text

        return if (isEnglish) "Server error ($httpCode)" else "Ошибка сервера ($httpCode)"
    }
}
