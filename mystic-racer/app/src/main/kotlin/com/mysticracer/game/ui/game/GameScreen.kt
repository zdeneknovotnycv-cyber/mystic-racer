package com.mysticracer.game.ui.game

import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.mysticracer.game.domain.LevelResult
import com.mysticracer.game.domain.LevelSpec
import com.mysticracer.game.domain.Settings
import com.mysticracer.game.domain.StarRules
import com.mysticracer.game.engine.GameEngine
import com.mysticracer.game.engine.GameEvent
import com.mysticracer.game.engine.GameStatus
import com.mysticracer.game.engine.READY_TIME
import com.mysticracer.game.engine.WORLD_W
import com.mysticracer.game.ui.theme.NeonCyan
import com.mysticracer.game.ui.theme.NeonGold
import com.mysticracer.game.ui.theme.NeonViolet
import com.mysticracer.game.ui.theme.palette

private const val DRAG_GAIN = 1.5f

@Composable
fun GameScreen(
    spec: LevelSpec,
    settings: Settings,
    onFinished: (LevelResult) -> Unit,
    onRestart: () -> Unit,
    onExit: () -> Unit,
) {
    val engine = remember(spec.id) { GameEngine(spec) }
    val renderer = remember(spec.id) { GameRenderer(spec) }
    val palette = remember(spec.id) { spec.biome.palette() }

    var tick by remember { mutableLongStateOf(0L) }
    var paused by remember { mutableStateOf(false) }
    var shields by remember { mutableIntStateOf(engine.shields) }
    var crystals by remember { mutableIntStateOf(0) }
    var banner by remember { mutableStateOf("") }
    var inverted by remember { mutableStateOf(false) }

    val view = LocalView.current
    val dens = LocalDensity.current
    val topInset = WindowInsets.safeDrawing.getTop(dens).toFloat()

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { paused = true }
    BackHandler(enabled = !paused) { paused = true }

    LaunchedEffect(engine, paused) {
        if (paused) return@LaunchedEffect
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last != 0L) {
                    engine.update(((now - last) / 1_000_000_000f).coerceAtMost(0.05f))
                }
                last = now
                tick = now
            }
            engine.drainEvents { ev ->
                if (settings.haptics) {
                    when (ev) {
                        GameEvent.HIT, GameEvent.LOSE ->
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        GameEvent.BOOST, GameEvent.PORTAL ->
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        GameEvent.WIN ->
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        GameEvent.CRYSTAL -> Unit
                    }
                }
            }
            shields = engine.shields
            crystals = engine.crystals
            inverted = engine.isInverted
            banner = when {
                engine.status == GameStatus.READY -> engine.countdownDigit().toString()
                engine.status == GameStatus.PLAYING && engine.time < READY_TIME + 0.6f -> "START!"
                engine.status == GameStatus.WON -> "CÍL!"
                engine.status == GameStatus.LOST -> "LOĎ ZNIČENA"
                else -> ""
            }
            if (engine.isFinished && engine.endTimer > 1.1f) {
                onFinished(engine.toResult())
                return@LaunchedEffect
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(settings.buttonControls) {
                    if (!settings.buttonControls) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                if (!paused) {
                                    engine.dragBy(dragAmount.x * DRAG_GAIN * WORLD_W / size.width)
                                }
                            },
                        )
                    }
                },
        ) {
            val dp = dens.density
            with(renderer) { render(engine, tick, topInset, dp) }
        }

        // HUD: štíty, krystaly, pauza
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .safeDrawingPadding()
                .padding(start = 16.dp, end = 16.dp, top = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                for (k in 0 until StarRules.MAX_SHIELDS) {
                    Box(
                        Modifier
                            .padding(end = 6.dp)
                            .size(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (k < shields) NeonCyan else Color.White.copy(alpha = 0.15f)),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Box(
                    Modifier
                        .size(11.dp)
                        .rotate(45f)
                        .background(palette.crystal),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "$crystals / ${engine.totalCrystals}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .pointerInput(Unit) { detectTapGestures(onTap = { paused = true }) },
                contentAlignment = Alignment.Center,
            ) {
                Text("II", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
        }

        // odpočet / hlášky
        if (banner.isNotEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = banner,
                    color = if (banner == "LOĎ ZNIČENA") Color(0xFFFF6B6B) else NeonGold,
                    fontSize = if (banner.length == 1) 96.sp else 40.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                )
            }
        }
        if (inverted) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .safeDrawingPadding()
                    .padding(top = 70.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                Text(
                    "INVERZE OVLÁDÁNÍ",
                    color = NeonViolet,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 2.sp,
                )
            }
        }

        // tlačítkové ovládání (přístupnost)
        if (settings.buttonControls && !paused) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.28f)
                    .safeDrawingPadding()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SteerButton(left = true, onAxis = { engine.setAxis(it) }, modifier = Modifier.weight(1f))
                SteerButton(left = false, onAxis = { engine.setAxis(it) }, modifier = Modifier.weight(1f))
            }
        }

        if (paused) {
            PauseOverlay(
                onResume = { paused = false },
                onRestart = onRestart,
                onExit = onExit,
            )
        }
    }
}

@Composable
private fun SteerButton(left: Boolean, onAxis: (Float) -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.10f))
            .pointerInput(left) {
                detectTapGestures(
                    onPress = {
                        onAxis(if (left) -1f else 1f)
                        tryAwaitRelease()
                        onAxis(0f)
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (left) Icons.AutoMirrored.Filled.KeyboardArrowLeft else Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = if (left) "Doleva" else "Doprava",
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(56.dp),
        )
    }
}

@Composable
private fun PauseOverlay(onResume: () -> Unit, onRestart: () -> Unit, onExit: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
            .pointerInput(Unit) { detectTapGestures { } },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                "PAUZA",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = NeonCyan,
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = onResume, modifier = Modifier.width(240.dp).height(52.dp)) { Text("Pokračovat") }
            OutlinedButton(onClick = onRestart, modifier = Modifier.width(240.dp).height(52.dp)) { Text("Znovu") }
            OutlinedButton(onClick = onExit, modifier = Modifier.width(240.dp).height(52.dp)) { Text("Zpět na výběr") }
        }
    }
}
