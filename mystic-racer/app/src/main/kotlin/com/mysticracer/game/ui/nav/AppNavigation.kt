package com.mysticracer.game.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mysticracer.game.AppContainer
import com.mysticracer.game.BuildConfig
import com.mysticracer.game.data.LevelCatalog
import com.mysticracer.game.domain.LevelResult
import com.mysticracer.game.ui.game.GameScreen
import com.mysticracer.game.ui.levels.LevelSelectScreen
import com.mysticracer.game.ui.menu.MenuScreen
import com.mysticracer.game.ui.result.ResultScreen
import com.mysticracer.game.ui.settings.SettingsScreen

private object Routes {
    const val MENU = "menu"
    const val LEVELS = "levels"
    const val SETTINGS = "settings"
    const val GAME = "game/{level}"
    const val RESULT = "result/{level}/{won}/{crystals}/{total}/{shields}"

    fun game(id: Int) = "game/$id"
    fun result(r: LevelResult) =
        "result/${r.levelId}/${if (r.won) 1 else 0}/${r.crystals}/${r.totalCrystals}/${r.shieldsLeft}"
}

@Composable
fun AppNavigation(container: AppContainer) {
    val nav = rememberNavController()
    val progress by container.repository.progress.collectAsState()
    val settings by container.repository.settings.collectAsState()
    val levels = LevelCatalog.levels

    NavHost(navController = nav, startDestination = Routes.MENU) {
        composable(Routes.MENU) {
            MenuScreen(
                totalStars = progress.totalStars,
                maxStars = levels.size * 3,
                versionName = BuildConfig.VERSION_NAME,
                onPlay = { nav.navigate(Routes.LEVELS) },
                onSettings = { nav.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.LEVELS) {
            LevelSelectScreen(
                levels = levels,
                progress = progress,
                onBack = { nav.popBackStack() },
                onSelect = { id -> nav.navigate(Routes.game(id)) },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                settings = settings,
                onChange = { transform -> container.repository.updateSettings(transform) },
                onBack = { nav.popBackStack() },
            )
        }
        composable(
            route = Routes.GAME,
            arguments = listOf(navArgument("level") { type = NavType.IntType }),
        ) { entry ->
            val id = entry.arguments?.getInt("level") ?: 1
            GameScreen(
                spec = LevelCatalog.byId(id),
                settings = settings,
                onFinished = { result ->
                    container.repository.saveResult(result)
                    nav.navigate(Routes.result(result)) {
                        popUpTo(Routes.GAME) { inclusive = true }
                    }
                },
                onRestart = {
                    nav.navigate(Routes.game(id)) {
                        popUpTo(Routes.GAME) { inclusive = true }
                    }
                },
                onExit = { nav.popBackStack() },
            )
        }
        composable(
            route = Routes.RESULT,
            arguments = listOf(
                navArgument("level") { type = NavType.IntType },
                navArgument("won") { type = NavType.IntType },
                navArgument("crystals") { type = NavType.IntType },
                navArgument("total") { type = NavType.IntType },
                navArgument("shields") { type = NavType.IntType },
            ),
        ) { entry ->
            val args = entry.arguments
            val result = LevelResult(
                levelId = args?.getInt("level") ?: 1,
                won = (args?.getInt("won") ?: 0) == 1,
                crystals = args?.getInt("crystals") ?: 0,
                totalCrystals = args?.getInt("total") ?: 0,
                shieldsLeft = args?.getInt("shields") ?: 0,
            )
            ResultScreen(
                result = result,
                onNext = {
                    nav.navigate(Routes.game(result.levelId + 1)) {
                        popUpTo(Routes.RESULT) { inclusive = true }
                    }
                },
                onRetry = {
                    nav.navigate(Routes.game(result.levelId)) {
                        popUpTo(Routes.RESULT) { inclusive = true }
                    }
                },
                onMenu = { nav.popBackStack(Routes.LEVELS, inclusive = false) },
            )
        }
    }
}
