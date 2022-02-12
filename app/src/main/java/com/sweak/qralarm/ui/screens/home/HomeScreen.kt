package com.sweak.qralarm.ui.screens.home

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.sweak.qralarm.R
import com.sweak.qralarm.ui.screens.shared.components.AlarmPermissionDialog
import com.sweak.qralarm.ui.screens.shared.components.CameraPermissionDialog
import com.sweak.qralarm.ui.screens.shared.components.CameraPermissionRevokedDialog
import com.sweak.qralarm.ui.theme.space
import com.sweak.qralarm.util.Meridiem
import com.sweak.qralarm.util.Screen
import com.sweak.qralarm.util.TimeFormat
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@ExperimentalPermissionsApi
@Composable
fun HomeScreen(
    navController: NavHostController,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val uiState = remember { homeViewModel.homeUiState }
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    val constraints = ConstraintSet {
        val menuButton = createRefFor("menuButton")
        val timePicker = createRefFor("timePicker")
        val startStopAlarmButton = createRefFor("startStopAlarmButton")

        constrain(startStopAlarmButton) {
            top.linkTo(timePicker.bottom)
            bottom.linkTo(parent.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(menuButton) {
            top.linkTo(parent.top)
            end.linkTo(parent.end)
        }

        constrain(timePicker) {
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }
    }

    ConstraintLayout(
        constraints,
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colors.primary,
                        MaterialTheme.colors.primaryVariant
                    )
                )
            )
    ) {
        MenuButton(
            navController = navController,
            modifier = Modifier
                .layoutId("menuButton")
                .padding(MaterialTheme.space.medium)
        )

        TimePicker(
            modifier = Modifier.layoutId("timePicker"),
            uiState = uiState
        )

        StartStopAlarmButton(
            modifier = Modifier.layoutId("startStopAlarmButton"),
            uiState = uiState,
            onClick = {
                homeViewModel.startOrStopAlarm(cameraPermissionState)
            }
        )
    }

    val context = LocalContext.current

    CameraPermissionDialog(
        uiState = uiState,
        onPositiveClick = {
            if (!cameraPermissionState.shouldShowRationale) {
                uiState.value = uiState.value.copy(
                    showCameraPermissionDialog = false,
                    showCameraPermissionRevokedDialog = true
                )
            } else {
                cameraPermissionState.launchPermissionRequest()
                uiState.value = uiState.value.copy(showCameraPermissionDialog = false)
            }
        },
        onNegativeClick = { uiState.value = uiState.value.copy(showCameraPermissionDialog = false) }
    )

    CameraPermissionRevokedDialog(
        uiState = uiState,
        onPositiveClick = {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
            )
            uiState.value = uiState.value.copy(showCameraPermissionRevokedDialog = false)
        },
        onNegativeClick = {
            uiState.value = uiState.value.copy(showCameraPermissionRevokedDialog = false)
        }
    )

    AlarmPermissionDialog(
        uiState = uiState,
        onPositiveClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            }
            uiState.value = uiState.value.copy(showAlarmPermissionDialog = false)
        },
        onNegativeClick = { uiState.value = uiState.value.copy(showAlarmPermissionDialog = false) }
    )
}

@Composable
fun MenuButton(navController: NavHostController, modifier: Modifier = Modifier) {
    IconButton(
        onClick = { navController.navigate(Screen.ScannerScreen.route) },
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_menu),
            contentDescription = "Menu button",
            tint = MaterialTheme.colors.secondary
        )
    }
}

@Composable
fun TimePicker(
    modifier: Modifier = Modifier,
    uiState: MutableState<HomeUiState>
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NumberPicker(
            uiState = uiState,
            responsibility = PickerResponsibility.HOUR,
            range = when (uiState.value.timeFormat) {
                TimeFormat.MILITARY -> 0..23
                TimeFormat.AMPM -> 1..12
            },
            label = { if (it in 0..9) "0$it" else it.toString() }
        )
        Text(
            text = ":",
            fontSize = 64.sp
        )
        NumberPicker(
            uiState = uiState,
            responsibility = PickerResponsibility.MINUTE,
            range = 0..59,
            label = { if (it in 0..9) "0$it" else it.toString() }
        )
        if (uiState.value.timeFormat == TimeFormat.AMPM) {
            NumberPicker(
                uiState = uiState,
                responsibility = PickerResponsibility.MERIDIEM,
                range = 0..1,
                label = {
                    when (it) {
                        Meridiem.AM.ordinal -> "AM"
                        else -> "PM"
                    }
                }
            )
        }
    }
}

