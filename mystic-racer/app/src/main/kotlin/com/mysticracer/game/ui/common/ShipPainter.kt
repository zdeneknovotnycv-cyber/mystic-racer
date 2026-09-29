package com.mysticracer.game.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform

// Tvary lodi v jednotkových souřadnicích (délka ~ 56, nos míří nahoru).
private val hullPath = Path().apply {
    moveTo(0f, -30f); lineTo(8f, -10f); lineTo(14f, 12f); lineTo(10f, 24f)
    lineTo(-10f, 24f); lineTo(-14f, 12f); lineTo(-8f, -10f); close()
}
private val finLeft = Path().apply {
    moveTo(-9f, 2f); lineTo(-23f, 18f); lineTo(-21f, 27f); lineTo(-10f, 22f); close()
}
private val finRight = Path().apply {
    moveTo(9f, 2f); lineTo(23f, 18f); lineTo(21f, 27f); lineTo(10f, 22f); close()
}
private val cockpitPath = Path().apply {
    moveTo(0f, -16f); lineTo(5f, -3f); lineTo(0f, 5f); lineTo(-5f, -3f); close()
}

/**
 * Nakreslí sci-fi hover-auto. [scale] je počet pixelů na jednotku, [tiltDeg] náklon,
 * [flame] 0..1 síla plamene motorů.
 */
fun DrawScope.drawShip(
    cx: Float,
    cy: Float,
    scale: Float,
    tiltDeg: Float,
    accent: Color,
    flame: Float,
    alpha: Float = 1f,
) {
    withTransform({
        translate(left = cx, top = cy)
        rotate(degrees = tiltDeg, pivot = Offset.Zero)
        scale(scaleX = scale, scaleY = scale, pivot = Offset.Zero)
    }) {
        // záře pod lodí
        drawCircle(color = accent.copy(alpha = 0.16f * alpha), radius = 46f, center = Offset(0f, 4f))
        // plameny motorů
        val len = 8f + 16f * flame
        for (ex in floatArrayOf(-6f, 6f)) {
            drawLine(
                color = Color(0xFFFF9A3D).copy(alpha = 0.85f * alpha),
                start = Offset(ex, 24f), end = Offset(ex, 24f + len),
                strokeWidth = 6f, cap = StrokeCap.Round,
            )
            drawLine(
                color = Color.White.copy(alpha = 0.9f * alpha),
                start = Offset(ex, 24f), end = Offset(ex, 24f + len * 0.5f),
                strokeWidth = 2.5f, cap = StrokeCap.Round,
            )
        }
        // křídla
        drawPath(finLeft, color = accent.copy(alpha = 0.85f * alpha))
        drawPath(finRight, color = accent.copy(alpha = 0.85f * alpha))
        // trup
        drawPath(
            hullPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFEAFBFF), accent),
                startY = -30f, endY = 24f,
            ),
            alpha = alpha,
        )
        drawPath(hullPath, color = accent.copy(alpha = alpha), style = Stroke(width = 1.6f))
        // kokpit
        drawPath(cockpitPath, color = Color(0xFF0B1030).copy(alpha = alpha))
        drawPath(cockpitPath, color = Color(0xFF7DF9FF).copy(alpha = alpha), style = Stroke(width = 1.2f))
        // světla na křídlech
        drawCircle(color = Color(0xFFFF4D6D).copy(alpha = alpha), radius = 1.8f, center = Offset(-21f, 24f))
        drawCircle(color = Color(0xFF7DFFB0).copy(alpha = alpha), radius = 1.8f, center = Offset(21f, 24f))
    }
}

/** Zobrazení lodi do Compose layoutu (menu, výsledky). */
@androidx.compose.runtime.Composable
fun ShipPreview(modifier: androidx.compose.ui.Modifier, accent: Color, bob: Float, flame: Float) {
    Canvas(modifier = modifier) {
        val s = size.minDimension / 110f
        drawShip(
            cx = size.width / 2f,
            cy = size.height / 2f + bob * s * 5f,
            scale = s,
            tiltDeg = bob * 4f,
            accent = accent,
            flame = flame,
        )
    }
}
