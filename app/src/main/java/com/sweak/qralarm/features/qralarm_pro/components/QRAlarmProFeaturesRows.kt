package com.sweak.qralarm.features.qralarm_pro.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.features.qralarm_pro.model.qrAlarmProChipFeatures

@Composable
fun QRAlarmProFeaturesRows(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        val endlessPagerMultiplier = 1000
        val featureItemsCount = qrAlarmProChipFeatures.size
        val itemsCount = endlessPagerMultiplier * featureItemsCount

        val firstLazyListState = rememberLazyListState()
        val secondLazyListState = rememberLazyListState()

        LaunchedEffect(Unit) {
            while (true) { firstLazyListState.autoScroll() }
        }

        LaunchedEffect(Unit) {
           while (true) { secondLazyListState.autoScroll() }
        }

        LazyRow(
            state = firstLazyListState,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.space.medium),
            userScrollEnabled = false
        ) {
            items(count = itemsCount) {
                val resolvedItemIndex = it % featureItemsCount

                QRAlarmProFeatureChip(
                    chipFeature = qrAlarmProChipFeatures[resolvedItemIndex]
                )
            }
        }

        LazyRow(
            state = secondLazyListState,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.space.medium),
            reverseLayout = true,
            userScrollEnabled = false
        ) {
            items(count = itemsCount) {
                val resolvedItemIndex = (featureItemsCount - it % featureItemsCount) - 1

                QRAlarmProFeatureChip(
                    chipFeature = qrAlarmProChipFeatures[resolvedItemIndex]
                )
            }
        }
    }
}

private suspend fun ScrollableState.autoScroll() {
    var previousValue = 0f
    scroll(MutatePriority.PreventUserInput) {
        animate(
            initialValue = 0f,
            targetValue = 100f,
            animationSpec = tween(durationMillis = 800, easing = LinearEasing)
        ) { currentValue, _ ->
            previousValue += scrollBy(currentValue - previousValue)
        }
    }
}

@Preview
@Composable
private fun QRAlarmProFeaturesRowsPreview() {
    QRAlarmTheme {
        QRAlarmProFeaturesRows()
    }
}