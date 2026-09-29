package com.mysticracer.game.ui.levels

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mysticracer.game.domain.LevelSpec
import com.mysticracer.game.domain.Progress
import com.mysticracer.game.ui.common.SpaceBackground
import com.mysticracer.game.ui.theme.NeonGold
import com.mysticracer.game.ui.theme.TextSoft
import com.mysticracer.game.ui.theme.palette

@Composable
fun LevelSelectScreen(
    levels: List<LevelSpec>,
    progress: Progress,
    onBack: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    SpaceBackground {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zpět", tint = Color.White)
                }
                Text(
                    "Vyber úroveň",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(levels, key = { it.id }) { spec ->
                    LevelCard(
                        spec = spec,
                        stars = progress.starsOf(spec.id),
                        unlocked = progress.isUnlocked(spec.id),
                        onClick = { onSelect(spec.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelCard(spec: LevelSpec, stars: Int, unlocked: Boolean, onClick: () -> Unit) {
    val palette = spec.biome.palette()
    val alpha = if (unlocked) 1f else 0.45f
    Surface(
        onClick = onClick,
        enabled = unlocked,
        shape = RoundedCornerShape(18.dp),
        color = palette.bgTop.copy(alpha = 0.85f * alpha + 0.1f),
        border = BorderStroke(1.5.dp, palette.accent.copy(alpha = 0.7f * alpha)),
        modifier = Modifier.fillMaxWidth().aspectRatio(0.95f),
    ) {
        Box(Modifier.fillMaxSize().padding(14.dp)) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        "${spec.id}",
                        color = palette.accent.copy(alpha = alpha),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        spec.name,
                        color = Color.White.copy(alpha = alpha),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(spec.biome.displayName, color = TextSoft.copy(alpha = alpha), fontSize = 12.sp)
                }
                Row {
                    for (k in 1..3) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (k <= stars) NeonGold else Color.White.copy(alpha = 0.18f),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
            if (!unlocked) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = "Zamčeno",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.align(Alignment.TopEnd).size(24.dp),
                )
            }
        }
    }
}
