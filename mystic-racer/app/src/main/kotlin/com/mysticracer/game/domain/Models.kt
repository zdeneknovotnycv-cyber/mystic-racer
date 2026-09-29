package com.mysticracer.game.domain

import kotlinx.coroutines.flow.StateFlow

/** Prostředí (biom) levelu. Čistý Kotlin, žádné Android importy. */
enum class Biome(val displayName: String) {
    NEBULA_DRIFT("Nebula Drift"),
    ANCIENT_RUINS("Ancient Ruins"),
    VOID_RIFT("Void Rift"),
    CRYSTAL_CAVERNS("Crystal Caverns"),
    DARK_STAR("Dark Star"),
}

/** Popis levelu. Samotné rozestavění překážek se generuje deterministicky ze [seed]. */
data class LevelSpec(
    val id: Int,
    val name: String,
    val biome: Biome,
    /** Rychlost v jednotkách světa za sekundu. */
    val speed: Float,
    val durationSec: Int,
    /** Průměrný čas mezi vlnami překážek. */
    val spacingSec: Float,
    /** Max. počet asteroidů ve vlně (1..4). */
    val asteroidDensity: Int,
    /** Šance, že vlna je zeď s mezerou (Ancient Ruins a dál). */
    val wallChance: Float = 0f,
    /** Poloviční šířka mezery ve zdi. */
    val gapHalf: Float = 80f,
    /** Šance na temný portál (obrátí ovládání). */
    val darkPortalChance: Float = 0f,
    /** Šance na gravitační anomálii (stáhne loď do strany). */
    val anomalyChance: Float = 0f,
    /** Šance, že se asteroid houpe do stran. */
    val driftChance: Float = 0f,
    /** Šance na urychlovací bránu. */
    val boostChance: Float = 0f,
    /** 0..1 — síla mlhy zakrývající výhled dopředu. */
    val fog: Float = 0f,
    val seed: Long,
) {
    val length: Float get() = speed * durationSec
}

data class LevelResult(
    val levelId: Int,
    val won: Boolean,
    val crystals: Int,
    val totalCrystals: Int,
    val shieldsLeft: Int,
) {
    val stars: Int get() = StarRules.stars(won, crystals, totalCrystals, shieldsLeft)
}

/**
 * Hvězdy: 1 = level dokončen, +1 za alespoň 60 % krystalů, +1 za max. jeden zásah.
 */
object StarRules {
    const val MAX_SHIELDS = 3
    const val CRYSTAL_PERCENT_FOR_STAR = 60

    fun stars(won: Boolean, crystals: Int, totalCrystals: Int, shieldsLeft: Int): Int {
        if (!won) return 0
        var s = 1
        if (crystalGoalMet(crystals, totalCrystals)) s++
        if (shieldsLeft >= MAX_SHIELDS - 1) s++
        return s
    }

    fun crystalGoalMet(crystals: Int, totalCrystals: Int): Boolean =
        totalCrystals > 0 && crystals * 100 >= totalCrystals * CRYSTAL_PERCENT_FOR_STAR
}

data class Progress(
    val stars: Map<Int, Int> = emptyMap(),
    val bestCrystals: Map<Int, Int> = emptyMap(),
) {
    fun starsOf(levelId: Int): Int = stars[levelId] ?: 0
    fun isUnlocked(levelId: Int): Boolean = levelId <= 1 || starsOf(levelId - 1) > 0
    val totalStars: Int get() = stars.values.sum()
}

data class Settings(
    /** true = dvě tlačítka vlevo/vpravo místo tažení prstem. */
    val buttonControls: Boolean = false,
    val haptics: Boolean = true,
)

interface ProgressRepository {
    val progress: StateFlow<Progress>
    val settings: StateFlow<Settings>
    fun saveResult(result: LevelResult)
    fun updateSettings(transform: (Settings) -> Settings)
}
