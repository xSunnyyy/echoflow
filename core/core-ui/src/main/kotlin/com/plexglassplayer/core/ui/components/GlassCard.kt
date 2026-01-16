package com.plexglassplayer.core.ui.components

import android.os.Build
import android.view.RenderEffect
import android.view.Shader
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.plexglassplayer.core.ui.theme.GlassTheme

/**
 * Glass card with translucent background, blur effect, and subtle stroke
 *
 * ✅ IMPORTANT:
 * - We DO NOT blur the content (children).
 * - Instead, we blur an EMPTY background layer and draw content above it.
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
            .clip(shape)
    ) {
        // ✅ 1) Backdrop blur layer (empty layer — will not blur children)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        renderEffect = RenderEffect.createBlurEffect(
                            blurRadius.value,
                            blurRadius.value,
                            Shader.TileMode.CLAMP
                        )
                    }
            )
        }

        // ✅ 2) Frosted surface fill (translucent)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(glassColors.surface, shape)
        )

        // ✅ 3) Stroke border
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 1.dp,
                    color = glassColors.stroke,
                    shape = shape
                )
        )

        // ✅ 4) Optional highlight gradient overlay (top-left)
        if (showHighlight) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                glassColors.highlight,
                                Color.Transparent
                            )
                        ),
                        shape = shape
                    )
            )
        }

        // ✅ 5) Content (NOT blurred)
        Box(
            modifier = Modifier.matchParentSize(),
            content = content
        )
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
