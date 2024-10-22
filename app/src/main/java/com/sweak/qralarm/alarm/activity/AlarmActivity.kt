package com.sweak.qralarm.alarm.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.sweak.qralarm.alarm.service.AlarmService
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.features.alarm.navigation.ALARM_SCREEN_ROUTE
import com.sweak.qralarm.features.alarm.navigation.alarmScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmActivity : ComponentActivity() {

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.apply {
                addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
                addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
            }
        }

        val alarmId = intent.extras?.getLong(EXTRA_ALARM_ID) ?: 0

        setContent {
            QRAlarmTheme {
                NavHost(
                    navController = rememberNavController(),
                    startDestination = "$ALARM_SCREEN_ROUTE/$alarmId"
                ) {
                    alarmScreen(
                        onStopAlarm = {
                            stopService(Intent(this@AlarmActivity, AlarmService::class.java))
                            finish()
                        }
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_ALARM_ID = "alarmId"
    }
}