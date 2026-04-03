package com.example.bumpscountdowntimer.ui.timer

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bumpscountdowntimer.ui.theme.*
import java.util.Calendar

@Composable
fun TimerScreen(
    viewModel: TimerViewModel = viewModel()
) {
    val remainingMillis by viewModel.remainingMillis.collectAsStateWithLifecycle()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val isHoldingFor4Min by viewModel.isHoldingFor4Min.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    val showTimePickerState = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.hapticEvents.collect { type ->
            when (type) {
                HapticType.MARK_60S -> {
                    vibrate(vibrator, 500)
                }
                HapticType.TICK_1S -> {
                    vibrate(vibrator, 100)
                }
                HapticType.START -> {
                    vibrate(vibrator, 1500)
                }
            }
        }
    }

    TimerContent(
        remainingMillis = remainingMillis,
        timerState = timerState,
        isHoldingFor4Min = isHoldingFor4Min,
        onSync4Min = { viewModel.sync4Min() },
        onSync1Min = { viewModel.sync1Min() },
        onStartRollingHold = { viewModel.startRollingHold() },
        onReset = { viewModel.reset() },
        onShowTimePicker = { showTimePickerState.value = true },
        onRollingHoldComplete = { confirmed -> viewModel.onRollingHoldComplete(confirmed) }
    )

    if (showTimePickerState.value) {
        ScheduledTimePickerDialog(
            onDismiss = { showTimePickerState.value = false },
            onConfirm = { hour, minute ->
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                viewModel.setScheduledStartTime(calendar.timeInMillis)
                showTimePickerState.value = false
            }
        )
    }
}

private fun vibrate(vibrator: Vibrator, duration: Long) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(duration)
    }
}

