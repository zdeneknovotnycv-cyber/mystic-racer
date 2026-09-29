package com.mysticracer.game

import android.app.Application
import android.content.Context
import com.mysticracer.game.data.PrefsProgressRepository
import com.mysticracer.game.domain.ProgressRepository

/** Jednoduchý ruční DI kontejner (bez Hilt) — závislosti vlastní Application. */
class AppContainer(context: Context) {
    val repository: ProgressRepository = PrefsProgressRepository(context)
}

class MysticApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
