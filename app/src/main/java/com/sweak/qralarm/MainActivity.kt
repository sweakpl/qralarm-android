package com.sweak.qralarm

import android.os.Bundle
import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sweak.qralarm.data.DataStoreManager
import com.sweak.qralarm.ui.screens.home.HomeScreen
import com.sweak.qralarm.ui.theme.QRAlarmTheme
import com.sweak.qralarm.util.swapTimeFormats
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        convertTimeFormatIfNeeded()

        setContent {
            QRAlarmTheme {
                ScreenContent()
            }
        }
    }

    @Composable
    fun ScreenContent() {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = Screen.HomeScreen.route) {
            composable(route = Screen.HomeScreen.route) {
                HomeScreen()
            }
        }
    }

    private fun convertTimeFormatIfNeeded() {
        runBlocking {
            val (newHour, newTimeFormatString, newMeridiemString) = swapTimeFormats(
                dataStoreManager.getString(DataStoreManager.ALARM_TIME_FORMAT).first(),
                DateFormat.is24HourFormat(this@MainActivity),
                dataStoreManager.getInt(DataStoreManager.ALARM_HOUR).first(),
                dataStoreManager.getString(DataStoreManager.ALARM_MERIDIEM).first()
            )

            if (newHour != null && newTimeFormatString != null) {
                setNewHourAndTimeFormat(newHour, newTimeFormatString, newMeridiemString)
            }
        }
    }

    private suspend fun setNewHourAndTimeFormat(
        newHour: Int,
        newTimeFormatString: String,
        newMeridiemString: String? = null
    ) {
        dataStoreManager.apply {
            putInt(
                DataStoreManager.ALARM_HOUR,
                newHour
            )
            putString(
                DataStoreManager.ALARM_TIME_FORMAT,
                newTimeFormatString
            )
            if (newMeridiemString != null) {
                putString(
                    DataStoreManager.ALARM_MERIDIEM,
                    newMeridiemString
                )
            }
        }
    }
}