package com.sweak.qralarm.features.onboarding.social_proof.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.features.onboarding.social_proof.model.onboardingReviews

@Composable
fun ReviewsCarousel(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val reviewItemsCount = onboardingReviews.size
    val pageCount = 1000 * reviewItemsCount
    val pagerState = rememberPagerState(
        initialPage = pageCount / 2,
        pageCount = { pageCount }
    )
    val progress = remember { Animatable(0f) }

    LaunchedEffect(isActive, pagerState.settledPage) {
        if (!isActive) {
            progress.snapTo(0f)
            return@LaunchedEffect
        }
        val index = pagerState.currentPage % reviewItemsCount
        val readingTime = onboardingReviews[index].approximateReadingTimeMillis
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(readingTime.toInt(), easing = LinearEasing)
        )
        pagerState.animateScrollToPage((pagerState.currentPage + 1) % pageCount)
    }

    HorizontalPager(
        state = pagerState,
        verticalAlignment = Alignment.Top,
        modifier = modifier.fillMaxWidth()
    ) { page ->
        val index = page % reviewItemsCount
        val review = onboardingReviews[index]
        ReviewCard(
            contentResourceId = review.contentResourceId,
            authorResourceId = review.authorResourceId,
            progress = if (page == pagerState.settledPage) progress.value else 0f,
            modifier = Modifier
                .padding(horizontal = MaterialTheme.space.mediumLarge)
                .fillMaxWidth()
        )
    }
}
