package com.khalid.vyntra.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Lightweight Canvas-drawn sparkline. No labels, no axes — just the line
 * shape for at-a-glance trend context inside a card. Animates from a flat
 * baseline to the real curve on first composition so the value "draws in".
 *
 * @param values    One Y value per X step (oldest → newest).
 * @param lineColor Stroke color for the line.
 * @param fillColor Optional fill below the line; pass [Color.Transparent] to skip.
 */
@Composable
fun Sparkline(
    values: List<Double>,
    modifier: Modifier = Modifier,
    lineColor: Color,
    fillColor: Color = Color.Transparent,
    strokeWidthPx: Float = 4f
) {
    if (values.size < 2) return

    // Animate 0..1 once on mount → controls the path's vertical "rise".
    val progress = remember { Animatable(0f) }
    LaunchedEffect(values) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMillis = 700))
    }

    val minV = values.min()
    val maxV = values.max()
    val range = (maxV - minV).coerceAtLeast(1.0) // avoid /0 when flat

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stepX = w / (values.size - 1).toFloat()

        val pts = values.mapIndexed { i, v ->
            val ratio = ((v - minV) / range).toFloat()
            val y = h - ratio * h * progress.value - (1f - progress.value) * h / 2f
            Offset(i * stepX, y.coerceIn(0f, h))
        }

        // Filled area underneath the line — adds depth, very subtle.
        if (fillColor != Color.Transparent) {
            val fillPath = Path().apply {
                moveTo(pts.first().x, h)
                pts.forEach { lineTo(it.x, it.y) }
                lineTo(pts.last().x, h)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(fillColor, Color.Transparent),
                    startY = 0f,
                    endY = h
                )
            )
        }

        // The line itself
        val linePath = Path().apply {
            moveTo(pts.first().x, pts.first().y)
            pts.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = strokeWidthPx)
        )
    }
}
