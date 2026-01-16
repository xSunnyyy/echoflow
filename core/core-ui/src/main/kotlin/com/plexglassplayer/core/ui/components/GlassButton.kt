package com.plexglassplayer.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.plexglassplayer.core.ui.theme.GlassTheme

enum class GlassButtonStyle {
    PRIMARY,    // Glass + accent stroke
    SECONDARY,  // Glass + neutral stroke
    GHOST       // Text only + subtle pressed background
}

@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: GlassButtonStyle = GlassButtonStyle.PRIMARY,
    shape: Shape = RoundedCornerShape(20.dp),
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val glassColors = GlassTheme.colors
    val accentColor = MaterialTheme.colorScheme.primary

    val backgroundColor = when (style) {
        GlassButtonStyle.PRIMARY -> glassColors.surface
        GlassButtonStyle.SECONDARY -> glassColors.surface
        GlassButtonStyle.GHOST -> Color.Transparent
    }

    val borderColor = when (style) {
        GlassButtonStyle.PRIMARY -> accentColor
        GlassButtonStyle.SECONDARY -> glassColors.stroke
        GlassButtonStyle.GHOST -> Color.Transparent
    }

    Row(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .then(
                if (style != GlassButtonStyle.GHOST) {
                    Modifier.border(
                        width = 1.dp,
                        color = borderColor,
                        shape = shape
                    )
                } else {
                    Modifier
                }
            )
            .clickable(
                enabled = enabled,
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple()
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: GlassButtonStyle = GlassButtonStyle.PRIMARY,
    enabled: Boolean = true
) {
    GlassButton(
        onClick = onClick,
        modifier = modifier,
        style = style,
        enabled = enabled
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
