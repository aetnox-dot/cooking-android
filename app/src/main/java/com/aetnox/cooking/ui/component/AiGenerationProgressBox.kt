package com.aetnox.cooking.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aetnox.cooking.ui.util.AppLocaleManager
import kotlinx.coroutines.delay

private data class ProgressStage(
    val iconEmoji: String,
    val ruTitle: String,
    val enTitle: String,
    val ruDetail: String,
    val enDetail: String,
    val startPercent: Int,
    val endPercent: Int
) {
    val title: String get() = AppLocaleManager.tr(ruTitle, enTitle)
    val detail: String get() = AppLocaleManager.tr(ruDetail, enDetail)
}

private val STAGES = listOf(
    ProgressStage(
        iconEmoji = "📡",
        ruTitle = "Подключение к Шеф-ИИ...",
        enTitle = "Connecting to Chef AI...",
        ruDetail = "Передача запроса и данных рецепта",
        enDetail = "Sending request and recipe details",
        startPercent = 0,
        endPercent = 25
    ),
    ProgressStage(
        iconEmoji = "🧠",
        ruTitle = "Анализ рецепта и ингредиентов...",
        enTitle = "Analyzing recipe & ingredients...",
        ruDetail = "Распознавание продуктов, единиц и дозировок",
        enDetail = "Parsing products, units and quantities",
        startPercent = 25,
        endPercent = 55
    ),
    ProgressStage(
        iconEmoji = "⏱️",
        ruTitle = "Формирование шагов и таймеров...",
        enTitle = "Generating steps & timers...",
        ruDetail = "Расчет порядка готовки и времени",
        enDetail = "Calculating cooking order and time",
        startPercent = 55,
        endPercent = 80
    ),
    ProgressStage(
        iconEmoji = "✨",
        ruTitle = "Финализация структуры рецепта...",
        enTitle = "Finalizing recipe structure...",
        ruDetail = "Упаковка рецепта в карточку блюда",
        enDetail = "Packaging recipe into dish card",
        startPercent = 80,
        endPercent = 95
    ),
    ProgressStage(
        iconEmoji = "✅",
        ruTitle = "Готово!",
        enTitle = "Done!",
        ruDetail = "Поля рецепта успешно структурированы!",
        enDetail = "Recipe fields successfully structured!",
        startPercent = 100,
        endPercent = 100
    )
)

@Composable
fun AiGenerationProgressBox(
    modifier: Modifier = Modifier,
    isCompleted: Boolean = false
) {
    var stageIndex by remember { mutableIntStateOf(0) }
    var percent by remember { mutableIntStateOf(0) }
    var tipIndex by remember { mutableIntStateOf(0) }

    val culinaryTips = remember {
        listOf(
            AppLocaleManager.tr("💡 Совет: Встроенные таймеры в шагах активируются в 1 клик в режиме готовки!", "💡 Tip: Built-in timers in steps are activated in 1 click in cooking mode!"),
            AppLocaleManager.tr("💡 Совет: Шеф-Помощник автоматически разбивает продукты по категориям.", "💡 Tip: Chef Assistant automatically categorizes products into sections."),
            AppLocaleManager.tr("💡 Совет: В режиме готовки экран смартфона автоматически остается включенным!", "💡 Tip: In cooking mode, your smartphone screen stays on automatically!"),
            AppLocaleManager.tr("💡 Совет: Вы можете делиться своими рецептами через облачный код доступа.", "💡 Tip: You can share your recipes via cloud access code.")
        )
    }

    // Smooth stage progression with readable display time per phase (>= 1000ms per stage)
    LaunchedEffect(isCompleted) {
        if (!isCompleted) {
            // Stage 0: 0 -> 25%
            stageIndex = 0
            for (p in STAGES[0].startPercent..STAGES[0].endPercent) {
                percent = p
                delay(35L)
            }
            delay(800L) // Guarantee stage 0 stays visible long enough to read

            // Stage 1: 25 -> 55%
            stageIndex = 1
            for (p in STAGES[1].startPercent..STAGES[1].endPercent) {
                percent = p
                delay(35L)
            }
            delay(1000L) // Guarantee stage 1 stays visible

            // Stage 2: 55 -> 80%
            stageIndex = 2
            for (p in STAGES[2].startPercent..STAGES[2].endPercent) {
                percent = p
                delay(35L)
            }
            delay(1000L) // Guarantee stage 2 stays visible

            // Stage 3: 80 -> 95%
            stageIndex = 3
            for (p in STAGES[3].startPercent..STAGES[3].endPercent) {
                percent = p
                delay(40L)
            }
        } else {
            // Transition smoothly to completion
            percent = 100
            stageIndex = 4
        }
    }

    // Completion handler
    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            percent = 100
            stageIndex = 4
        }
    }

    // Rotate culinary tips periodically
    LaunchedEffect(isCompleted) {
        while (!isCompleted) {
            delay(3500L)
            tipIndex = (tipIndex + 1) % culinaryTips.size
        }
    }

    val currentStage = STAGES.getOrElse(stageIndex) { STAGES.first() }

    val animatedProgress by animateFloatAsState(
        targetValue = percent / 100f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "progressAnimation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulsingScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsingScale"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Pulsing Glow Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(68.dp)
                    .scale(pulsingScale)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.tertiaryContainer
                            )
                        )
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Animated Status Text (only animates when stageIndex changes, avoiding flickering!)
            AnimatedContent(
                targetState = currentStage,
                transitionSpec = {
                    (slideInVertically { height -> height } + fadeIn()) togetherWith
                            (slideOutVertically { height -> -height } + fadeOut())
                },
                label = "statusTransition"
            ) { stage ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stage.iconEmoji,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stage.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }

                    Text(
                        text = stage.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            // Progress Bar & Percentage
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (percent == 100) AppLocaleManager.tr("Завершено!", "Completed!") else AppLocaleManager.tr("Шеф-Помощник в процессе...", "Chef Assistant in progress..."),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$percent%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Culinary Tip Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = culinaryTips[tipIndex % culinaryTips.size],
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