@Composable
fun TimerContent(
    remainingMillis: Long,
    timerState: TimerState,
    isHoldingFor4Min: Boolean,
    onSync4Min: () -> Unit,
    onSync1Min: () -> Unit,
    onStartRollingHold: () -> Unit,
    onReset: () -> Unit,
    onShowTimePicker: () -> Unit,
    onRollingHoldComplete: (Boolean) -> Unit
) {
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
                text = getStateLabel(timerState, isHoldingFor4Min),
                style = MaterialTheme.typography.headlineMedium,
                color = contentColor.copy(alpha = 0.8f)
            )

            // Buttons Column
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Main Action for IDLE - Prominent Button
                if (timerState == TimerState.IDLE) {
                    Button(
                        onClick = onShowTimePicker,
                        modifier = Modifier.fillMaxWidth().height(72.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Schedule, null)
                        Spacer(Modifier.width(12.dp))
                        Text("SCHEDULE DIVISION START", style = MaterialTheme.typography.titleLarge)
                    }
                }

                // Sync Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SyncButton(
                        text = "4-MIN SYNC",
                        onClick = onSync4Min,
                        modifier = Modifier.weight(1f)
                    )
                    SyncButton(
                        text = "1-MIN SYNC",
                        onClick = onSync1Min,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Secondary Control Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ElevatedButton(
                        onClick = onStartRollingHold,
                        modifier = Modifier.weight(1f).height(64.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    ) {
                        Text("ROLLING HOLD", style = MaterialTheme.typography.labelLarge)
                    }
                    
                    ElevatedButton(
                        onClick = onReset,
                        modifier = Modifier.weight(1f).height(64.dp),
                        enabled = timerState != TimerState.IDLE,
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text("RESET", style = MaterialTheme.typography.labelLarge)
                    }
                }

                // Edit Schedule / Schedule Next - Text Button for secondary action
                if (timerState == TimerState.PRE_SEQUENCE || timerState == TimerState.STARTED) {
                    TextButton(
                        onClick = onShowTimePicker,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val label = if (timerState == TimerState.PRE_SEQUENCE) "EDIT SCHEDULED TIME" else "SCHEDULE NEXT DIVISION"
                        Text(label, color = contentColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Rolling Hold Overlay
        if (timerState == TimerState.ROLLING_HOLD && remainingMillis <= 0) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.95f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = if (isHoldingFor4Min) "Did the 4 minute gun go?" else "Did the 1 minute gun go?",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 48.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { onRollingHoldComplete(true) },
                            modifier = Modifier.weight(1f).height(88.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StartGreen),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Text(
                                "YES",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        Button(
                            onClick = { onRollingHoldComplete(false) },
                            modifier = Modifier.weight(1f).height(88.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FinalRed),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Text("NO", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduledTimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val currentTime = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
        ) {
            // Fix: Wrap TimePicker in a local MaterialTheme with standard typography
            // to prevent it from inheriting the massive 120sp global displayLarge.
            MaterialTheme(
                typography = MaterialTheme.typography.copy(
                    displayLarge = TextStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = 57.sp,
                        lineHeight = 64.sp,
                        letterSpacing = (-0.25).sp
                    ),
                    displayMedium = TextStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = 45.sp,
                        lineHeight = 52.sp,
                        letterSpacing = 0.sp
                    ),
                    displaySmall = TextStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = 36.sp,
                        lineHeight = 44.sp,
                        letterSpacing = 0.sp
                    ),
                    labelLarge = TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        letterSpacing = 0.1.sp
                    ),
                    labelMedium = TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 0.5.sp
                    ),
                    labelSmall = TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 0.5.sp
                    )
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SET DIVISION START TIME",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                    )
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("CANCEL")
                        }
                        TextButton(onClick = { onConfirm(timePickerState.hour, timePickerState.minute) }) {
                            Text("OK")
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
        shape = MaterialTheme.shapes.medium,
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
        TimerState.PRE_SEQUENCE -> Color(0xFF2C3E50) // Dark blue-gray for pre-sequence
        TimerState.WARNING_4_MIN -> WarningBlue
        TimerState.PREP_1_MIN -> PrepYellow
        TimerState.ROLLING_HOLD -> RollingOrange
        TimerState.FINAL_COUNTDOWN -> FinalRed
        TimerState.STARTED -> StartGreen
    }
}

private fun getStateLabel(state: TimerState, isHoldingFor4Min: Boolean): String {
    return when (state) {
        TimerState.IDLE -> "READY"
        TimerState.PRE_SEQUENCE -> "PRE-SEQUENCE: COUNTING TO 4-MIN GUN"
        TimerState.WARNING_4_MIN -> "WARNING (4-MIN)"
        TimerState.PREP_1_MIN -> "PREP (1-MIN)"
        TimerState.FINAL_COUNTDOWN -> "FINAL COUNTDOWN"
        TimerState.ROLLING_HOLD -> if (isHoldingFor4Min) "DELAY: WAITING FOR 4-MIN GUN" else "DELAY: WAITING FOR 1-MIN GUN"
        TimerState.STARTED -> "STARTED!"
    }
}

private fun formatMillis(millis: Long, state: TimerState): String {
    if (state == TimerState.STARTED) return "START!"

    val totalSeconds = (millis + 999) / 1000 // Round up
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    // We use a string template and padStart instead of String.format to reduce
    // object allocation overhead during high-frequency recomposition loops.
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp", name = "Idle")
@Composable
fun IdlePreview() {
    BumpsCountdownTimerTheme {
        TimerContent(
            remainingMillis = 0L,
            timerState = TimerState.IDLE,
            isHoldingFor4Min = false,
            onSync4Min = {},
            onSync1Min = {},
            onStartRollingHold = {},
            onReset = {},
            onShowTimePicker = {},
            onRollingHoldComplete = {}
        )
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp", name = "Pre-Sequence")
@Composable
fun PreSequencePreview() {
    BumpsCountdownTimerTheme {
        TimerContent(
            remainingMillis = 125000L,
            timerState = TimerState.PRE_SEQUENCE,
            isHoldingFor4Min = false,
            onSync4Min = {},
            onSync1Min = {},
            onStartRollingHold = {},
            onReset = {},
            onShowTimePicker = {},
            onRollingHoldComplete = {}
        )
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp", name = "4-Min Rolling Hold")
@Composable
fun RollingHold4MinPreview() {
    BumpsCountdownTimerTheme {
        TimerContent(
            remainingMillis = 0L,
            timerState = TimerState.ROLLING_HOLD,
            isHoldingFor4Min = true,
            onSync4Min = {},
            onSync1Min = {},
            onStartRollingHold = {},
            onReset = {},
            onShowTimePicker = {},
            onRollingHoldComplete = {}
        )
    }
}
