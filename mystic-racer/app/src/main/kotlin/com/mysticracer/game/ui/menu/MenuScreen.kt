package com.mysticracer.game.ui.menu

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mysticracer.game.ui.common.ShipPreview
import com.mysticracer.game.ui.common.SpaceBackground
import com.mysticracer.game.ui.theme.NeonCyan
import com.mysticracer.game.ui.theme.NeonGold
import com.mysticracer.game.ui.theme.NeonViolet
import com.mysticracer.game.ui.theme.TextSoft

@Composable
fun MenuScreen(
    totalStars: Int,
    maxStars: Int,
    versionName: String,
    onPlay: () -> Unit,
    onSettings: () -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "menuShip")
    val bob by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob",
    )
    val flame by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(140), RepeatMode.Reverse),
        label = "flame",
    )

    SpaceBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                "SCI-FI",
                color = NeonCyan,
                fontSize = 20.sp,
                letterSpacing = 8.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "MYSTIC\nRACER",
                color = Color.White,
                fontSize = 46.sp,
                lineHeight = 48.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            ShipPreview(
                modifier = Modifier.size(200.dp),
                accent = NeonCyan,
                bob = bob,
                flame = flame,
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onPlay,
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(58.dp),
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("HRÁT", fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 3.sp)
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onSettings,
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(48.dp),
            ) {
                Icon(Icons.Filled.Settings, contentDescription = null, tint = NeonViolet)
                Spacer(Modifier.width(8.dp))
                Text("Nastavení")
            }
            Spacer(Modifier.height(24.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = NeonGold, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("$totalStars / $maxStars", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(6.dp))
                Text("v$versionName", color = TextSoft, fontSize = 12.sp)
            }
        }
    }
}
