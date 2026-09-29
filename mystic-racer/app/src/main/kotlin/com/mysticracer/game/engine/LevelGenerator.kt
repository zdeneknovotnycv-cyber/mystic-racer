package com.mysticracer.game.engine

import com.mysticracer.game.domain.LevelSpec
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Deterministický generátor levelu. Každá vlna překážek se staví kolem „bezpečného koridoru"
 * (střed cx, poloviční šířka [CORRIDOR_HALF]), který se mezi vlnami posouvá jen tak,
 * aby ho loď stihla dojet (omezení podle [MAX_LAT_SPEED]). Level je tedy vždy průjezdný
 * a krystaly leží na ideální stopě.
 */
object LevelGenerator {
    const val CORRIDOR_HALF = 100f
    private val CRYSTAL_FRACTIONS = floatArrayOf(0.15f, 0.27f, 0.73f, 0.85f)
    private const val TAU = 6.2831855f

    fun generate(spec: LevelSpec): GeneratedLevel {
        val rnd = Random(spec.seed)
        val out = ArrayList<Entity>(360)
        val path = ArrayList<PathPoint>(80)

        val speed = spec.speed
        val spacing = speed * spec.spacingSec
        val end = spec.length - speed * 1.5f
        val maxShift = min(260f, MAX_LAT_SPEED * spec.spacingSec * 0.5f)
        val minCx = MIN_X + CORRIDOR_HALF * 0.8f
        val maxCx = MAX_X - CORRIDOR_HALF * 0.8f

        var prevD = speed * 0.9f
        var prevCx = WORLD_W / 2f
        path.add(PathPoint(0f, prevCx))
        path.add(PathPoint(prevD, prevCx))
        var d = speed * 2.4f

        while (d <= end) {
            val gap = d - prevD
            val cx = (prevCx + (rnd.nextFloat() * 2f - 1f) * maxShift).coerceIn(minCx, maxCx)
            val midD = prevD + gap * 0.5f
            val midX = prevCx + (cx - prevCx) * 0.5f

            // 1) Krystaly na ideální stopě mezi předchozí a touto vlnou
            if (rnd.nextFloat() < 0.9f) {
                for (f in CRYSTAL_FRACTIONS) {
                    out.add(Entity(EntityKind.CRYSTAL, prevCx + (cx - prevCx) * f, prevD + gap * f, 12f))
                }
            }
            // bonusová odbočka mimo stopu
            if (rnd.nextFloat() < 0.3f) {
                val side = if (rnd.nextBoolean()) 1f else -1f
                val bx = (midX + side * (CORRIDOR_HALF + 45f)).coerceIn(MIN_X, MAX_X)
                out.add(Entity(EntityKind.CRYSTAL, bx, midD - 18f, 12f))
                out.add(Entity(EntityKind.CRYSTAL, bx, midD + 18f, 12f))
            }

            // 2) Vlna překážek: zeď s mezerou, nebo asteroidy mimo koridor
            if (spec.wallChance > 0f && rnd.nextFloat() < spec.wallChance) {
                out.add(Entity(EntityKind.BARRIER, cx, d, 13f).also { it.gapHalf = spec.gapHalf })
            } else {
                addAsteroids(out, rnd, spec, cx, d)
            }

            // 3) Prvky uprostřed mezi vlnami
            if (rnd.nextFloat() < spec.darkPortalChance) {
                val onLine = spec.id >= 5 && rnd.nextFloat() < 0.2f
                val side = if (rnd.nextBoolean()) 1f else -1f
                val px = if (onLine) midX else
                    (midX + side * (CORRIDOR_HALF + 60f + rnd.nextFloat() * 30f)).coerceIn(MIN_X + 20f, MAX_X - 20f)
                out.add(Entity(EntityKind.DARK_PORTAL, px, midD, 34f))
            } else if (rnd.nextFloat() < spec.boostChance) {
                out.add(Entity(EntityKind.BOOST, midX, midD, 32f))
            }
            if (rnd.nextFloat() < spec.anomalyChance) {
                val ax = MIN_X + 40f + rnd.nextFloat() * (MAX_X - MIN_X - 80f)
                out.add(Entity(EntityKind.ANOMALY, ax, midD + (rnd.nextFloat() - 0.5f) * gap * 0.4f, 110f))
            }

            path.add(PathPoint(d, cx))
            prevD = d
            prevCx = cx
            d += spacing * (0.85f + rnd.nextFloat() * 0.3f)
        }
        path.add(PathPoint(spec.length + speed, prevCx))

        out.sortBy { it.d }
        return GeneratedLevel(out, path)
    }

    private fun addAsteroids(out: MutableList<Entity>, rnd: Random, spec: LevelSpec, cx: Float, d: Float) {
        val n = max(1, spec.asteroidDensity - rnd.nextInt(2))
        val placed = ArrayList<Float>(4)
        repeat(n) {
            val r = 18f + rnd.nextFloat() * 14f
            val drift = rnd.nextFloat() < spec.driftChance
            val amp = if (drift) 25f + rnd.nextFloat() * 15f else 0f
            var x = -1f
            for (attempt in 0 until 10) {
                val cand = MIN_X + 10f + rnd.nextFloat() * (MAX_X - MIN_X - 20f)
                val clear = abs(cand - cx) > CORRIDOR_HALF + r + amp
                val spaced = placed.all { abs(it - cand) > 70f }
                if (clear && spaced) {
                    x = cand
                    break
                }
            }
            if (x >= 0f) {
                placed.add(x)
                val e = Entity(EntityKind.ASTEROID, x, d + (rnd.nextFloat() - 0.5f) * 80f, r)
                e.phase = rnd.nextFloat() * TAU
                e.spin = (rnd.nextFloat() - 0.5f) * 2.4f
                if (drift) e.amp = amp
                e.freq = 1.2f + rnd.nextFloat() * 1.3f
                e.shape = FloatArray(9) { 0.75f + rnd.nextFloat() * 0.3f }
                out.add(e)
            }
        }
    }
}
