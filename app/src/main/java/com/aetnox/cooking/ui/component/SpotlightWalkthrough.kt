package com.aetnox.cooking.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

enum class SpotlightTargetKey {
    SEARCH_BAR,
    FILTER_BUTTON,
    VIEW_TOGGLE,
    SORT_BUTTON,
    SETTINGS_BUTTON,
    COOK_BUTTON,
    FAB_BUTTON
}

private data class SpotlightStep(
    val key: SpotlightTargetKey,
    val title: String,
    val description: String,
    val tip: String,
    val icon: ImageVector
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SpotlightWalkthrough(
    targetBounds: Map<SpotlightTargetKey, Rect>,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenSampleRecipe: () -> Unit = {}
) {
    var currentStepIndex by remember { mutableIntStateOf(0) }

    val steps = listOf(
        SpotlightStep(
            key = SpotlightTargetKey.SEARCH_BAR,
            title = "1. Поиск по названию 🔍",
            description = "Введите название рецепта или его часть. Список автоматически отфильтруется на лету!",
            tip = "💡 Поддерживает поиск по русским и английским названиям.",
            icon = Icons.Default.Search
        ),
        SpotlightStep(
            key = SpotlightTargetKey.FILTER_BUTTON,
            title = "2. Мульти-фильтр и теги ⚙️",
            description = "Нажмите эту кнопку, чтобы открыть мульти-поиск по нескольким ингредиентам (например: «сыр, яйца») и фильтрацию по тегам.",
            tip = "💡 Отличный способ быстро решить, что приготовить из имеющихся продуктов!",
            icon = Icons.Default.Tune
        ),
        SpotlightStep(
            key = SpotlightTargetKey.VIEW_TOGGLE,
            title = "3. Переключение вида 📱",
            description = "Переключайтесь между подробным списком с большими карточками и компактной сеткой из двух колонок.",
            tip = "💡 Вид сетки идеален для быстрого обзора большой кулинарной книги.",
            icon = Icons.Default.GridView
        ),
        SpotlightStep(
            key = SpotlightTargetKey.SORT_BUTTON,
            title = "4. Меню сортировки 📊",
            description = "Сортируйте рецепты по дате редактирования, по названию в алфавитном порядке или по продолжительности готовки.",
            tip = "💡 Позволяет мгновенно найти самые быстрые рецепты для ужина.",
            icon = Icons.Default.FilterList
        ),
        SpotlightStep(
            key = SpotlightTargetKey.SETTINGS_BUTTON,
            title = "5. Настройки & Бэкап 💾",
            description = "Раздел локальных резервных копий JSON, шаблонов промптов для нейросетей (ChatGPT / Claude) и повторного запуска этого обучения!",
            tip = "💡 Ваши кулинарные данные хранятся 100% локально на устройстве.",
            icon = Icons.Default.Settings
        ),
        SpotlightStep(
            key = SpotlightTargetKey.COOK_BUTTON,
            title = "6. Режим «Готовить» 👨‍🍳",
            description = "Самая главная кнопка! Запускает интерактивный пошаговый режим шефа с авто-пересчетом ингредиентов на число порций и встроенными таймерами.",
            tip = "💡 В текстах шагов ингредиенты представлены кликабельными плашками `@[Ингредиент]`.",
            icon = Icons.Default.PlayArrow
        ),
        SpotlightStep(
            key = SpotlightTargetKey.FAB_BUTTON,
            title = "7. Создание и Импорт ➕",
            description = "Добавляйте новые рецепты вручную, вставляйте скопированный текст из любого сайта через «Из буфера» или создавайте быстрые черновики.",
            tip = "💡 Импорт из буфера автоматически распознает ингредиенты и шаги!",
            icon = Icons.Default.Add
        )
    )

    val currentStep = steps[currentStepIndex]
    val isLastStep = currentStepIndex == steps.size - 1
    val density = LocalDensity.current

    val currentRect = targetBounds[currentStep.key]

    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(100f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        // FULLSCREEN SPOTLIGHT CANVAS WITH CUTOUT
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
        ) {
            // Draw dark translucent scrim overlay
            drawRect(color = Color.Black.copy(alpha = 0.78f))

            currentRect?.let { rect ->
                val padding = 8.dp.toPx()
                val topLeft = Offset((rect.left - padding).coerceAtLeast(0f), (rect.top - padding).coerceAtLeast(0f))
                val size = Size(rect.width + padding * 2, rect.height + padding * 2)
                val cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())

                // Clear translucent background over target element (Spotlight Cutout)
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = topLeft,
                    size = size,
                    cornerRadius = cornerRadius,
                    blendMode = BlendMode.Clear
                )

                // Draw glowing border stroke around target element
                drawRoundRect(
                    color = Color(0xFFFFB74D), // Warm Amber Gold Highlight
                    topLeft = topLeft,
                    size = size,
                    cornerRadius = cornerRadius,
                    style = Stroke(
                        width = 3.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
                    )
                )
            }
        }

        // TOOLTIP CARD POSITIONING
        val isTargetInTopHalf = remember(currentRect) {
            if (currentRect != null) {
                currentRect.top < (with(density) { 380.dp.toPx() })
            } else {
                false
            }
        }

        val alignment = if (isTargetInTopHalf) Alignment.BottomCenter else Alignment.TopCenter

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = alignment
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // TOP ROW: STEP BADGE + SKIP BUTTON
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Элемент ${currentStepIndex + 1} из ${steps.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

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
                                contentDescription = "Пропустить",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ANIMATED STEP CONTENT
                    AnimatedContent(
                        targetState = currentStepIndex,
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
                        label = "SpotlightStepTransition"
                    ) { stepIdx ->
                        val step = steps[stepIdx]

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = step.icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = step.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = step.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                                )
                            ) {
                                Text(
                                    text = step.tip,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // INDICATOR DOTS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        steps.indices.forEach { index ->
                            val isSelected = index == currentStepIndex
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .height(8.dp)
                                    .width(if (isSelected) 22.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outlineVariant
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // BOTTOM CONTROLS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (currentStepIndex > 0) currentStepIndex--
                            },
                            enabled = currentStepIndex > 0,
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

                        if (isLastStep) {
                            Button(
                                onClick = {
                                    onDismissRequest()
                                    onOpenSampleRecipe()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Завершить гид")
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (currentStepIndex < steps.size - 1) currentStepIndex++
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Далее")
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Далее",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
