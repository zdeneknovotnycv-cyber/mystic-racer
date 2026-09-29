package com.mysticracer.game.data

import com.mysticracer.game.domain.Biome
import com.mysticracer.game.domain.LevelSpec

/** 10 levelů: 5 biomů × 2 levely, obtížnost plynule roste. */
object LevelCatalog {

    val levels: List<LevelSpec> = listOf(
        LevelSpec(
            id = 1, name = "Úsvit v mlhovině", biome = Biome.NEBULA_DRIFT,
            speed = 230f, durationSec = 38, spacingSec = 1.5f, asteroidDensity = 1,
            boostChance = 0.25f, seed = 1101L,
        ),
        LevelSpec(
            id = 2, name = "Fialový příboj", biome = Biome.NEBULA_DRIFT,
            speed = 250f, durationSec = 42, spacingSec = 1.35f, asteroidDensity = 2,
            driftChance = 0.2f, boostChance = 0.25f, seed = 1202L,
        ),
        LevelSpec(
            id = 3, name = "Brány zapomnění", biome = Biome.ANCIENT_RUINS,
            speed = 255f, durationSec = 42, spacingSec = 1.35f, asteroidDensity = 2,
            wallChance = 0.30f, gapHalf = 80f, boostChance = 0.25f, seed = 1303L,
        ),
        LevelSpec(
            id = 4, name = "Chrám ozvěn", biome = Biome.ANCIENT_RUINS,
            speed = 275f, durationSec = 45, spacingSec = 1.25f, asteroidDensity = 2,
            wallChance = 0.40f, gapHalf = 76f, anomalyChance = 0.15f, boostChance = 0.25f, seed = 1404L,
        ),
        LevelSpec(
            id = 5, name = "Trhlina prázdnoty", biome = Biome.VOID_RIFT,
            speed = 285f, durationSec = 46, spacingSec = 1.2f, asteroidDensity = 2,
            wallChance = 0.20f, gapHalf = 74f, darkPortalChance = 0.35f, anomalyChance = 0.15f,
            fog = 0.15f, boostChance = 0.2f, seed = 1505L,
        ),
        LevelSpec(
            id = 6, name = "Okraj světa", biome = Biome.VOID_RIFT,
            speed = 305f, durationSec = 48, spacingSec = 1.15f, asteroidDensity = 3,
            wallChance = 0.25f, gapHalf = 72f, darkPortalChance = 0.40f, anomalyChance = 0.30f,
            driftChance = 0.3f, fog = 0.25f, boostChance = 0.2f, seed = 1606L,
        ),
        LevelSpec(
            id = 7, name = "Krystalová katedrála", biome = Biome.CRYSTAL_CAVERNS,
            speed = 320f, durationSec = 50, spacingSec = 1.1f, asteroidDensity = 3,
            wallChance = 0.30f, gapHalf = 72f, darkPortalChance = 0.20f, anomalyChance = 0.25f,
            driftChance = 0.4f, fog = 0.20f, boostChance = 0.3f, seed = 1707L,
        ),
        LevelSpec(
            id = 8, name = "Ozvěna křišťálů", biome = Biome.CRYSTAL_CAVERNS,
            speed = 340f, durationSec = 52, spacingSec = 1.05f, asteroidDensity = 3,
            wallChance = 0.35f, gapHalf = 70f, darkPortalChance = 0.30f, anomalyChance = 0.30f,
            driftChance = 0.5f, fog = 0.30f, boostChance = 0.25f, seed = 1808L,
        ),
        LevelSpec(
            id = 9, name = "Zrození temné hvězdy", biome = Biome.DARK_STAR,
            speed = 360f, durationSec = 54, spacingSec = 1.0f, asteroidDensity = 3,
            wallChance = 0.40f, gapHalf = 68f, darkPortalChance = 0.40f, anomalyChance = 0.40f,
            driftChance = 0.5f, fog = 0.35f, boostChance = 0.25f, seed = 1909L,
        ),
        LevelSpec(
            id = 10, name = "Srdce temné hvězdy", biome = Biome.DARK_STAR,
            speed = 385f, durationSec = 60, spacingSec = 0.95f, asteroidDensity = 4,
            wallChance = 0.45f, gapHalf = 66f, darkPortalChance = 0.45f, anomalyChance = 0.45f,
            driftChance = 0.6f, fog = 0.40f, boostChance = 0.30f, seed = 2010L,
        ),
    )

    fun byId(id: Int): LevelSpec = levels.firstOrNull { it.id == id } ?: levels.first()

    fun hasNext(id: Int): Boolean = levels.any { it.id == id + 1 }
}
