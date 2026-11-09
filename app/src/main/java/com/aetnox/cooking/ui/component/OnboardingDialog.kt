package com.aetnox.cooking.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private data class OnboardingSlideData(
    val icon: ImageVector,
    val iconBgColor: Color,
    val badgeText: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val featureHighlights: List<Pair<String, String>>,
    val chips: List<String>
)

@OptIn(ExperimentalAnimationApi::class, ExperimentalLayoutApi::class)
@Composable
fun OnboardingDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenSampleRecipe: () -> Unit = {}
) {
    var currentSlideIndex by remember { mutableIntStateOf(0) }

    val slides = listOf(
        OnboardingSlideData(
            icon = Icons.Default.RestaurantMenu,
            iconBgColor = MaterialTheme.colorScheme.primaryContainer,
            badgeText = "Шаг 1 из 6 • Знакомство",
            title = "Добро пожаловать в «Кулинарный Помощник»! 🍳",
            subtitle = "Ваш умный кулинарный ассистент и книга рецептов",
            description = "Приложение создано для удобного хранения рецептов, точного управления ингредиентами и интерактивного сопровождения в процессе готовки.",
            featureHighlights = listOf(
                "📱 Полностью автономно" to "Все данные хранятся локально на вашем устройстве, работа без интернета.",
                "🍫 Готовый рецепт-образец" to "В базу занесён образец брауни с вишней для мгновенного знакомства.",
                "🔒 Чистый интерфейс" to "Никакой рекламы, подписок и лишних отвлекающих элементов."
            ),
            chips = listOf("100% Офлайн", "Без рекламы", "Готовый образец")
        ),
        OnboardingSlideData(
            icon = Icons.Default.Search,
            iconBgColor = MaterialTheme.colorScheme.secondaryContainer,
            badgeText = "Шаг 2 из 6 • Поиск и фильтры",
            title = "Умный каталог и поиск 🔍",
            subtitle = "Быстрый выбор блюд из имеющихся продуктов",
            description = "Находите нужные рецепты за секунды благодаря развитой системе поиска, тегов и статусов.",
            featureHighlights = listOf(
                "🏷️ Мульти-поиск по ингредиентам" to "Вводите продукты через запятую (например: «сыр, яйца»), чтобы найти подходящие рецепты.",
                "📌 Фильтрация по статусам" to "Разделение рецептов: «В планах», «Проверено», «Над ошибками» и «Архив».",
                "⭐ Оценки и сортировка" to "Сортируйте по дате, названию или общей продолжительности готовки."
            ),
            chips = listOf("Поиск по ингредиентам", "Теги", "Статусы рецептов")
        ),
        OnboardingSlideData(
            icon = Icons.Default.SoupKitchen,
            iconBgColor = MaterialTheme.colorScheme.tertiaryContainer,
            badgeText = "Шаг 3 из 6 • Режим «Готовить»",
            title = "Интерактивный Шеф-помощник 👨‍🍳",
            subtitle = "Умное сопровождение шагов и таймеры",
            description = "Главная фишка приложения! Нажмите «Готовить» на карточке любого рецепта для перехода в режим шефа.",
            featureHighlights = listOf(
                "⚖️ Масштабирование порций" to "Автоматический перерасчёт точных объёмов всех ингредиентов при изменении порций.",
                "🏷️ Токены ингредиентов" to "В текстах шагов ингредиенты отображаются кликабельными плашками со скалированными граммами.",
                "⏱️ Встроенные таймеры" to "Распознавание времени в тексте шага и запуск обратного отсчета с уведомлением.",
                "✅ Чек-лист ингредиентов" to "Предварительная отметка имеющихся продуктов перед стартом."
            ),
            chips = listOf("Перерасчет порций", "Токены ингредиентов", "Таймеры")
        ),
        OnboardingSlideData(
            icon = Icons.Default.AutoAwesome,
            iconBgColor = MaterialTheme.colorScheme.primaryContainer,
            badgeText = "Шаг 4 из 6 • Создание & ИИ",
            title = "Редактор & ИИ-Промпты 📝",
            subtitle = "Простое добавление и умный импорт",
            description = "Удобный конструктор рецептов с поддержкой ИИ-генерации и распознавания текста.",
            featureHighlights = listOf(
                "🤖 Генератор ИИ-промптов" to "Создание готовых запросов для ChatGPT / Claude с авто-выгрузкой в структуру.",
                "📋 Импорт из буфера" to "Быстрое вставление рецепта из любого веб-сайта или кулинарного блога.",
                "📸 Фотографии" to "Добавление главных обложек и пошаговых фото процесса.",
                "⚡ Быстрый черновик" to "Мгновенное создание заготовки рецепта."
            ),
            chips = listOf("ИИ-Промпты", "Вставка из буфера", "Фото шагов")
        ),
        OnboardingSlideData(
            icon = Icons.Default.CloudSync,
            iconBgColor = MaterialTheme.colorScheme.secondaryContainer,
            badgeText = "Шаг 5 из 6 • Экспорт & Бэкап",
            title = "Экспорт и Безопасность 💾",
            subtitle = "Ваша кулинарная книга всегда с вами",
            description = "Легко сохраняйте резервные копии базы и делитесь рецептами с друзьями.",
            featureHighlights = listOf(
                "📦 Бэкап в JSON" to "Сохранение и восстановление всей базы рецептов в один клик.",
                "📤 Экспорт в Markdown" to "Форматирование рецептов в красивый текст для мессенджеров.",
                "🛡️ Полный контроль" to "Данные хранятся на вашем устройстве и легко переносятся на новый телефон."
            ),
            chips = listOf("JSON Резервирование", "Экспорт в Markdown", "Сохранность")
        ),
        OnboardingSlideData(
            icon = Icons.Default.CheckCircle,
            iconBgColor = MaterialTheme.colorScheme.tertiaryContainer,
            badgeText = "Шаг 6 из 6 • Всё готово!",
            title = "Вы готовы готовить! 🚀",
            subtitle = "Испробуйте приложение прямо сейчас",
            description = "Мы подготовили образец «🍫 Шоколадный Брауни с вишней». Откройте его в режиме «Готовить», чтобы проверить масштабитор порций, токены и таймеры на практике!",
            featureHighlights = listOf(
                "🍫 Откройте образец" to "Запустите режим «Готовить» прямо сейчас на готовом тестовом рецепте.",
                "🎓 Повтор обучения" to "Вы всегда сможете пересмотреть это обучение, нажав иконку «🎓» в шапке экрана."
            ),
            chips = listOf("Брауни с вишней", "Шеф-режим", "Приступаем!")
        )
    )

    val isLastSlide = currentSlideIndex == slides.size - 1
    val currentSlide = slides[currentSlideIndex]

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.93f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // TOP HEADER ROW: BADGE + SKIP BUTTON
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = currentSlide.badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    // SKIP BUTTON ("Пропустить")
                    TextButton(
                        onClick = onDismissRequest,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "Пропустить",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Пропустить обучение",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ANIMATED SLIDE CONTENT
                AnimatedContent(
                    targetState = currentSlideIndex,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut()
                            )
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut()
                            )
                        }
                    },
                    label = "OnboardingSlideTransition",
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) { slideIndex ->
                    val slide = slides[slideIndex]

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        // ICON & TITLE
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(slide.iconBgColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = slide.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = slide.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = slide.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = slide.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // FEATURE HIGHLIGHT CARDS
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            slide.featureHighlights.forEach { (featureTitle, featureDesc) ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = featureTitle,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = featureDesc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // CHIPS ROW
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            slide.chips.forEach { chipText ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                ) {
                                    Text(
                                        text = "✓ $chipText",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // PAGE INDICATOR DOTS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    slides.indices.forEach { index ->
                        val isSelected = index == currentSlideIndex
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .height(8.dp)
                                .width(if (isSelected) 24.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // BOTTOM ACTION BUTTONS
                if (isLastSlide) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onDismissRequest()
                                onOpenSampleRecipe()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text = "🍫 Открыть рецепт-образец в режиме «Готовить»",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }

                        FilledTonalButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = "Перейти к списку рецептов",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (currentSlideIndex > 0) currentSlideIndex--
                            },
                            enabled = currentSlideIndex > 0,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Назад",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Назад")
                        }

                        Button(
                            onClick = {
                                if (currentSlideIndex < slides.size - 1) currentSlideIndex++
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Далее")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Вперед",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
