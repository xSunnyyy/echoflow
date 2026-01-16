package com.plexglassplayer.core.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.plexglassplayer.core.ui.theme.GlassTheme

/**
 * Glass card with translucent background, blur effect, and subtle stroke
 *
 * Blur only works on Android 12+, gracefully degrades to translucent on older devices
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    blurRadius: Dp = 16.dp,
    showHighlight: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val glassColors = GlassTheme.colors

    Box(
        modifier = modifier
            .then(
                // Apply blur only on Android 12+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Modifier.blur(blurRadius)
                } else {
                    Modifier
                }
            )
            .clip(shape)
            .background(glassColors.surface)
            .border(
                width = 1.dp,
                color = glassColors.stroke,
                shape = shape
            )
            .then(
                // Add highlight gradient on top-left if enabled
                if (showHighlight) {
                    Modifier.background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                glassColors.highlight,
                                Color.Transparent
                            )
                        ),
                        shape = shape
                    )
                } else {
                    Modifier
                }
            )
    ) {
        content()
    }
}

/**
 * Small glass card variant for chips and compact elements
 */
@Composable
fun GlassChip(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        blurRadius = 12.dp,
        showHighlight = false,
        content = content
    )
}
