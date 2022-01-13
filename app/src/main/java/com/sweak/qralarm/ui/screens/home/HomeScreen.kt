package com.sweak.qralarm.ui.screens.home

import android.util.Log
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sweak.qralarm.R
import com.sweak.qralarm.ui.theme.space
import com.sweak.qralarm.util.Meridiem
import com.sweak.qralarm.util.TimeFormat
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel()
) {
    homeViewModel.initializeUiState()
    val uiState = remember { homeViewModel.homeUiState }

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
            modifier = Modifier
                .layoutId("menuButton")
                .padding(MaterialTheme.space.medium)
        )

        TimePicker(
            modifier = Modifier.layoutId("timePicker"),
            uiState = uiState
        )

        StartStopAlarmButton(
            text = "START",
            modifier = Modifier.layoutId("startStopAlarmButton"),
            onClick = {
                Log.i(
                    "HomeScreen",
                    "Selected time is: " +
                            "${uiState.value.hour}:${uiState.value.minute}" +
                            if (uiState.value.timeFormat == TimeFormat.AMPM) {
                                when (uiState.value.meridiem.ordinal) {
                                    Meridiem.AM.ordinal -> " AM"
                                    else -> " PM"
                                }
                            } else ""
                )
            }
        )
    }
}

@Composable
fun MenuButton(modifier: Modifier = Modifier) {
    IconButton(
        onClick = {},
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
                TimeFormat.AMPM -> 0..12
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

    Column(
        modifier = modifier
            .wrapContentSize()
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
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .offset { IntOffset(x = 0, y = coercedAnimatedOffset.roundToInt()) }
        ) {
            PickerLabel(
                text = label(animatedStateValue - 1),
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = -halvedNumbersColumnHeight)
                    .alpha(coercedAnimatedOffset / halvedNumbersColumnHeightPx)
            )
            PickerLabel(
                text = label(animatedStateValue),
                modifier = Modifier
                    .align(Alignment.Center)
                    .alpha(1 - abs(coercedAnimatedOffset) / halvedNumbersColumnHeightPx)
            )
            PickerLabel(
                text = label(animatedStateValue + 1),
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = halvedNumbersColumnHeight)
                    .alpha(-coercedAnimatedOffset / halvedNumbersColumnHeightPx)
            )
        }
    }
}

enum class PickerResponsibility {
    HOUR, MINUTE, MERIDIEM
}

@Composable
private fun PickerLabel(
    text: String,
    modifier: Modifier
) {
    Text(
        text = text,
        fontSize = 64.sp,
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { }
            }
    )
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
    text: String,
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
            text = text,
            modifier = Modifier.padding(
                MaterialTheme.space.medium,
                MaterialTheme.space.small,
                MaterialTheme.space.medium,
                MaterialTheme.space.extraSmall
            )
        )
    }
}
