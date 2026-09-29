package com.mysticracer.game.engine

import kotlin.math.sin

// ---- Ladění hry (souřadnice světa: šířka 600 jednotek, y = uražená vzdálenost) ----
const val WORLD_W = 600f
const val TRACK_L = 40f
const val TRACK_R = 560f
const val MIN_X = 55f
const val MAX_X = 545f
const val SHIP_R = 13f
const val MAX_LAT_SPEED = 900f
const val FOLLOW_K = 12f
const val BUTTON_SPEED = 520f
const val READY_TIME = 2.4f
const val HIT_INVULN = 1.4f
const val BOOST_TIME = 1.5f
const val BOOST_MUL = 1.7f
const val INVERT_TIME = 1.8f
const val ANOMALY_PULL = 420f

enum class EntityKind { ASTEROID, CRYSTAL, BOOST, DARK_PORTAL, ANOMALY, BARRIER }

/** Objekt ve světě: [x] střed, [d] pozice podél trati, [r] poloměr (u zdi poloviční tloušťka). */
class Entity(val kind: EntityKind, val x: Float, val d: Float, val r: Float) {
    var active = true
    var amp = 0f
    var freq = 0f
    var phase = 0f
    var spin = 0f
    /** Zeď: poloviční šířka mezery, střed mezery je [x]. */
    var gapHalf = 0f
    /** Asteroid: radiální faktory nepravidelného tvaru. */
    var shape: FloatArray? = null

    fun curX(t: Float): Float = if (amp == 0f) x else x + amp * sin(phase + t * freq)
}

class PathPoint(val d: Float, val x: Float)

/** Vygenerovaný level: objekty seřazené podle [Entity.d] a bezpečná „ideální stopa" [path]. */
class GeneratedLevel(val entities: List<Entity>, val path: List<PathPoint>) {
    val totalCrystals: Int = entities.count { it.kind == EntityKind.CRYSTAL }

    /** X souřadnice bezpečné stopy ve vzdálenosti [d]. */
    fun pathX(d: Float): Float {
        if (path.isEmpty()) return WORLD_W / 2f
        if (d <= path.first().d) return path.first().x
        for (i in 1 until path.size) {
            val b = path[i]
            if (d <= b.d) {
                val a = path[i - 1]
                val span = b.d - a.d
                val f = if (span <= 0f) 1f else (d - a.d) / span
                return a.x + (b.x - a.x) * f
            }
        }
        return path.last().x
    }
}

enum class GameStatus { READY, PLAYING, WON, LOST }

enum class GameEvent { HIT, CRYSTAL, BOOST, PORTAL, WIN, LOSE }

object ParticleKind {
    const val TRAIL = 0
    const val SPARK = 1
    const val BLAST = 2
    const val VOID = 3
    const val BOOST = 4
    const val CONFETTI = 5
}

class Particle(
    var x: Float,
    var d: Float,
    var vx: Float,
    var vd: Float,
    var life: Float,
    val maxLife: Float,
    val size: Float,
    val kind: Int,
)
