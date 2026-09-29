package com.mysticracer.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mysticracer.game.ui.nav.AppNavigation
import com.mysticracer.game.ui.theme.MysticTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as MysticApp).container
        setContent {
            MysticTheme {
                AppNavigation(container)
            }
        }
    }
}
