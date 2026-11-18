package com.aetnox.cooking.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aetnox.cooking.ui.util.ActiveCookingManager
import com.aetnox.cooking.ui.util.AppLocaleManager
import com.aetnox.cooking.ui.viewmodel.StepTimerParser

@Composable
fun ActiveCookingTicker(
    onNavigateToCookMode: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sessionsMap by ActiveCookingManager.activeSessionsMap
    val sessionsList = sessionsMap.values.toList()

    AnimatedVisibility(
        visible = sessionsList.isNotEmpty(),
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        if (sessionsList.isNotEmpty()) {
            val pagerState = rememberPagerState(initialPage = 0, pageCount = { sessionsList.size })

            Column(modifier = Modifier.fillMaxWidth()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    val session = sessionsList.getOrNull(page) ?: return@HorizontalPager
                    val runningTimers = session.activeTimers.values.filter { it.isRunning || it.isFinished }
                    val activeTimer = runningTimers.firstOrNull { it.isRunning } ?: runningTimers.firstOrNull()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .clickable { onNavigateToCookMode(session.recipeId) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Text(
                                        text = "🍳",
                                        modifier = Modifier.padding(6.dp),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = session.recipeTitle,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    val stepSubtitle = if (session.activeStepIndex == 0) {
                                        AppLocaleManager.tr("Подготовка ингредиентов", "Ingredients preparation")
                                    } else {
                                        AppLocaleManager.tr("Шаг ${session.activeStepIndex} из ${session.totalSteps}", "Step ${session.activeStepIndex} of ${session.totalSteps}")
                                    }

                                    Text(
                                        text = stepSubtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // TIMER TICKER CHIP
                            activeTimer?.let { timer ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (timer.isFinished) MaterialTheme.colorScheme.errorContainer
                                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = null,
                                            tint = if (timer.isFinished) MaterialTheme.colorScheme.onErrorContainer
                                            else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (timer.isFinished) AppLocaleManager.tr("Готово!", "Done!") else StepTimerParser.formatTime(timer.secondsRemaining),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (timer.isFinished) MaterialTheme.colorScheme.onErrorContainer
                                            else MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = {
                                                ActiveCookingManager.startOrPauseTimer(
                                                    context,
                                                    session.recipeId,
                                                    timer.stepNumber,
                                                    timer.totalSeconds,
                                                    timer.label
                                                )
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (timer.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = AppLocaleManager.tr("Пауза/Старт", "Pause/Start"),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            IconButton(
                                onClick = { ActiveCookingManager.clearSession(context, session.recipeId) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = AppLocaleManager.tr("Скрыть", "Dismiss"),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                if (sessionsList.size > 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        sessionsList.indices.forEach { index ->
                            val isSelected = pagerState.currentPage == index
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .size(if (isSelected) 8.dp else 6.dp)
                            ) {}
                        }
                    }
                }
            }
        }
    }
}
