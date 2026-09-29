package com.mysticracer.game.ui.game

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import com.mysticracer.game.domain.Biome
import com.mysticracer.game.domain.LevelSpec
import com.mysticracer.game.engine.Entity
import com.mysticracer.game.engine.EntityKind
import com.mysticracer.game.engine.GameEngine
import com.mysticracer.game.engine.GameStatus
import com.mysticracer.game.engine.ParticleKind
import com.mysticracer.game.engine.TRACK_L
import com.mysticracer.game.engine.TRACK_R
import com.mysticracer.game.engine.WORLD_W
import com.mysticracer.game.ui.common.drawShip
import com.mysticracer.game.ui.theme.NeonGold
import com.mysticracer.game.ui.theme.NeonViolet
import com.mysticracer.game.ui.theme.palette
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.random.Random

private const val TAU = 6.2831855f

/**
 * Vykresluje jeden level. Všechny souřadnice světa se převádějí na pixely poměrem
 * `s = šířka / WORLD_W`; loď stojí ve 78 % výšky a svět se pod ní posouvá.
 */
class GameRenderer(private val spec: LevelSpec) {

    private val palette = spec.biome.palette()
    private val path = Path()

    private class Star(val x: Float, val y: Float, val r: Float, val layer: Int, val alpha: Float)

    private val stars: List<Star> = run {
        val rnd = Random(spec.seed)
        List(70) {
            Star(
                rnd.nextFloat(), rnd.nextFloat(), 0.8f + rnd.nextFloat() * 1.6f,
                1 + rnd.nextInt(3), 0.25f + rnd.nextFloat() * 0.6f,
            )
        }
    }

    private fun hash(i: Int, salt: Int): Float {
        var x = i * 374761393 + salt * 668265263 + spec.seed.toInt()
        x = (x xor (x ushr 13)) * 1274126177.toInt()
        x = x xor (x ushr 16)
        return (x and 0x7fffffff) / 2147483647f
    }

