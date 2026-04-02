package com.example.bumpscountdowntimer.ui.timer

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bumpscountdowntimer.ui.theme.*
import java.util.Locale

@Composable
fun TimerScreen(
    viewModel: TimerViewModel = viewModel()
) {
    val remainingMillis by viewModel.remainingMillis.collectAsStateWithLifecycle()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val hapticFeedback = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        viewModel.hapticEvents.collect { type ->
            when (type) {
                HapticType.MARK_60S -> {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                HapticType.TICK_1S -> {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                HapticType.START -> {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
        }
    }

    val backgroundColor by animateColorAsState(
        targetValue = getBackgroundColorForState(timerState),
        label = "backgroundColor"
    )

    val contentColor = if (backgroundColor == PrepYellow) Color.Black else Color.White

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxSize()
        ) {
            // Timer Display
            Text(
                text = formatMillis(remainingMillis, timerState),
                style = MaterialTheme.typography.displayLarge,
                color = contentColor
            )

            // State Label
            Text(
                text = timerState.name.replace("_", " "),
                style = MaterialTheme.typography.headlineMedium,
                color = contentColor.copy(alpha = 0.8f)
            )

            // Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SyncButton(
                        text = "4-min Sync",
                        onClick = { viewModel.sync4Min() },
                        modifier = Modifier.weight(1f)
                    )
                    SyncButton(
                        text = "1-min Sync",
                        onClick = { viewModel.sync1Min() },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ElevatedButton(
                        onClick = { viewModel.startRollingHold() },
                        modifier = Modifier.weight(1f).height(64.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    ) {
                        Text("Rolling Hold", style = MaterialTheme.typography.labelLarge)
                    }
                    ElevatedButton(
                        onClick = { viewModel.reset() },
                        modifier = Modifier.weight(1f).height(64.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text("Reset", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        // Rolling Hold Overlay
        if (timerState == TimerState.ROLLING_HOLD && remainingMillis <= 0) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.9f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = "Did the cannon fire?",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 48.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { viewModel.onRollingHoldComplete(confirmed = true) },
                            modifier = Modifier.weight(1f).height(80.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StartGreen)
                        ) {
                            Text("1-Min Gun Now", color = Color.Black, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        }
                        Button(
                            onClick = { viewModel.onRollingHoldComplete(confirmed = false) },
                            modifier = Modifier.weight(1f).height(80.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FinalRed)
                        ) {
                            Text("NO", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SyncButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun getBackgroundColorForState(state: TimerState): Color {
    return when (state) {
        TimerState.IDLE -> backgroundLight
        TimerState.WARNING_4_MIN -> WarningBlue
        TimerState.PREP_1_MIN -> PrepYellow
        TimerState.ROLLING_HOLD -> RollingOrange
        TimerState.FINAL_COUNTDOWN -> FinalRed
        TimerState.STARTED -> StartGreen
    }
}

private fun formatMillis(millis: Long, state: TimerState): String {
    if (state == TimerState.STARTED) return "START!"
    
    val totalSeconds = (millis + 999) / 1000 // Round up
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun TimerScreenPreview() {
    BumpsCountdownTimerTheme {
        TimerScreen()
    }
}
