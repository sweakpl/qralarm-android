package com.sweak.qralarm

import android.app.Application
import com.sweak.qralarm.data.DataStoreManager
import com.sweak.qralarm.util.CurrentTime
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class QRAlarmApp : Application() {

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    override fun onCreate() {
        super.onCreate()

        val firstLaunch = runBlocking {
            dataStoreManager.getBoolean(DataStoreManager.FIRST_LAUNCH).first()
        }

        if (firstLaunch) {
            val currentTime = CurrentTime(this)

            runBlocking {
                dataStoreManager.apply {
                    putString(DataStoreManager.ALARM_TIME_FORMAT, currentTime.timeFormat.name)
                    putInt(DataStoreManager.ALARM_HOUR, currentTime.hour)
                    putInt(DataStoreManager.ALARM_MINUTE, currentTime.minute)
                    putString(DataStoreManager.ALARM_MERIDIEM, currentTime.meridiem.name)
                    putBoolean(DataStoreManager.ALARM_SET, false)
                    putBoolean(DataStoreManager.FIRST_LAUNCH, false)
                }
            }
        }
    }
}