package com.sweak.qralarm.features.alarm.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

@Composable
fun TimeTickReceiver(onTimeTick: () -> Unit) {
    val context = LocalContext.current
    val currentOnTimeTick by rememberUpdatedState(onTimeTick)

    DisposableEffect(context) {
        val intentFilter = IntentFilter(Intent.ACTION_TIME_TICK)
        val broadcast = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                currentOnTimeTick()
            }
        }

        context.registerReceiver(broadcast, intentFilter)

        onDispose {
            context.unregisterReceiver(broadcast)
        }
    }
}