    fun DrawScope.render(engine: GameEngine, frameNanos: Long, topInset: Float, dp: Float) {
        val w = size.width
        val h = size.height
        val s = w / WORLD_W
        val shipY = h * 0.78f
        val at = frameNanos / 1_000_000_000f
        val dist = engine.distance

        drawRect(
            brush = Brush.verticalGradient(listOf(palette.bgTop, palette.bgBottom)),
            size = size,
        )

        var ox = 0f
        var oy = 0f
        if (engine.shake > 0f) {
            val k = engine.shake * 22f * dp
            ox = sin(at * 97f) * k * 0.5f
            oy = cos(at * 83f) * k * 0.5f
        }

        translate(left = ox, top = oy) {
            drawStars(dist, s, h, at)
            drawDecor(dist, s, shipY, at)
            drawTrack(engine, s, shipY)
            if (engine.boostTime > 0f) drawSpeedLines(at)
            drawEntities(engine, s, shipY, at)
            drawParticles(engine, s, shipY)
            if (engine.status != GameStatus.LOST) drawPlayer(engine, s, shipY, at)
        }

        // mlha zakrývající výhled dopředu
        if (spec.fog > 0f) {
            val fogH = h * 0.55f
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(palette.fog.copy(alpha = (0.25f + spec.fog * 1.2f).coerceAtMost(0.85f)), Color.Transparent),
                    startY = 0f, endY = fogH,
                ),
                size = Size(w, fogH),
            )
        }

        // tint při obrácení ovládání
        if (engine.isInverted) {
            drawRect(color = NeonViolet.copy(alpha = 0.10f + 0.05f * sin(at * 9f)))
        }

        // ukazatel postupu
        val barX = 16f * dp
        val barW = w - 32f * dp
        val barH = 6f * dp
        val barY = topInset + 6f * dp
        drawRoundRect(
            color = Color.White.copy(alpha = 0.15f),
            topLeft = Offset(barX, barY), size = Size(barW, barH),
            cornerRadius = CornerRadius(barH / 2f),
        )
        drawRoundRect(
            color = palette.accent,
            topLeft = Offset(barX, barY), size = Size(barW * engine.progress, barH),
            cornerRadius = CornerRadius(barH / 2f),
        )
        drawCircle(color = NeonGold, radius = barH * 0.9f, center = Offset(barX + barW, barY + barH / 2f))
    }

    // ---------- pozadí ----------

    private fun DrawScope.drawStars(dist: Float, s: Float, h: Float, at: Float) {
        for (st in stars) {
            val y = (st.y * h + dist * s * 0.05f * st.layer) % h
            val tw = 0.7f + 0.3f * sin(at * 2f + st.x * 10f)
            drawCircle(
                color = Color.White.copy(alpha = st.alpha * tw),
                radius = st.r,
                center = Offset(st.x * size.width, y),
            )
        }
    }

    private fun DrawScope.drawDecor(dist: Float, s: Float, shipY: Float, at: Float) {
        val step = 200f
        val dd = dist * 0.55f
        val first = floor((dd - 150f) / step).toInt()
        val last = ceil((dd + shipY / s + 150f) / step).toInt()
        for (i in first..last) {
            val dWorld = i * step + hash(i, 1) * step
            val x = hash(i, 2) * WORLD_W
            val sz = 30f + hash(i, 3) * 70f
            val cx = x * s
            val cy = shipY - (dWorld - dd) * s
            val alt = hash(i, 5) < 0.5f
            when (spec.biome) {
                Biome.NEBULA_DRIFT -> {
                    val c = if (alt) palette.accent2 else palette.accent
                    val rad = sz * s * 1.8f
                    drawCircle(
                        brush = Brush.radialGradient(listOf(c.copy(alpha = 0.20f), Color.Transparent), Offset(cx, cy), rad),
                        radius = rad, center = Offset(cx, cy),
                    )
                }
                Biome.ANCIENT_RUINS -> {
                    val left = hash(i, 4) < 0.5f
                    val px = if (left) 4f * s else (WORLD_W - 4f) * s
                    val pw = 30f * s
                    drawRect(
                        color = palette.accent.copy(alpha = 0.28f),
                        topLeft = Offset(px - pw / 2f, cy - sz * s), size = Size(pw, sz * s * 2f),
                    )
                    drawRect(
                        color = palette.accent.copy(alpha = 0.6f),
                        topLeft = Offset(px - pw / 2f, cy - sz * s), size = Size(pw, sz * s * 2f),
                        style = Stroke(width = 1.5f),
                    )
                    drawLine(
                        palette.accent.copy(alpha = 0.5f),
                        Offset(px - pw / 3f, cy - sz * s * 0.3f), Offset(px + pw / 3f, cy - sz * s * 0.3f), 2f,
                    )
                    drawCircle(
                        color = palette.accent2.copy(alpha = 0.10f),
                        radius = sz * s * 1.2f, center = Offset(cx, cy), style = Stroke(width = 2f),
                    )
                }
                Biome.VOID_RIFT -> {
                    drawCircle(color = Color.Black.copy(alpha = 0.45f), radius = sz * s, center = Offset(cx, cy))
                    drawCircle(
                        color = palette.accent.copy(alpha = 0.45f),
                        radius = sz * s, center = Offset(cx, cy), style = Stroke(width = 2f),
                    )
                    for (k in 0 until 3) {
                        val a = hash(i, 10 + k) * TAU
                        val len = sz * s * 2.2f
                        drawLine(
                            palette.accent.copy(alpha = 0.30f),
                            Offset(cx, cy), Offset(cx + cos(a) * len, cy + sin(a) * len), 2f,
                        )
                    }
                }
                Biome.CRYSTAL_CAVERNS -> {
                    val ex = if (hash(i, 4) < 0.5f) hash(i, 6) * 120f else WORLD_W - hash(i, 6) * 120f
                    val px = ex * s
                    val hh = sz * s * 1.1f
                    val ww = sz * s * 0.45f
                    path.reset()
                    path.moveTo(px, cy - hh); path.lineTo(px + ww, cy); path.lineTo(px, cy + hh * 0.6f); path.lineTo(px - ww, cy); path.close()
                    drawPath(path, palette.accent.copy(alpha = 0.22f))
                    drawPath(path, Color.White.copy(alpha = 0.25f), style = Stroke(width = 1.5f))
                }
                Biome.DARK_STAR -> {
                    val c = if (alt) palette.accent else palette.accent2
                    val rad = sz * s * 1.7f
                    drawCircle(
                        brush = Brush.radialGradient(listOf(c.copy(alpha = 0.20f), Color.Transparent), Offset(cx, cy), rad),
                        radius = rad, center = Offset(cx, cy),
                    )
                    val flick = 0.5f + 0.5f * sin(at * 5f + i)
                    drawCircle(
                        color = palette.accent2.copy(alpha = 0.5f * flick),
                        radius = 2.5f, center = Offset(cx + hash(i, 7) * 60f * s, cy - 30f * s),
                    )
                }
            }
        }
    }

    private fun DrawScope.drawTrack(engine: GameEngine, s: Float, shipY: Float) {
        val h = size.height
        val dist = engine.distance
        val l = TRACK_L * s
        val r = TRACK_R * s
        drawRect(color = Color.Black.copy(alpha = 0.38f), topLeft = Offset(l, 0f), size = Size(r - l, h))
        for (rx in floatArrayOf(l, r)) {
            drawLine(palette.accent.copy(alpha = 0.22f), Offset(rx, 0f), Offset(rx, h), 10f * s)
            drawLine(palette.accent, Offset(rx, 0f), Offset(rx, h), 2.5f * s)
        }

        // přerušované čáry pruhů
        val period = 100f
        val laneW = (TRACK_R - TRACK_L) / 3f
        val kMin = floor((dist - (h - shipY) / s) / period).toInt()
        val kMax = ceil((dist + shipY / s) / period).toInt()
        for (k in kMin..kMax) {
            val cy = shipY - (k * period - dist) * s
            for (lane in 1..2) {
                val x = (TRACK_L + laneW * lane) * s
                drawLine(Color.White.copy(alpha = 0.20f), Offset(x, cy), Offset(x, cy - 40f * s), 3f * s)
            }
        }

        // start a cíl
        drawCheckLine(0f, dist, s, shipY, Color.White.copy(alpha = 0.35f), rows = 1)
        drawCheckLine(engine.length, dist, s, shipY, NeonGold, rows = 2)
    }

    private fun DrawScope.drawCheckLine(d: Float, dist: Float, s: Float, shipY: Float, tint: Color, rows: Int) {
        val cy = shipY - (d - dist) * s
        if (cy < -80f || cy > size.height + 80f) return
        val l = TRACK_L * s
        val r = TRACK_R * s
        val n = 12
        val sq = (r - l) / n
        drawRect(
            color = tint.copy(alpha = 0.18f),
            topLeft = Offset(l, cy - sq * rows - 10f * s), size = Size(r - l, sq * rows * 2f + 20f * s),
        )
        for (row in 0 until rows) {
            for (c in 0 until n) {
                if ((row + c) % 2 == 0) {
                    drawRect(
                        color = tint,
                        topLeft = Offset(l + c * sq, cy - sq * (row + 1)), size = Size(sq, sq),
                    )
                }
            }
        }
    }

    private fun DrawScope.drawSpeedLines(at: Float) {
        val w = size.width
        val h = size.height
        for (k in 0 until 14) {
            val x = hash(k, 9) * w
            val y = ((at * 2200f + hash(k, 10) * h) % (h + 200f)) - 100f
            drawLine(palette.accent.copy(alpha = 0.35f), Offset(x, y), Offset(x, y + 120f), 2f)
        }
    }

    // ---------- objekty ----------

    private fun DrawScope.drawEntities(engine: GameEngine, s: Float, shipY: Float, at: Float) {
        val h = size.height
        val dist = engine.distance
        val ents = engine.entities
        var idx = engine.firstIndexAtOrAfter(dist - (h - shipY) / s - 130f)
        val maxD = dist + shipY / s + 130f
        while (idx < ents.size) {
            val e = ents[idx]
            idx++
            if (e.d > maxD) break
            if (!e.active) continue
            val cy = shipY - (e.d - dist) * s
            when (e.kind) {
                EntityKind.ANOMALY -> drawAnomaly(e, cy, s, at)
                EntityKind.BARRIER -> drawBarrier(e, cy, s)
                EntityKind.DARK_PORTAL -> drawDarkPortal(e, cy, s, at)
                EntityKind.BOOST -> drawBoostGate(e, cy, s, at)
                EntityKind.CRYSTAL -> drawCrystal(e.x * s, cy, e.r * s, at)
                EntityKind.ASTEROID -> drawAsteroid(e, e.curX(engine.time) * s, cy, e.r * s, at)
            }
        }
    }

    private fun DrawScope.drawCrystal(cx: Float, cy: Float, sz: Float, at: Float) {
        val pulse = 0.85f + 0.15f * sin(at * 4f + cx * 0.02f)
        drawCircle(color = palette.crystal.copy(alpha = 0.18f), radius = sz * 2f * pulse, center = Offset(cx, cy))
        val w = sz * 0.62f * (0.75f + 0.25f * abs(cos(at * 2f + cy * 0.01f)))
        path.reset()
        path.moveTo(cx, cy - sz); path.lineTo(cx + w, cy); path.lineTo(cx, cy + sz); path.lineTo(cx - w, cy); path.close()
        drawPath(path, palette.crystal)
        drawPath(path, Color.White.copy(alpha = 0.9f), style = Stroke(width = 1.5f))
    }

    private fun DrawScope.drawAsteroid(e: Entity, cx: Float, cy: Float, r: Float, at: Float) {
        val shape = e.shape ?: return
        val n = shape.size
        val rot = e.phase + at * e.spin
        path.reset()
        for (k in 0 until n) {
            val a = rot + k * (TAU / n)
            val rr = r * shape[k]
            val px = cx + cos(a) * rr
            val py = cy + sin(a) * rr
            if (k == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
        drawPath(path, palette.rock)
        drawPath(path, palette.rockEdge, style = Stroke(width = 2.2f))
        drawCircle(palette.rockEdge.copy(alpha = 0.30f), r * 0.22f, Offset(cx - r * 0.25f, cy - r * 0.15f))
        drawCircle(palette.rockEdge.copy(alpha = 0.22f), r * 0.14f, Offset(cx + r * 0.3f, cy + r * 0.25f))
    }

    private fun DrawScope.drawBarrier(e: Entity, cy: Float, s: Float) {
        val thick = e.r * s
        val gl = (e.x - e.gapHalf) * s
        val gr = (e.x + e.gapHalf) * s
        val w = size.width
        drawRect(palette.wall, Offset(0f, cy - thick), Size(gl, thick * 2f))
        drawRect(palette.wall, Offset(gr, cy - thick), Size(w - gr, thick * 2f))
        drawRect(palette.accent.copy(alpha = 0.85f), Offset(0f, cy - thick), Size(gl, thick * 2f), style = Stroke(width = 2f))
        drawRect(palette.accent.copy(alpha = 0.85f), Offset(gr, cy - thick), Size(w - gr, thick * 2f), style = Stroke(width = 2f))
        // energetické pruhy na zdi
        var x = 8f * s
        while (x < gl - 6f * s) {
            drawLine(palette.accent.copy(alpha = 0.35f), Offset(x, cy - thick * 0.6f), Offset(x + 6f * s, cy + thick * 0.6f), 2f)
            x += 26f * s
        }
        x = gr + 8f * s
        while (x < w - 6f * s) {
            drawLine(palette.accent.copy(alpha = 0.35f), Offset(x, cy - thick * 0.6f), Offset(x + 6f * s, cy + thick * 0.6f), 2f)
            x += 26f * s
        }
        // zářící okraje mezery
        for (gx in floatArrayOf(gl, gr)) {
            drawLine(palette.accent.copy(alpha = 0.3f), Offset(gx, cy - thick * 1.6f), Offset(gx, cy + thick * 1.6f), 8f * s)
            drawCircle(palette.accent, 3.2f * s, Offset(gx, cy))
        }
    }

    private fun DrawScope.drawBoostGate(e: Entity, cy: Float, s: Float, at: Float) {
        val cx = e.x * s
        val r = e.r * s
        val pulse = 0.9f + 0.1f * sin(at * 6f)
        drawCircle(palette.accent.copy(alpha = 0.16f), r * 1.6f * pulse, Offset(cx, cy))
        drawCircle(palette.accent, r * pulse, Offset(cx, cy), style = Stroke(width = 3f * s))
        val a = r * 0.45f
        for (k in 0 until 2) {
            val oy = (k - 0.5f) * a * 1.1f
            path.reset()
            path.moveTo(cx - a, cy + a * 0.4f + oy)
            path.lineTo(cx, cy - a * 0.6f + oy)
            path.lineTo(cx + a, cy + a * 0.4f + oy)
            drawPath(path, Color.White, style = Stroke(width = 3f * s, cap = StrokeCap.Round))
        }
    }

    private fun DrawScope.drawDarkPortal(e: Entity, cy: Float, s: Float, at: Float) {
        val cx = e.x * s
        val r = e.r * s
        drawCircle(NeonViolet.copy(alpha = 0.16f), r * 1.7f, Offset(cx, cy))
        drawCircle(Color(0xFF0A0014), r, Offset(cx, cy))
        drawCircle(NeonViolet, r, Offset(cx, cy), style = Stroke(width = 2.5f * s))
        for (k in 0 until 3) {
            val rr = r * (0.35f + 0.2f * k)
            drawArc(
                color = NeonViolet.copy(alpha = 0.9f),
                startAngle = at * 140f * (k + 1) + k * 120f, sweepAngle = 80f, useCenter = false,
                topLeft = Offset(cx - rr, cy - rr), size = Size(rr * 2f, rr * 2f),
                style = Stroke(width = 2.5f * s, cap = StrokeCap.Round),
            )
        }
    }

    private fun DrawScope.drawAnomaly(e: Entity, cy: Float, s: Float, at: Float) {
        val cx = e.x * s
        val r = e.r * s
        drawCircle(
            brush = Brush.radialGradient(listOf(palette.accent2.copy(alpha = 0.30f), Color.Transparent), Offset(cx, cy), r),
            radius = r, center = Offset(cx, cy),
        )
        for (k in 0 until 4) {
            val rr = r * (0.3f + 0.18f * k)
            drawArc(
                color = palette.accent2.copy(alpha = 0.55f),
                startAngle = -at * 70f * (k + 1) + k * 90f, sweepAngle = 110f, useCenter = false,
                topLeft = Offset(cx - rr, cy - rr), size = Size(rr * 2f, rr * 2f),
                style = Stroke(width = 2f * s, cap = StrokeCap.Round),
            )
        }
    }

    // ---------- částice a hráč ----------

    private fun DrawScope.drawParticles(engine: GameEngine, s: Float, shipY: Float) {
        val dist = engine.distance
        val list = engine.particles
        for (i in 0 until list.size) {
            val p = list[i]
            val a = (p.life / p.maxLife).coerceIn(0f, 1f)
            val color = when (p.kind) {
                ParticleKind.TRAIL -> palette.accent
                ParticleKind.SPARK -> palette.crystal
                ParticleKind.BLAST -> Color(0xFFFF8A3D)
                ParticleKind.VOID -> NeonViolet
                ParticleKind.BOOST -> Color(0xFF9FFBFF)
                else -> when (i % 4) {
                    0 -> NeonGold
                    1 -> palette.accent
                    2 -> NeonViolet
                    else -> Color.White
                }
            }
            drawCircle(
                color = color.copy(alpha = a),
                radius = p.size * s * (0.4f + 0.6f * a),
                center = Offset(p.x * s, shipY - (p.d - dist) * s),
            )
        }
    }

    private fun DrawScope.drawPlayer(engine: GameEngine, s: Float, shipY: Float, at: Float) {
        val boosting = engine.boostTime > 0f
        if (!boosting && engine.invuln > 0f && (engine.invuln * 12f).toInt() % 2 == 0) return
        val tilt = ((engine.targetX - engine.shipX) * 0.25f).coerceIn(-18f, 18f)
        val flame = 0.5f + 0.5f * sin(at * 40f)
        val cx = engine.shipX * s
        drawShip(
            cx = cx, cy = shipY, scale = s * 0.9f, tiltDeg = tilt,
            accent = palette.accent, flame = if (boosting) 1f else flame,
        )
        if (boosting) {
            drawCircle(
                color = Color(0xFF9FFBFF).copy(alpha = 0.7f),
                radius = 36f * s, center = Offset(cx, shipY), style = Stroke(width = 2.5f * s),
            )
        }
    }
}
