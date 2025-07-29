package com.sweak.qralarm.alarm.activity

import android.app.KeyguardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.sweak.qralarm.alarm.service.AlarmService
import com.sweak.qralarm.app.MainActivity
import com.sweak.qralarm.core.designsystem.theme.QRAlarmTheme
import com.sweak.qralarm.features.alarm.navigation.ALARM_SCREEN_ROUTE
import com.sweak.qralarm.features.alarm.navigation.alarmScreen
import com.sweak.qralarm.features.disable_alarm_scanner.navigation.disableAlarmScannerScreen
import com.sweak.qralarm.features.disable_alarm_scanner.navigation.navigateToDisableAlarmScanner
import com.sweak.qralarm.features.emergency.navigation.emergencyScreen
import com.sweak.qralarm.features.emergency.navigation.navigateToEmergencyScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlarmActivity : FragmentActivity() {

    private var isLaunchedFromMainActivity: Boolean = false
    private var lastNavigateUpTime: Long = 0L

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

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
            intent.extras?.getBoolean(EXTRA_LAUNCHED_FROM_MAIN_ACTIVITY) == true

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
                            navController.navigateToDisableAlarmScanner(alarmId = alarmId)
                        },
                        onSnoozeAlarm = {
                            stopService(Intent(this@AlarmActivity, AlarmService::class.java))

                            if (!isLaunchedFromMainActivity) {
                                lifecycleScope.launch {
                                    delay(1000)
                                    finish()
                                }
                            }
                        },
                        onEmergencyClicked = {
                            navController.navigateToEmergencyScreen()
                        }
                    )

                    disableAlarmScannerScreen(
                        onAlarmDisabled = { uriStringToTryToOpen ->
                            if (uriStringToTryToOpen == null ||
                                Build.VERSION.SDK_INT < Build.VERSION_CODES.O
                            ) {
                                stopService(Intent(this@AlarmActivity, AlarmService::class.java))

                                lifecycleScope.launch {
                                    navController.popBackStack()
                                    delay(1500)
                                    finish()

                                    if (isLaunchedFromMainActivity) {
                                        startActivity(
                                            Intent(this@AlarmActivity, MainActivity::class.java)
                                        )
                                    }
                                }
                            } else {
                                var uri = uriStringToTryToOpen

                                if (!uriStringToTryToOpen.startsWith("http://") &&
                                    !uriStringToTryToOpen.startsWith("https://")
                                ) {
                                    uri = "http://$uri"
                                }

                                val keyguardManager =
                                    getSystemService(KEYGUARD_SERVICE) as KeyguardManager

                                if (keyguardManager.isKeyguardSecure &&
                                    keyguardManager.isDeviceLocked
                                ) {
                                    keyguardManager.requestDismissKeyguard(
                                        this@AlarmActivity,
                                        object : KeyguardManager.KeyguardDismissCallback() {
                                            override fun onDismissSucceeded() {
                                                super.onDismissSucceeded()

                                                stopService(
                                                    Intent(
                                                        this@AlarmActivity,
                                                        AlarmService::class.java
                                                    )
                                                )
                                                finish()

                                                try {
                                                    startActivity(
                                                        Intent(Intent.ACTION_VIEW, uri.toUri())
                                                    )
                                                } catch (_: Exception) {
                                                    if (isLaunchedFromMainActivity) {
                                                        startActivity(
                                                            Intent(
                                                                this@AlarmActivity,
                                                                MainActivity::class.java
                                                            )
                                                        )
                                                    }
                                                }
                                            }

                                            override fun onDismissCancelled() {
                                                navController.popBackStack()
                                            }
                                        }
                                    )
                                } else {
                                    stopService(
                                        Intent(this@AlarmActivity, AlarmService::class.java)
                                    )
                                    finish()

                                    try {
                                        startActivity(Intent(Intent.ACTION_VIEW, uri.toUri()))
                                    } catch (_: Exception) {
                                        if (isLaunchedFromMainActivity) {
                                            startActivity(
                                                Intent(this@AlarmActivity, MainActivity::class.java)
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        onCloseClicked = {
                            val currentTimeMillis = System.currentTimeMillis()

                            // Prevent excessive back navigation (due to double close press).
                            // We allow navigating up only if at least 3 seconds have passed since
                            // the last navigation:
                            if (lastNavigateUpTime + 3000L <= currentTimeMillis) {
                                lastNavigateUpTime = currentTimeMillis
                                navController.navigateUp()
                            }
                        }
                    )

                    emergencyScreen(
                        onCloseClicked = {
                            val currentTimeMillis = System.currentTimeMillis()

                            // Prevent excessive back navigation (due to double close press).
                            // We allow navigating up only if at least 3 seconds have passed since
                            // the last navigation:
                            if (lastNavigateUpTime + 3000L <= currentTimeMillis) {
                                lastNavigateUpTime = currentTimeMillis
                                navController.navigateUp()
                            }
                        },
                        onEmergencyTaskCompleted = {
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
            intent.extras?.getBoolean(EXTRA_LAUNCHED_FROM_MAIN_ACTIVITY) == true
    }

    companion object {
        const val EXTRA_ALARM_ID = "alarmId"
        const val EXTRA_LAUNCHED_FROM_MAIN_ACTIVITY = "launchedFromMainActivity"
    }
}