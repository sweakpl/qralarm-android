package com.sweak.qralarm.core.domain.user.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Theme {
    @Serializable
    @SerialName("default")
    object Default : Theme()

    @Serializable
    @SerialName("dynamic")
    object Dynamic : Theme()
}
