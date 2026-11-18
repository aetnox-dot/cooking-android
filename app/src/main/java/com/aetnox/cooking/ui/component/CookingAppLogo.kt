package com.aetnox.cooking.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aetnox.cooking.R
import com.aetnox.cooking.ui.util.AppLocaleManager
import com.aetnox.cooking.ui.util.LogoColorOption
import com.aetnox.cooking.ui.util.LogoManager
import com.aetnox.cooking.ui.util.LogoStyle

@Composable
fun CookingAppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    val style = LogoManager.currentStyle.value
    val colorOpt = LogoManager.currentColorOption.value
    val tintColor = colorOpt.color ?: MaterialTheme.colorScheme.primary
    val logoDesc = AppLocaleManager.tr("Логотип Cooking", "Cooking Logo")

    when (style) {
        LogoStyle.ORIGINAL -> {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = logoDesc,
                modifier = modifier.size(size)
            )
        }
        LogoStyle.MONOCHROME -> {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_monochrome),
                contentDescription = logoDesc,
                colorFilter = ColorFilter.tint(tintColor),
                modifier = modifier.size(size)
            )
        }
        LogoStyle.SURFACE_BADGE -> {
            Surface(
                shape = CircleShape,
                color = tintColor.copy(alpha = 0.15f),
                modifier = modifier
            ) {
                Box(
                    modifier = Modifier.padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_monochrome),
                        contentDescription = logoDesc,
                        colorFilter = ColorFilter.tint(tintColor),
                        modifier = Modifier.size(size - 8.dp)
                    )
                }
            }
        }
    }
}
