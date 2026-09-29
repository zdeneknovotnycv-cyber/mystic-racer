package com.mysticracer.game.engine

import com.mysticracer.game.data.LevelCatalog
import com.mysticracer.game.domain.StarRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Simulace „dokonalého bota", který jede po bezpečné stopě vygenerovaného levelu.
 * Ověřuje, že každý z 10 levelů je průjezdný bez zásahu a že geometrie kolizí
 * odpovídá generátoru (koridor je opravdu volný).
 */
class LevelSimulationTest {

    private fun runBot(engine: GameEngine): Int {
        val dt = 1f / 60f
        var frames = 0
        val spec = engine.spec
        while (!engine.isFinished && frames < 60 * 240) {
            engine.aimAt(engine.level.pathX(engine.distance + spec.speed * 0.10f))
            engine.update(dt)
            frames++
        }
        return frames
    }

    @Test
    fun catalogHasTenLevelsWithUniqueIds() {
        assertEquals(10, LevelCatalog.levels.size)
        assertEquals((1..10).toList(), LevelCatalog.levels.map { it.id })
    }

    @Test
    fun generationIsDeterministicAndSorted() {
        for (spec in LevelCatalog.levels) {
            val a = LevelGenerator.generate(spec)
            val b = LevelGenerator.generate(spec)
            assertEquals(a.entities.size, b.entities.size)
            assertTrue(a.totalCrystals >= 60)
            for (i in 1 until a.entities.size) {
                assertTrue(a.entities[i - 1].d <= a.entities[i].d)
            }
        }
    }

    @Test
    fun perfectBotClearsEveryLevelWithoutDamage() {
        for (spec in LevelCatalog.levels) {
            val engine = GameEngine(spec)
            runBot(engine)
            val label = "level ${spec.id} (${spec.name})"
            assertEquals("$label: status", GameStatus.WON, engine.status)
            assertEquals("$label: shields", StarRules.MAX_SHIELDS, engine.shields)
            val pct = engine.crystals * 100 / engine.totalCrystals
            assertTrue("$label: crystals $pct %", pct >= 70)
            assertEquals("$label: stars", 3, engine.toResult().stars)
        }
    }

    @Test
    fun idleShipCrashesOrFinishesButNeverHangs() {
        val engine = GameEngine(LevelCatalog.byId(6))
        var frames = 0
        while (!engine.isFinished && frames < 60 * 240) {
            engine.update(1f / 60f)
            frames++
        }
        assertTrue(engine.isFinished)
    }
}
