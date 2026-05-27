package com.sweak.qralarm.features.menu

import com.sweak.qralarm.core.domain.user.model.Theme

data class MenuScreenState(
    val theme: Theme = Theme.Default
)
