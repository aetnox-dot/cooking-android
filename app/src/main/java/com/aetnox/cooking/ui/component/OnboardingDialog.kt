package com.aetnox.cooking.ui.component

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aetnox.cooking.ui.theme.ThemeManager
import com.aetnox.cooking.ui.util.AppLocaleManager
import kotlinx.coroutines.delay

private data class InteractiveSlideData(
    val icon: ImageVector,
    val iconBgColor: Color,
    val ruStepTitle: String,
    val enStepTitle: String,
    val ruHeadline: String,
    val enHeadline: String,
    val ruCaption: String,
    val enCaption: String,
    val interactiveContentType: SlideType
) {
    val stepTitle: String get() = AppLocaleManager.tr(ruStepTitle, enStepTitle)
    val headline: String get() = AppLocaleManager.tr(ruHeadline, enHeadline)
    val caption: String get() = AppLocaleManager.tr(ruCaption, enCaption)
}

private enum class SlideType {
    WELCOME_DEMO,
    SEARCH_DEMO,
    COOK_MODE_DEMO,
    AI_COOK_DEMO,
    EDITOR_DEMO,
    BACKUP_DEMO,
    FINAL_START
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenSampleRecipe: () -> Unit = {}
) {
    var currentSlideIndex by remember { mutableIntStateOf(0) }

    val slides = remember {
        listOf(
            InteractiveSlideData(
                icon = Icons.Default.RestaurantMenu,
                iconBgColor = Color(0xFFFFE0B2),
                ruStepTitle = "Шаг 1 из 7 • Настройка",
                enStepTitle = "Step 1 of 7 • Setup",
                ruHeadline = "Тема, Язык и Иконка 🎨",
                enHeadline = "Theme, Language & Icon 🎨",
                ruCaption = "Выберите тему, язык и иконку. По умолчанию всё системное, но для Nothing OS предвыбран их стиль!",
                enCaption = "Select theme, language and icon. Defaults are system, but Nothing OS preselects Nothing style!",
                interactiveContentType = SlideType.WELCOME_DEMO
            ),
            InteractiveSlideData(
                icon = Icons.Default.Search,
                iconBgColor = Color(0xFFE1BEE7),
                ruStepTitle = "Шаг 2 из 7 • Каталог & ИИ",
                enStepTitle = "Step 2 of 7 • Catalog & AI",
                ruHeadline = "Умный поиск и ИИ 🔍",
                enHeadline = "Smart Search & AI 🔍",
                ruCaption = "Нажимайте на продукты, чтобы моментально отфильтровать рецепты!",
                enCaption = "Tap products to instantly filter recipes!",
                interactiveContentType = SlideType.SEARCH_DEMO
            ),
            InteractiveSlideData(
                icon = Icons.Default.SoupKitchen,
                iconBgColor = Color(0xFFC8E6C9),
                ruStepTitle = "Шаг 3 из 7 • Шеф-режим",
                enStepTitle = "Step 3 of 7 • Chef Mode",
                ruHeadline = "Порции и Авто-таймеры ⏱️",
                enHeadline = "Servings & Timers ⏱️",
                ruCaption = "Меняйте число порций кнопками «+ / -» — граммовки пересчитаются!",
                enCaption = "Change servings with '+ / -' — quantities recalculate automatically!",
                interactiveContentType = SlideType.COOK_MODE_DEMO
            ),
            InteractiveSlideData(
                icon = Icons.Default.SmartToy,
                iconBgColor = Color(0xFFBBDEFB),
                ruStepTitle = "Шаг 4 из 7 • Интерактивный Су-шеф",
                enStepTitle = "Step 4 of 7 • Interactive Sous-Chef",
                ruHeadline = "ИИ-Помощник при готовке 🤖",
                enHeadline = "AI Cooking Assistant 🤖",
                ruCaption = "Выбирайте вопросы ниже — ИИ предложит таймер или замену!",
                enCaption = "Pick questions below — AI will suggest a timer or substitution!",
                interactiveContentType = SlideType.AI_COOK_DEMO
            ),
            InteractiveSlideData(
                icon = Icons.Default.AutoAwesome,
                iconBgColor = Color(0xFFFFD180),
                ruStepTitle = "Шаг 5 из 7 • Создание рецепта",
                enStepTitle = "Step 5 of 7 • Recipe Creation",
                ruHeadline = "Умный Редактор 📝",
                enHeadline = "Smart Editor 📝",
                ruCaption = "Нажмите «Заполнить через ИИ», чтобы разбить сырой текст по секциям!",
                enCaption = "Tap 'Fill via AI' to split raw text into structured sections!",
                interactiveContentType = SlideType.EDITOR_DEMO
            ),
            InteractiveSlideData(
                icon = Icons.Default.CloudSync,
                iconBgColor = Color(0xFFD1C4E9),
                ruStepTitle = "Шаг 6 из 7 • Бэкапы и Экспорт",
                enStepTitle = "Step 6 of 7 • Backups & Export",
                ruHeadline = "Сохранность 100% 💾",
                enHeadline = "100% Safety 💾",
                ruCaption = "Проверьте кнопки экспорта и бэкапа прямо в обучении!",
                enCaption = "Test export and backup buttons right in tutorial!",
                interactiveContentType = SlideType.BACKUP_DEMO
            ),
            InteractiveSlideData(
                icon = Icons.Default.CheckCircle,
                iconBgColor = Color(0xFFA5D6A7),
                ruStepTitle = "Шаг 7 из 7 • Все готово!",
                enStepTitle = "Step 7 of 7 • All set!",
                ruHeadline = "Вы готовы творить! 🚀",
                enHeadline = "You are ready to create! 🚀",
                ruCaption = "Откройте образец брауни с вишней прямо сейчас!",
                enCaption = "Open cherry brownie sample right now!",
                interactiveContentType = SlideType.FINAL_START
            )
        )
    }

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
                .fillMaxWidth(0.94f)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // TOP HEADER: STEP BADGE + SKIP BUTTON
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                    ) {
                        Text(
                            text = currentSlide.stepTitle,
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
                            text = AppLocaleManager.tr("Пропустить", "Skip"),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = AppLocaleManager.tr("Пропустить", "Skip"),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

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
                            .padding(vertical = 4.dp)
                    ) {
                        // TITLE ROW
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(slide.iconBgColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = slide.icon,
                                    contentDescription = null,
                                    tint = Color.Black.copy(alpha = 0.8f),
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = slide.headline,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = slide.caption,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // REALISTIC INTERACTIVE MOCK COMPONENT PREVIEW
                        when (slide.interactiveContentType) {
                            SlideType.WELCOME_DEMO -> MockInitialSetupDemo()
                            SlideType.SEARCH_DEMO -> MockSearchDemo()
                            SlideType.COOK_MODE_DEMO -> MockCookModeDemo()
                            SlideType.AI_COOK_DEMO -> MockAiInteractiveDemo()
                            SlideType.EDITOR_DEMO -> MockEditorDemo()
                            SlideType.BACKUP_DEMO -> MockBackupExportDemo()
                            SlideType.FINAL_START -> MockFinalStartDemo(
                                onOpenSample = {
                                    onDismissRequest()
                                    onOpenSampleRecipe()
                                },
                                onDismiss = onDismissRequest
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

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

                Spacer(modifier = Modifier.height(12.dp))

                // BOTTOM ACTION BUTTONS
                if (isLastSlide) {
                    Button(
                        onClick = {
                            onDismissRequest()
                            onOpenSampleRecipe()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = AppLocaleManager.tr("🍫 Открыть «Брауни» в режиме «Готовить»", "🍫 Open 'Brownie' in 'Cook' mode"),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
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
                                contentDescription = AppLocaleManager.tr("Назад", "Back"),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(AppLocaleManager.tr("Назад", "Back"))
                        }

                        Button(
                            onClick = {
                                if (currentSlideIndex < slides.size - 1) currentSlideIndex++
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(AppLocaleManager.tr("Далее", "Next"))
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = AppLocaleManager.tr("Вперед", "Next"),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Slide 1 Interactive Setup: Theme, Language, Icon Selector with Live Icon Preview & Apply Button
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MockInitialSetupDemo() {
    val context = LocalContext.current
    val isNothingDevice = remember { com.aetnox.cooking.data.remote.DeviceUtils.isNothingDevice() }

    var selectedTheme by remember {
        mutableStateOf(
            if (isNothingDevice && ThemeManager.currentTheme.value == com.aetnox.cooking.ui.theme.AppTheme.SYSTEM) {
                com.aetnox.cooking.ui.theme.AppTheme.NOTHING
            } else {
                ThemeManager.currentTheme.value
            }
        )
    }

    var selectedLanguage by remember {
        mutableStateOf(AppLocaleManager.currentLanguage.value)
    }

    var selectedIconVariant by remember {
        mutableStateOf(
            if (isNothingDevice && com.aetnox.cooking.ui.util.AppIconManager.currentIconVariant.value == com.aetnox.cooking.ui.util.AppIconVariant.DEFAULT) {
                com.aetnox.cooking.ui.util.AppIconVariant.NOTHING
            } else {
                com.aetnox.cooking.ui.util.AppIconManager.currentIconVariant.value
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (isNothingDevice) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = AppLocaleManager.tr(
                                "Устройство Nothing OS — фирменная тема и иконка выбраны по умолчанию!",
                                "Nothing OS device — Nothing theme & icon selected by default!"
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Theme selector
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "🎨 " + AppLocaleManager.tr("Тема оформления:", "App Theme:"),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    com.aetnox.cooking.ui.theme.AppTheme.entries.forEach { theme ->
                        val isSelected = selectedTheme == theme
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedTheme = theme
                                if (theme == com.aetnox.cooking.ui.theme.AppTheme.NOTHING) {
                                    selectedIconVariant = com.aetnox.cooking.ui.util.AppIconVariant.NOTHING
                                }
                            },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(theme.label, style = MaterialTheme.typography.labelSmall)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    val swatches = com.aetnox.cooking.ui.theme.themePreviewSwatches(theme)
                                    swatches.take(2).forEach { color ->
                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 1.dp)
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // Language selector
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "🌐 " + AppLocaleManager.tr("Язык приложения:", "App Language:"),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    com.aetnox.cooking.ui.util.AppLanguage.entries.forEach { lang ->
                        val isSelected = selectedLanguage == lang
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedLanguage = lang },
                            label = {
                                Text(lang.label, style = MaterialTheme.typography.labelSmall)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Icon selector
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "🖼️ " + AppLocaleManager.tr("Иконка приложения:", "App Icon:"),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    com.aetnox.cooking.ui.util.AppIconVariant.entries.forEach { iconVariant ->
                        val isSelected = selectedIconVariant == iconVariant
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedIconVariant = iconVariant },
                            label = {
                                Text(iconVariant.label, style = MaterialTheme.typography.labelSmall)
                            }
                        )
                    }
                }
            }

            // Live Icon Preview ("предпросмотр")
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "📱 " + AppLocaleManager.tr("Предпросмотр иконки", "Icon Preview"),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    val previewResId = when (selectedIconVariant) {
                        com.aetnox.cooking.ui.util.AppIconVariant.DEFAULT -> com.aetnox.cooking.R.mipmap.ic_launcher
                        com.aetnox.cooking.ui.util.AppIconVariant.NEON -> com.aetnox.cooking.R.mipmap.ic_launcher_neon
                        com.aetnox.cooking.ui.util.AppIconVariant.NIGHT_CHEF -> com.aetnox.cooking.R.mipmap.ic_launcher_night_chef
                        com.aetnox.cooking.ui.util.AppIconVariant.NOTHING -> com.aetnox.cooking.R.mipmap.ic_launcher_nothing
                    }

                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (selectedIconVariant == com.aetnox.cooking.ui.util.AppIconVariant.NOTHING) Color.Black
                                else MaterialTheme.colorScheme.primaryContainer
                            )
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = previewResId),
                            contentDescription = selectedIconVariant.label,
                            modifier = Modifier.size(52.dp)
                        )
                    }

                    Text(
                        text = selectedIconVariant.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Apply Button ("Применить")
            Button(
                onClick = {
                    ThemeManager.setTheme(context, selectedTheme)
                    com.aetnox.cooking.ui.util.AppIconManager.setAppIcon(context, selectedIconVariant)
                    AppLocaleManager.setLanguage(context, selectedLanguage)
                    Toast.makeText(
                        context,
                        AppLocaleManager.tr("✨ Настройки успешно применены!", "✨ Settings applied successfully!"),
                        Toast.LENGTH_SHORT
                    ).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = AppLocaleManager.tr("Применить настройки", "Apply Settings"),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Slide 2 Interactive Mock: Real-looking Search Bar & Ingredient Chips
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MockSearchDemo() {
    val chChoc = AppLocaleManager.tr("Шоколад", "Chocolate")
    val chEggs = AppLocaleManager.tr("Яйца", "Eggs")
    val chCherry = AppLocaleManager.tr("Вишня", "Cherry")
    val chCream = AppLocaleManager.tr("Сливки", "Cream")
    val chFlour = AppLocaleManager.tr("Мука", "Flour")

    var selectedChips by remember { mutableStateOf(setOf(chChoc, chEggs)) }
    var isAiClicked by remember { mutableStateOf(false) }

    val allChips = listOf(chChoc, chEggs, chCherry, chCream, chFlour)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedChips.isEmpty()) AppLocaleManager.tr("Поиск по названию или ингредиентам...", "Search by title or ingredients...") else selectedChips.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selectedChips.isEmpty()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    if (selectedChips.isNotEmpty()) {
                        IconButton(onClick = { selectedChips = emptySet() }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = AppLocaleManager.tr("Очистить", "Clear"), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Text(
                text = AppLocaleManager.tr("Нажмите на продукты для фильтрации:", "Tap products to filter:"),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                allChips.forEach { chipName ->
                    val isSelected = selectedChips.contains(chipName)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedChips = if (isSelected) selectedChips - chipName else selectedChips + chipName
                        },
                        label = { Text(chipName) }
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedChips.isNotEmpty()) AppLocaleManager.tr("🎯 Найдено: 1 рецепт (Брауни)", "🎯 Found: 1 recipe (Brownie)") else AppLocaleManager.tr("🔍 Выберите продукты выше", "🔍 Select products above"),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Button(
                        onClick = { isAiClicked = !isAiClicked },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(AppLocaleManager.tr("✨ ИИ-Поиск", "✨ AI Search"), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            AnimatedVisibility(visible = isAiClicked) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = AppLocaleManager.tr("🤖 ИИ: Из ваших продуктов идеальнее всего испечь Брауни! Рецепт уже в вашей базе.", "🤖 AI: With your ingredients, Brownie is the best match! It's already in your book."),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Slide 3 Interactive Mock: Live Servings Scaler & Timers
 */
@Composable
private fun MockCookModeDemo() {
    var servings by remember { mutableIntStateOf(4) }
    var isTimerActive by remember { mutableStateOf(false) }
    var timerSecondsLeft by remember { mutableIntStateOf(1500) } // 25:00

    LaunchedEffect(isTimerActive) {
        if (isTimerActive) {
            while (timerSecondsLeft > 0 && isTimerActive) {
                delay(1000)
                timerSecondsLeft--
            }
        }
    }

    val multiplier = servings / 4f
    val chocolateGram = (200 * multiplier).toInt()
    val butterGram = (100 * multiplier).toInt()
    val gUnit = AppLocaleManager.tr("г", "g")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // SERVINGS STEPPER WIDGET
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = AppLocaleManager.tr("⚖️ Перерасчет порций:", "⚖️ Recalculate servings:"),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (servings > 1) servings-- },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = AppLocaleManager.tr("Меньше", "Less"), modifier = Modifier.size(16.dp))
                        }

                        Text(
                            text = "$servings ${AppLocaleManager.tr("порций", "servings")}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )

                        IconButton(
                            onClick = { if (servings < 12) servings++ },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = AppLocaleManager.tr("Больше", "More"), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // DYNAMICALLY SCALED INGREDIENTS
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "🍫 ${AppLocaleManager.tr("Шоколад", "Chocolate")}: ${chocolateGram}$gUnit",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "🧈 ${AppLocaleManager.tr("Масло", "Butter")}: ${butterGram}$gUnit",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            // TIMER RUNNER WIDGET
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = AppLocaleManager.tr("Шаг 3: Выпекать брауни", "Step 3: Bake brownie"),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        val mins = timerSecondsLeft / 60
                        val secs = timerSecondsLeft % 60
                        Text(
                            text = String.format("⏱️ %02d:%02d (%s)", mins, secs, AppLocaleManager.tr("Кликните запуск", "Click start")),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = { isTimerActive = !isTimerActive },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            if (isTimerActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isTimerActive) AppLocaleManager.tr("Пауза", "Pause") else AppLocaleManager.tr("Старт", "Start"), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

/**
 * Slide 4 Interactive Mock: AI Chef Assistant Simulator
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MockAiInteractiveDemo(
    modifier: Modifier = Modifier
) {
    var selectedQuestionIndex by remember { mutableIntStateOf(0) }
    var isTimerStarted by remember { mutableStateOf(false) }
    var isPatchApplied by remember { mutableStateOf(false) }

    val mockQuestions = listOf(
        AppLocaleManager.tr("🥛 Чем заменить сливки?", "🥛 Substitute for cream?"),
        AppLocaleManager.tr("⏱️ Время запекания?", "⏱️ Baking duration?"),
        AppLocaleManager.tr("🔥 Подгорает сверху!", "🔥 Burning on top!")
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "🤖 ${AppLocaleManager.tr("Симулятор Шеф-Помощника", "Chef Assistant Simulator")}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                mockQuestions.forEachIndexed { index, question ->
                    FilterChip(
                        selected = selectedQuestionIndex == index,
                        onClick = {
                            selectedQuestionIndex = index
                            isTimerStarted = false
                            isPatchApplied = false
                        },
                        label = { Text(question, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    when (selectedQuestionIndex) {
                        0 -> {
                            Text(
                                text = AppLocaleManager.tr("💬 «Чем заменить сливки 33%?»", "💬 'Substitute for heavy cream?'"),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = AppLocaleManager.tr("🤖 Возьмите 200г масла + 100мл молока (растопить) либо 250г сметаны 20%.", "🤖 Use 200g butter + 100ml milk (melted) or 250g sour cream 20%."),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        1 -> {
                            Text(
                                text = AppLocaleManager.tr("💬 «Сколько запекать брауни?»", "💬 'How long to bake brownie?'"),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = AppLocaleManager.tr("🤖 Выпекайте при 180°C 25 минут. Нажмите кнопку запуска таймера ниже:", "🤖 Bake at 180°C for 25 minutes. Click timer button below:"),
                                style = MaterialTheme.typography.bodySmall
                            )

                            Button(
                                onClick = { isTimerStarted = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                enabled = !isTimerStarted
                            ) {
                                Icon(Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isTimerStarted) AppLocaleManager.tr("✅ Таймер 25 мин запущен!", "✅ 25 min timer started!") else AppLocaleManager.tr("⏱️ Поставить таймер на 25 мин", "⏱️ Set timer for 25 min"))
                            }
                        }
                        2 -> {
                            Text(
                                text = AppLocaleManager.tr("💬 «Подгорает сверху, что делать?»", "💬 'Burning on top, what to do?'"),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = AppLocaleManager.tr("🤖 Накройте фольгой и снизьте температуру до 165°C.", "🤖 Cover with foil and reduce temp to 165°C."),
                                style = MaterialTheme.typography.bodySmall
                            )

                            Button(
                                onClick = { isPatchApplied = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                enabled = !isPatchApplied
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isPatchApplied) AppLocaleManager.tr("✅ Нагрев 165°C применен!", "✅ Heat 165°C applied!") else AppLocaleManager.tr("⚡ Применить 165°C к рецепту", "⚡ Apply 165°C to recipe"))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Slide 5 Interactive Mock: Recipe Editor AI Auto-fill
 */
@Composable
private fun MockEditorDemo() {
    var isParsed by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "📝 ${AppLocaleManager.tr("Сырые заметки рецепта:", "Raw recipe notes:")}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = AppLocaleManager.tr("«Брауни: 200г шоколада 70%, 100г сливочного масла, 2 яйца. Выпекать 25 мин при 180C.»", "'Brownie: 200g chocolate 70%, 100g butter, 2 eggs. Bake 25 min at 180C.'"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Button(
                onClick = { isParsed = !isParsed },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isParsed) AppLocaleManager.tr("Сбросить", "Reset") else AppLocaleManager.tr("✨ Нажмите: «Заполнить через ИИ»", "✨ Tap: 'Fill via AI'"), fontWeight = FontWeight.Bold)
            }

            AnimatedVisibility(visible = isParsed) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "📌 ${AppLocaleManager.tr("Секция: Для основы", "Section: Base")}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            val gUnit = AppLocaleManager.tr("г", "g")
                            val pcUnit = AppLocaleManager.tr("шт", "pc")
                            Text("• 🍫 ${AppLocaleManager.tr("Шоколад 70%", "Chocolate 70%")} — 200 $gUnit", style = MaterialTheme.typography.bodySmall)
                            Text("• 🧈 ${AppLocaleManager.tr("Сливочное масло", "Butter")} — 100 $gUnit", style = MaterialTheme.typography.bodySmall)
                            Text("• 🥚 ${AppLocaleManager.tr("Яйца куриные", "Eggs")} — 2 $pcUnit", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Slide 6 Interactive Mock: Backup & Export Actions
 */
@Composable
private fun MockBackupExportDemo() {
    val context = LocalContext.current
    var lastActionText by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        lastActionText = AppLocaleManager.tr("📦 Файл резервной копии «backup_cooking.json» упешно создан!", "📦 Backup file 'backup_cooking.json' successfully created!")
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(AppLocaleManager.tr("📦 Бэкап JSON", "📦 JSON Backup"), style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = {
                        lastActionText = AppLocaleManager.tr("📋 Рецепт скопирован в буфер в формате Markdown!", "📋 Recipe copied to clipboard in Markdown format!")
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(AppLocaleManager.tr("📤 Экспорт MD", "📤 Export MD"), style = MaterialTheme.typography.labelSmall)
                }
            }

            AnimatedVisibility(visible = lastActionText != null) {
                lastActionText?.let { msg ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Slide 7 Interactive Mock: Final Start Card
 */
@Composable
private fun MockFinalStartDemo(
    onOpenSample: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Default.Restaurant,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )

            Text(
                text = AppLocaleManager.tr("Приятного аппетита и удачных кулинарных шедевров!", "Bon appétit and happy cooking!"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onOpenSample,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(AppLocaleManager.tr("🍫 Открыть «Брауни» в режиме «Готовить»", "🍫 Open 'Brownie' in 'Cook' mode"), fontWeight = FontWeight.Bold)
            }

            TextButton(onClick = onDismiss) {
                Text(AppLocaleManager.tr("🚀 Перейти к каталогу рецептов", "🚀 Go to recipe catalog"))
            }
        }
    }
}
