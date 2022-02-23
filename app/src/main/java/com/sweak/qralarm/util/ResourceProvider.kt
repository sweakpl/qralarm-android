package com.sweak.qralarm.util

import android.content.Context
import androidx.annotation.StringRes

class ResourceProvider(private val context: Context) {

    fun getString(@StringRes stringResourceId: Int): String {
        return context.getString(stringResourceId)
    }
}