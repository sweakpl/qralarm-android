package com.sweak.qralarm.alarm.activity

import android.app.KeyguardManager
import android.app.KeyguardManager.KeyguardDismissCallback
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.UserManager
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.sweak.qralarm.R
import com.sweak.qralarm.alarm.service.AlarmService
import com.sweak.qralarm.app.MainActivity
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.features.alarm.navigation.ALARM_SCREEN_ROUTE
import com.sweak.qralarm.features.alarm.navigation.alarmScreen
import com.sweak.qralarm.features.disable_alarm_scanner.navigation.disableAlarmScannerScreen
import com.sweak.qralarm.features.disable_alarm_scanner.navigation.navigateToDisableAlarmScanner
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlarmActivity : FragmentActivity() {

    private var isLaunchedFromMainActivity: Boolean = false

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

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val alarmId = intent.extras?.getLong(EXTRA_ALARM_ID) ?: 0
        isLaunchedFromMainActivity =
            intent.extras?.getBoolean(EXTRA_LAUNCHED_FROM_MAIN_ACTIVITY) ?: false

        setContent {
            QRAlarmTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "$ALARM_SCREEN_ROUTE/$alarmId/${!isLaunchedFromMainActivity}"
                ) {
                    alarmScreen(
                        onStopAlarm = {
                            stopService(Intent(this@AlarmActivity, AlarmService::class.java))
                            finish()

                            if (isLaunchedFromMainActivity) {
                                startActivity(Intent(this@AlarmActivity, MainActivity::class.java))
                            }
                        },
                        onRequestCodeScan = {
                            val userManager = getSystemService(USER_SERVICE) as UserManager

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                                !userManager.isUserUnlocked
                            ) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val keyguardManager =
                                        getSystemService(KEYGUARD_SERVICE) as KeyguardManager

                                    keyguardManager.requestDismissKeyguard(
                                        this@AlarmActivity,
                                        object : KeyguardDismissCallback() {
                                            override fun onDismissSucceeded() {
                                                super.onDismissSucceeded()

                                                if (userManager.isUserUnlocked) {
                                                    navController.navigateToDisableAlarmScanner(
                                                        alarmId = alarmId
                                                    )
                                                }
                                            }
                                        }
                                    )
                                } else {
                                    Toast.makeText(
                                        this@AlarmActivity,
                                        getString(R.string.unlock_device_to_scan_code),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                navController.navigateToDisableAlarmScanner(alarmId = alarmId)
                            }
                        },
                        onSnoozeAlarm = {
                            stopService(Intent(this@AlarmActivity, AlarmService::class.java))

                            if (!isLaunchedFromMainActivity) {
                                lifecycleScope.launch {
                                    delay(1000)
                                    finish()
                                }
                            }
                        }
                    )

                    disableAlarmScannerScreen(
                        onAlarmDisabled = {
                            stopService(Intent(this@AlarmActivity, AlarmService::class.java))
                            finish()

                            if (isLaunchedFromMainActivity) {
                                startActivity(Intent(this@AlarmActivity, MainActivity::class.java))
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        isLaunchedFromMainActivity =
            intent.extras?.getBoolean(EXTRA_LAUNCHED_FROM_MAIN_ACTIVITY) ?: false
    }

    companion object {
        const val EXTRA_ALARM_ID = "alarmId"
        const val EXTRA_LAUNCHED_FROM_MAIN_ACTIVITY = "launchedFromMainActivity"
    }
}