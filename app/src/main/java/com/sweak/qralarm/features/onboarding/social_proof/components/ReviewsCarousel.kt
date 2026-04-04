package com.sweak.qralarm.features.onboarding.social_proof.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.core.designsystem.theme.ButterflyBush
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.features.onboarding.social_proof.model.onboardingReviews

@Composable
fun ReviewsCarousel(modifier: Modifier = Modifier) {
    val endlessPagerMultiplier = 1000
    val reviewItemsCount = onboardingReviews.size
    val pageCount = endlessPagerMultiplier * reviewItemsCount
    val initialPage = pageCount / 2

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        initialPageOffsetFraction = 0f,
        pageCount = { pageCount },
    )

    val progress = remember { Animatable(0f) }

    LaunchedEffect(pagerState.settledPage) {
        val resolvedPageContentIndex = pagerState.currentPage % reviewItemsCount
        val readingTime = onboardingReviews[resolvedPageContentIndex].approximateReadingTimeMillis

        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = readingTime.toInt(),
                easing = LinearEasing
            )
        )

        val nextPage = (pagerState.currentPage + 1) % pageCount
        pagerState.animateScrollToPage(page = nextPage)
    }

    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val resolvedPageContentIndex = page % reviewItemsCount
            val review = onboardingReviews[resolvedPageContentIndex]

            ReviewCard(
                contentResourceId = review.contentResourceId,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.space.mediumLarge)
            )
        }

        LinearProgressIndicator(
            progress = { progress.value },
            color = ButterflyBush,
            trackColor = Color.White,
            strokeCap = StrokeCap.Round,
            drawStopIndicator = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.space.mediumLarge)
                .padding(vertical = MaterialTheme.space.medium)
                .height(MaterialTheme.space.xSmall)
        )
    }
}

@Preview
@Composable
private fun ReviewsCarouselPreview() {
    QRAlarmTheme {
        ReviewsCarousel()
    }
}
