package com.sweak.qralarm.features.qralarm_pro.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.sweak.qralarm.R

data class CarouselFeature(
    @param:StringRes val titleResourceId: Int,
    @param:StringRes val descriptionResourceId: Int,
    @param:DrawableRes val animatedResourceId: Int,
    val animationDurationMillis: Long
)

val qrAlarmProCarouselFeatures = listOf(
    CarouselFeature(
        titleResourceId = R.string.alarms_chain,
        descriptionResourceId = R.string.alarms_chain_description,
        animatedResourceId = R.drawable.alarms_chain,
        animationDurationMillis = 9600L
    ),
    CarouselFeature(
        titleResourceId = R.string.do_not_leave_alarm,
        descriptionResourceId = R.string.do_not_leave_alarm_description,
        animatedResourceId = R.drawable.do_not_leave_alarm,
        animationDurationMillis = 4020L
    ),
    CarouselFeature(
        titleResourceId = R.string.power_off_guard,
        descriptionResourceId = R.string.power_off_guard_description,
        animatedResourceId = R.drawable.power_off_guard,
        animationDurationMillis = 6310L
    ),
    CarouselFeature(
        titleResourceId = R.string.block_volume_down,
        descriptionResourceId = R.string.block_volume_down_description,
        animatedResourceId = R.drawable.block_volume_down,
        animationDurationMillis = 6310L
    ),
    CarouselFeature(
        titleResourceId = R.string.keep_ringer_on,
        descriptionResourceId = R.string.keep_ringer_on_description,
        animatedResourceId = R.drawable.keep_ringer_on,
        animationDurationMillis = 6310L
    )
)
// Note: animationDurationMillis have 400ms subtracted for proper fad-out between carousel items.