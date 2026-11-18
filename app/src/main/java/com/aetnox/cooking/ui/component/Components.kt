package com.aetnox.cooking.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aetnox.cooking.data.entity.RecipeEntity
import com.aetnox.cooking.ui.theme.StatusArchivedColor
import com.aetnox.cooking.ui.theme.StatusNeedsWorkColor
import com.aetnox.cooking.ui.theme.StatusPlannedColor
import com.aetnox.cooking.ui.theme.StatusVerifiedColor
import com.aetnox.cooking.ui.util.AppLocaleManager
import java.util.Locale

enum class ViewMode {
    LIST, GRID
}

@Composable
fun RecipeStatusChip(
    status: String,
    modifier: Modifier = Modifier
) {
    val (color, icon, text) = when (status) {
        RecipeEntity.STATUS_PLANNED -> Triple(StatusPlannedColor, Icons.Default.HourglassTop, AppLocaleManager.tr("В планах", "Planned"))
        RecipeEntity.STATUS_VERIFIED -> Triple(StatusVerifiedColor, Icons.Default.CheckCircle, AppLocaleManager.tr("Проверено", "Verified"))
        RecipeEntity.STATUS_NEEDS_WORK -> Triple(StatusNeedsWorkColor, Icons.Default.Build, AppLocaleManager.tr("Над ошибками", "Needs work"))
        RecipeEntity.STATUS_ARCHIVED -> Triple(StatusArchivedColor, Icons.Default.Archive, AppLocaleManager.tr("Архив", "Archive"))
        else -> Triple(MaterialTheme.colorScheme.primary, Icons.Default.CheckCircle, status)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TimeAndCountBadge(
    totalMinutes: Int,
    ingredientsCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (totalMinutes > 0) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
            ) {
                Text(
                    text = "⏱ $totalMinutes ${AppLocaleManager.tr("мин", "min")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        if (ingredientsCount > 0) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
            ) {
                Text(
                    text = "🛒 $ingredientsCount ${AppLocaleManager.tr("ингред.", "ingred.")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
fun StarRatingBar(
    rating: Int,
    modifier: Modifier = Modifier,
    onRatingChanged: ((Int) -> Unit)? = null,
    starSize: Int = 20
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        (1..5).forEach { starIndex ->
            val isSelected = starIndex <= rating
            IconButton(
                onClick = { onRatingChanged?.invoke(if (rating == starIndex) 0 else starIndex) },
                enabled = onRatingChanged != null,
                modifier = Modifier.size((starSize + 8).dp)
            ) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.Star else Icons.Outlined.Star,
                    contentDescription = "$starIndex ${AppLocaleManager.tr("звезд", "stars")}",
                    tint = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(starSize.dp)
                )
            }
        }
    }
}

@Composable
fun ExpandableSectionCard(
    title: String,
    icon: ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (!subtitle.isNullOrBlank()) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun ViewModeToggle(
    currentMode: ViewMode,
    onModeSelected: (ViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(modifier = Modifier.padding(2.dp)) {
            IconButton(
                onClick = { onModeSelected(ViewMode.LIST) },
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (currentMode == ViewMode.LIST) MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ViewList,
                    contentDescription = AppLocaleManager.tr("Список", "List"),
                    tint = if (currentMode == ViewMode.LIST) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = { onModeSelected(ViewMode.GRID) },
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (currentMode == ViewMode.GRID) MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.GridView,
                    contentDescription = AppLocaleManager.tr("Сетка", "Grid"),
                    tint = if (currentMode == ViewMode.GRID) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun StepTimerCard(
    secondsRemaining: Int,
    isRunning: Boolean,
    isFinished: Boolean,
    onStartPause: () -> Unit,
    onReset: () -> Unit,
    onAddSeconds: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val hrs = secondsRemaining / 3600
    val mins = (secondsRemaining % 3600) / 60
    val secs = secondsRemaining % 60
    val timeFormatted = if (hrs > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    }

    val cardBg = if (isFinished) {
        MaterialTheme.colorScheme.errorContainer
    } else if (isRunning) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val textColor = if (isFinished) {
        MaterialTheme.colorScheme.onErrorContainer
    } else if (isRunning) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isFinished) AppLocaleManager.tr("🔔 ТАЙМЕР ЗАВЕРШЁН!", "🔔 TIMER COMPLETED!") else if (isRunning) AppLocaleManager.tr("⏱ ИДЁТ ОТСЧЁТ...", "⏱ COUNTING DOWN...") else AppLocaleManager.tr("⏱ ТАЙМЕР", "⏱ TIMER"),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(
                        onClick = { onAddSeconds(60) },
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(AppLocaleManager.tr("+1 мин", "+1 min"), style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(
                        onClick = { onAddSeconds(300) },
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(AppLocaleManager.tr("+5 мин", "+5 min"), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = timeFormatted,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isFinished) {
                    Button(
                        onClick = onReset,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(AppLocaleManager.tr("Готово (Сбросить)", "Done (Reset)"), fontWeight = FontWeight.Bold)
                    }
                } else {
                    FilledTonalButton(
                        onClick = onStartPause,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isRunning) AppLocaleManager.tr("Пауза", "Pause") else AppLocaleManager.tr("Старт", "Start"))
                    }

                    IconButton(
                        onClick = onReset,
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = AppLocaleManager.tr("Сброс", "Reset")
                        )
                    }
                }
            }
        }
    }
}
