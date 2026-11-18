# 🍳 Cooking — Умный кулинарный помощник & Рецепты

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2026.02.01-4285F4.svg?style=flat&logo=android)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material%203-Latest-006C4C.svg?style=flat&logo=materialdesign)](https://m3.material.io)
[![Nothing OS Design](https://img.shields.io/badge/Design-Nothing%20OS%20Style-D71921.svg?style=flat)](https://nothing.tech)

**Cooking** — современное мобильное приложение на **Kotlin** и **Jetpack Compose** для хранения, создания, поиска и приготовления рецептов с встроенным ИИ-су-шефом, гибкими таймерами, динамическим перерасчетом порций и бэкапом данных.

Проект поддерживает **дизайн-систему Nothing OS** (черный монохром, NDot шрифт, акцентный Nothing Red `#D71921`), автоматическое определение устройств Nothing Phone, 10 динамических тем и 4 кастомных иконки рабочего стола.

---

## 🌟 Ключевые возможности

### 🤖 Интерактивный ИИ-Су-шеф & Умный поиск
- **Режим готовки с ИИ-помощником**: подсказывает замены ингредиентов, высчитывает время запекания и на лету предлагает применить исправления к рецепту.
- **Двухфазный ИИ-поиск рецептов**: двухэтапный подбор рецептов в облаке с квотированием и локальной фильтрацией.
- **Умный редактор**: автоматический разбор сырого текста рецепта на понятные секции и шаги с помощью генеративного ИИ.

### ⏱️ Шеф-режим & Готовка
- **Динамический перерасчет порций**: мгновенный пересчет всех граммовок при изменении количества порций.
- **Интерактивные таймеры**: встроенные таймеры с уведомлениями в шторке Android и звуковым сопровождением.
- **Мультимедиа плеер**: интеграция Media3 ExoPlayer для видео-инструкций к шагам рецепта.

### 🎨 Персонализация & Дизайн Nothing OS
- **Nothing OS Style**: монохромная палитра `#000000` / `#141414`, акцентный Nothing Red `#D71921`, NDot typography и пружинистый Haptic Feedback.
- **Автоопределение Nothing Phone**: устройства Nothing автоматически запускаются в фирменном стиле.
- **10 Тем оформления**: Системная (Material You), Nothing (Dot Style), Травяная кухня, Светлая пекарня, Утренний сад, Морская глубина, Ночной шеф, Шоколад и карамель, Неоновая кухня, Кастомная.
- **4 Иконки рабочего стола**: Фирменная полноцветная, Неон, Ночной Шеф, Nothing.

### 💾 Сохранность данных & Экспорт
- **Бэкапы в 1 клик**: 100% сохранение рецептов в структурированный JSON с транзакционным импортом.
- **Экспорт в Markdown**: быстрое копирование любого рецепта в буфер обмена в чистом форматированном виде.
- **Полная локализация**: интерфейс на русском (🇷🇺) и английском (🇬🇧) с учётом правил плюрализации.

---

## 🛠️ Технологический стек (Tech Stack)

| Компонент | Технологии |
| :--- | :--- |
| **Язык** | Kotlin 2.2.10 |
| **UI Фреймворк** | Jetpack Compose (BOM 2026.02.01), Material 3, Navigation Compose |
| **Архитектура** | Clean Architecture + MVVM + Unidirectional Data Flow (StateFlow / Coroutines) |
| **База данных** | Room 2.8.5 (KSP, Coroutines, Flow) |
| **Сеть & API** | Retrofit 2.11.0, OkHttp 4.12.0, Kotlinx Serialization, DNS-over-HTTPS (DoH) |
| **Мультимедиа & Изображения** | Coil Compose 2.7.0, Media3 ExoPlayer 1.5.1 |
| **Хранение настроек** | Jetpack DataStore Preferences, SharedPreferences |

---

## 📱 Сборка и запуск проекта

### Требования
- **Android Studio**: 2026.1+ (Ladybug / Jellyfish or newer)
- **JDK**: Java 17+ (JBR)
- **Min SDK**: API 26 (Android 8.0)
- **Target SDK**: API 35 (Android 15)

### Консольная сборка (Git Bash / PowerShell)

```bash
# Установка JAVA_HOME (при необходимости)
export JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"

# Быстрая проверка компиляции Kotlin
./gradlew.bat :app:compileDebugKotlin --console=plain

# Запуск юнит-тестов
./gradlew.bat :app:testDebugUnitTest --console=plain

# Сборка Debug APK
./gradlew.bat :app:assembleDebug --console=plain
```

---

## 📄 Лицензия

Copyright © 2026 Aetnox.
