package com.mysticracer.game.ui.result

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mysticracer.game.data.LevelCatalog
import com.mysticracer.game.domain.LevelResult
import com.mysticracer.game.domain.StarRules
import com.mysticracer.game.ui.common.SpaceBackground
import com.mysticracer.game.ui.theme.NeonCyan
import com.mysticracer.game.ui.theme.NeonGold
import com.mysticracer.game.ui.theme.TextSoft
import com.mysticracer.game.ui.theme.palette
import kotlinx.coroutines.delay

@Composable
fun ResultScreen(
    result: LevelResult,
    onNext: () -> Unit,
    onRetry: () -> Unit,
    onMenu: () -> Unit,
) {
    val spec = LevelCatalog.byId(result.levelId)
    val palette = spec.biome.palette()
    val stars = result.stars
    var shown by remember { mutableIntStateOf(0) }
    LaunchedEffect(stars) {
        repeat(stars) {
            delay(380)
            shown++
        }
    }

    val hasNext = LevelCatalog.hasNext(result.levelId)
    val crystalGoal = StarRules.crystalGoalMet(result.crystals, result.totalCrystals)
    val fewHits = result.won && result.shieldsLeft >= StarRules.MAX_SHIELDS - 1

    SpaceBackground(top = palette.bgTop, bottom = Color(0xFF050816)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                if (result.won) "LEVEL SPLNĚN" else "LOĎ ZNIČENA",
                color = if (result.won) NeonGold else Color(0xFFFF6B6B),
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )
            Text(
                "${spec.id}. ${spec.name}",
                color = TextSoft,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                for (k in 0 until 3) {
                    val earned = k < shown
                    val sc by animateFloatAsState(
                        targetValue = if (earned) 1f else 0.85f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "star$k",
                    )
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = if (earned) NeonGold else Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.size(if (k == 1) 84.dp else 64.dp).scale(sc),
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Objective("Dokončit level", result.won)
            Objective(
                "Sebrat alespoň ${StarRules.CRYSTAL_PERCENT_FOR_STAR} % krystalů (${result.crystals}/${result.totalCrystals})",
                result.won && crystalGoal,
            )
            Objective("Přežít nejvýš s jedním zásahem", fewHits)

            Spacer(Modifier.height(32.dp))
            if (result.won && hasNext) {
                Button(onClick = onNext, modifier = Modifier.fillMaxWidth(0.75f).height(54.dp)) {
                    Text("Další level", fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
                Spacer(Modifier.height(10.dp))
            } else if (result.won) {
                Text(
                    "Gratulace! Dokončil jsi všech 10 levelů.",
                    color = NeonCyan,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(14.dp))
            }
            OutlinedButton(onClick = onRetry, modifier = Modifier.fillMaxWidth(0.75f).height(48.dp)) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (result.won) "Hrát znovu" else "Zkusit znovu")
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = onMenu, modifier = Modifier.fillMaxWidth(0.75f).height(48.dp)) {
                Text("Výběr levelu")
            }
        }
    }
}

@Composable
private fun Objective(text: String, done: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(0.9f).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (done) Icons.Filled.Check else Icons.Filled.Close,
            contentDescription = if (done) "Splněno" else "Nesplněno",
            tint = if (done) NeonCyan else Color.White.copy(alpha = 0.35f),
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(text, color = if (done) Color.White else TextSoft.copy(alpha = 0.7f), fontSize = 15.sp)
    }
}
