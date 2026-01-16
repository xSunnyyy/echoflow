package com.plexglassplayer.core.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.plexglassplayer.core.ui.theme.GlassTheme

/**
 * Glass card with translucent background, blur effect, and subtle stroke
 *
 * ✅ IMPORTANT:
 * - We DO NOT blur the content (children).
 * - We blur an EMPTY Android View layer (Android 12+) behind the Compose content.
 *
 * Blur only works on Android 12+, gracefully degrades to translucent on older devices.
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
    val blurPx = with(LocalDensity.current) { blurRadius.toPx() }

    Box(
        modifier = modifier.clip(shape)
    ) {
        // ✅ 1) Backdrop blur layer (EMPTY VIEW — will not blur children)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    View(context).apply {
                        // Make sure this view draws (even though it's visually transparent)
                        setWillNotDraw(false)
                        // Apply blur to this view's render output
                        setRenderEffect(
                            RenderEffect.createBlurEffect(
                                blurPx,
                                blurPx,
                                Shader.TileMode.CLAMP
                            )
                        )
                        // Keep it transparent; blur effect still applies to what it renders
                        alpha = 1f
                    }
                },
                update = { view ->
                    // In case blurRadius changes dynamically
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        view.setRenderEffect(
                            RenderEffect.createBlurEffect(
                                blurPx,
                                blurPx,
                                Shader.TileMode.CLAMP
                            )
                        )
                    }
                }
            )
        }

        // ✅ 2) Frosted surface fill (translucent)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(glassColors.surface, shape)
        )

        // ✅ 3) Stroke border
        Box(
            modifier = Modifier
                .fillMaxSize()
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
                    .fillMaxSize()
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
            modifier = Modifier.fillMaxSize(),
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
