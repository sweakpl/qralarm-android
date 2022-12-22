package com.sweak.qralarm.ui.screens.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.navigation.NavHostController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.sweak.qralarm.R
import com.sweak.qralarm.ui.screens.shared.components.*
import com.sweak.qralarm.ui.screens.shared.viewmodels.AlarmViewModel
import com.sweak.qralarm.ui.theme.Victoria
import com.sweak.qralarm.ui.theme.amikoFamily
import com.sweak.qralarm.ui.theme.space
import com.sweak.qralarm.util.Meridiem
import com.sweak.qralarm.util.Screen
import com.sweak.qralarm.util.TimeFormat
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@ExperimentalPagerApi
@InternalCoroutinesApi
@ExperimentalPermissionsApi
@Composable
fun HomeScreen(
    navController: NavHostController,
    alarmViewModel: AlarmViewModel,
    finishableActionSideEffect: () -> Unit,
    context: Context = LocalContext.current
) {
    val uiState = remember { alarmViewModel.homeUiState }
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val notificationsPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    } else {
        object : PermissionState {
            override val hasPermission: Boolean get() = true
            override val permission: String get() = "android.permission.POST_NOTIFICATIONS"
            override val permissionRequested: Boolean get() = true
            override val shouldShowRationale: Boolean get() = false
            override fun launchPermissionRequest() { /* no-op */ }
        }
    }
    val composableScope = rememberCoroutineScope()

    val constraints = ConstraintSet {
        val menuButton = createRefFor("menuButton")
        val alarmAtText = createRefFor("alarmAtText")
        val timePicker = createRefFor("timePicker")
        val startStopAlarmButton = createRefFor("startStopAlarmButton")
        val snoozeButton = createRefFor("snoozeButton")

        constrain(menuButton) {
            top.linkTo(parent.top)
            end.linkTo(parent.end)
        }

        constrain(alarmAtText) {
            bottom.linkTo(timePicker.top)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(timePicker) {
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(startStopAlarmButton) {
            top.linkTo(timePicker.bottom)
            bottom.linkTo(parent.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(snoozeButton) {
            top.linkTo(startStopAlarmButton.bottom)
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
        AnimatedVisibility(
            visible = !uiState.value.alarmSet,
            modifier = Modifier.layoutId("menuButton")
        ) {
            MenuButton(
                modifier = Modifier.padding(MaterialTheme.space.medium),
                navController = navController
            )
        }

        Text(
            text = if (uiState.value.alarmSet) stringResource(R.string.alarm_at) else stringResource(
                R.string.drag_to_set_alarm
            ),
            modifier = Modifier
                .layoutId("alarmAtText")
                .padding(
                    MaterialTheme.space.medium,
                    0.dp,
                    MaterialTheme.space.medium,
                    56.dp
                ),
            textAlign = TextAlign.Center,
            fontSize = 32.sp,
            fontFamily = amikoFamily,
            fontWeight = FontWeight.SemiBold
        )

        TimePicker(
            modifier = Modifier.layoutId("timePicker"),
            uiState = uiState
        )

        StartStopAlarmButton(
            modifier = Modifier.layoutId("startStopAlarmButton"),
            uiState = uiState,
            onClick = {
                alarmViewModel.handleStartOrStopButtonClick(
                    navController,
                    cameraPermissionState,
                    notificationsPermissionState,
                    composableScope
                )
            }
        )

        AnimatedVisibility(
            visible = uiState.value.alarmServiceRunning && uiState.value.snoozeAvailable,
            modifier = Modifier.layoutId("snoozeButton")
        ) {
            SnoozeButton(
                onClick = { alarmViewModel.handleSnoozeButtonClick(finishableActionSideEffect) },
                modifier = Modifier.padding(0.dp, MaterialTheme.space.medium, 0.dp, 0.dp)
            )
        }
    }

    CameraPermissionSetAlarmDialog(
        uiState = uiState,
        onPositiveClick = {
            cameraPermissionState.launchPermissionRequest()
            uiState.value = uiState.value.copy(showCameraPermissionDialog = false)
        },
        onNegativeClick = { uiState.value = uiState.value.copy(showCameraPermissionDialog = false) }
    )

    CameraPermissionSetAlarmRevokedDialog(
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

    NotificationsPermissionDialog(
        uiState = uiState,
        onPositiveClick = {
            notificationsPermissionState.launchPermissionRequest()
            uiState.value = uiState.value.copy(showNotificationsPermissionDialog = false)
        },
        onNegativeClick = {
            uiState.value = uiState.value.copy(showNotificationsPermissionDialog = false)
        }
    )

    NotificationsPermissionRevokedDialog(
        uiState = uiState,
        onPositiveClick = {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
            )
            uiState.value = uiState.value.copy(showNotificationsPermissionRevokedDialog = false)
        },
        onNegativeClick = {
            uiState.value = uiState.value.copy(showNotificationsPermissionRevokedDialog = false)
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

    AlarmSetSnackbar(snackbarHostState = uiState.value.snackbarHostState)
}

@Composable
fun MenuButton(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    IconButton(
        onClick = {
            navController.navigate(Screen.MenuScreen.route)
        },
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
    range: IntRange,
    label: (Int) -> String = { it.toString() }
) {
    val coroutineScope = rememberCoroutineScope()

    val numbersColumnHeight = 164.dp
    val halvedNumbersColumnHeight = numbersColumnHeight / 2
    val halvedNumbersColumnHeightPx =
        with(LocalDensity.current) { halvedNumbersColumnHeight.toPx() }

    val minuteSpeedMultiplier = uiState.value.minutesSpeed

    // changing these makes things awkward right now and i'm not sure why, so i've set these to 1 to let things be
    // it's possible the "fling" logic is buggy?
    val minuteFlingVelocityMultiplier = 1f;
    val minuteFlingFrictionMultiplier = 1f;

    fun getHeightPx() : Float
    {
        return when(responsibility) {
            PickerResponsibility.HOUR -> halvedNumbersColumnHeightPx
            PickerResponsibility.MINUTE -> halvedNumbersColumnHeightPx / minuteSpeedMultiplier
            PickerResponsibility.MERIDIEM -> halvedNumbersColumnHeightPx
        }
    }

    fun getFlingVelocity(velocity: Float): Float {
        return when(responsibility)
        {
            PickerResponsibility.HOUR -> velocity
            PickerResponsibility.MINUTE -> velocity * minuteFlingVelocityMultiplier
            PickerResponsibility.MERIDIEM -> velocity
        }
    }

    fun getFlingFriction(friction: Float): Float {
        return when(responsibility)
        {
            PickerResponsibility.HOUR -> friction
            PickerResponsibility.MINUTE -> friction * minuteFlingFrictionMultiplier
            PickerResponsibility.MERIDIEM -> friction
        }
    }

    val animatedOffset = remember { Animatable(0f) }.apply {
        updateBounds(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY)
    }

    fun adjustLowerOffsetValueToRange(initialValue: Int, offsetValue: Int, range: IntRange): Int {
        var newValue = initialValue

        for (i in initialValue downTo offsetValue) {
            if (i == offsetValue) {
                return newValue
            }

            if (newValue - 1 < range.first) {
                newValue = range.last
            } else {
                newValue -= 1
            }
        }

        return newValue
    }

    fun adjustHigherOffsetValueToRange(initialValue: Int, offsetValue: Int, range: IntRange): Int {
        var newValue = initialValue

        for (i in initialValue..offsetValue) {
            if (i == offsetValue) {
                return newValue
            }

            if (newValue + 1 > range.last) {
                newValue = range.first
            } else {
                newValue += 1
            }
        }

        return newValue
    }

    fun animatedStateValue(offset: Float): Int {
        val initialValue = when (responsibility) {
            PickerResponsibility.HOUR -> uiState.value.hour
            PickerResponsibility.MINUTE -> uiState.value.minute
            PickerResponsibility.MERIDIEM -> uiState.value.meridiem.ordinal
        }
        val offsetValue = initialValue - (offset / getHeightPx()).toInt()

        return when {
            offsetValue < range.first ->
                adjustLowerOffsetValueToRange(initialValue, offsetValue, range)
            offsetValue > range.last ->
                adjustHigherOffsetValueToRange(initialValue, offsetValue, range)
            else -> offsetValue
        }
    }

    val coercedAnimatedOffset = animatedOffset.value % getHeightPx()
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
                    val actualVelocity = getFlingVelocity(velocity);
                    val friction = getFlingFriction(20f);
                    val endValue = animatedOffset.fling(
                        initialVelocity = actualVelocity,
                        animationSpec = exponentialDecay(frictionMultiplier = friction),
                        adjustTarget = { target ->
                            val height = getHeightPx();
                            val coercedTarget = target % height
                            val coercedAnchors = listOf(
                                -height,
                                0f,
                                height
                            )
                            val coercedPoint = coercedAnchors.minByOrNull {
                                abs(it - coercedTarget)
                            }!!
                            val base = height *
                                    (target / height).toInt()
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
                text = label(
                    if (animatedStateValue - 1 < range.first) range.last else animatedStateValue - 1
                ),
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
                text = label(
                    if (animatedStateValue + 1 > range.last) range.first else animatedStateValue + 1
                ),
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

@Composable
fun SnoozeButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        elevation = ButtonDefaults.elevation(defaultElevation = 0.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.Transparent
        )
    ) {
        Text(
            text = stringResource(id = R.string.snooze),
            fontSize = 20.sp,
            modifier = Modifier.padding(
                MaterialTheme.space.medium,
                MaterialTheme.space.small,
                MaterialTheme.space.medium,
                MaterialTheme.space.extraSmall
            )
        )
    }
}

@Composable
fun AlarmSetSnackbar(
    snackbarHostState: SnackbarHostState
) {
    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {
        val snackbar = createRef()

        SnackbarHost(
            modifier = Modifier
                .constrainAs(snackbar) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .padding(MaterialTheme.space.medium),
            hostState = snackbarHostState,
            snackbar = {
                Snackbar(
                    action = {
                        TextButton(
                            onClick = { snackbarHostState.currentSnackbarData?.performAction() }
                        ) {
                            Text(
                                text = snackbarHostState.currentSnackbarData?.actionLabel
                                    ?: stringResource(R.string.cancel),
                            )
                        }
                    },
                    shape = MaterialTheme.shapes.medium,
                    backgroundColor = Victoria
                ) {
                    Text(
                        text = snackbarHostState.currentSnackbarData?.message
                            ?: stringResource(R.string.alarm_set),
                        style = MaterialTheme.typography.h2
                    )
                }
            }
        )
    }
}
