package com.aetnox.cooking.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.aetnox.cooking.ui.util.AppLocaleManager

enum class SpotlightTargetKey {
    SEARCH_BAR,
    FILTER_BUTTON,
    VIEW_TOGGLE,
    SORT_BUTTON,
    SETTINGS_BUTTON,
    COOK_BUTTON,
    FAB_BUTTON
}

private data class GameQuestStep(
    val key: SpotlightTargetKey,
    val ruQuestTitle: String,
    val enQuestTitle: String,
    val ruActionPrompt: String,
    val enActionPrompt: String,
    val icon: ImageVector
) {
    val questTitle: String get() = AppLocaleManager.tr(ruQuestTitle, enQuestTitle)
    val actionPrompt: String get() = AppLocaleManager.tr(ruActionPrompt, enActionPrompt)
}

/**
 * Быстрый игровой гид-квест ("как в играх").
 * Подсвечивает целевую кнопку в интерфейсе и мгновенно засчитывает квест при нажатии.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SpotlightWalkthrough(
    targetBounds: Map<SpotlightTargetKey, Rect>,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenSampleRecipe: () -> Unit = {}
) {
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var showMissHint by remember { mutableStateOf(false) }

    LaunchedEffect(currentStepIndex) {
        showMissHint = false
    }

    val questSteps = remember {
        listOf(
            GameQuestStep(
                key = SpotlightTargetKey.COOK_BUTTON,
                ruQuestTitle = "КВЕСТ 1: ШЕФ-РЕЖИМ 👨‍🍳",
                enQuestTitle = "QUEST 1: CHEF MODE 👨‍🍳",
                ruActionPrompt = "Жми «Готовить» на карточке брауни!",
                enActionPrompt = "Tap 'Cook' on the brownie card!",
                icon = Icons.Default.PlayArrow
            ),
            GameQuestStep(
                key = SpotlightTargetKey.SEARCH_BAR,
                ruQuestTitle = "КВЕСТ 2: БЫСТРЫЙ ПОИСК 🔍",
                enQuestTitle = "QUEST 2: QUICK SEARCH 🔍",
                ruActionPrompt = "Жми на поисковую строку!",
                enActionPrompt = "Tap on the search bar!",
                icon = Icons.Default.Search
            ),
            GameQuestStep(
                key = SpotlightTargetKey.FILTER_BUTTON,
                ruQuestTitle = "КВЕСТ 3: ПОИСК ПО ПРОДУКТАМ ⚙️",
                enQuestTitle = "QUEST 3: PRODUCT FILTER ⚙️",
                ruActionPrompt = "Жми сюда, чтобы найти рецепты по продуктам!",
                enActionPrompt = "Tap here to filter recipes by ingredients!",
                icon = Icons.Default.Tune
            ),
            GameQuestStep(
                key = SpotlightTargetKey.SETTINGS_BUTTON,
                ruQuestTitle = "КВЕСТ 4: НАСТРОЙКИ & БЭКАП 💾",
                enQuestTitle = "QUEST 4: SETTINGS & BACKUP 💾",
                ruActionPrompt = "Жми сюда для настройки тем и сохранения!",
                enActionPrompt = "Tap here for themes and backup options!",
                icon = Icons.Default.Settings
            )
        )
    }

    val currentStep = questSteps[currentStepIndex.coerceIn(0, questSteps.size - 1)]
    val isLastStep = currentStepIndex == questSteps.size - 1
    val density = LocalDensity.current

    val currentRect = targetBounds[currentStep.key]

    val infiniteTransition = rememberInfiniteTransition(label = "GamePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(100f)
            .pointerInput(currentRect, currentStepIndex) {
                detectTapGestures { tapOffset ->
                    if (currentRect != null) {
                        val paddingPx = 24.dp.toPx()
                        val isInsideTarget = tapOffset.x >= (currentRect.left - paddingPx) &&
                                tapOffset.x <= (currentRect.right + paddingPx) &&
                                tapOffset.y >= (currentRect.top - paddingPx) &&
                                tapOffset.y <= (currentRect.bottom + paddingPx)

                        if (isInsideTarget) {
                            if (currentStep.key == SpotlightTargetKey.COOK_BUTTON) {
                                onOpenSampleRecipe()
                                onDismissRequest()
                            } else if (currentStepIndex < questSteps.size - 1) {
                                currentStepIndex++
                            } else {
                                onDismissRequest()
                            }
                        } else {
                            showMissHint = true
                        }
                    } else {
                        if (currentStepIndex < questSteps.size - 1) currentStepIndex++
                        else onDismissRequest()
                    }
                }
            }
    ) {
        // FULLSCREEN SCRIM CANVAS WITH GLOWING CUTOUT
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        ) {
            drawRect(color = Color.Black.copy(alpha = 0.82f))

            currentRect?.let { rect ->
                val padding = 8.dp.toPx()
                val topLeft = Offset((rect.left - padding).coerceAtLeast(0f), (rect.top - padding).coerceAtLeast(0f))
                val size = Size(rect.width + padding * 2, rect.height + padding * 2)
                val cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())

                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = topLeft,
                    size = size,
                    cornerRadius = cornerRadius,
                    blendMode = BlendMode.Clear
                )

                // Neon Gaming Target Stroke
                drawRoundRect(
                    color = Color(0xFFFFB74D),
                    topLeft = topLeft,
                    size = size,
                    cornerRadius = cornerRadius,
                    style = Stroke(
                        width = (4f * pulseScale).dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 12f), 0f)
                    )
                )
            }
        }

        // FLOATING POINTER ICON OVER TARGET BUTTON
        currentRect?.let { rect ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = (rect.left + rect.width / 2f - 26.dp.toPx())
                        translationY = (rect.top + rect.height / 2f - 26.dp.toPx())
                    }
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                    shadowElevation = 10.dp,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AdsClick,
                            contentDescription = AppLocaleManager.tr("Жми сюда", "Tap here"),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        // GAMING HUD CARD AT TOP / BOTTOM
        val configuration = LocalConfiguration.current
        val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

        val tooltipAlignment = remember(currentRect, screenHeightPx) {
            if (currentRect != null) {
                val targetCenterY = currentRect.top + (currentRect.height / 2f)
                if (targetCenterY < screenHeightPx / 2f) Alignment.BottomCenter else Alignment.TopCenter
            } else Alignment.TopCenter
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = tooltipAlignment
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // HUD HEADER BADGE & PROGRESS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "🎯 ${currentStep.questTitle}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(onClick = onDismissRequest, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = AppLocaleManager.tr("Пропустить", "Skip"), modifier = Modifier.size(18.dp))
                        }
                    }

                    val progress = (currentStepIndex + 1).toFloat() / questSteps.size.toFloat()
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary
                    )

                    // 1-SENTENCE ACTION PROMPT IN GAME BUBBLE
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = currentStep.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "👉 ${currentStep.actionPrompt}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    if (showMissHint) {
                        Text(
                            text = AppLocaleManager.tr("⚡ Жми прямо на подсвеченную кнопку на экране!", "⚡ Tap directly on the highlighted button on screen!"),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // MINIMAL CONTROLS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppLocaleManager.tr("Шаг ${currentStepIndex + 1} из ${questSteps.size}", "Step ${currentStepIndex + 1} of ${questSteps.size}"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (isLastStep) {
                            Button(
                                onClick = {
                                    onDismissRequest()
                                    onOpenSampleRecipe()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(AppLocaleManager.tr("Погнали!", "Let's go!"), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
