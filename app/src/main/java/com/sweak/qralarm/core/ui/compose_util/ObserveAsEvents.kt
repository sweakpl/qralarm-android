package com.sweak.qralarm.core.ui.compose_util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

@Composable
fun <T> ObserveAsEvents(flow: Flow<T>, onEvent: (T) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current

    // More information on this approach: https://www.youtube.com/watch?v=njchj9d_Lf8
    LaunchedEffect(key1 = flow, key2 = lifecycleOwner.lifecycle) {
        // Collect the flow when the lifecycle of calling component is at least STARTED:
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            // using Dispatchers.Main.immediate will ensure that no events will be missed
            // when e.g. there happens to be a configuration change:
            withContext(Dispatchers.Main.immediate) {
                flow.collect(onEvent)
            }
        }
    }
}