@Composable
fun NumberPicker(
    modifier: Modifier = Modifier,
    uiState: MutableState<HomeUiState>,
    responsibility: PickerResponsibility,
    range: IntRange? = null,
    label: (Int) -> String = { it.toString() }
) {
    val coroutineScope = rememberCoroutineScope()

    val numbersColumnHeight = 164.dp
    val halvedNumbersColumnHeight = numbersColumnHeight / 2
    val halvedNumbersColumnHeightPx =
        with(LocalDensity.current) { halvedNumbersColumnHeight.toPx() }

    val animatedOffset = remember { Animatable(0f) }.apply {
        if (range != null) {
            val value = when (responsibility) {
                PickerResponsibility.HOUR -> uiState.value.hour
                PickerResponsibility.MINUTE -> uiState.value.minute
                PickerResponsibility.MERIDIEM -> uiState.value.meridiem.ordinal
            }
            updateBounds(
                (-(range.last - value) * halvedNumbersColumnHeightPx),
                (-(range.first - value) * halvedNumbersColumnHeightPx)
            )
        }
    }

    fun animatedStateValue(offset: Float): Int {
        val value = when (responsibility) {
            PickerResponsibility.HOUR -> uiState.value.hour
            PickerResponsibility.MINUTE -> uiState.value.minute
            PickerResponsibility.MERIDIEM -> uiState.value.meridiem.ordinal
        }
        return value - (offset / halvedNumbersColumnHeightPx).toInt()
    }

    val coercedAnimatedOffset = animatedOffset.value % halvedNumbersColumnHeightPx
    val animatedStateValue = animatedStateValue(animatedOffset.value)

    val newModifier = if (uiState.value.alarmSet) {
        modifier
    } else {
        modifier
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    coroutineScope.launch {
                        animatedOffset.snapTo(animatedOffset.value + delta)
                    }
                },
                onDragStopped = { velocity ->
                    val endValue = animatedOffset.fling(
                        initialVelocity = velocity,
                        animationSpec = exponentialDecay(frictionMultiplier = 20f),
                        adjustTarget = { target ->
                            val coercedTarget = target % halvedNumbersColumnHeightPx
                            val coercedAnchors = listOf(
                                -halvedNumbersColumnHeightPx,
                                0f,
                                halvedNumbersColumnHeightPx
                            )
                            val coercedPoint = coercedAnchors.minByOrNull {
                                abs(it - coercedTarget)
                            }!!
                            val base = halvedNumbersColumnHeightPx *
                                    (target / halvedNumbersColumnHeightPx).toInt()
                            coercedPoint + base
                        }
                    ).endState.value

                    when (responsibility) {
                        PickerResponsibility.HOUR -> uiState.value.hour =
                            animatedStateValue(endValue)
                        PickerResponsibility.MINUTE -> uiState.value.minute =
                            animatedStateValue(endValue)
                        PickerResponsibility.MERIDIEM -> uiState.value.meridiem =
                            when (animatedStateValue(endValue)) {
                                0 -> Meridiem.AM
                                else -> Meridiem.PM
                            }
                    }

                    animatedOffset.snapTo(0f)
                }
            )
    }

    Column(
        modifier = newModifier
            .wrapContentSize()
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .offset { IntOffset(x = 0, y = coercedAnimatedOffset.roundToInt()) }
        ) {
            Text(
                text = label(animatedStateValue - 1),
                fontSize = 64.sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = -halvedNumbersColumnHeight)
                    .alpha(coercedAnimatedOffset / halvedNumbersColumnHeightPx)
                    .pointerInput(Unit) {
                        detectTapGestures { }
                    }
            )
            Text(
                text = label(animatedStateValue),
                fontSize = 63.8.sp,
                modifier = modifier
                    .align(Alignment.Center)
                    .alpha(1 - abs(coercedAnimatedOffset) / halvedNumbersColumnHeightPx)
                    .pointerInput(Unit) {
                        detectTapGestures { }
                    }
            )
            Text(
                text = label(animatedStateValue + 1),
                fontSize = 64.sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = halvedNumbersColumnHeight)
                    .alpha(-coercedAnimatedOffset / halvedNumbersColumnHeightPx)
                    .pointerInput(Unit) {
                        detectTapGestures { }
                    }
            )
        }
    }
}

enum class PickerResponsibility {
    HOUR, MINUTE, MERIDIEM
}

private suspend fun Animatable<Float, AnimationVector1D>.fling(
    initialVelocity: Float,
    animationSpec: DecayAnimationSpec<Float>,
    adjustTarget: ((Float) -> Float)?,
    block: (Animatable<Float, AnimationVector1D>.() -> Unit)? = null,
): AnimationResult<Float, AnimationVector1D> {
    val targetValue = animationSpec.calculateTargetValue(value, initialVelocity)
    val adjustedTarget = adjustTarget?.invoke(targetValue)

    return if (adjustedTarget != null) {
        animateTo(
            targetValue = adjustedTarget,
            initialVelocity = initialVelocity,
            block = block
        )
    } else {
        animateDecay(
            initialVelocity = initialVelocity,
            animationSpec = animationSpec,
            block = block,
        )
    }
}

@Composable
fun StartStopAlarmButton(
    modifier: Modifier = Modifier,
    uiState: MutableState<HomeUiState>,
    onClick: () -> Unit = {}
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.secondary
        )
    ) {
        Text(
            text = stringResource(if (uiState.value.alarmSet) R.string.stop else R.string.start),
            fontSize = 26.sp,
            modifier = Modifier.padding(
                MaterialTheme.space.medium,
                MaterialTheme.space.small,
                MaterialTheme.space.medium,
                MaterialTheme.space.extraSmall
            )
        )
    }
}
