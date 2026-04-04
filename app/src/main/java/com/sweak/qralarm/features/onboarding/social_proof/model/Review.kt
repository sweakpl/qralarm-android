package com.sweak.qralarm.features.onboarding.social_proof.model

import androidx.annotation.StringRes
import com.sweak.qralarm.R

data class Review(
    @param:StringRes val contentResourceId: Int,
    val approximateReadingTimeMillis: Long
)

val onboardingReviews = listOf(
    Review(contentResourceId = R.string.review_1, approximateReadingTimeMillis = 7000L),
    Review(contentResourceId = R.string.review_2, approximateReadingTimeMillis = 11000L),
    Review(contentResourceId = R.string.review_3, approximateReadingTimeMillis = 7000L),
    Review(contentResourceId = R.string.review_4, approximateReadingTimeMillis = 10000L),
    Review(contentResourceId = R.string.review_5, approximateReadingTimeMillis = 5000L),
)
