package com.sweak.qralarm.features.onboarding.social_proof.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.core.designsystem.theme.space
import com.sweak.qralarm.features.onboarding.social_proof.model.onboardingReviews
import kotlinx.coroutines.delay

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

    LaunchedEffect(pagerState.settledPage) {
        val resolvedPageContentIndex = pagerState.currentPage % reviewItemsCount
        val readingTime = onboardingReviews[resolvedPageContentIndex].approximateReadingTimeMillis

        delay(readingTime)
        val nextPage = (pagerState.currentPage + 1) % pageCount
        pagerState.animateScrollToPage(page = nextPage)
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier
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
}

@Preview
@Composable
private fun ReviewsCarouselPreview() {
    QRAlarmTheme {
        ReviewsCarousel()
    }
}
