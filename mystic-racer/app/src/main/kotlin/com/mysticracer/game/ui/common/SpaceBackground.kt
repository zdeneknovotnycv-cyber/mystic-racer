package com.mysticracer.game.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.mysticracer.game.ui.theme.DeepSpace
import kotlin.math.sin
import kotlin.random.Random

private class BgStar(val x: Float, val y: Float, val r: Float, val speed: Int, val alpha: Float)

/** Animované hvězdné pozadí pro menu obrazovky. */
@Composable
fun SpaceBackground(
    modifier: Modifier = Modifier,
    top: Color = Color(0xFF0B1240),
    bottom: Color = DeepSpace,
    content: @Composable BoxScope.() -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "bg")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(60_000, easing = LinearEasing), RepeatMode.Restart),
        label = "bgT",
    )
    val stars = remember {
        val rnd = Random(7)
        List(90) {
            BgStar(rnd.nextFloat(), rnd.nextFloat(), 0.8f + rnd.nextFloat() * 1.8f, 1 + rnd.nextInt(3), 0.2f + rnd.nextFloat() * 0.6f)
        }
    }
    Box(modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(brush = Brush.verticalGradient(listOf(top, bottom)))
            for (s in stars) {
                val y = ((s.y + t * s.speed) % 1f) * size.height
                val twinkle = 0.7f + 0.3f * sin(t * 600f * s.speed + s.x * 20f)
                drawCircle(
                    color = Color.White.copy(alpha = s.alpha * twinkle),
                    radius = s.r,
                    center = Offset(s.x * size.width, y),
                )
            }
        }
        content()
    }
}
