package com.mysticracer.game.data

import android.content.Context
import com.mysticracer.game.domain.LevelResult
import com.mysticracer.game.domain.Progress
import com.mysticracer.game.domain.ProgressRepository
import com.mysticracer.game.domain.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Ukládá postup a nastavení do SharedPreferences (bez vnějších závislostí). */
class PrefsProgressRepository(context: Context) : ProgressRepository {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _progress = MutableStateFlow(loadProgress())
    private val _settings = MutableStateFlow(loadSettings())

    override val progress: StateFlow<Progress> = _progress
    override val settings: StateFlow<Settings> = _settings

    override fun saveResult(result: LevelResult) {
        if (!result.won) return
        val id = result.levelId
        val old = _progress.value
        val newStars = maxOf(old.starsOf(id), result.stars)
        val newCrystals = maxOf(old.bestCrystals[id] ?: 0, result.crystals)
        prefs.edit()
            .putInt("stars_$id", newStars)
            .putInt("crystals_$id", newCrystals)
            .apply()
        _progress.value = loadProgress()
    }

    override fun updateSettings(transform: (Settings) -> Settings) {
        val updated = transform(_settings.value)
        prefs.edit()
            .putBoolean(KEY_BUTTONS, updated.buttonControls)
            .putBoolean(KEY_HAPTICS, updated.haptics)
            .apply()
        _settings.value = updated
    }

    private fun loadProgress(): Progress {
        val stars = HashMap<Int, Int>()
        val crystals = HashMap<Int, Int>()
        for (id in 1..LevelCatalog.levels.size) {
            val s = prefs.getInt("stars_$id", 0)
            if (s > 0) stars[id] = s
            val c = prefs.getInt("crystals_$id", 0)
            if (c > 0) crystals[id] = c
        }
        return Progress(stars, crystals)
    }

    private fun loadSettings(): Settings = Settings(
        buttonControls = prefs.getBoolean(KEY_BUTTONS, false),
        haptics = prefs.getBoolean(KEY_HAPTICS, true),
    )

    private companion object {
        const val PREFS_NAME = "mystic_racer"
        const val KEY_BUTTONS = "button_controls"
        const val KEY_HAPTICS = "haptics"
    }
}
