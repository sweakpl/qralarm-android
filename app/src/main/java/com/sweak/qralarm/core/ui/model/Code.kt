package com.sweak.qralarm.core.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Code(
    val id: Long = 0L,
    val value: String,
    val name: String? = null
) : Parcelable
