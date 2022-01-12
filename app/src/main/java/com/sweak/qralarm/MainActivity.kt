package com.sweak.qralarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sweak.qralarm.ui.screens.home.HomeScreen
import com.sweak.qralarm.ui.theme.QRAlarmTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            QRAlarmTheme {
                HomeScreen()
            }
        }
    }
}