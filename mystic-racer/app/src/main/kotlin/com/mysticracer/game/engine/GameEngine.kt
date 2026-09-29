package com.mysticracer.game.engine

import com.mysticracer.game.domain.LevelResult
import com.mysticracer.game.domain.LevelSpec
import com.mysticracer.game.domain.StarRules
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Herní logika bez závislosti na Androidu (testovatelná na JVM).
 * Renderer jen čte stav; vstup přichází přes [dragBy], [setAxis] a [aimAt].
 */
class GameEngine(
    val spec: LevelSpec,
    val level: GeneratedLevel = LevelGenerator.generate(spec),
) {
    val entities: List<Entity> = level.entities
    val totalCrystals: Int = level.totalCrystals
    val length: Float = spec.length

    var status = GameStatus.READY
        private set
    var time = 0f
        private set
    var distance = 0f
        private set
    var shipX = WORLD_W / 2f
        private set
    var targetX = WORLD_W / 2f
        private set
    var shields = StarRules.MAX_SHIELDS
        private set
    var crystals = 0
        private set
    var invuln = 0f
        private set
    var boostTime = 0f
        private set
    var invertTime = 0f
        private set
    var shake = 0f
        private set
    var endTimer = 0f
        private set

    val particles = ArrayList<Particle>(128)
    private val events = ArrayList<GameEvent>(8)
    private var axis = 0f
    private var startIdx = 0
    private val rnd = Random(spec.seed + 99L)

    val progress: Float get() = (distance / length).coerceIn(0f, 1f)
    val isInverted: Boolean get() = invertTime > 0f
    val isFinished: Boolean get() = status == GameStatus.WON || status == GameStatus.LOST

    /** Číslo odpočtu 3..1 před startem. */
    fun countdownDigit(): Int =
        kotlin.math.ceil((READY_TIME - time) / (READY_TIME / 3f)).toInt().coerceIn(1, 3)

    // ---- vstup ----

    /** Relativní tažení prstem v jednotkách světa (respektuje inverzi). */
    fun dragBy(dxUnits: Float) {
        if (isFinished) return
        val dir = if (invertTime > 0f) -1f else 1f
        targetX = (targetX + dxUnits * dir).coerceIn(MIN_X, MAX_X)
    }

    /** Ovládání tlačítky: -1 vlevo, 0 nic, +1 vpravo. */
    fun setAxis(value: Float) {
        axis = value.coerceIn(-1f, 1f)
    }

    /** Absolutní zamíření (pro testovacího bota; obchází inverzi). */
    fun aimAt(x: Float) {
        targetX = x.coerceIn(MIN_X, MAX_X)
    }

    fun drainEvents(sink: (GameEvent) -> Unit) {
        for (i in 0 until events.size) sink(events[i])
        events.clear()
    }

    fun toResult() = LevelResult(spec.id, status == GameStatus.WON, crystals, totalCrystals, shields)

    /** Index prvního objektu s d >= [dMin] (objekty jsou seřazené). */
    fun firstIndexAtOrAfter(dMin: Float): Int {
        var lo = 0
        var hi = entities.size
        while (lo < hi) {
            val mid = (lo + hi) ushr 1
            if (entities[mid].d < dMin) lo = mid + 1 else hi = mid
        }
        return lo
    }

    // ---- simulace ----

    fun update(dt: Float) {
        if (isFinished) {
            endTimer += dt
            if (shake > 0f) shake = max(0f, shake - dt)
            stepParticles(dt)
            return
        }

        time += dt
        if (invuln > 0f) invuln = max(0f, invuln - dt)
        if (boostTime > 0f) boostTime = max(0f, boostTime - dt)
        if (invertTime > 0f) invertTime = max(0f, invertTime - dt)
        if (shake > 0f) shake = max(0f, shake - dt)

        if (axis != 0f) {
            val dir = if (invertTime > 0f) -1f else 1f
            targetX = (targetX + axis * dir * BUTTON_SPEED * dt).coerceIn(MIN_X, MAX_X)
        }
        val maxStep = MAX_LAT_SPEED * dt
        shipX += ((targetX - shipX) * FOLLOW_K * dt).coerceIn(-maxStep, maxStep)

        if (status == GameStatus.READY) {
            if (time >= READY_TIME) status = GameStatus.PLAYING
            emitTrail()
            stepParticles(dt)
            return
        }

        val mul = if (boostTime > 0f) BOOST_MUL else 1f
        distance += spec.speed * mul * dt

        while (startIdx < entities.size && entities[startIdx].d < distance - 220f) startIdx++
        var i = startIdx
        while (i < entities.size) {
            val e = entities[i]
            i++
            val rel = e.d - distance
            if (rel > 180f) break
            if (!e.active) continue
            interact(e, rel, dt)
            if (status == GameStatus.LOST) break
        }
        shipX = shipX.coerceIn(MIN_X, MAX_X)

        emitTrail()
        stepParticles(dt)

        if (status == GameStatus.PLAYING && distance >= length) {
            status = GameStatus.WON
            endTimer = 0f
            events.add(GameEvent.WIN)
            confetti()
        }
    }

    private fun interact(e: Entity, rel: Float, dt: Float) {
        when (e.kind) {
            EntityKind.ASTEROID -> {
                val dx = e.curX(time) - shipX
                val rr = e.r * 0.85f + SHIP_R
                if (dx * dx + rel * rel < rr * rr) {
                    if (boostTime > 0f) {
                        explode(e.curX(time), e.d, 10, ParticleKind.BLAST, 160f)
                        e.active = false
                    } else if (invuln <= 0f) {
                        e.active = false
                        explode(e.curX(time), e.d, 12, ParticleKind.BLAST, 200f)
                        hit()
                    }
                }
            }
            EntityKind.CRYSTAL -> {
                val dx = e.x - shipX
                val rr = e.r + SHIP_R + 8f
                if (dx * dx + rel * rel < rr * rr) {
                    e.active = false
                    crystals++
                    explode(e.x, e.d, 6, ParticleKind.SPARK, 120f)
                    events.add(GameEvent.CRYSTAL)
                }
            }
            EntityKind.BOOST -> {
                val dx = e.x - shipX
                val rr = e.r + SHIP_R
                if (dx * dx + rel * rel < rr * rr) {
                    e.active = false
                    boostTime = BOOST_TIME
                    invuln = max(invuln, BOOST_TIME + 0.4f)
                    explode(e.x, e.d, 14, ParticleKind.BOOST, 220f)
                    events.add(GameEvent.BOOST)
                }
            }
            EntityKind.DARK_PORTAL -> {
                val dx = e.x - shipX
                val rr = e.r * 0.8f + SHIP_R
                if (dx * dx + rel * rel < rr * rr) {
                    e.active = false
                    invertTime = INVERT_TIME
                    explode(e.x, e.d, 18, ParticleKind.VOID, 190f)
                    events.add(GameEvent.PORTAL)
                }
            }
            EntityKind.ANOMALY -> {
                val dx = e.x - shipX
                val dist = sqrt(dx * dx + rel * rel)
                if (dist < e.r) {
                    val strength = 1f - dist / e.r
                    shipX += sign(dx) * ANOMALY_PULL * strength * dt
                }
            }
            EntityKind.BARRIER -> {
                if (kotlin.math.abs(rel) < e.r + SHIP_R * 0.6f) {
                    val left = e.x - e.gapHalf
                    val right = e.x + e.gapHalf
                    val inWall = shipX - SHIP_R * 0.8f < left || shipX + SHIP_R * 0.8f > right
                    if (inWall && boostTime <= 0f && invuln <= 0f) {
                        explode(shipX, distance, 12, ParticleKind.BLAST, 200f)
                        hit()
                    }
                }
            }
        }
    }

    private fun hit() {
        shields--
        invuln = HIT_INVULN
        shake = 0.35f
        explode(shipX, distance, 16, ParticleKind.BLAST, 260f)
        if (shields <= 0) {
            shields = 0
            status = GameStatus.LOST
            endTimer = 0f
            shake = 0.6f
            explode(shipX, distance, 40, ParticleKind.BLAST, 340f)
            events.add(GameEvent.LOSE)
        } else {
            events.add(GameEvent.HIT)
        }
    }

    private fun emitTrail() {
        if (particles.size > 380) return
        val boosting = boostTime > 0f
        val n = if (boosting) 3 else 1
        repeat(n) {
            particles.add(
                Particle(
                    x = shipX + (rnd.nextFloat() - 0.5f) * 10f,
                    d = distance - 14f,
                    vx = (rnd.nextFloat() - 0.5f) * 20f,
                    vd = -30f,
                    life = 0.4f + rnd.nextFloat() * 0.2f,
                    maxLife = 0.6f,
                    size = 4f + rnd.nextFloat() * 3f,
                    kind = if (boosting) ParticleKind.BOOST else ParticleKind.TRAIL,
                ),
            )
        }
    }

    private fun explode(x: Float, d: Float, count: Int, kind: Int, speed: Float) {
        if (particles.size > 420) return
        repeat(count) {
            val a = rnd.nextFloat() * 6.2831855f
            val s = 40f + rnd.nextFloat() * speed
            val life = 0.4f + rnd.nextFloat() * 0.5f
            particles.add(
                Particle(x, d, cos(a) * s, sin(a) * s, life, life, 3f + rnd.nextFloat() * 4f, kind),
            )
        }
    }

    private fun confetti() {
        repeat(70) {
            val a = rnd.nextFloat() * 6.2831855f
            val s = 60f + rnd.nextFloat() * 360f
            val life = 0.8f + rnd.nextFloat() * 0.9f
            particles.add(
                Particle(shipX, distance, cos(a) * s, sin(a) * s, life, life, 3f + rnd.nextFloat() * 4f, ParticleKind.CONFETTI),
            )
        }
    }

    private fun stepParticles(dt: Float) {
        var i = particles.size - 1
        while (i >= 0) {
            val p = particles[i]
            p.life -= dt
            if (p.life <= 0f) {
                particles.removeAt(i)
            } else {
                p.x += p.vx * dt
                p.d += p.vd * dt
            }
            i--
        }
    }
}
