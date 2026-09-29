package com.mysticracer.game.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.mysticracer.game.domain.Biome

val NeonCyan = Color(0xFF4DF3FF)
val NeonViolet = Color(0xFFB65CFF)
val NeonGold = Color(0xFFFFD166)
val DeepSpace = Color(0xFF050816)
val Panel = Color(0xFF111735)
val TextSoft = Color(0xFFB8C4EE)

@Composable
fun MysticTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = NeonCyan,
            onPrimary = DeepSpace,
            secondary = NeonViolet,
            onSecondary = DeepSpace,
            tertiary = NeonGold,
            background = DeepSpace,
            onBackground = Color(0xFFE8EEFF),
            surface = Panel,
            onSurface = Color(0xFFE8EEFF),
            surfaceVariant = Color(0xFF1B2350),
            onSurfaceVariant = TextSoft,
        ),
        content = content,
    )
}

/** Barvy jednoho biomu (používá renderer i karty levelů). */
class BiomePalette(
    val bgTop: Color,
    val bgBottom: Color,
    val accent: Color,
    val accent2: Color,
    val rock: Color,
    val rockEdge: Color,
    val crystal: Color,
    val fog: Color,
    val wall: Color,
)

fun Biome.palette(): BiomePalette = when (this) {
    Biome.NEBULA_DRIFT -> BiomePalette(
        bgTop = Color(0xFF1A0B45), bgBottom = Color(0xFF060A24),
        accent = Color(0xFF4DF3FF), accent2 = Color(0xFFFF5CD1),
        rock = Color(0xFF3A3560), rockEdge = Color(0xFF9C8CFF),
        crystal = Color(0xFFFFD166), fog = Color(0xFF3A1B80), wall = Color(0xFF241A55),
    )
    Biome.ANCIENT_RUINS -> BiomePalette(
        bgTop = Color(0xFF2A1A06), bgBottom = Color(0xFF0A0A16),
        accent = Color(0xFFFFC857), accent2 = Color(0xFF4DF3FF),
        rock = Color(0xFF4A3B26), rockEdge = Color(0xFFFFC857),
        crystal = Color(0xFF7DF9FF), fog = Color(0xFF5A3D10), wall = Color(0xFF3B2C14),
    )
    Biome.VOID_RIFT -> BiomePalette(
        bgTop = Color(0xFF12002B), bgBottom = Color(0xFF020008),
        accent = Color(0xFFB65CFF), accent2 = Color(0xFF4DF3FF),
        rock = Color(0xFF241338), rockEdge = Color(0xFFB65CFF),
        crystal = Color(0xFF7DFFB0), fog = Color(0xFF2A0A55), wall = Color(0xFF1A0B33),
    )
    Biome.CRYSTAL_CAVERNS -> BiomePalette(
        bgTop = Color(0xFF032A33), bgBottom = Color(0xFF020E18),
        accent = Color(0xFF3DFFE0), accent2 = Color(0xFF7AA8FF),
        rock = Color(0xFF15414A), rockEdge = Color(0xFF3DFFE0),
        crystal = Color(0xFFFFE27A), fog = Color(0xFF0B5560), wall = Color(0xFF0E3A44),
    )
    Biome.DARK_STAR -> BiomePalette(
        bgTop = Color(0xFF330A08), bgBottom = Color(0xFF0A0206),
        accent = Color(0xFFFF6B3D), accent2 = Color(0xFFFFD166),
        rock = Color(0xFF3A1614), rockEdge = Color(0xFFFF6B3D),
        crystal = Color(0xFF7DF9FF), fog = Color(0xFF6A1408), wall = Color(0xFF2E100E),
    )
}
