package com.sweak.qralarm.features.onboarding.social_proof.model

import androidx.annotation.StringRes
import com.sweak.qralarm.R

data class Review(
    @param:StringRes val contentResourceId: Int,
    @param:StringRes val authorResourceId: Int,
    val approximateReadingTimeMillis: Long
)

val onboardingReviews = listOf(
    Review(R.string.review_1, R.string.review_1_author, 7000L),
    Review(R.string.review_2, R.string.review_2_author, 11000L),
    Review(R.string.review_3, R.string.review_3_author, 7000L),
    Review(R.string.review_4, R.string.review_4_author, 10000L),
    Review(R.string.review_5, R.string.review_5_author, 5000L),
)
